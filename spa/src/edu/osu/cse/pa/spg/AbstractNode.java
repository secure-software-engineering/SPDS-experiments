package edu.osu.cse.pa.spg;

import soot.SootMethod;
import soot.Type;


public abstract class AbstractNode  {
	protected SootMethod method;

	//protected int nodeID;


	public AbstractNode(SootMethod sm) {
		method = sm;
		//nodeID = id;
	}

	public SootMethod getMethod() {
		return method;
	}

//	public int getIntegerID() {
//
//		return nodeID;
//	}

	public abstract Type getType();

}
