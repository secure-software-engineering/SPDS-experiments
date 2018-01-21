package experiments.typestate.microbench.tests;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.ibm.safe.core.tests.SafeRegressionDriver;
import com.ibm.safe.internal.exceptions.SafeException;
import com.ibm.safe.options.WholeProgramProperties;

import experiments.typestate.microbench.IDEALTestSetup;
import experiments.typestate.microbench.TypestateRegressionUnit;
import junit.framework.TestCase;

public class AbstractTestCase extends TestCase{
	

	public static void runAllTests(Class clz) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
		for (Method m : clz.getDeclaredMethods()) {
			System.out.println(m);
			m.invoke(clz.newInstance(), null);
		}
	}
	
	
	public void run(TypestateRegressionUnit test) throws SafeException, Exception{
		System.setProperty("expectedFinding", String.valueOf(test.getExpectedNumberOfFindings()));
		if(System.getProperty("analysis").equalsIgnoreCase("fink-staged")){
			test.setOption(WholeProgramProperties.Props.CG_KIND.getName(), "ZERO_ONE_CFA");
			test.selectStagedTypestateSolver(); 
			SafeRegressionDriver.run(test);
		} else if(System.getProperty("analysis").equalsIgnoreCase("ideal")){
			IDEALTestSetup.run(test);
		} else
			throw new RuntimeException("Inappropriate -Danalysis option for JVM!");
	}
	
}
