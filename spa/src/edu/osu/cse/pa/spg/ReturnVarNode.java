package edu.osu.cse.pa.spg;

import soot.SootMethod;
import soot.Type;

public class ReturnVarNode extends VarNode {
	ReturnVarNode(SootMethod sm) {
		super(sm);
	}

	public Type getType() {
		return method.getReturnType();
	}

	public String toString() {
		return "Return node in " + method;
	}

}
