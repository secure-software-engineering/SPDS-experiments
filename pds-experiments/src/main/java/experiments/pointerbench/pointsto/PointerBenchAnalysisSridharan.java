package experiments.pointerbench.pointsto;

import java.util.Collection;

import alias.Util;
import boomerang.Query;
import experiments.demand.driven.sridharan.DemandCSPointsTo;
import experiments.demand.driven.sridharan.SridharanHelper;
import soot.Local;
import soot.PointsToSet;
import soot.Scene;

public class PointerBenchAnalysisSridharan extends PointerBenchAnalysis {

	public PointerBenchAnalysisSridharan(String pointerBenchClassesPath, String mainClass) {
		super(pointerBenchClassesPath, mainClass);
	}

	@Override
	protected void runAndCompare() {
		int backwardResults = runQuery();
		compare(allocationSites, backwardResults);
	}

	private void compare(Collection<Query> expectedResults, int numberOfAllocSites) {
		int difference = expectedResults.size() - numberOfAllocSites;
		if(difference > 0){
			unsoundErrors = difference;
		} else {
			imprecisionErrors = -difference;
		}
	}

	private int runQuery() {
		DemandCSPointsTo pts = DemandCSPointsTo.makeDefault();
		Util.aliasStart = System.currentTimeMillis();
		int pointsToSize = 0;
		for(Query q : queryForCallSites){
			Local v = (Local) q.asNode().fact().value();
			try{
				PointsToSet reachingObjects = pts.reachingObjects(v);
				System.out.println(reachingObjects);
				pointsToSize = SridharanHelper.getPointsToSize(reachingObjects);
			} catch (Exception e){
				e.printStackTrace();
				pointsToSize = SridharanHelper.getPointsToSize(Scene.v().getPointsToAnalysis().reachingObjects(v));
			}
		}
		return pointsToSize;
	}
}
