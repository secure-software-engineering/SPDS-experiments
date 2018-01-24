package experiments.pointerbench.pointsto;

public class PointerBenchResult {
	
	public final String className;
	public final int ptsFalsePositives;
	public final int ptsFalseNegatives;
	public final int ptsTruePositives;

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
