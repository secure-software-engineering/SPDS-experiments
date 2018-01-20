package edu.osu.cse.pa.spg;

import soot.AnySubType;
import soot.RefType;
import soot.SootMethod;
import soot.Type;

public class AnySubTypeNode extends AbstractAllocNode {
	private RefType baseType;

	public AnySubTypeNode(SootMethod sm, RefType baseType) {
		super(sm);
		this.baseType = baseType;
	}

	public Type getType() {
		// TODO Auto-generated method stub
		return AnySubType.v(baseType);
	}

}
