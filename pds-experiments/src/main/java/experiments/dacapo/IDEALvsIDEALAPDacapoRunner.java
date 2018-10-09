package experiments.dacapo;

import java.io.File;
import java.util.regex.Pattern;

import experiments.dacapo.ideal.IDEALRunner;
import experiments.dacapo.idealap.IDEALAPRunner;

public class IDEALvsIDEALAPDacapoRunner extends SootSceneSetupDacapo {
	private static String project;
	private static String benchFolder;

	public IDEALvsIDEALAPDacapoRunner(String benchmarkFolder, String benchFolder) {
		super(benchmarkFolder, benchFolder);
	}



	public static void main(String[] args) throws Exception {
		System.setProperty("analysis", args[0]);
		System.setProperty("rule", args[1]);
		benchFolder = args[2];
		project =  args[3];
		new IDEALvsIDEALAPDacapoRunner(benchFolder,project).run();
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
		System.setProperty("rule", Util.selectTypestateMachine(System.getProperty("rule")).getName());
		System.out.println("running " + System.getProperty("rule"));
		System.setProperty("dacapo", "true");
		if(analysis.equalsIgnoreCase("ideal")){
			new IDEALRunner(benchFolder,project).run(outputFile);
		}else if(analysis.equalsIgnoreCase("ideal-ap")){
			new IDEALAPRunner(benchFolder,project).run(outputFile);
		}
	}

}
