package iohoister.analysis.lattice;

import java.util.HashMap;
import java.util.Map;

import soot.jimple.Stmt;

public class ReturnLatticeElement extends LatticeElement {

	private static Map<Stmt, ReturnLatticeElement> instances = new HashMap<Stmt, ReturnLatticeElement>();

	public static ReturnLatticeElement v(Stmt callsite) {
		ReturnLatticeElement instance = instances.get(callsite);
		if (instance == null) {
			instance = new ReturnLatticeElement(callsite);
			instances.put(callsite, instance);
		}
		return instance;
	}

	private Stmt callsite;

	private ReturnLatticeElement(Stmt callsite) {
		this.callsite = callsite;
	}

	public Stmt getCallSiteStmt() {
		return callsite;
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
			if (le == this)
				return this;
			else
				return TopLatticeElement.v();
		} else if (le instanceof ParamLatticeElement)
			return TopLatticeElement.v();
		else if (le instanceof AllocLatticeElement)
			return TopLatticeElement.v();

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
			if (this == le)
				return this;
			else
				return BottomLatticeElement.v();
		} else if (le instanceof ParamLatticeElement)
			return BottomLatticeElement.v();
		else if (le instanceof AllocLatticeElement)
			return BottomLatticeElement.v();

		return null;
	}

}
