package experiments.dacapo;

import java.io.File;
import java.util.regex.Pattern;

import com.ibm.safe.core.tests.SafeRegressionDriver;
import com.ibm.safe.options.WholeProgramProperties;
import com.ibm.safe.properties.CommonProperties;
import com.ibm.safe.typestate.tests.TypestateRegressionUnit;

import experiments.dacapo.idealap.IDEALAPRunner;
import experiments.typestate.microbench.Util;

public class FinkOrIDEALDacapoRunner extends SootSceneSetupDacapo {
	private static String project;
	private static String benchFolder;

	public FinkOrIDEALDacapoRunner(String benchmarkFolder, String benchFolder) {
		super(benchmarkFolder, benchFolder);
	}



	public static void main(String[] args) throws Exception {
		System.setProperty("analysis", args[0]);
		System.setProperty("rule", args[1]);
		benchFolder = args[2];
		project =  args[3];
		new FinkOrIDEALDacapoRunner(benchFolder,project).run();
	}

	

	private void run() throws Exception {
		String analysis = System.getProperty("analysis");

		String library_jar_files = benchProperties.getProperty("application_includes");
		System.setProperty("application_includes", library_jar_files);
		if(analysis == null)
			throw new RuntimeException("Add -Danalysis to JVM arguments");
		String rule = System.getProperty("rule");
		if(Pattern.matches("PipedInputStream|InputStreamCloseThenRead|OutputStreamCloseThenWrite|PipedOutputStream|PrintStream|PrintWriter", rule)){
			rule = "IO";
		}
		System.setProperty("ruleIdentifier",rule);
		String outputDirectory = "outputDacapo";
		File outputDir = new File(outputDirectory);
		if(!outputDir.exists())
			outputDir.mkdir();
		String outputFile = outputDirectory+File.separator+getMainClass() +"-"+analysis+"-" + rule+".csv";
		System.setProperty("outputCsvFile", outputFile);
		
		System.out.println("Writing output to file " +outputFile);
		if(analysis.equalsIgnoreCase("ideal")){
			System.setProperty("rule", Util.selectTypestateMachine(System.getProperty("rule")).getName());
			System.out.println("running " + System.getProperty("rule"));
			System.setProperty("dacapo", "true");
			new IDEALRunner(benchFolder,project).run(outputFile);
		}else if(analysis.equalsIgnoreCase("ideal-ap")){
			System.setProperty("rule", Util.selectTypestateMachine(System.getProperty("rule")).getName());
			System.out.println("running " + System.getProperty("rule"));
			System.setProperty("dacapo", "true");
			new IDEALAPRunner(benchFolder,project).run(outputFile);
		} else if(analysis.equalsIgnoreCase("fink-apmust")){
			TypestateRegressionUnit test = new TypestateRegressionUnit(null, 0);
			test.selectTypestateRule(System.getProperty("rule"));
			test.setOption(CommonProperties.Props.MODULES.getName(), getModuleNames());
			test.setOption(CommonProperties.Props.MAIN_CLASSES.getName(), getMainClass());
			test.setOption(CommonProperties.Props.TIMEOUT_SECS.getName(), "60000");
			test.setOption(WholeProgramProperties.Props.CG_KIND.getName(), "ZERO_ONE_CFA");

			test.selectAPMustMustNotTypestateSolver();
			SafeRegressionDriver.run(test);
		} else if(analysis.equalsIgnoreCase("fink-unique")){
			TypestateRegressionUnit test = new TypestateRegressionUnit(null, 0);
			test.selectTypestateRule(System.getProperty("rule"));
			test.setOption(CommonProperties.Props.MODULES.getName(), getModuleNames());
			test.setOption(CommonProperties.Props.MAIN_CLASSES.getName(), getMainClass());
			test.setOption(CommonProperties.Props.TIMEOUT_SECS.getName(), "60000");
			test.setOption(WholeProgramProperties.Props.CG_KIND.getName(), "ZERO_ONE_CFA");

			test.selectUniqueTypestateSolver();
			SafeRegressionDriver.run(test);
		}

	}

	private String getModuleNames() {
		String input_jar_files = getBasePath() + benchProperties.getProperty("input_jar_files");
		String library_jar_files = getBasePath() + benchProperties.getProperty("library_jar_files");
		input_jar_files += File.pathSeparator + library_jar_files;
		System.out.println(input_jar_files);
		return input_jar_files.replace(":", ",");
	}
}
