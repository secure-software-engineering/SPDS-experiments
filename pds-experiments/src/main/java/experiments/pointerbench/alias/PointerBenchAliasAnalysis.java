package experiments.pointerbench.alias;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

import boomerang.BackwardQuery;
import boomerang.Query;
import boomerang.jimple.Statement;
import boomerang.jimple.Val;
import boomerang.preanalysis.BoomerangPretransformer;
import boomerang.seedfactory.SeedFactory;
import experiments.pointerbench.pointsto.PointerBenchResult;
import soot.G;
import soot.Local;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.Value;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import soot.options.Options;
import wpds.impl.Weight.NoWeight;

public abstract class PointerBenchAliasAnalysis {

	private String pointerBenchClassesPath;
	private String mainClass;
	protected JimpleBasedInterproceduralCFG icfg;
	protected Collection<? extends Query> queryForCallSites;
	protected Collection<AliasQuery> queries = Sets.newHashSet();
	protected int truePositive = 0;
	protected int falseNegative = 0;
	protected int falsePositive = 0;
	protected SeedFactory<NoWeight> seedFactory;

	public PointerBenchAliasAnalysis(String pointerBenchClassesPath, String mainClass) {
		this.pointerBenchClassesPath = pointerBenchClassesPath;
		this.mainClass = mainClass;
		initializeSootWithEntryPoint();
	}

	public PointerBenchResult run() {
		Transform transform = new Transform("wjtp.ifds", createAnalysisTransformer());
		PackManager.v().getPack("wjtp").add(transform);
		PackManager.v().getPack("cg").apply();
		PackManager.v().getPack("wjtp").apply();
		return new PointerBenchResult(mainClass, truePositive, falsePositive,
				falseNegative);
	}

	@SuppressWarnings("static-access")
	private void initializeSootWithEntryPoint() {
		G.v().reset();
		Options.v().set_whole_program(true);
		Options.v().setPhaseOption("cg.spark", "on");
		Options.v().set_output_format(Options.output_format_none);
		// Options.v().setPhaseOption("cg", "trim-clinit:false");
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

		// SootMethod methodByName = c.getMethodByName("main");
		// List<SootMethod> ePoints = new LinkedList<>();
		// ePoints.add(methodByName);
		// Scene.v().setEntryPoints(ePoints);
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
						Stmt stmt = (Stmt) u;
						if (!(stmt.containsInvokeExpr()))
							return Collections.emptySet();
						InvokeExpr invokeExpr = stmt.getInvokeExpr();
						if (!invokeExpr.getMethod().getName().matches("mayAliasQuery"))
							return Collections.emptySet();
						Value param1 = invokeExpr.getArg(0);
						Value param2 = invokeExpr.getArg(1);
						Value param3 = invokeExpr.getArg(2);
						Set<Query> out = Sets.newHashSet();
						BackwardQuery queryA = new BackwardQuery(new Statement(stmt, icfg.getMethodOf(stmt)),
								new Val(param1, icfg.getMethodOf(stmt)));
						BackwardQuery queryB = new BackwardQuery(new Statement(stmt, icfg.getMethodOf(stmt)),
								new Val(param2, icfg.getMethodOf(stmt)));
						queries.add(new AliasQuery(stmt, (Local) param1, (Local) param2, queryA,queryB,((IntConstant) param3).value > 0));
						out.add(queryA);
						out.add(queryB);
						return out;
					}
				};
				queryForCallSites = seedFactory.computeSeeds();
				if (queryForCallSites.isEmpty())
					System.err.println("No query found for " + mainClass);
				runAndCompare();
			}
		};
	}

	protected void runAndCompare(){
		for(AliasQuery q : queries){	
			boolean result = computeQuery(q);
			if(result == q.alias){
				truePositive++;
			} else if(q.alias == true && result == false){
				falseNegative++;
			} else if(q.alias == false && result == true){
				System.out.println(q.stmt);
				falsePositive++;
			}
		}
	}
	
	protected abstract boolean computeQuery(AliasQuery q);

	protected static class AliasQuery{
		final Unit stmt;
		final Local a;
		final Local b;
		final boolean alias;
		final BackwardQuery queryA;
		final BackwardQuery queryB;

		public AliasQuery(Unit stmt, Local a, Local b, BackwardQuery queryA, BackwardQuery queryB, boolean alias){
			this.stmt = stmt;
			this.a = a;
			this.b = b;
			this.queryA = queryA;
			this.queryB = queryB;
			this.alias = alias;
		}
		
		@Override
		public String toString() {
			return stmt.toString();
		}
	}

}