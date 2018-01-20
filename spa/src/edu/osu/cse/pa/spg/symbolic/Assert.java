package edu.osu.cse.pa.spg.symbolic;

public class Assert {
	public static void assertTrue(boolean b) {
		if (!b)
			throw new RuntimeException("Assert Violated");
	}

}
