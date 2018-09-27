package alias;

import soot.Local;
import soot.PointsToAnalysis;
import soot.PointsToSet;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.spark.ondemand.flowdroid.DemandCSPointsTo;
import iohoister.analysis.MayAliasAnalysis;

public class ManuMayAliasAnalysis implements MayAliasAnalysis {
	private static ManuMayAliasAnalysis ins;
	private PointsToAnalysis pta;
	
	private ManuMayAliasAnalysis() {		
		pta = DemandCSPointsTo.makeWithBudget(75000000, 10000, false);
	}
	
	public static ManuMayAliasAnalysis v() {
		if (ins == null) {
			ins = new ManuMayAliasAnalysis();
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
