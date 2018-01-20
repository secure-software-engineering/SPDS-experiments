package iohoister.analysis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import soot.IntType;
import soot.Local;
import soot.SootMethod;
import soot.Type;
import soot.UnitPrinter;
import soot.jimple.FieldRef;
import soot.jimple.Jimple;
import soot.util.Switch;

public class LocalWrapperforFieldRef implements Local {

	private static Map<FieldRef, LocalWrapperforFieldRef> instances = new HashMap<FieldRef, LocalWrapperforFieldRef>();

	public static LocalWrapperforFieldRef v(FieldRef fr) {
		LocalWrapperforFieldRef instance = instances.get(fr);
		if (instance == null) {
			instance = new LocalWrapperforFieldRef(fr);
			instances.put(fr, instance);
		}
		return instance;
	}

	private FieldRef reference = null;
	private Local shadowLocal = null;
	private Local oldShadowValue = null;

	private LocalWrapperforFieldRef(FieldRef fr) {
		reference = fr;
	}

	public FieldRef getFieldReference() {
		return reference;
	}

	public Local makeShadowLocal(SootMethod m) {
		if (shadowLocal == null) {
			int id = UniqueID.getUniqueID("field");
			shadowLocal = Jimple.v().newLocal(
					"_f" + UniqueID.getUniqueID("field"), IntType.v());
			oldShadowValue = Jimple.v().newLocal("_of" + id, IntType.v());
			m.getActiveBody().getLocals().add(shadowLocal);
			m.getActiveBody().getLocals().add(oldShadowValue);
		}
		return shadowLocal;
	}

	public Local makeOldShadowLocal(SootMethod m) {
		if (shadowLocal == null) {
			int id = UniqueID.getUniqueID("field");
			shadowLocal = Jimple.v().newLocal(
					"_f" + UniqueID.getUniqueID("field"), IntType.v());
			oldShadowValue = Jimple.v().newLocal("_of" + id, IntType.v());
			m.getActiveBody().getLocals().add(shadowLocal);
			m.getActiveBody().getLocals().add(oldShadowValue);
		}
		return oldShadowValue;
	}

	public String getName() {
		return null;
	}

	public Object clone() {
		return this;
	}

	public void setName(String arg0) {
	}

	public void setType(Type arg0) {

	}

	public Type getType() {
		return null;
	}

	public List getUseBoxes() {
		return null;
	}

	public void toString(UnitPrinter arg0) {

	}

	public void apply(Switch arg0) {

	}

	public int equivHashCode() {
		return 0;
	}

	public boolean equivTo(Object arg0) {
		return false;
	}

	public int getNumber() {
		return 0;
	}

	public void setNumber(int arg0) {

	}

}
