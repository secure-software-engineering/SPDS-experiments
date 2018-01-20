package edu.osu.cse.pa.spg;

import soot.Local;
import soot.SootField;
import soot.SootMethod;
import soot.Type;

public class FieldVarNode extends VarNode {
	private SootField f;

	private Local l;

	FieldVarNode(SootMethod sm, SootField f, Local base) {
		super(sm);
		this.f = f;
		this.l = base;
	}

	public SootField getField() {
		return f;
	}

	public Local getBase() {
		return l;
	}

	public Type getType() {
		return f.getType();
	}

	public String toString() {
		return l.getName() + "." + f.getName() + " in " + method.getSignature() +"@" + Integer.toHexString(hashCode());
	}
}
