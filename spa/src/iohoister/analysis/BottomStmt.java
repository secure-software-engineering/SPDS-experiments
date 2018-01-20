package iohoister.analysis;

import java.util.List;

import soot.Unit;
import soot.UnitBox;
import soot.UnitPrinter;
import soot.ValueBox;
import soot.jimple.ArrayRef;
import soot.jimple.FieldRef;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.tagkit.Host;
import soot.tagkit.Tag;
import soot.util.Switch;

/**
 * 
 * @author Harry Xu
 * 
 *         The class represents invisible statements outside a method that write
 *         to a heap location
 */
class BottomStmt implements Stmt {

	private static BottomStmt instance = null;

	public static BottomStmt v() {
		if (instance == null)
			instance = new BottomStmt();
		return instance;
	}

	public Object clone() {
		return this;
	}

	public boolean containsArrayRef() {
		return false;
	}

	public boolean containsFieldRef() {
		return false;
	}

	public boolean containsInvokeExpr() {
		return false;
	}

	public ArrayRef getArrayRef() {
		return null;
	}

	public ValueBox getArrayRefBox() {
		return null;
	}

	public FieldRef getFieldRef() {
		return null;
	}

	public ValueBox getFieldRefBox() {
		return null;
	}

	public InvokeExpr getInvokeExpr() {
		return null;
	}

	public ValueBox getInvokeExprBox() {
		return null;
	}

	public void toString(UnitPrinter arg0) {

	}

	public void addBoxPointingToThis(UnitBox arg0) {
	}

	public boolean branches() {
		return false;
	}

	public void clearUnitBoxes() {
		// TODO Auto-generated method stub

	}

	public boolean fallsThrough() {
		// TODO Auto-generated method stub
		return false;
	}

	public List<UnitBox> getBoxesPointingToThis() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<ValueBox> getDefBoxes() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<UnitBox> getUnitBoxes() {
		// TODO Auto-generated method stub
		return null;
	}

	public List getUseAndDefBoxes() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<ValueBox> getUseBoxes() {
		// TODO Auto-generated method stub
		return null;
	}

	public void redirectJumpsToThisTo(Unit arg0) {
		// TODO Auto-generated method stub

	}

	public void removeBoxPointingToThis(UnitBox arg0) {
		// TODO Auto-generated method stub

	}

	public void apply(Switch arg0) {
		// TODO Auto-generated method stub

	}

	public void addAllTagsOf(Host arg0) {
		// TODO Auto-generated method stub

	}

	public void addTag(Tag arg0) {
		// TODO Auto-generated method stub

	}

	public Tag getTag(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Tag> getTags() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasTag(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeAllTags() {
		// TODO Auto-generated method stub

	}

	public void removeTag(String arg0) {
		// TODO Auto-generated method stub

	}

	public String toString() {
		return "BOTTOM_STMT";
	}

	@Override
	public int getJavaSourceStartLineNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getJavaSourceStartColumnNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

}
