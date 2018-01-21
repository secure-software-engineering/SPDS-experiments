package microbench;

import java.lang.reflect.InvocationTargetException;

import com.ibm.safe.j2se.typestate.IOTest;
import com.ibm.safe.j2se.typestate.IteratorTest;
import com.ibm.safe.j2se.typestate.KeyStoreTest;
import com.ibm.safe.j2se.typestate.SignatureTest;
import com.ibm.safe.j2se.typestate.URLConnectionTest;
import com.ibm.safe.j2se.typestate.VectorTest;

public class MicroBenchRunner {
	public static final Class[] TEST_CLASSES = {
			VectorTest.class,
			IteratorTest.class,
			URLConnectionTest.class,
			IOTest.class,
			KeyStoreTest.class,
			SignatureTest.class,
	};
	public static void main(String... args) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException{
		for(Class c : TEST_CLASSES)
			SingleTestClassRunner.main(new String[]{c.getName().toString()});
	}
}
