package edu.osu.cse.pa;

public class Timer {
	private long start = 0;

	private long end = 0;

	private static Timer instance;

	private Timer() {
	}

	public static Timer v() {
		if (instance == null)
			instance = new Timer();
		return instance;
	}

	public void startTimer() {
		start = System.currentTimeMillis();
	}

	public void endTimer() {
		end = System.currentTimeMillis();
	}

	public long getDuration() {
		return end - start;
	}

	public long getTime() {
		return System.currentTimeMillis() - start;
	}

}
