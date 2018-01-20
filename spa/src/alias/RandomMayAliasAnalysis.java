package alias;

import soot.Local;
import soot.SootMethod;
import iohoister.analysis.MayAliasAnalysis;

public class RandomMayAliasAnalysis implements MayAliasAnalysis {

	public boolean mayAlias(Local v1, SootMethod m1, Local v2, SootMethod m2) {
		return Math.random() > 0.5;
	}
	
//	public static RandomMayAliasAnalysis v() {
//		
//	}

}
