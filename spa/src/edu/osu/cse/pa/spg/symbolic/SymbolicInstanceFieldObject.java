package edu.osu.cse.pa.spg.symbolic;

import java.util.Iterator;

import edu.osu.cse.pa.spg.AbstractAllocNode;

import edu.osu.cse.pa.spg.SymbolicPointerGraph;

import soot.SootField;
import soot.SootFieldRef;
import soot.SootMethod;
import soot.Type;
import soot.jimple.InstanceFieldRef;

public class SymbolicInstanceFieldObject extends SymbolicObject {

	private SootField field;

	public SymbolicInstanceFieldObject(SootMethod m, SootField field) {
		super(field.getType(), m);
		this.field = field;

	}

	public SootField getField() {
		return field;
	}


	public String toString() {
		return "SIF@" + Integer.toHexString(hashCode()) + " for " + method.getSignature();
	}
}
