package microbench;

import com.ibm.safe.core.tests.SafeRegressionDriver;
import com.ibm.safe.internal.exceptions.SafeException;
import com.ibm.safe.options.WholeProgramProperties;

import junit.framework.TestCase;

public class AbstractTestCase extends TestCase{
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
