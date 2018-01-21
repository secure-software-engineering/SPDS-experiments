package microbench;

import java.lang.reflect.InvocationTargetException;

import com.ibm.safe.j2se.typestate.IOTest;
import com.ibm.safe.j2se.typestate.IteratorTest;
import com.ibm.safe.j2se.typestate.KeyStoreTest;
import com.ibm.safe.j2se.typestate.SignatureTest;
import com.ibm.safe.j2se.typestate.URLConnectionTest;
import com.ibm.safe.j2se.typestate.VectorTest;

public class Main {
	public static final Class[] TEST_CLASSES = { VectorTest.class, IteratorTest.class, URLConnectionTest.class,
			IOTest.class, KeyStoreTest.class, SignatureTest.class, };

	public static void main(String... args) throws ClassNotFoundException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, InstantiationException {
		runIDEALBasedTypestate();
		runFinkStagedTypestate();
		runIDEALBasedTypestateNoStrongUpdate();
		runIDEALBasedTypestateNoAliasing();
	}

	private static void runFinkStagedTypestate() throws ClassNotFoundException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, InstantiationException {
		System.setProperty("analysis", "fink-staged");
		System.setProperty("aliasing", "true");
		System.setProperty("strongUpdates", "true");
		for (Class c : TEST_CLASSES)
			SingleTestClassRunner.main(new String[] { c.getName().toString() });
	}

	private static void runIDEALBasedTypestate() throws ClassNotFoundException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, InstantiationException {
		System.setProperty("analysis", "ideal");
		System.setProperty("aliasing", "true");
		System.setProperty("strongUpdates", "true");
		for (Class c : TEST_CLASSES)
			SingleTestClassRunner.main(new String[] { c.getName().toString() });
	}

	private static void runIDEALBasedTypestateNoAliasing() throws ClassNotFoundException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, InstantiationException {
		System.setProperty("analysis", "ideal");
		System.setProperty("aliasing", "false");
		System.setProperty("strongUpdates", "true");
		for (Class c : TEST_CLASSES)
			SingleTestClassRunner.main(new String[] { c.getName().toString() });
	}

	private static void runIDEALBasedTypestateNoStrongUpdate() throws ClassNotFoundException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, InstantiationException {
		System.setProperty("analysis", "ideal");
		System.setProperty("aliasing", "true");
		System.setProperty("strongUpdates", "false");
		for (Class c : TEST_CLASSES)
			SingleTestClassRunner.main(new String[] { c.getName().toString() });
	}
}
