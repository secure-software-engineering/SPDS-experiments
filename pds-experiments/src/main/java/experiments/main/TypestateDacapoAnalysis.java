package experiments.main;

import java.io.File;
import java.io.IOException;

import experiments.dacapo.FinkOrIDEALDacapoRunner;

public class TypestateDacapoAnalysis {

	static String[] dacapo = new String[] { "antlr", "chart", "eclipse", "hsqldb", "jython", "luindex", "lusearch",
			"pmd", "xalan", "bloat" };
	static String[] analyses = new String[] { "#ideal","ideal-ap","#fink-apmust","#fink-unique" };
	static String[] rules = new String[] { 
	    "EmptyVector",	"IteratorHasNext",
		    "KeyStore",
		    "URLConnection",
		    "InputStreamCloseThenRead",
		    "PipedInputStream",
		    "OutputStreamCloseThenWrite",
		    "PipedOutputStream",
		    "PrintStream",
		    "PrintWriter",
		    "Signature" };
	static String benchmarkFolder = "/Users/johannesspath/Arbeit/Fraunhofer/pointsto-experiments/dacapo/";
	 
	public static void main(String... args) {
		for(String rule: rules){
			for (String bench : dacapo) {
				for(String analysis : analyses){
					if(ignore(rule)) {
						continue;
					}
					if(ignore(analysis)) {
						continue;
					}
					if(ignore(bench)) {
						continue;
					}
					String javaHome = System.getProperty("java.home");
			        String javaBin = javaHome +
			                File.separator + "bin" +
			                File.separator + "java";
					ProcessBuilder builder = new ProcessBuilder(new String[] {javaBin, "-Xmx12g","-Xss164m","-cp",  System.getProperty("java.class.path"), FinkOrIDEALDacapoRunner.class.getName(),analysis, rule, benchmarkFolder, bench,"true", "true"});
					builder.inheritIO();
					Process process;
					try {
						process = builder.start();
						process.waitFor();
					} catch (IOException | InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private static boolean ignore(String rule) {
		return rule.startsWith("#");
	}
}
