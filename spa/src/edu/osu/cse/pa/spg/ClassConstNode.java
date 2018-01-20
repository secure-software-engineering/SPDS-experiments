package edu.osu.cse.pa.spg;


import soot.Scene;
import soot.SootMethod;
import soot.Type;
import soot.jimple.ClassConstant;

public class ClassConstNode extends AbstractAllocNode {
    public static ClassConstNode node = new ClassConstNode();
	ClassConstNode() {
		super(null);
	}

	public Type getType() {
		return Scene.v().getRefType("java.lang.Class");
	}

	public String toString() {
		return "class constant ";
	}

}
