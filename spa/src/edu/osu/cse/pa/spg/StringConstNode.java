package edu.osu.cse.pa.spg;

import soot.Scene;
import soot.SootMethod;
import soot.Type;
import soot.jimple.StringConstant;

public class StringConstNode extends AbstractAllocNode {

	// public static StringConstNode node = new StringConstNode();
	private StringConstant literal;

	public StringConstNode(SootMethod m, StringConstant literal) {
		super(m);
		this.literal = literal;
	}

	public StringConstant getStringLiteral() {
		return literal;
	}

	public Type getType() {
		return Scene.v().getRefType("java.lang.String");
	}

	public String toString() {
		return "string constant " + literal + " in " + getMethod();
	}

}
