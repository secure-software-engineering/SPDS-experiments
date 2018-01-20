package edu.osu.cse.pa.spg;


import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.jimple.StaticFieldRef;

public class GlobalVarNode extends VarNode {

	private SootField ref;

	GlobalVarNode(SootMethod sm, SootField ref) {
		super(sm);
		this.ref = ref;
	}

	public Type getType() {
		return ref.getType();
	}

	public SootField getFieldRef() {
		return ref;
	}

	public String toString() {
		return "GVN " + ref + " " + method;
	}

}
