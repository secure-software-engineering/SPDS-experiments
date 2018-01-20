package edu.osu.cse.pa.spg;

import soot.AnySubType;
import soot.Scene;
import soot.Type;

public class ExceptionVarNode extends VarNode {
    public static ExceptionVarNode node = new ExceptionVarNode();
	ExceptionVarNode() {
		super(null);
	}

	public Type getType() {
		return AnySubType.v(Scene.v().getRefType("java.lang.Exception"));
	}

	public String toString() {
		return "Exception node ";
	}

}
