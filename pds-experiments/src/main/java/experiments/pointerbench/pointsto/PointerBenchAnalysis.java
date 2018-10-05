package experiments.pointerbench.pointsto;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;

import boomerang.BackwardQuery;
import boomerang.ForwardQuery;
import boomerang.Query;
import boomerang.jimple.AllocVal;
import boomerang.jimple.Statement;
import boomerang.jimple.Val;
import boomerang.preanalysis.BoomerangPretransformer;
import boomerang.seedfactory.SeedFactory;
import soot.G;
import soot.Local;
import soot.PackManager;
import soot.RefType;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.NewExpr;
import soot.jimple.Stmt;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import soot.options.Options;
import wpds.impl.Weight.NoWeight;

public abstract class PointerBenchAnalysis {

	private String pointerBenchClassesPath;
	private String mainClass;
	protected JimpleBasedInterproceduralCFG icfg;
	protected Collection<Query> allocationSites = Sets.newHashSet();
	protected Collection<? extends Query> queryForCallSites;
	protected int unsoundErrors = 0;
	protected int imprecisionErrors = 0;
	protected SeedFactory<NoWeight> seedFactory;

	public PointerBenchAnalysis(String pointerBenchClassesPath, String mainClass) {
		this.pointerBenchClassesPath = pointerBenchClassesPath;
		this.mainClass = mainClass;
		initializeSootWithEntryPoint();
	}

	public PointerBenchResult run() {
		Transform transform = new Transform("wjtp.ifds", createAnalysisTransformer());
		PackManager.v().getPack("wjtp").add(transform);
		PackManager.v().getPack("cg").apply();
		PackManager.v().getPack("wjtp").apply();
		return new PointerBenchResult(mainClass, allocationSites.size()-unsoundErrors, imprecisionErrors, unsoundErrors);
	}

	@SuppressWarnings("static-access")
	private void initializeSootWithEntryPoint() {
		G.v().reset();
		Options.v().set_whole_program(true);
		Options.v().setPhaseOption("cg.spark", "on");
		Options.v().set_output_format(Options.output_format_none);
//		Options.v().setPhaseOption("cg", "trim-clinit:false");
		Options.v().set_no_bodies_for_excluded(true);
		Options.v().set_allow_phantom_refs(true);

		List<String> includeList = new LinkedList<String>();
		includeList.add("java.lang.*");
		includeList.add("java.util.*");
//		includeList.add("java.io.*");
//		includeList.add("sun.misc.*");
//		includeList.add("java.net.*");
//		includeList.add("javax.servlet.*");
//		includeList.add("javax.crypto.*");

		Options.v().set_include(includeList);
		Options.v().setPhaseOption("jb", "use-original-names:true");

		Options.v().set_exclude(excludedPackages());
		Options.v().set_soot_classpath(pointerBenchClassesPath);
		Options.v().set_prepend_classpath(true);
		// Options.v().set_main_class(this.getTargetClass());
		Scene.v().loadNecessaryClasses();
		SootClass c = Scene.v().forceResolve(mainClass, SootClass.BODIES);
		if (c != null) {
			c.setApplicationClass();
		}

//		SootMethod methodByName = c.getMethodByName("main");
//		List<SootMethod> ePoints = new LinkedList<>();
//		ePoints.add(methodByName);
//		Scene.v().setEntryPoints(ePoints);
	}

	protected boolean includeJDK() {
		return true;
	}

	public List<String> excludedPackages() {
		List<String> excludedPackages = new LinkedList<>();
		excludedPackages.add("sun.*");
		excludedPackages.add("javax.*");
		excludedPackages.add("com.sun.*");
		excludedPackages.add("com.ibm.*");
		excludedPackages.add("org.xml.*");
		excludedPackages.add("org.w3c.*");
		excludedPackages.add("apple.awt.*");
		excludedPackages.add("com.apple.*");
		return excludedPackages;
	}


	protected SceneTransformer createAnalysisTransformer() {
		return new SceneTransformer() {

			protected void internalTransform(String phaseName, @SuppressWarnings("rawtypes") Map options) {
				BoomerangPretransformer.TRANSFORM_CONSTANTS = false;
				BoomerangPretransformer.v().reset();
				BoomerangPretransformer.v().apply();
				icfg = new JimpleBasedInterproceduralCFG(true);
				seedFactory = new SeedFactory<NoWeight>() {
					@Override
					public BiDiInterproceduralCFG<Unit, SootMethod> icfg() {
						return icfg;
					}

					@Override
					protected Collection<? extends Query> generate(SootMethod method, Stmt u,
							Collection calledMethods) {
						Optional<? extends Query> query = new FirstArgumentOf("pointsToQuery").test(u);
						
						Optional<? extends Query> alloc = new AllocationSiteOf().test(u);
						//Side effect:
						if(alloc.isPresent()){
							allocationSites.add(alloc.get());
						}

						if (query.isPresent()) {
							return Collections.singleton(query.get());
						}
						return Collections.emptySet();
					}
				};
				queryForCallSites = seedFactory.computeSeeds();
				if(queryForCallSites.isEmpty())
					System.err.println("No query found for "+ mainClass);
				runAndCompare();
			}
		};
	}


	protected abstract void runAndCompare();


	private class AllocationSiteOf implements ValueOfInterestInUnit {
		public Optional<? extends Query> test(Stmt unit) {
			if (unit instanceof AssignStmt) {
				AssignStmt as = (AssignStmt) unit;
				if (as.getLeftOp() instanceof Local && as.getRightOp() instanceof NewExpr) {
					NewExpr expr = ((NewExpr) as.getRightOp());
					if (allocatesObjectOfInterest(expr)) {
						Local local = (Local) as.getLeftOp();
						Statement statement = new Statement(unit, icfg.getMethodOf(unit));
						ForwardQuery forwardQuery = new ForwardQuery(statement,
								new AllocVal(local, icfg.getMethodOf(unit), as.getRightOp(), statement));
						return Optional.<Query>of(forwardQuery);
					}
				}
			}
			return Optional.absent();
		}
	}

	private class FirstArgumentOf implements ValueOfInterestInUnit {

		private String methodNameMatcher;

		public FirstArgumentOf(String methodNameMatcher) {
			this.methodNameMatcher = methodNameMatcher;
		}

		@Override
		public Optional<? extends Query> test(Stmt unit) {
			Stmt stmt = (Stmt) unit;
			if (!(stmt.containsInvokeExpr()))
				return Optional.absent();
			InvokeExpr invokeExpr = stmt.getInvokeExpr();
			if (!invokeExpr.getMethod().getName().matches(methodNameMatcher))
				return Optional.absent();
			Value param = invokeExpr.getArg(0);
			if (!(param instanceof Local))
				return Optional.absent();
			return Optional.<Query>of(new BackwardQuery(new Statement(unit, icfg.getMethodOf(unit)),
					new Val(param, icfg.getMethodOf(unit))));
		}
	}

	

	private boolean allocatesObjectOfInterest(NewExpr rightOp) {
		SootClass interfaceType = Scene.v().getSootClass("pointerbench.markers.Allocation");
		if (!interfaceType.isInterface())
			return false;
		RefType allocatedType = rightOp.getBaseType();
		return Scene.v().getActiveHierarchy().getImplementersOf(interfaceType).contains(allocatedType.getSootClass());
	}

	private interface ValueOfInterestInUnit {
		Optional<? extends Query> test(Stmt unit);
	}

}