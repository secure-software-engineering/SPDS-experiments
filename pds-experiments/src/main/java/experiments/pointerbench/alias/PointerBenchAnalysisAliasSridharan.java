package experiments.pointerbench.alias;

import alias.Util;
import experiments.demand.driven.sridharan.DemandCSPointsTo;
import soot.Local;
import soot.PointsToSet;
import soot.Scene;

public class PointerBenchAnalysisAliasSridharan extends PointerBenchAliasAnalysis {


	public PointerBenchAnalysisAliasSridharan(String pointerBenchClassesPath, String mainClass) {
		super(pointerBenchClassesPath, mainClass);
	}

	private PointsToSet getPointsTo(Local l){
		DemandCSPointsTo pts = DemandCSPointsTo.makeDefault();
		return pts.reachingObjects(l);
	}

	@Override
	protected boolean computeQuery(AliasQuery q) {
		try {
			PointsToSet a = getPointsTo(q.a);
			PointsToSet b = getPointsTo(q.b);
			return a.hasNonEmptyIntersection(b);
		} catch (Exception e){
			e.printStackTrace();
		} catch (StackOverflowError e) {
			System.out.println("StackoverflowError");
			//We count this as a false positive
			return false;
		}	
		return true;
	}
}
