package experiments.main;

import experiments.dacapo.demand.driven.DataraceClientExperiment;

public class DataraceClient {

	static String[] dacapo = new String[] { "antlr", "chart", "eclipse", "hsqldb", "jython", "luindex", "lusearch",
			"xalan", "fop", "bloat","pmd" };

	public static void main(String... args) {
		for (String bench : dacapo) {
			if (ignore(bench)) {
				continue;
			}		
			DataraceClientExperiment.main(new String[] {args[0],bench });
		}
	}

	private static boolean ignore(String rule) {
		return rule.startsWith("#");
	}
}
