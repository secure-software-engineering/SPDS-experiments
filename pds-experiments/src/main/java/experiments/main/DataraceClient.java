package experiments.main;

import experiments.dacapo.demand.driven.DataraceClientExperiment;

public class DataraceClient {

	static String[] dacapo = new String[] { "antlr", "chart", "eclipse", "hsqldb", "jython", "luindex", "lusearch",
			"pmd", "xalan", "fop", "bloat" };

	public static void main(String... args) {
//		for (String bench : dacapo) {
//			if (ignore(bench)) {
//				continue;
//			}		
			DataraceClientExperiment.main(new String[] {args[0], "chart" });
//		}
	}

	private static boolean ignore(String rule) {
		return rule.startsWith("#");
	}
}
