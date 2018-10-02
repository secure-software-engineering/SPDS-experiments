package experiments.dacapo;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;

import boomerang.BoomerangOptions;
import boomerang.DefaultBoomerangOptions;
import boomerang.ForwardQuery;
import boomerang.Query;
import boomerang.WeightedForwardQuery;
import boomerang.accessgraph.AccessGraph;
import boomerang.ap.AliasFinder;
import boomerang.ap.AliasResults;
import boomerang.cfg.ExtendedICFG;
import boomerang.cfg.IExtendedICFG;
import boomerang.debugger.Debugger;
import boomerang.jimple.Statement;
import boomerang.jimple.Val;
import boomerang.preanalysis.BoomerangPretransformer;
import boomerang.results.ForwardBoomerangResults;
import heros.solver.PathEdge;
import ideal.IDEALAnalysis;
import ideal.IDEALAnalysisDefinition;
import ideal.IDEALResultHandler;
import ideal.IDEALSeedSolver;
import ideal.ap.Analysis;
import ideal.ap.AnalysisSolver;
import ideal.ap.FactAtStatement;
import ideal.ap.IFactAtStatement;
import ideal.ap.ResultReporter;
import ideal.debug.IDebugger;
import ideal.pointsofaliasing.PointOfAlias;
import soot.G;
import soot.Local;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;
import sync.pds.solver.WeightFunctions;
import typestate.TransitionFunction;
import typestate.ap.ConcreteState;
import typestate.ap.TypestateAnalysisProblem;
import typestate.ap.TypestateChangeFunction;
import typestate.ap.TypestateDomainValue;
import typestate.finiteautomata.ITransition;
import typestate.finiteautomata.TypeStateMachineWeightFunctions;

public class IDEALandIDEALAPSanitizeCheck extends SootSceneSetupDacapo {

	private Map<IFactAtStatement, Boolean> apAnalysisErrors = Maps.newHashMap();
	private Map<Query, Boolean> pdsAnalysisErrors = Maps.newHashMap();
	
	public IDEALandIDEALAPSanitizeCheck(String benchmarkFolder, String benchFolder) {
		super(benchmarkFolder, benchFolder);
	}

	public void run() {
		G.v().reset();
		setupSoot();
		Transform transform = new Transform("wjtp.ifds", new SceneTransformer() {

			protected void internalTransform(String phaseName, @SuppressWarnings("rawtypes") Map options) {
				BoomerangPretransformer.v().reset();
				BoomerangPretransformer.v().apply();
				final ExtendedICFG icfg = new ExtendedICFG(false);
				IDEALAnalysis<TransitionFunction> pdsAnalysis = createPDSAnalysis(icfg);
				Analysis<TypestateDomainValue<ConcreteState>> apAnalysis = createAPAnalysis(icfg);
				Collection<Query> computeSeeds = pdsAnalysis.computeSeeds();
				if (Scene.v().getMainMethod() == null)
					throw new RuntimeException("No main class existing.");
				for (SootClass c : Scene.v().getClasses()) {
					for (String app : IDEALandIDEALAPSanitizeCheck.this.getApplicationClasses()) {
						if (c.isApplicationClass())
							continue;
						if (c.toString().startsWith(app.replace("<", ""))) {
							c.setApplicationClass();
						}
					}
				}
				System.out.println("Application Classes: " + Scene.v().getApplicationClasses().size());
				for (Query q : computeSeeds) {
//					if(!q.toString().contains("<dacapo.TestHarness: java.util.Vector vectorise(java.lang.String[])>"))
//						continue;
					System.out.println("Executing seed with PDS " + q);
					pdsAnalysis.run((ForwardQuery) q);
					if(!pdsAnalysisErrors.containsKey(q))
						continue;
					System.out.println("Executing seed with AP " + q);
					FactAtStatement factAtStatement = new ideal.ap.FactAtStatement(q.stmt().getUnit().get(),
							new AccessGraph((Local) q.var().value()));
					apAnalysis.analysisForSeed(factAtStatement);
					if(apAnalysisErrors.containsKey(factAtStatement) && !pdsAnalysisErrors.get(q).equals(apAnalysisErrors.get(factAtStatement))) {
						System.out.println("Results not equals for " + q);
						System.out.println("PDS: " + pdsAnalysisErrors.get(q) );
					}
				}
			}
		});

		// PackManager.v().getPack("wjtp").add(new Transform("wjtp.prep", new
		// PreparationTransformer()));
		PackManager.v().getPack("wjtp").add(transform);
		PackManager.v().getPack("cg").apply();
		PackManager.v().getPack("wjtp").apply();
	}

	private boolean isInErrorState(WeightedForwardQuery<TransitionFunction> key,
			ForwardBoomerangResults<TransitionFunction> forwardBoomerangResults) {
		Table<Statement, Val, TransitionFunction> objectDestructingStatements = forwardBoomerangResults
				.asStatementValWeightTable();
		System.out.println("RES " + key);
		for (Table.Cell<Statement, Val, TransitionFunction> c : objectDestructingStatements.cellSet()) {
			System.out.println(c);
			for (ITransition t : c.getValue().values()) {
				if (t.to() != null) {
					if (t.to().isErrorState()) {
						return true;
					}
				}
			}

		}
		return false;
	}


	protected long getBudget() {
		return TimeUnit.MINUTES.toMillis(1);
	}

	
	protected IDEALAnalysis<TransitionFunction> createPDSAnalysis(IExtendedICFG icfg) {
		String className = System.getProperty("rule");
		try {
			System.out.println("Reachable Methods" + Scene.v().getReachableMethods().size());
			final TypeStateMachineWeightFunctions genericsType = (TypeStateMachineWeightFunctions) Class
					.forName(className).getConstructor().newInstance();

			return new IDEALAnalysis<TransitionFunction>(new IDEALAnalysisDefinition<TransitionFunction>() {

				@Override
				public Collection<WeightedForwardQuery<TransitionFunction>> generate(SootMethod method, Unit stmt,
						Collection<SootMethod> calledMethod) {
					if (!method.getDeclaringClass().isApplicationClass())
						return Collections.emptyList();
					return genericsType.generateSeed(method, stmt, calledMethod);
				}

				@Override
				public WeightFunctions<Statement, Val, Statement, TransitionFunction> weightFunctions() {
					return genericsType;
				}

				@Override
				public BiDiInterproceduralCFG<Unit, SootMethod> icfg() {
					return icfg;
				}

				@Override
				public BoomerangOptions boomerangOptions() {
					return new DefaultBoomerangOptions() {
						@Override
						public int analysisTimeoutMS() {
							return (int) IDEALandIDEALAPSanitizeCheck.this.getBudget();
						}

						@Override
						public boolean arrayFlows() {
							return false;
						}
					};
				}

				@Override
				public Debugger<TransitionFunction> debugger(IDEALSeedSolver<TransitionFunction> solver) {
					return new Debugger<>();
				}

				@Override
				public IDEALResultHandler<TransitionFunction> getResultHandler() {
					return new IDEALResultHandler<TransitionFunction>() {
						@Override
						public void report(WeightedForwardQuery<TransitionFunction> seed,
								ForwardBoomerangResults<TransitionFunction> res) {
							
							pdsAnalysisErrors.put(seed, isInErrorState(seed, res));
						}
					};
				}
			}) {
			};

		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected Analysis<TypestateDomainValue<ConcreteState>> createAPAnalysis(ExtendedICFG icfg) {
		String className = System.getProperty("rule");
		className = className.replace("typestate.", "typestate.ap.");
		try {
			System.out.println("Reachable Methods" + Scene.v().getReachableMethods().size());
			Analysis.ALIASING_FOR_STATIC_FIELDS = false;
			Analysis.SEED_IN_APPLICATION_CLASS_METHOD = true;
			AliasFinder.HANDLE_EXCEPTION_FLOW = false;
			final TypestateChangeFunction genericsType = (TypestateChangeFunction) Class.forName(className)
					.getConstructor().newInstance();
			final IDebugger debugger = new IDebugger<TypestateDomainValue<ConcreteState>>() {

				

				@Override
				public void addSummary(SootMethod methodToSummary, PathEdge<Unit, AccessGraph> summary) {
				}

				@Override
				public void normalFlow(Unit start, AccessGraph startFact, Unit target, AccessGraph targetFact) {
				}

				@Override
				public void callFlow(Unit start, AccessGraph startFact, Unit target, AccessGraph targetFact) {
				}

				@Override
				public void callToReturn(Unit start, AccessGraph startFact, Unit target, AccessGraph targetFact) {
				}

				@Override
				public void returnFlow(Unit start, AccessGraph startFact, Unit target, AccessGraph targetFact) {
				}

				@Override
				public void setValue(Unit start, AccessGraph startFact, TypestateDomainValue<ConcreteState> value) {
					
					
				}

				@Override
				public void beforeAnalysis() {
					
					
				}

				@Override
				public void startWithSeed(IFactAtStatement seed) {
					
					
				}

				@Override
				public void startPhase1WithSeed(IFactAtStatement seed,
						AnalysisSolver<TypestateDomainValue<ConcreteState>> solver) {
					
					
				}

				@Override
				public void startPhase2WithSeed(IFactAtStatement seed,
						AnalysisSolver<TypestateDomainValue<ConcreteState>> solver) {
					
					
				}

				@Override
				public void finishPhase1WithSeed(IFactAtStatement seed,
						AnalysisSolver<TypestateDomainValue<ConcreteState>> solver) {
					
					
				}

				@Override
				public void finishPhase2WithSeed(IFactAtStatement seed,
						AnalysisSolver<TypestateDomainValue<ConcreteState>> solver) {
					
					
				}

				@Override
				public void finishWithSeed(PathEdge<Unit, AccessGraph> seed, boolean timeout, boolean isInErrorState,
						AnalysisSolver<TypestateDomainValue<ConcreteState>> solver) {
					
					
				}

				@Override
				public void afterAnalysis() {
					
					
				}

				@Override
				public void startAliasPhase(Set<PointOfAlias<TypestateDomainValue<ConcreteState>>> pointsOfAlias) {
					
					
				}

				@Override
				public void startForwardPhase(Set<PathEdge<Unit, AccessGraph>> worklist) {
					
					
				}

				@Override
				public void onAliasesComputed(AccessGraph boomerangAccessGraph, Unit curr, AccessGraph d1,
						AliasResults res) {
					
					
				}

				@Override
				public void onAliasTimeout(AccessGraph boomerangAccessGraph, Unit curr, AccessGraph d1) {
					
					
				}

				@Override
				public void beforeAlias(AccessGraph boomerangAccessGraph, Unit curr, AccessGraph d1) {
					
					
				}

				@Override
				public void killAsOfStrongUpdate(AccessGraph d1, Unit callSite, AccessGraph callNode, Unit returnSite,
						AccessGraph returnSideNode2) {
					
					
				}

				@Override
				public void detectedStrongUpdate(Unit callSite, AccessGraph receivesUpdate) {
					
					
				}

				@Override
				public void onAnalysisTimeout(IFactAtStatement seed) {
				}

				@Override
				public void solvePOA(PointOfAlias<TypestateDomainValue<ConcreteState>> p) {
					
					
				}

				@Override
				public void onNormalPropagation(AccessGraph d1, Unit curr, Unit succ, AccessGraph source) {
					
					
				}

				@Override
				public void indirectFlowAtWrite(AccessGraph source, Unit curr, AccessGraph target) {
					
					
				}

				@Override
				public void indirectFlowAtCall(AccessGraph source, Unit curr, AccessGraph target) {
					
					
				}

				@Override
				public void onSeedFinished(IFactAtStatement seed,
						AnalysisSolver<TypestateDomainValue<ConcreteState>> solver) {
					System.out.println(solver.getVisitedMethods());
					HashBasedTable<Unit, AccessGraph, TypestateDomainValue<ConcreteState>> endPathOfPropagation = solver.results();
					boolean error = false;
					for(Cell<Unit, AccessGraph, TypestateDomainValue<ConcreteState>> e : endPathOfPropagation.cellSet()){
						 for(ConcreteState s : e.getValue().getStates()){
							 if(s.isErrorState()){
								 error = true;
							 }	
						 }
					}
					apAnalysisErrors.put(seed,error);
					
				}
			};
			return new Analysis<TypestateDomainValue<ConcreteState>>(new TypestateAnalysisProblem<ConcreteState>() {

				@Override
				public long analysisBudgetInSeconds() {
					return TimeUnit.MINUTES.toSeconds(10);
				}

				@Override
				public TypestateChangeFunction<ConcreteState> createTypestateChangeFunction() {
					return genericsType;
				}

				@Override
				public ResultReporter<TypestateDomainValue<ConcreteState>> resultReporter() {
					return new ResultReporter<TypestateDomainValue<ConcreteState>>() {

						@Override
						public void onSeedFinished(IFactAtStatement seed,
								AnalysisSolver<TypestateDomainValue<ConcreteState>> solver) {

						}

						@Override
						public void onSeedTimeout(IFactAtStatement seed) {

						}
					};
				}

				@Override
				public IExtendedICFG icfg() {
					return icfg;
				}

				@Override
				public boolean enableAliasing() {
					return true;
				}

				@Override
				public boolean enableStrongUpdates() {
					return true;
				}

				@Override
				public boomerang.ap.BoomerangOptions boomerangOptions() {
					return new boomerang.ap.BoomerangOptions() {
						@Override
						public long getTimeBudget() {
							return 30000;
						}

						@Override
						public boolean getTrackStaticFields() {
							return Analysis.ALIASING_FOR_STATIC_FIELDS;
						}

						@Override
						public IExtendedICFG icfg() {
							return icfg;
						}
					};
				}

				@Override
				public IDebugger<TypestateDomainValue<ConcreteState>> debugger() {
					return debugger;
				}
			});
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
}
