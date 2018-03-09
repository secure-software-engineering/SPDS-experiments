package experiments.pointerbench.pointsto;

import boomerang.DefaultBoomerangOptions;

public class PointerBenchBoomerangOptions extends DefaultBoomerangOptions {
	@Override
	public boolean arrayFlows() {
		return true;
	}
	@Override
	public boolean staticFlows() {
		return true;
	}

	@Override
	public int analysisTimeoutMS() {
		return 300000;
	}
}
