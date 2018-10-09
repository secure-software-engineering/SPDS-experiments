package experiments.main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import boomerang.accessgraph.AccessGraph;
import experiments.field.complexity.AbstractAnalysis;
import experiments.field.complexity.AccessPathAnalysis;
import experiments.field.complexity.PDSAnalysis;

public class FieldExplosion {

	/**
	 * Generale timeout for each individual query.
	 */
	private static final int TIMEOUT_IN_MS = 100 * 1000;
	
	/**
	 * The analysis starts with test case Fields2LongTest (N = 2) and continues until MAX_EXPL_N. 
	 */
	private static final int MAX_EXPL_N = 21;
	/**
	 * Mapping from TestCaseName(class name of classes
	 * experiments.field.complexity.benchmark) to the analysis time for the SPDS
	 * based analysis
	 */
	private static Map<String, Long> pdsAnalysisTimes = new LinkedHashMap<>();

	/**
	 * Defines the used k-limit for the access path based analysis. Note: -1 equals
	 * Access Graph model
	 */
	private static int[] AP = new int[] { -1, 1, 2, 3, 4, 5 };
	/**
	 * Maps the access path length to a map. The latter than maps from a test case
	 * to the analysis time.
	 */
	private static Map<Integer, Map<String, Long>> apAnalysisTimes = new LinkedHashMap<>();

	/**
	 * A map holding which k-limit already timedout. Used to prevent overlong
	 * computation.
	 */
	private static boolean[] aptimeouts = new boolean[] { false, false, false, false, false, false, false };

	/**
	 * Number of times one wants to repeat this experiment
	 */
	public static int NUMBER_OF_ITERATIONS = 5;

	public static void main(String... args) {
		for (int i = 0; i < NUMBER_OF_ITERATIONS; i++) {
			runOneIteration();
		}
		printResults();
	}

	private static void printResults() {
		File file = new File("FieldExplosion-ApVsPds.csv");
		try {
			FileWriter writer = new FileWriter(file);

			String header = "index;testCaseName;PushdownSystem;";
			for (int length : AP) {
				header += "AccessPath_" + length + ";";
			}
			writer.write(header);
			writer.write("\n");
			int i = 2;
			for (String s : pdsAnalysisTimes.keySet()) {
				Long long1 = pdsAnalysisTimes.get(s);
				Float f1 = (long1 != null ? ((float) long1 / (NUMBER_OF_ITERATIONS * 1000)) : TIMEOUT_IN_MS);
				String line = i + ";" + s + ";" + f1 + ";";
				for (int length : AP) {
					Map<String, Long> map = apAnalysisTimes.get(length);
					Long long2 = map.get(s);
					Float f2 = (long2 != null ? ((float) long2 / (NUMBER_OF_ITERATIONS * 1000)) : TIMEOUT_IN_MS);
					line += f2 + ";";
				}
				writer.write(line);
				writer.write("\n");
				i++;
			}
			writer.close();
		} catch (IOException e1) {

		}
	}

	private static void runOneIteration() {
		// Load test case class in package experiments.field.complexity.benchmark
		for (int i = 2; i < MAX_EXPL_N; i++) {
			// Define class name to run the analysis on.
			String testCaseName = "experiments.field.complexity.benchmark.Fields" + i + "LongTest";

			// Execute SPDS based analysis
			AbstractAnalysis analysis = new PDSAnalysis(testCaseName, TIMEOUT_IN_MS);
			System.out.println("Running analysis based on SPDS");
			analysis.run();
			increase(testCaseName, pdsAnalysisTimes, analysis.getAnalysisTime().toMillis());

			// Run for all k-limits
			for (int length : AP) {
				System.out.println("Running analysis with Access Path / Access Graph (k = " + length);
				runAnalysisWithKLimit(testCaseName, length);
			}
		}
	}

	private static void runAnalysisWithKLimit(String testCaseName, int i) {
		// If the analysis already timeout for a early, skip.
		if (!aptimeouts[(i < 0 ? 0 : i)]) {
			AccessGraph.KLimiting = i;
			AccessPathAnalysis analysis = new AccessPathAnalysis(testCaseName, TIMEOUT_IN_MS);
			analysis.run();
			long millis = analysis.getAnalysisTime().toMillis();
			if (millis > TIMEOUT_IN_MS) {
				aptimeouts[(i < 0 ? 0 : i)] = true;
			}
			Map<String, Long> map = apAnalysisTimes.get(i);
			if (map == null) {
				map = new HashMap<>();
				apAnalysisTimes.put(i, map);
			}
			increase(testCaseName, map, millis);
		} else {
			System.out.println("Skipping test for Access Path Based Analysis k = " + i +" (previous analysis timed-out already) " + testCaseName);
		}
	}

	private static void increase(String testCaseName, Map<String, Long> map, long millis) {
		if (millis > TIMEOUT_IN_MS)
			millis = TIMEOUT_IN_MS;
		Long curr = map.get(testCaseName);
		if (curr == null) {
			map.put(testCaseName, millis);
		} else {
			map.put(testCaseName, millis + curr);
		}
	}

}
