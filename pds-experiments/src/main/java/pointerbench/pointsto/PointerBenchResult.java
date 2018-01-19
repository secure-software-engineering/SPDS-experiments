package pointerbench.pointsto;

public class PointerBenchResult {
	
	final String className;
	final int ptsFalsePositives;
	final int ptsFalseNegatives;
	final int ptsTruePositives;

	public PointerBenchResult(String className, int ptsTruePositives, int ptsFalsePositives, int ptsFalseNegatives) {
		this.className = className;
		this.ptsFalsePositives = ptsFalsePositives;
		this.ptsFalseNegatives = ptsFalseNegatives;
		this.ptsTruePositives = ptsTruePositives;
	}
	
	@Override
	public String toString() {
		return className +"\tTP: " + ptsTruePositives + "\tFP: " + ptsFalsePositives +"\tFN: " + ptsFalseNegatives;
	}
}
