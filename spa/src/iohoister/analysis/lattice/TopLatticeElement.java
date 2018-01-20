package iohoister.analysis.lattice;

public class TopLatticeElement extends LatticeElement {

	private static TopLatticeElement instance = null;

	public static TopLatticeElement v() {
		if (instance == null)
			instance = new TopLatticeElement();
		return instance;
	}

	private TopLatticeElement() {
	}

	@Override
	public LatticeElement join(LatticeElement le) {
		return this;
	}

	@Override
	public LatticeElement meet(LatticeElement le) {
		return le;
	}

}
