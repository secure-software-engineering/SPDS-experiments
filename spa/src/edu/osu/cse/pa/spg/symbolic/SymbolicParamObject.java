package edu.osu.cse.pa.spg.symbolic;

import java.util.Iterator;


import edu.osu.cse.pa.spg.ParameterVarNode;
import edu.osu.cse.pa.spg.SymbolicPointerGraph;
import soot.SootMethod;
import soot.Type;
import soot.jimple.IdentityRef;
import soot.jimple.ParameterRef;
import soot.jimple.ThisRef;

public class SymbolicParamObject extends SymbolicObject {

	private IdentityRef ref;

	public SymbolicParamObject(SootMethod m, IdentityRef ref) {
		super(ref.getType(), m);
		this.ref = ref;
	}

	public IdentityRef getIdentityRef() {
		return ref;
	}


	public String toString() {
		return "SP@" + Integer.toHexString(hashCode()) + " for " + method.getSignature();
	}
}
