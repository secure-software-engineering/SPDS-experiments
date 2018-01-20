package edu.osu.cse.pa.spg.symbolic;

import java.util.List;

import soot.SootMethod;
import soot.Type;

public class SCCRepresentative extends SootMethod {

	public SCCRepresentative(String name, List parameterTypes, Type returnType) {
		super(name, parameterTypes, returnType);
	}

}
