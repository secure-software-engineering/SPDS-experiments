package iohoister.analysis.lattice;

import java.util.HashMap;
import java.util.Map;

import soot.jimple.IdentityRef;
import soot.jimple.ParameterRef;
import soot.jimple.Stmt;

public class ParamLatticeElement extends LatticeElement {
	private static Map<IdentityRef, ParamLatticeElement> instances = new HashMap<IdentityRef, ParamLatticeElement>();

	public static ParamLatticeElement v(IdentityRef ref) {
		ParamLatticeElement instance = instances.get(ref);
		if (instance == null) {
			instance = new ParamLatticeElement(ref);
			instances.put(ref, instance);
		}
		return instance;
	}

	private IdentityRef ref;

	private ParamLatticeElement(IdentityRef ref) {
		this.ref = ref;
	}

	public IdentityRef getReference() {
		return ref;
	}

	@Override
	public LatticeElement join(LatticeElement le) {
		if (le instanceof BottomLatticeElement)
			return this;
		else if (le instanceof TopLatticeElement)
			return le;
		else if (le instanceof FieldRefLatticeElement)
			return TopLatticeElement.v();
		else if (le instanceof ReturnLatticeElement) {
			return TopLatticeElement.v();
		} else if (le instanceof ParamLatticeElement) {
			if (le == this)
				return this;
			else
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
			return BottomLatticeElement.v();
		} else if (le instanceof ReturnLatticeElement) {
			return BottomLatticeElement.v();
		} else if (le instanceof ParamLatticeElement) {
			if (this == le)
				return this;
			else
				return BottomLatticeElement.v();
		} else if (le instanceof AllocLatticeElement) {
			return BottomLatticeElement.v();
		}
		return null;
	}
}
