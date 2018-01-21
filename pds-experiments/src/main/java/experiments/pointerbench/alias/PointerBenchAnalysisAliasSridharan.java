package experiments.pointerbench.alias;

import soot.Local;
import soot.PointsToSet;
import soot.Scene;
import soot.jimple.spark.ondemand2.DemandCSPointsTo;

public class PointerBenchAnalysisAliasSridharan extends PointerBenchAliasAnalysis {


	public PointerBenchAnalysisAliasSridharan(String pointerBenchClassesPath, String mainClass) {
		super(pointerBenchClassesPath, mainClass);
	}

	private PointsToSet getPointsTo(Local l){
		DemandCSPointsTo pts = DemandCSPointsTo.makeDefault();
		try{
			return pts.reachingObjects(l);
		} catch (Exception e){
			e.printStackTrace();
		}
		return Scene.v().getPointsToAnalysis().reachingObjects(l);
	}

	@Override
	protected boolean computeQuery(AliasQuery q) {
		PointsToSet a = getPointsTo(q.a);
		PointsToSet b = getPointsTo(q.b);
		return a.hasNonEmptyIntersection(b);
	}
}
