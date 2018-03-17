package experiments.typestate.microbench.tests;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.ibm.safe.core.tests.SafeRegressionDriver;
import com.ibm.safe.internal.exceptions.SafeException;
import com.ibm.safe.options.WholeProgramProperties;

import experiments.typestate.microbench.IDEALAPTestSetup;
import experiments.typestate.microbench.IDEALTestSetup;
import experiments.typestate.microbench.TypestateRegressionUnit;
import experiments.typestate.microbench.Util;
import junit.framework.TestCase;

public class AbstractTestCase extends TestCase{
	

	public static void runAllTests(Class clz) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
		for (Method m : clz.getDeclaredMethods()) {
			System.setProperty("method", m.getName().toString());
			m.invoke(clz.newInstance(), null);
		}
	}
	
	
	public void run(TypestateRegressionUnit test) throws SafeException, Exception{
		System.setProperty("expectedFinding", String.valueOf(test.getExpectedNumberOfFindings()));
		if(!new File("outputMicro").exists())
			new File("outputMicro").mkdirs();
		String outputFile =  "outputMicro/"+System.getProperty("analysis") + "-" + this.getClass().getName() +  (Util.aliasing() ? "" : "-noAliasing") + (Util.strongUpdates() ? "" : "-noStrongUpdates") + ".csv";
		System.setProperty("outputCsvFile", outputFile);
		if(System.getProperty("analysis").equalsIgnoreCase("fink-mustnot")){
			test.setOption(WholeProgramProperties.Props.CG_KIND.getName(), "ZERO_ONE_CUSTOM");
			test.selectAPMustTypestateSolver(); 
			SafeRegressionDriver.run(test);
		} else if(System.getProperty("analysis").equalsIgnoreCase("fink-unique")){
			test.setOption(WholeProgramProperties.Props.CG_KIND.getName(), "ZERO_ONE_CUSTOM");
			test.selectUniqueTypestateSolver(); 
			SafeRegressionDriver.run(test);
		} else if(System.getProperty("analysis").equalsIgnoreCase("ideal")){
			IDEALTestSetup.run(test);
		} else if(System.getProperty("analysis").equalsIgnoreCase("ideal-ap")){
			IDEALAPTestSetup.run(test);
		} else
			throw new RuntimeException("Inappropriate -Danalysis option for JVM!");
	}
	
}
