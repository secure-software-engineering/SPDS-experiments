package com.ibm.safe.typestate.tests;

import com.ibm.safe.core.tests.SafeRegressionDriver;
import com.ibm.safe.internal.exceptions.SafeException;
import com.ibm.safe.properties.CommonProperties;

public class SAFETypestateDacapoRunner extends ResearchQuestion {
	public static void main(String[] args) throws Exception {
		new SAFETypestateDacapoRunner().run();
	}

	private void run() throws Exception {
		TypestateRegressionUnit test = new TypestateRegressionUnit(null, 0);
		test.selectTypestateRule(System.getProperty("analysis"));
		// ""-DbenchmarkFolder=/Users/spaeth/Documents/Projects/benchmark/
		// -Danalysis=typestate.iteratoranalysis.HasNextAnalysis -DtoCSV=true
		// -Dbenchmark=retrofit"
		test.setOption(CommonProperties.Props.MODULES_DIRS.getName(), getBasePath()); // $NON-NLS-1$
		test.setOption(CommonProperties.Props.MODULES.getName(), getModuleNames());
		test.setOption(CommonProperties.Props.MAIN_CLASSES.getName(), getMainClass());
		test.setOption(CommonProperties.Props.TIMEOUT_SECS.getName(), "6000");
		test.setOption(CommonProperties.Props.SHORT_PROGRAM_NAME.getName(), getBenchName());
		test.selectAPMustMustNotTypestateSolver();
		SafeRegressionDriver.run(test);
	}

	private String getModuleNames() {
		String input_jar_files = benchProperties.getProperty("input_jar_files");
		String library_jar_files = benchProperties.getProperty("library_jar_files");
		input_jar_files += ":" + library_jar_files;
		return input_jar_files.replace(":", ",");
	}
}
