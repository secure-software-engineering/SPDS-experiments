package experiments.main;

import java.io.File;
import java.io.IOException;

import experiments.dacapo.IDEALvsIDEALAPDacapoRunner;

public class TypestateDacapoAnalysis {
	/**
	 * All benchmark programs of DaCapo.
	 */
	static String[] dacapo = new String[] { "antlr", "chart", "eclipse", "hsqldb", "jython", "luindex", "lusearch",
			"pmd", "fop", "xalan", "bloat" };
	
	/**
	 * Strings to select the analysis: ideal-ap uses access graphs as heap model. ideal is the implementation
	 * using synchronized pushdown system as heap model.
	 */
	static String[] analyses = new String[] { "ideal", "ideal-ap" };
	
	/**
	 * Common typestate problem rules. The definition of the finite state machines are found in   
	 * package typestate.impl.statemachines of the project idealPDS.
	 */
	static String[] rules = new String[] { "IteratorHasNext", "InputStreamCloseThenRead", "PipedInputStream",
			"OutputStreamCloseThenWrite", "PipedOutputStream", "PrintStream", "PrintWriter", "EmptyVector", };

	/**
	 * Runs two IDEal-based typestate analysis using common typetate rules 
	 * @param args[0] must be defined as the absolute path to the DaCapo benchmark programs.
	 */
	public static void main(String... args) {
		if (args.length < 1) {
			System.out.println("Please supply path to dacapo benchmark (must end in slash)!");
		}
		for (String analysis : analyses) {
			for (String bench : dacapo) {
				for (String rule : rules) {
					if (ignore(rule)) {
						continue;
					}
					if (ignore(analysis)) {
						continue;
					}
					if (ignore(bench)) {
						continue;
					}
					
					//Start analysis in a separate process such that a potential OutOfMemoryException does not crash other results
					String javaHome = System.getProperty("java.home");
					String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
					ProcessBuilder builder = new ProcessBuilder(
							new String[] { javaBin, "-Xmx12g", "-Xss164m", "-cp", System.getProperty("java.class.path"),
									IDEALvsIDEALAPDacapoRunner.class.getName(), analysis, rule, args[0], bench });
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
