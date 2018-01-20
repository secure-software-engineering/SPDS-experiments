package iohoister.analysis.lattice;

import java.util.HashMap;
import java.util.Map;

import soot.jimple.ClassConstant;
import soot.jimple.FieldRef;
import soot.jimple.NewExpr;
import soot.jimple.StringConstant;

public class AllocLatticeElement extends LatticeElement {

	private static Map<NewExpr, AllocLatticeElement> instances = new HashMap<NewExpr, AllocLatticeElement>();
	private static Map<StringConstant, AllocLatticeElement> stringInstances = new HashMap<StringConstant, AllocLatticeElement>();
	private static Map<ClassConstant, AllocLatticeElement> classInstances = new HashMap<ClassConstant, AllocLatticeElement>();

	public static AllocLatticeElement v(NewExpr ne) {
		AllocLatticeElement element = instances.get(ne);
		if (element == null) {
			element = new AllocLatticeElement(ne);
			instances.put(ne, element);
		}
		return element;
	}

	public static AllocLatticeElement v(StringConstant c) {
		AllocLatticeElement element = stringInstances.get(c);
		if (element == null) {
			element = new AllocLatticeElement(c);
			stringInstances.put(c, element);
		}
		return element;
	}

	public static AllocLatticeElement v(ClassConstant c) {
		AllocLatticeElement element = classInstances.get(c);
		if (element == null) {
			element = new AllocLatticeElement(c);
			classInstances.put(c, element);
		}
		return element;
	}

	private NewExpr ne;

	private StringConstant sc;

	private ClassConstant cc;

	private AllocLatticeElement(NewExpr ne) {
		this.ne = ne;
	}

	private AllocLatticeElement(StringConstant c) {
		this.sc = c;
	}

	private AllocLatticeElement(ClassConstant cc) {
		this.cc = cc;
	}

	public LatticeElement join(LatticeElement le) {
		if (le instanceof BottomLatticeElement)
			return this;
		else if (le instanceof TopLatticeElement)
			return le;
		else if (le instanceof FieldRefLatticeElement) {
			return TopLatticeElement.v();
		} else if (le instanceof ReturnLatticeElement)
			return TopLatticeElement.v();
		else if (le instanceof ParamLatticeElement) {
			return TopLatticeElement.v();
		} else if (le instanceof AllocLatticeElement) {
			if (le == this)
				return this;
			else
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
		} else if (le instanceof ReturnLatticeElement)
			return BottomLatticeElement.v();
		else if (le instanceof ParamLatticeElement)
			return BottomLatticeElement.v();
		else if (le instanceof AllocLatticeElement) {
			if (this == le)
				return this;
			else
				return BottomLatticeElement.v();
		}
		return null;
	}

}
