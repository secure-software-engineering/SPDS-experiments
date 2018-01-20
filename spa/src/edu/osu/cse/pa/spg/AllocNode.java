package edu.osu.cse.pa.spg;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import soot.SootMethod;
import soot.Type;
import soot.jimple.NewExpr;

public class AllocNode extends AbstractAllocNode {
	private NewExpr obj;

	AllocNode(SootMethod sm, NewExpr obj) {
		super(sm);
		this.obj = obj;
	}

	public Type getType() {
		return obj.getBaseType();
	}

	public String toString() {
		return obj + "@" + Integer.toHexString(hashCode()) + " in " + method;
	}


}
