package edu.osu.cse.pa.spg;

import java.util.Iterator;
import java.util.Set;

import soot.SootMethod;
import soot.util.ArraySet;

public abstract class AbstractAllocNode extends AbstractNode {

	private Set<AbstractSPGEdge> outgoingEdges = new ArraySet<AbstractSPGEdge>();
	private Set<AbstractSPGEdge> incomingEdges = new ArraySet<AbstractSPGEdge>();
	private Set<VarNode> pointBy = new ArraySet<VarNode>();

	public AbstractAllocNode(SootMethod sm) {
		super(sm);
	}

	public Iterator<VarNode> getPointBy() {
		return pointBy.iterator();
	}
	public void addPointBy(VarNode vn) {
		pointBy.add(vn);
	}
	
	public void addIncomingEdge(AbstractSPGEdge e) {
		incomingEdges.add(e);
	}

	public void addOutgoingEdge(AbstractSPGEdge e) {
		outgoingEdges.add(e);
	}

	public Iterator<AbstractSPGEdge> getOutgoingEdges() {
		return outgoingEdges.iterator();
	}

	public Iterator<AbstractSPGEdge> getIncomingEdges() {
		return incomingEdges.iterator();
	}

}
