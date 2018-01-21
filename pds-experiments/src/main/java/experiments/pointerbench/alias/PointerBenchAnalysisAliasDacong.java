package experiments.pointerbench.alias;

import edu.osu.cse.pa.Main;
import edu.osu.cse.pa.spg.SymbolicPointerGraph;
import experiments.pointerbench.pointsto.PointerBenchResult;

public class PointerBenchAnalysisAliasDacong extends PointerBenchAliasAnalysis {

	private Main m;


	public PointerBenchAnalysisAliasDacong(String pointerBenchClassesPath, String mainClass) {
		super(pointerBenchClassesPath, mainClass);

	}
	
	@Override
	protected void runAndCompare() {
		m = edu.osu.cse.pa.Main.v();
		m.buildSPG();
		super.runAndCompare();
	}

	@Override
	protected boolean computeQuery(AliasQuery q) {
		return m.mayAlias(q.a, q.queryA.stmt().getMethod(), q.b, q.queryA.stmt().getMethod());
	}
}
