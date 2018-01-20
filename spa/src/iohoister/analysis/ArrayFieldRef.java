package iohoister.analysis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import soot.SootField;
import soot.SootFieldRef;
import soot.Type;
import soot.UnitPrinter;
import soot.Value;
import soot.ValueBox;
import soot.jimple.ArrayRef;
import soot.jimple.InstanceFieldRef;
import soot.util.Switch;

public class ArrayFieldRef implements InstanceFieldRef {
	private ArrayRef ref;

	private static Map<ArrayRef, ArrayFieldRef> instances = new HashMap<ArrayRef, ArrayFieldRef>();

	public static ArrayFieldRef v(ArrayRef f) {
		ArrayFieldRef instance = instances.get(f);
		if (instance == null) {
			instance = new ArrayFieldRef(f);
			instances.put(f, instance);
		}
		return instance;
	}

	private ArrayFieldRef(ArrayRef ref) {
		this.ref = ref;
	}

	public Value getBase() {
		return ref.getBase();
	}

	public ValueBox getBaseBox() {
		return ref.getBaseBox();
	}

	public void setBase(Value base) {
		throw new RuntimeException("Cannot modify an ArrayFieldRef object!\n");
	}

	public Object clone() {
		return this;
	}

	public SootField getField() {
		return null;
	}

	public SootFieldRef getFieldRef() {
		return null;
	}

	public void setFieldRef(SootFieldRef sfr) {
		throw new RuntimeException("Cannot modify an ArrayFieldRef object!\n");
	}

	public Type getType() {
		return ref.getBase().getType();
	}

	public List getUseBoxes() {
		return null;
	}

	public void toString(UnitPrinter up) {

	}

	public void apply(Switch sw) {

	}

	public int equivHashCode() {

		return 0;
	}

	public boolean equivTo(Object o) {
		return false;
	}

	public boolean equals(Object o) {
		if (o instanceof ArrayFieldRef) {
			if (((ArrayFieldRef) o).ref.equals(ref))
				return true;
		}
		return false;
	}

}
