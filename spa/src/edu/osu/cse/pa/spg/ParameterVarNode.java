package edu.osu.cse.pa.spg;


import soot.Local;
import soot.SootMethod;
import soot.Type;
import soot.jimple.IdentityRef;
import soot.jimple.ThisRef;

public class ParameterVarNode extends VarNode {
	private IdentityRef paramRef;

	ParameterVarNode(SootMethod sm, IdentityRef ref) {
		super(sm);
		paramRef = ref;
	}

	public Type getType() {
		return paramRef.getType();
	}

	public boolean isThisRef() {
		return paramRef instanceof ThisRef;
	}

	public IdentityRef getParameterRef() {
		return paramRef;
	}

	public String toString() {
		return paramRef + " " + " in " + method;
	}

}
