package edu.osu.cse.pa.spg;

import soot.Local;
import soot.SootMethod;
import soot.Type;

public class LocalVarNode extends VarNode {
	private Local local;

	LocalVarNode(SootMethod sm, Local l) {
		super(sm);
		local = l;
	}

	public Type getType() {
		// TODO Auto-generated method stub
		return local.getType();
	}

	public Local getLocal() {
		return local;
	}

	public String toString() {
		return local + " " + " in "
				+ method;
	}

}
