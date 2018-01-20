package iohoister.analysis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import soot.IntType;
import soot.Local;
import soot.SootMethod;
import soot.Type;
import soot.UnitPrinter;
import soot.jimple.ArrayRef;
import soot.jimple.Jimple;
import soot.util.Switch;

public class LocalWrapperforArrayRef implements Local {

	public static Map<ArrayRef, LocalWrapperforArrayRef> instances = new HashMap<ArrayRef, LocalWrapperforArrayRef>();

	public static LocalWrapperforArrayRef v(ArrayRef r) {
		LocalWrapperforArrayRef instance = instances.get(r);
		if (instance == null) {
			instance = new LocalWrapperforArrayRef(r);
			instances.put(r, instance);
		}
		return instance;
	}

	private ArrayRef ar;
	private Local shadowLocal;

	private Local oldShadowValue;

	private LocalWrapperforArrayRef(ArrayRef r) {
		this.ar = r;
	}

	public ArrayRef getArrayRef() {
		return ar;
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

	// ================================= interface methods that
	public String getName() {

		return null;
	}

	public void setName(String arg0) {
	}

	public void setType(Type arg0) {

	}

	public Object clone() {
		return this;
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
