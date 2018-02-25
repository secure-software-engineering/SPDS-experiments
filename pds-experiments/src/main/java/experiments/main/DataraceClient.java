package experiments.main;

import java.io.File;
import java.io.IOException;

import experiments.dacapo.demand.driven.DataraceClientExperiment;

public class DataraceClient {

	static String[] dacapo = new String[] { "antlr", "chart", "eclipse", "hsqldb", "jython", "luindex", "lusearch",
			"xalan", "fop", "bloat","pmd" };

	public static void main(String... args) {
		for (String bench : dacapo) {
			if (ignore(bench)) {
				continue;
			}		
			String javaHome = System.getProperty("java.home");
	        String javaBin = javaHome +
	                File.separator + "bin" +
	                File.separator + "java";
			ProcessBuilder builder = new ProcessBuilder(new String[] {javaBin, "-Xmx12g","-Xss164m","-cp",  System.getProperty("java.class.path"), DataraceClientExperiment.class.getName(),args[0], bench});
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

	private static boolean ignore(String rule) {
		return rule.startsWith("#");
	}
}
