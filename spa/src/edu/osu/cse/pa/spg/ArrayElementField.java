package edu.osu.cse.pa.spg;

import soot.SootField;

public class ArrayElementField extends SootField {
	private static ArrayElementField instance = new ArrayElementField();

	public static ArrayElementField v() {
		return instance;
	}

	public ArrayElementField() {
		super("arrayElement", null);
	}
}
