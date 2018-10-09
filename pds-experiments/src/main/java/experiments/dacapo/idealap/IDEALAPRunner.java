package experiments.dacapo.idealap;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import boomerang.ap.AliasFinder;
import boomerang.ap.BoomerangOptions;
import boomerang.cfg.ExtendedICFG;
import boomerang.cfg.IExtendedICFG;
import boomerang.preanalysis.PreparationTransformer;
import experiments.dacapo.SootSceneSetupDacapo;
import ideal.ap.Analysis;
import ideal.ap.AnalysisSolver;
import ideal.ap.DefaultIDEALAnalysisDefinition;
import ideal.ap.IFactAtStatement;
import ideal.ap.ResultReporter;
import ideal.debug.IDebugger;
import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.Transform;
import typestate.ap.ConcreteState;
import typestate.ap.TypestateAnalysisProblem;
import typestate.ap.TypestateChangeFunction;
import typestate.ap.TypestateDomainValue;

public class IDEALAPRunner extends SootSceneSetupDacapo {

	public IDEALAPRunner(String benchmarkFolder, String benchFolder) {
		super(benchmarkFolder, benchFolder);
	}

	protected Analysis<TypestateDomainValue<ConcreteState>> createAnalysis() {
		String className = System.getProperty("rule");
		className = className.replace("typestate.", "typestate.ap.");
		try {
			System.out.println("Reachable Methods" + Scene.v().getReachableMethods().size());
			final ExtendedICFG icfg = new ExtendedICFG(false);
			Analysis.ALIASING_FOR_STATIC_FIELDS = false;
			Analysis.SEED_IN_APPLICATION_CLASS_METHOD = true;
			AliasFinder.HANDLE_EXCEPTION_FLOW = false;
			final TypestateChangeFunction genericsType = (TypestateChangeFunction) Class.forName(className)
					.getConstructor().newInstance();
			final IDebugger debugger = new StatsDebugger(icfg);
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
				public BoomerangOptions boomerangOptions() {
					return new BoomerangOptions() {
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

	private Analysis<TypestateDomainValue<ConcreteState>> analysis;
	protected long analysisTime;

	public void run(final String outputFile) {
		G.v().reset();

		setupSoot();
		Transform transform = new Transform("wjtp.ifds", new SceneTransformer() {
			protected void internalTransform(String phaseName, @SuppressWarnings("rawtypes") Map options) {
				if (Scene.v().getMainMethod() == null)
					throw new RuntimeException("No main class existing.");
				for (SootClass c : Scene.v().getClasses()) {
					for (String app : IDEALAPRunner.this.getApplicationClasses()) {
						if (c.isApplicationClass())
							continue;
						if (c.toString().startsWith(app.replace("<", ""))) {
							c.setApplicationClass();
						}
					}
				}
				System.out.println("Application Classes: " + Scene.v().getApplicationClasses().size());
				IDEALAPRunner.this.getAnalysis().run();
			}
		});

		PackManager.v().getPack("wjtp").add(new Transform("wjtp.prep", new PreparationTransformer()));
		PackManager.v().getPack("wjtp").add(transform);
		PackManager.v().getPack("cg").apply();
		PackManager.v().getPack("wjtp").apply();
	}

	protected Analysis<TypestateDomainValue<ConcreteState>> getAnalysis() {
		if (analysis == null)
			analysis = createAnalysis();
		return analysis;
	}

}
