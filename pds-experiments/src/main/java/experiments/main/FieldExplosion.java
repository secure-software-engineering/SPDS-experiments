package experiments.main;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import experiments.field.complexity.AbstractAnalysis;
import experiments.field.complexity.AccessPathAnalysis;
import experiments.field.complexity.PDSAnalysis;
import heros.utilities.DefaultValueMap;

public class FieldExplosion {
	
	private static final int TIMEOUT_IN_MS = 100*1000;
	private static Map<String, Long> pdsAnalysisTimes = new LinkedHashMap<>();
	private static Map<String, Long> apAnalysisTimes = new LinkedHashMap<>();
	public static int NUMBER_OF_ITERATIONS = 5;
	public static void main(String...args) {
		for(int i = 0; i < NUMBER_OF_ITERATIONS; i++){
			runOnce();
		}
		System.out.println("index;testCaseName;PushdownSystem;AccessPathBased");
		int i = 2;
		for(String s : pdsAnalysisTimes.keySet()){
			Long long1 = pdsAnalysisTimes.get(s);
			Long long2 = apAnalysisTimes.get(s);
			Float f1 = (long1 != null ? ((float) long1/(NUMBER_OF_ITERATIONS*1000)) : TIMEOUT_IN_MS);
			Float f2 = (long2 != null ? ((float) long2/(NUMBER_OF_ITERATIONS*1000)) : TIMEOUT_IN_MS);
			System.out.println(i + ";" + s +";" +f1 + ";"+f2);
			i++;
		}
	}

	private static void runOnce() {
		boolean apTimeout = false;
		for(int i = 2; i < 21; i++){
			String testCaseName = "experiments.field.complexity.benchmark.Fields"+i+"LongTest";
			AbstractAnalysis analysis = new PDSAnalysis(testCaseName, TIMEOUT_IN_MS);
			analysis.run();
			increase(testCaseName,pdsAnalysisTimes,analysis.getAnalysisTime().toMillis());
			if(!apTimeout){
				analysis = new AccessPathAnalysis(testCaseName, TIMEOUT_IN_MS);
				analysis.run();
				long millis = analysis.getAnalysisTime().toMillis();
				if(millis > TIMEOUT_IN_MS){
					apTimeout = true;
				}
				increase(testCaseName,apAnalysisTimes,millis);
			} else{
				System.out.println("Skipping test for Access Path Based Analysis " + testCaseName);
			}
		}		
	}

	private static void increase(String testCaseName, Map<String, Long> map, long millis) {
		if(millis > TIMEOUT_IN_MS)
			millis = TIMEOUT_IN_MS;
		Long curr = map.get(testCaseName);
		if(curr == null){
			map.put(testCaseName, millis);
		} else{
			map.put(testCaseName, millis+curr);
		}
	}
	
}
