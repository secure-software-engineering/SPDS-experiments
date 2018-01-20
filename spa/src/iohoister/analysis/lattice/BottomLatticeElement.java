package iohoister.analysis.lattice;

public class BottomLatticeElement extends LatticeElement {

	private static BottomLatticeElement instance = null;

	public static BottomLatticeElement v() {
		if (instance == null)
			instance = new BottomLatticeElement();
		return instance;
	}

	private BottomLatticeElement() {

	}

	@Override
	public LatticeElement join(LatticeElement le) {
		return le;
	}

	@Override
	public LatticeElement meet(LatticeElement le) {
		return this;
	}

}
