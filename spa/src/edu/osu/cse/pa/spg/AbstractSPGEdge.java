package edu.osu.cse.pa.spg;


public abstract class AbstractSPGEdge {
	private AbstractAllocNode src;
	private AbstractAllocNode tgt;

	public AbstractSPGEdge() {
		// fake constructor
	}
	public AbstractSPGEdge(AbstractAllocNode src, AbstractAllocNode tgt) {
		this.src = src;
		this.tgt = tgt;
		src.addOutgoingEdge(this);
		tgt.addIncomingEdge(this);
	}

	public AbstractAllocNode src() {
		return src;
	}

	public AbstractAllocNode tgt() {
		return tgt;
	}

	public abstract int getId();
	public abstract int getReverseId();
}
