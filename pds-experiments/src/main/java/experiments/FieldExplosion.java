package experiments;

import java.time.Duration;
import java.util.ArrayList;

import com.google.common.collect.Lists;

import field.complexity.AbstractAnalysis;
import field.complexity.AccessPathAnalysis;
import field.complexity.PDSAnalysis;

public class FieldExplosion {
	public static void main(String...args) {
		ArrayList<Long> pdsAnalysisTimes = Lists.<Long>newArrayList();
		ArrayList<Long> apAnalysisTimes = Lists.<Long>newArrayList();
		for(int i = 2; i < 21; i++){
			AbstractAnalysis analysis = new PDSAnalysis("field.complexity.benchmark.Fields"+i+"LongTest");
			analysis.run();
			pdsAnalysisTimes.add(analysis.getAnalysisTime().toMillis());
//			
			
			if(i < 11){
				analysis = new AccessPathAnalysis("field.complexity.benchmark.Fields"+i+"LongTest");
				analysis.run();
				apAnalysisTimes.add(analysis.getAnalysisTime().toMillis());
			}
			System.out.println("PDS: " + pdsAnalysisTimes);
			System.out.println("AP: " + apAnalysisTimes);
		}

	}
	
}
