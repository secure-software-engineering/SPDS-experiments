package edu.osu.cse.pa.spg;

import soot.SootMethod;
import soot.Type;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewMultiArrayExpr;

public class ArrayAllocNode extends AbstractAllocNode {

	private NewArrayExpr expr;

	private NewMultiArrayExpr mulexpr;

	ArrayAllocNode(SootMethod sm, NewArrayExpr t) {
		super(sm);
		this.expr = t;
	}

	ArrayAllocNode(SootMethod sm, NewMultiArrayExpr ex) {
		super(sm);
		mulexpr = ex;
	}

	public Type getType() {
		if (expr != null)
			return expr.getType();
		else
			return mulexpr.getType();
	}

	public String toString() {
		String s = expr != null ? expr.toString() : mulexpr.toString();
		return expr + " in " + method.toString();
	}
}
