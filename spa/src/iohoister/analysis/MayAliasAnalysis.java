package iohoister.analysis;

import soot.Local;
import soot.SootMethod;

public interface MayAliasAnalysis {
	public boolean mayAlias(Local v1, SootMethod m1, Local v2, SootMethod m2);
}
