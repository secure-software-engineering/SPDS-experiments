package microbench;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.ibm.safe.properties.CommonProperties;
import com.ibm.safe.typestate.options.TypestateProperties;

import boomerang.cfg.ExtendedICFG;
import boomerang.cfg.IExtendedICFG;
import boomerang.preanalysis.PreparationTransformer;
import ideal.Analysis;
import ideal.AnalysisSolver;
import ideal.IFactAtStatement;
import ideal.ResultReporter;
import ideal.debug.IDebugger;
import runner.Util;
import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.options.Options;
import typestate.ConcreteState;
import typestate.TypestateAnalysisProblem;
import typestate.TypestateChangeFunction;
import typestate.TypestateDomainValue;

public class IDEALTestSetup {

	static String BIN_PATH = "/Users/johannesspath/Documents/ideal-workspace/safe/com.ibm.safe.typestate.testdata/bin";
	protected long analysisTime;

	@SuppressWarnings("static-access")
	private static void initializeSoot(String targetClass) {
		G.v().reset();
		List<String> includeList = new LinkedList<String>();
		includeList.add("java.lang.*");
		includeList.add("java.util.*");
		includeList.add("java.io.*");
		includeList.add("sun.misc.*");
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
		Options.v().set_soot_classpath(BIN_PATH);
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

	protected static Analysis createAnalysis(TypestateRegressionUnit test) {
		String rule = test.getOptions().get(TypestateProperties.Props.SELECT_TYPESTATE_RULES.getName());
		Class className = Util.selectTypestateMachine(rule);
		try {
			final ExtendedICFG icfg = new ExtendedICFG(true);
			for (SootClass c : Scene.v().getClasses()) {
				if (c.toString().contains("j2se.typestate")) {
					c.setApplicationClass();
				} else {
					c.setLibraryClass();
				}
			}
			
			final TypestateChangeFunction genericsType = (TypestateChangeFunction) className.getConstructor()
					.newInstance();
			return new Analysis<TypestateDomainValue<ConcreteState>>(new TypestateAnalysisProblem<ConcreteState>() {
				
				private IDebugger<TypestateDomainValue<ConcreteState>> debugger;
				@Override
				public long analysisBudgetInSeconds() {
					return 30;
				}
				@Override
				public boolean enableNullPointOfAlias() {
					return true;
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
				public IDebugger<TypestateDomainValue<ConcreteState>> debugger() {
					if(debugger == null)
						debugger = new TestsuiteToFileDebugger(icfg,enableAliasing() && enableStrongUpdates());
					return debugger;
				}
				
				@Override
				public boolean enableAliasing() {
					return Util.aliasing();
				}
				@Override
				public boolean enableStrongUpdates() {
					return Util.strongUpdates();
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
		}
		return null;
	}

	public static void run(final TypestateRegressionUnit test) {
		String targetClass = test.getOptions().get(CommonProperties.Props.MAIN_CLASSES.getName());
		initializeSoot(targetClass);
		Transform transform = new Transform("wjtp.ifds", new SceneTransformer() {
			protected void internalTransform(String phaseName, @SuppressWarnings("rawtypes") Map options) {
				System.out.println(Scene.v().getMainMethod().getActiveBody());
				createAnalysis(test).run();
			}
		});
		PackManager.v().getPack("wjtp").add(transform);
		PackManager.v().getPack("cg").apply();
		PackManager.v().getPack("wjtp").apply();
	}

}
