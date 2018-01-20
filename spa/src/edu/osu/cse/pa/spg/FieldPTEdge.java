package edu.osu.cse.pa.spg;

import soot.SootField;

public class FieldPTEdge extends AbstractSPGEdge {

	private SootField f;

	public FieldPTEdge() {
		// fake constructor for wildcard
	}
	public FieldPTEdge(AbstractAllocNode src, SootField f,
			AbstractAllocNode tgt) {
		super(src, tgt);
		this.f = f;
	}

	public SootField getField() {
		return f;
	}

	@Override
	public int getId() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getReverseId() {
		throw new UnsupportedOperationException();
	}

}
