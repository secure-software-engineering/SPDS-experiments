package edu.osu.cse.pa.spg.symbolic;

import java.util.Iterator;

import soot.SootMethod;
import soot.Type;
import edu.osu.cse.pa.spg.ExceptionVarNode;


public class SymbolicExceptionObject extends SymbolicObject {

	public SymbolicExceptionObject(SootMethod m) {
		super(ExceptionVarNode.node.getType(), m);
	}
	

	public String toString() {
		return "SE@" + Integer.toHexString(hashCode());
	}
}
