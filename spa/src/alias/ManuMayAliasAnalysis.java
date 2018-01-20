package alias;

import soot.Local;
import soot.PointsToAnalysis;
import soot.PointsToSet;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.spark.ondemand2.DemandCSPointsTo;
import iohoister.analysis.MayAliasAnalysis;

public class ManuMayAliasAnalysis implements MayAliasAnalysis {
	private static ManuMayAliasAnalysis ins;
	private PointsToAnalysis pta;
	
	private ManuMayAliasAnalysis() {		
	}
	
	public static ManuMayAliasAnalysis v() {
		if (ins == null) {
			ins = new ManuMayAliasAnalysis();
			ins.pta = DemandCSPointsTo.makeWithBudget(
				Util.MANU_MAX_TRAVERSAL, Util.MANU_MAX_PASSES);
		}
		
		return ins;
	}

	public boolean mayAlias(Local var1, SootMethod m1, Local var2, SootMethod m2) {
//		if (!Util.traditionalMayAlias(var1, m1, var2, m2, Scene.v().getPointsToAnalysis())) {
//			return false;
//		}
		return Util.traditionalMayAlias(var1, m1, var2, m2, pta);
	}

}
