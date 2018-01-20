package alias;

import soot.Local;
import soot.PointsToAnalysis;
import soot.PointsToSet;
import soot.Scene;
import soot.SootMethod;
import iohoister.analysis.MayAliasAnalysis;

public class PaddleMayAliasAnalysis implements MayAliasAnalysis {

	private static PaddleMayAliasAnalysis theInst;
	private PointsToAnalysis pta;
	
	private PaddleMayAliasAnalysis() {}
	
	public static PaddleMayAliasAnalysis v() {
		if (theInst == null) {
			theInst = new PaddleMayAliasAnalysis();
			theInst.pta = Scene.v().getPointsToAnalysis();
		}
		
		return theInst;
	}

	public boolean mayAlias(Local v1, SootMethod m1, Local v2, SootMethod m2) {
		return Util.traditionalMayAlias(v1, m1, v2, m2, pta);
	}
}
