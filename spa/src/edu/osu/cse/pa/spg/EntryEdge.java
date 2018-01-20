package edu.osu.cse.pa.spg;

import alias.Util;
import soot.jimple.toolkits.callgraph.Edge;

public class EntryEdge extends AbstractSPGEdge {	
	private Edge e;
	
	private int id;

	public EntryEdge(AbstractAllocNode src, AbstractAllocNode tgt, Edge e) {
		super(src, tgt);
		this.e = e;
		this.id = Util.ctxId++;
	}

	public Edge getCallGraphEdge() {
		return e;
	}
	
	public int getId() {
		return id;
	}
	
	public int getReverseId() {
		return id + Util.ctxId;
	}

}
