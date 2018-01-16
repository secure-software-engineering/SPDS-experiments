package dacapo.demand.driven;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;

import boomerang.BackwardQuery;
import boomerang.Boomerang;
import boomerang.BoomerangTimeoutException;
import boomerang.DefaultBoomerangOptions;
import boomerang.Query;
import boomerang.jimple.Statement;
import boomerang.jimple.Val;
import boomerang.seedfactory.SeedFactory;
import boomerang.stats.AdvancedBoomerangStats;
import boomerang.stats.IBoomerangStats;
import dacapo.SootSceneSetupDacapo;
import heros.solver.Pair;
import java_cup.symbol_set;
import soot.Local;
import soot.PackManager;
import soot.PointsToSet;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootMethod;
import soot.Transform;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.spark.ondemand.AllocAndContext;
import soot.jimple.spark.ondemand.AllocAndContextSet;
import soot.jimple.spark.ondemand.genericutil.ObjectVisitor;
import soot.jimple.spark.pag.AllocNode;
import soot.jimple.spark.sets.PointsToSetInternal;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import wpds.impl.Weight.NoWeight;

public class DacapoDemandDrivenExperiments extends SootSceneSetupDacapo {

	protected Set<Pair<Local, Stmt>> queries = Sets.newHashSet();
	private JimpleBasedInterproceduralCFG icfg;
	private int TIMEOUT_IN_MS = 10000;

	public DacapoDemandDrivenExperiments(String benchmarkFolder, String benchFolder) {
		super(benchmarkFolder, benchFolder);
	}

	public void run() {
		setupSoot();
		Transform transform = new Transform("wjtp.ifds", new SceneTransformer() {

			protected void internalTransform(String phaseName, @SuppressWarnings("rawtypes") Map options) {

				icfg = new JimpleBasedInterproceduralCFG(false);
				System.out.println("Application Classes: " + Scene.v().getApplicationClasses().size());
				final SeedFactory<NoWeight> seedFactory = new SeedFactory<NoWeight>() {
					@Override
					public BiDiInterproceduralCFG<Unit, SootMethod> icfg() {
						return icfg;
					}

					@Override
					protected Collection<? extends Query> generate(SootMethod method, Stmt u,
							Collection calledMethods) {
						if (isQueryForStmt(u) && !method.isJavaLibraryMethod()) {
							Value val = ((InstanceInvokeExpr) u.getInvokeExpr()).getBase();
							return Collections
									.singleton(new BackwardQuery(new Statement(u, method), new Val(val, method)));
						}
						return Collections.emptySet();
					}

					private boolean isQueryForStmt(Stmt stmt) {
						if (!stmt.containsInvokeExpr())
							return false;
						if (stmt.getInvokeExpr() instanceof InstanceInvokeExpr) {
							InstanceInvokeExpr iie = (InstanceInvokeExpr) stmt.getInvokeExpr();
							if (iie.getBase() instanceof Local && iie.getMethod().getDeclaringClass().isInterface()) {
								Local local = (Local) iie.getBase();
								if (!Scene.v().getPointsToAnalysis().reachingObjects(local).isEmpty()) {
									if (!isIgnoredType(local.getType())) {
										return true;
									}
								}
							}
						}
						return false;
					}

					private boolean isIgnoredType(Type type) {
						return type.toString().equals("java.lang.String") || type.toString().equals("java.lang.StringBuilder");
					}
				};
				Collection<Query> seeds = seedFactory.computeSeeds();
				System.out.println("Points-To Queries: " + seeds.size());
				int i = 0;
				int manuFaster = 0;
				int pdsFaster = 0;
				int manuMorePrecise = 0;
				int pdsMorePrecise = 0;
				int pdsTimeout = 0;
				int manuTimeout = 0;
				int betterThanSPARK = 0;
				for (Query q : seeds) {
					System.out.println("Starting Manu");
					ExperimentResults manuTime = queryManu(q, seedFactory);
					System.out.println("Starting Boomerang");
					ExperimentResults pdsTime = queryWPDS(q, seedFactory);
					i++;
					System.out.println(i + ": Boomerang " + pdsTime + " Manu: " + manuTime);
					if(manuTime.timeout  && pdsTime.timeout){
						pdsTimeout++;
						manuTimeout++;
					} else if(pdsTime.timeout){
						pdsTimeout++;
						manuFaster++;
					}else if(manuTime.timeout){
						manuTimeout++;
						pdsFaster++;
					} else{
						if (manuTime.time.minus(pdsTime.time).isNegative()) {
							manuFaster++;
						} else {
							pdsFaster++;
						}
						if (manuTime.pointsToSetSize - pdsTime.pointsToSetSize < 0) {
							manuMorePrecise++;
							System.out.println("Manu more precise for " + q);
							System.out.println("Manu results: " +manuTime.results);
							System.out.println("Boomerang results: " +pdsTime.results);
						} else if (manuTime.pointsToSetSize - pdsTime.pointsToSetSize > 0) {
							pdsMorePrecise++;
							System.out.println("Boomerang more precise " + q);
							System.out.println("Manu results: " +manuTime.results);
							System.out.println("Boomerang results: " +pdsTime.results);
						}
					}
					System.out.println("Time: (-/+) " + manuFaster + " / " + pdsFaster);
					System.out.println("Timeouts: (-/+) " + pdsTimeout + " / " + manuTimeout);
					System.out.println("Precision (-/+/=): " + manuMorePrecise + " / " + pdsMorePrecise + " / "
							+ (i - manuMorePrecise - pdsMorePrecise));
					if(!pdsTime.timeout){
						PointsToSet reachingObjects = Scene.v().getPointsToAnalysis().reachingObjects((Local) q.asNode().fact().value());
						if(getPointsToSize(reachingObjects) > pdsTime.pointsToSetSize){
							betterThanSPARK++;
							System.out.println(reachingObjects);
							System.out.println(pdsTime.results);
						}
					}
					System.out.println("More precise than Spark in " + betterThanSPARK + " of " + i + " times!");
					
				}
			}

		});

		PackManager.v().getPack("wjtp").add(transform);
		PackManager.v().getPack("cg").apply();
		PackManager.v().getPack("wjtp").apply();
	}

	protected ExperimentResults queryManu(Query q, SeedFactory<NoWeight> seedFactory) {
		Stopwatch watch = Stopwatch.createStarted();
//		DemandCSPointsTo pts = DemandCSPointsTo.makeWithBudget(75000, 10, false);
		boolean timeout = false;
		DemandCSPointsTo.timeBudget = TIMEOUT_IN_MS;
		int ptsSetSize = 0;
		Object results = null;
		try {
//			PointsToSet reachingObjects = pts.reachingObjects((Local) q.var().value());
//			ptsSetSize = getPointsToSize(reachingObjects);
//			
//			results = reachingObjects;
		} catch (ManuTimeoutException e) {
			timeout = true;
		}
		return new ExperimentResults(watch.elapsed(), ptsSetSize, timeout, results);
	}

	private int getPointsToSize(PointsToSet reachingObjects) {
		if (reachingObjects instanceof PointsToSetInternal) {
			PointsToSetInternal i = (PointsToSetInternal) reachingObjects;
			return i.size();
		} else if (reachingObjects instanceof AllocAndContextSet) {
			final HashSet<AllocNode> flat = Sets.newHashSet();
			AllocAndContextSet set = ((AllocAndContextSet) reachingObjects);
			set.forall(new ObjectVisitor<AllocAndContext>() {
				
				@Override
				public void visit(AllocAndContext obj_) {
					flat.add(obj_.alloc);
				}
			});
			return flat.size();
		} else {
			System.out.println("Sure?" + reachingObjects.getClass());
		}
		return 0;
	}

	protected ExperimentResults queryWPDS(Query q, SeedFactory<NoWeight> seedFactory) {
		Boomerang solver = new Boomerang(new DefaultBoomerangOptions() {
			@Override
			public int analysisTimeoutMS() {
				return TIMEOUT_IN_MS;
			}
			@Override
			public boolean arrayFlows() {
				return true;
			}
			@Override
			public boolean trackAnySubclassOfThrowable() {
				return true;
			}
			@Override
			public boolean trackStrings() {
				return true;
			}
			@Override
			public IBoomerangStats statsFactory() {
				return new AdvancedBoomerangStats<>();
			}
		}) {
			@Override
			public BiDiInterproceduralCFG<Unit, SootMethod> icfg() {
				return icfg;
			}

			@Override
			public SeedFactory<NoWeight> getSeedFactory() {
				return seedFactory;
			}
		};
		Stopwatch watch = Stopwatch.createStarted();
		boolean timeout = false;
		try {
			solver.solve(q);
		} catch (BoomerangTimeoutException e) {
			timeout = true;
		}
		Duration time = watch.elapsed();
		Table<Statement, Val, NoWeight> results = solver.getResults(q);
		int pointsToSetSize = results.rowKeySet().size();
		return new ExperimentResults(time, pointsToSetSize, timeout,results.rowKeySet());
	}

	// protected void queryManu(Pair<Local, Stmt> q) {
	//
	// Stopwatch watch = Stopwatch.createStarted();
	// solver.solve(q);
	// return watch.elapsed();
	// }

	private static class ExperimentResults {
		private final Duration time;
		private final int pointsToSetSize;
		private boolean timeout;
		private Object results;

		ExperimentResults(Duration time, int pointsToSetSize, boolean timeout, Object results) {
			this.time = time;
			this.pointsToSetSize = pointsToSetSize;
			this.timeout = timeout;
			this.results = results;
		}

		@Override
		public String toString() {
			return "Time: " + time + " Size: " + pointsToSetSize;
		}
	}
}
