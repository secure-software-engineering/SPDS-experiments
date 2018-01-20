package iohoister.analysis.lattice;

import java.util.HashMap;
import java.util.Map;

import soot.jimple.FieldRef;

public class FieldRefLatticeElement extends LatticeElement {

	private static Map<FieldRef, FieldRefLatticeElement> instances = new HashMap<FieldRef, FieldRefLatticeElement>();

	public static FieldRefLatticeElement v(FieldRef fr) {
		FieldRefLatticeElement element = instances.get(fr);
		if (element == null) {
			element = new FieldRefLatticeElement(fr);
			instances.put(fr, element);
		}
		return element;
	}

	private FieldRef ref;

	private FieldRefLatticeElement(FieldRef fr) {
		this.ref = fr;
	}

	public FieldRef getFieldReference() {
		return ref;
	}

	@Override
	public LatticeElement join(LatticeElement le) {
		if (le instanceof BottomLatticeElement)
			return this;
		else if (le instanceof TopLatticeElement)
			return le;
		else if (le instanceof FieldRefLatticeElement) {
			if (le == this)
				return this;
			else
				return TopLatticeElement.v();
		} else if (le instanceof ReturnLatticeElement)
			return TopLatticeElement.v();
		else if (le instanceof ParamLatticeElement) {
			return TopLatticeElement.v();
		} else if (le instanceof AllocLatticeElement) {
			return TopLatticeElement.v();
		}
		return null;
	}

	@Override
	public LatticeElement meet(LatticeElement le) {
		if (le instanceof BottomLatticeElement)
			return BottomLatticeElement.v();
		else if (le instanceof TopLatticeElement)
			return this;
		else if (le instanceof FieldRefLatticeElement) {
			if (this == le)
				return this;
			else
				return BottomLatticeElement.v();
		} else if (le instanceof ReturnLatticeElement)
			return BottomLatticeElement.v();
		else if (le instanceof ParamLatticeElement)
			return BottomLatticeElement.v();
		else if (le instanceof AllocLatticeElement)
			return BottomLatticeElement.v();
		return null;
	}

}
