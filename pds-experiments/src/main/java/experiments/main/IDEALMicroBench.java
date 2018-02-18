package experiments.main;

import java.lang.reflect.InvocationTargetException;

import experiments.typestate.microbench.tests.AbstractTestCase;
import experiments.typestate.microbench.tests.IOTest;
import experiments.typestate.microbench.tests.IteratorTest;
import experiments.typestate.microbench.tests.KeyStoreTest;
import experiments.typestate.microbench.tests.SignatureTest;
import experiments.typestate.microbench.tests.URLConnectionTest;
import experiments.typestate.microbench.tests.VectorTest;

public class IDEALMicroBench {
	public static final Class[] TEST_CLASSES = { VectorTest.class,
			IteratorTest.class, URLConnectionTest.class,
			IOTest.class, KeyStoreTest.class, SignatureTest.class, };
//	public static final Class[] TEST_CLASSES = { VectorTest.class};

	public static void main(String... args) throws ClassNotFoundException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, InstantiationException {
		runIDEALBasedTypestate();
//		runFinkStagedTypestate();
//		runIDEALBasedTypestateNoStrongUpdate();
//		runIDEALBasedTypestateNoAliasing();
	}

	private static void runFinkStagedTypestate() throws ClassNotFoundException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, InstantiationException {
		System.setProperty("analysis", "fink-staged");
		System.setProperty("aliasing", "true");
		System.setProperty("strongUpdates", "true");
		System.setProperty("isTestsuite", "true");
		run();
	}

	private static void run() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
	
		for (Class c : TEST_CLASSES)
			AbstractTestCase.runAllTests(c);
	}

	private static void runIDEALBasedTypestate() throws ClassNotFoundException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, InstantiationException {
		System.setProperty("analysis", "ideal");
		System.setProperty("aliasing", "true");
		System.setProperty("strongUpdates", "true");
		run();
	}

	private static void runIDEALBasedTypestateNoAliasing() throws ClassNotFoundException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, InstantiationException {
		System.setProperty("analysis", "ideal");
		System.setProperty("aliasing", "false");
		System.setProperty("strongUpdates", "true");
		run();
	}

	private static void runIDEALBasedTypestateNoStrongUpdate() throws ClassNotFoundException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, InstantiationException {
		System.setProperty("analysis", "ideal");
		System.setProperty("aliasing", "true");
		System.setProperty("strongUpdates", "false");
		run();
	}
}
