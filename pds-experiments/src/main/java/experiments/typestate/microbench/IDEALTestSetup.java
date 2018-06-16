
package experiments.typestate.microbench;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.beust.jcommander.internal.Sets;
import com.google.common.collect.Table;
import com.ibm.safe.core.tests.SafeRegressionUnit;
import com.ibm.safe.internal.exceptions.SafeException;
import com.ibm.safe.properties.CommonProperties;
import com.ibm.safe.typestate.options.TypestateProperties;

import boomerang.BoomerangOptions;
import boomerang.DefaultBoomerangOptions;
import boomerang.WeightedBoomerang;
import boomerang.cfg.ExtendedICFG;
import boomerang.debugger.Debugger;
import boomerang.jimple.Statement;
import boomerang.jimple.Val;
import boomerang.preanalysis.PreparationTransformer;
import boomerang.results.ForwardBoomerangResults;
import boomerang.WeightedForwardQuery;
import ideal.IDEALAnalysis;
import ideal.IDEALAnalysisDefinition;
import ideal.IDEALSeedSolver;
import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import soot.options.Options;
import sync.pds.solver.WeightFunctions;
import typestate.TransitionFunction;
import typestate.finiteautomata.ITransition;
import typestate.finiteautomata.TypeStateMachineWeightFunctions;

public class IDEALTestSetup{

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

	protected static IDEALAnalysis<TransitionFunction> createAnalysis(TypestateRegressionUnit test) {
		String rule = test.getOptions().get(TypestateProperties.Props.SELECT_TYPESTATE_RULES.getName());
		Class className = Util.selectTypestateMachine(rule);
		try {
			final JimpleBasedInterproceduralCFG icfg = new JimpleBasedInterproceduralCFG(true);
			for (SootClass c : Scene.v().getClasses()) {
				if (c.toString().contains("j2se.typestate")) {
					c.setApplicationClass();
				} else {
					c.setLibraryClass();
				}
			}
			
			final TypeStateMachineWeightFunctions genericsType = (TypeStateMachineWeightFunctions) className.getConstructor()
			          .newInstance();
					
			return new IDEALAnalysis<TransitionFunction>(new IDEALAnalysisDefinition<TransitionFunction>() {

				@Override
				public Collection<WeightedForwardQuery<TransitionFunction>> generate(SootMethod method, Unit stmt, Collection<SootMethod> calledMethod) {
					if(!method.getDeclaringClass().isApplicationClass())
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
				public boolean enableStrongUpdates() {
					return Util.strongUpdates();
				}
				
				@Override
				public Debugger<TransitionFunction> debugger(IDEALSeedSolver<TransitionFunction> solver) {
					return new Debugger<>();
				}

				public BoomerangOptions boomerangOptions() {
					return new DefaultBoomerangOptions() {
						@Override
						public boolean aliasing() {
							return Util.aliasing();
						}
					};
				}
			}){};
			    	
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
		}
		return null;
	}

	public static void run(final TypestateRegressionUnit test) {
		System.out.println("Strong updates: " + Util.strongUpdates());
		System.out.println("Aliasing : " + Util.aliasing());
		String targetClass = test.getOptions().get(CommonProperties.Props.MAIN_CLASSES.getName());
		initializeSoot(targetClass);
		Transform transform = new Transform("wjtp.ifds", new SceneTransformer() {
			protected void internalTransform(String phaseName, @SuppressWarnings("rawtypes") Map options) {
//				System.out.println(Scene.v().getMainMethod().getActiveBody());
				IDEALAnalysis<TransitionFunction> idealSolver = createAnalysis(test);
				Map<WeightedForwardQuery<TransitionFunction>, ForwardBoomerangResults<TransitionFunction>> solvers = idealSolver.run();
				int totalPropagationCount = 0;
				Set<SootMethod> totalVisitedMethods = Sets.newHashSet();
				int errorCount = 0;
				for(Entry<WeightedForwardQuery<TransitionFunction>, ForwardBoomerangResults<TransitionFunction>> e : solvers.entrySet()) {
					ForwardBoomerangResults<TransitionFunction> phase2Solver = e.getValue();
					totalVisitedMethods.addAll(phase2Solver.getStats().getCallVisitedMethods());
					totalPropagationCount += phase2Solver.getStats().getForwardReachesNodes().size();
					
					if(isInErrorState(e.getKey(),e.getValue())) {
						errorCount++;
					}
				}
				System.out.println("Findings: " + errorCount);

				String output = System.getProperty("outputCsvFile");
				if (output != null && !output.equals("")) {
					File file = new File(output);
					boolean existed = file.exists();
					FileWriter writer;
					try {
						writer = new FileWriter(file, true);
						int expected = Integer.parseInt(System.getProperty("expectedFinding"));
						if (!existed)
							writer.write("Method;PropagationCounts;VisitedMethods;Actual Errors;Expected Errors;Number of Seeds;False Positives;False Negatives\n");
						int diff = errorCount - expected;
						int falseNegatives = (diff < 0 ? -diff : 0);
						int falsePositives = (diff > 0 ? diff : 0);
						writer.write(String.format("%s;%s;%s;%s;%s;%s;%s;%s;\n", System.getProperty("method"), totalPropagationCount, totalVisitedMethods.size(), errorCount, expected, solvers.keySet().size(), falsePositives, falseNegatives));
						writer.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		    private boolean isInErrorState(WeightedForwardQuery<TransitionFunction> key, ForwardBoomerangResults<TransitionFunction> forwardBoomerangResults) {
		        Table<Statement, Val, TransitionFunction> objectDestructingStatements = forwardBoomerangResults.getObjectDestructingStatements();
		        for(Table.Cell<Statement,Val,TransitionFunction> c : objectDestructingStatements.cellSet()){
		            for(ITransition t : c.getValue().values()){
		                if(t.to() != null){
		                    if(t.to().isErrorState()){
		                        return true;
		                    }
		                }
		            }

		        }
		      return false;
		    }
		});
		PackManager.v().getPack("wjtp").add(transform);
		PackManager.v().getPack("cg").apply();
		PackManager.v().getPack("wjtp").apply();
	}

}
