package edu.osu.cse.pa.spg;

public class PointsToEdge {

	private VarNode src;
	private AbstractAllocNode tgt;

	public PointsToEdge(VarNode src, AbstractAllocNode tgt) {
		this.src = src;
		this.tgt = tgt;
		src.addPointsToEdge(this);
		tgt.addPointBy(src);
	}

	public VarNode src() {
		return src;
	}

	public AbstractAllocNode tgt() {
		return tgt;
	}
}
