
package experiments.typestate.microbench;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Table;
import com.ibm.safe.properties.CommonProperties;
import com.ibm.safe.typestate.options.TypestateProperties;

import boomerang.ap.AliasFinder;
import boomerang.ap.BoomerangOptions;
import boomerang.cfg.ExtendedICFG;
import boomerang.cfg.IExtendedICFG;
import boomerang.jimple.Statement;
import boomerang.jimple.Val;
import boomerang.preanalysis.PreparationTransformer;
import boomerang.WeightedForwardQuery;
import experiments.dacapo.idealap.StatsDebugger;
import ideal.IDEALSeedSolver;
import ideal.ap.Analysis;
import ideal.ap.AnalysisSolver;
import ideal.ap.IFactAtStatement;
import ideal.ap.ResultReporter;
import ideal.debug.IDebugger;
import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.options.Options;
import typestate.TransitionFunction;
import typestate.ap.ConcreteState;
import typestate.ap.TypestateAnalysisProblem;
import typestate.ap.TypestateChangeFunction;
import typestate.ap.TypestateDomainValue;
import typestate.finiteautomata.ITransition;

public class IDEALAPTestSetup{

	private static StatsDebugger debugger;
	protected long analysisTime;

	@SuppressWarnings("static-access")
	private static void initializeSoot(String targetClass) {
		G.v().reset();
		List<String> includeList = new LinkedList<String>();
		includeList.add("java.lang.*");
		includeList.add("java.util.*");
		includeList.add("java.io.*");
		includeList.add("sun.misc.*");
		includeList.add("sun.nio.*");
		includeList.add("java.net.*");
		includeList.add("javax.servlet.*");
		includeList.add("javax.crypto.*");
		includeList.add("java.security.*");
		Options.v().set_include(includeList);
		Options.v().set_no_bodies_for_excluded(true);
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_whole_program(true);
		Options.v().set_prepend_classpath(true);
		Options.v().setPhaseOption("jb", "use-original-names:true");
		Options.v().setPhaseOption("cg.spark", "on");
		Options.v().set_include_all(true);
		Options.v().set_output_format(Options.output_format_none);
		Options.v().set_exclude(getExclusions());
		Options.v().set_soot_classpath(getClassPath());
		Options.v().set_main_class(targetClass);
		Scene.v().addBasicClass(targetClass, SootClass.BODIES);
		Scene.v().loadNecessaryClasses();
		SootClass c = Scene.v().forceResolve(targetClass, SootClass.BODIES);
		if (c != null) {
			c.setApplicationClass();
		}
		SootMethod methodByName = c.getMethodByName("main");
		List<SootMethod> ePoints = new LinkedList<>();
		ePoints.add(methodByName);
		Scene.v().setEntryPoints(ePoints);
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.preparationTransform", new PreparationTransformer()));
	}

	private static String getClassPath() {
		String relativePath = ".." + File.separator + "safeMVN" + File.separator +"target" +File.separator + "classes";
		return new File(relativePath).getAbsolutePath();
	}

	private static List<String> getExclusions() {
		LinkedList<String> excl = new LinkedList<String>();
		excl.add("COM.rsa.*");
		excl.add("com.ibm.jvm.*");
		excl.add("com.sun.corba.*");
		excl.add("com.sun.net.*");
		excl.add("com.sun.deploy.*");
		excl.add("sun.nio.*");
		excl.add("java.util.logging.*");
		excl.add("sun.util.logging.*");
		excl.add("javax.imageio.*");
		excl.add("javax.swing.*");
		excl.add("sun.swing.*");
		excl.add("java.awt.*");
		excl.add("sun.awt.*");
		excl.add("sun.security.*");
		excl.add("com.sun.*");
		excl.add("sun.*");
		return excl;
	}

	protected static Analysis<TypestateDomainValue<ConcreteState>> createAnalysis(TypestateRegressionUnit test) {
		String rule = test.getOptions().get(TypestateProperties.Props.SELECT_TYPESTATE_RULES.getName());
		Class className = Util.selectTypestateMachine(rule);
		try {
			for (SootClass c : Scene.v().getClasses()) {
				if (c.toString().contains("j2se.typestate")) {
					c.setApplicationClass();
				} else {
					c.setLibraryClass();
				}
			}

			String classNameStr = className.getName().replace("typestate.", "typestate.ap.");
		    	final ExtendedICFG icfg = new ExtendedICFG(false);
		    	System.out.println("Reachable Methods" +  Scene.v().getReachableMethods().size());
		    	Analysis.ALIASING_FOR_STATIC_FIELDS = false;
		    	Analysis.SEED_IN_APPLICATION_CLASS_METHOD = true;
			AliasFinder.HANDLE_EXCEPTION_FLOW = false;
			final TypestateChangeFunction genericsType = (TypestateChangeFunction) Class.forName(classNameStr).getConstructor()
		          .newInstance();
		    	debugger = new StatsDebugger(icfg);
		    	return new Analysis<TypestateDomainValue<ConcreteState>>(new TypestateAnalysisProblem<ConcreteState>() {

		    		@Override
		    		public long analysisBudgetInSeconds() {
		    			return 30;
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
						return new BoomerangOptions(){
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
					}});
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

	public static void run(final TypestateRegressionUnit test) {
		String targetClass = test.getOptions().get(CommonProperties.Props.MAIN_CLASSES.getName());
		initializeSoot(targetClass);
		Transform transform = new Transform("wjtp.ifds", new SceneTransformer() {
			protected void internalTransform(String phaseName, @SuppressWarnings("rawtypes") Map options) {
//				System.out.println(Scene.v().getMainMethod().getActiveBody());
				Analysis<TypestateDomainValue<ConcreteState>> idealSolver = createAnalysis(test);
				idealSolver.run();

				String output = System.getProperty("outputCsvFile");
//				if (output != null && !output.equals("")) {
//					File file = new File(output);
//					boolean existed = file.exists();
//					FileWriter writer;
//					try {
//						writer = new FileWriter(file, true);
//						int expected = Integer.parseInt(System.getProperty("expectedFinding"));
//						if (!existed)
//							writer.write("Method;PropagationCounts;VisitedMethods;Actual Errors;Expected Errors;Number of Seeds;False Positives;False Negatives\n");
//						int diff = errorCount - expected;
//						int falseNegatives = (diff < 0 ? -diff : 0);
//						int falsePositives = (diff > 0 ? diff : 0);
//						writer.write(String.format("%s;%s;%s;%s;%s;%s;%s;%s;\n", System.getProperty("method"), totalPropagationCount, totalVisitedMethods.size(), errorCount, expected, solvers.keySet().size(), falsePositives, falseNegatives));
//						writer.close();
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
			}

		});
		PackManager.v().getPack("wjtp").add(transform);
		PackManager.v().getPack("cg").apply();
		PackManager.v().getPack("wjtp").apply();
	}

}
