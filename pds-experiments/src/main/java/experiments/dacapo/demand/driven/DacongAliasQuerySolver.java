package experiments.dacapo.demand.driven;

import alias.Util;
import edu.osu.cse.pa.Main;

public class DacongAliasQuerySolver extends AliasQuerySolver {

	private static Main m;


	public DacongAliasQuerySolver(int timeoutMS) {
		super(timeoutMS);
		if(m == null) {
			m = edu.osu.cse.pa.Main.v();
			m.buildSPG();
			Util.TIME_BUDGET = timeoutMS;
		}
	}
	

	protected boolean internalComputeQuery(AliasQuery q) {
		Util.aliasStart = System.currentTimeMillis();
		return m.mayAlias(q.getLocalA(), q.queryA.stmt().getMethod(), q.getLocalB(), q.queryA.stmt().getMethod());
	}
}
