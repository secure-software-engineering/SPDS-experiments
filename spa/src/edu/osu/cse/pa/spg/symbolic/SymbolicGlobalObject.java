package edu.osu.cse.pa.spg.symbolic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import edu.osu.cse.pa.spg.GlobalVarNode;
import edu.osu.cse.pa.spg.SymbolicPointerGraph;

import soot.RefType;
import soot.Scene;
import soot.SootField;
import soot.SootMethod;
import soot.Type;


public class SymbolicGlobalObject extends SymbolicObject {
	private GlobalVarNode var;

	public SymbolicGlobalObject(SootMethod m, GlobalVarNode var) {
		super(var.getType(), m);
		this.var = var;
	}

	public SootField getField() {
		return var.getFieldRef();
	}


	public GlobalVarNode getGlobalVarNode(){
		return var;
	}


	public String toString() {
		return "SG@" + Integer.toHexString(hashCode()) + " for " + method.getSignature();
	}
}
