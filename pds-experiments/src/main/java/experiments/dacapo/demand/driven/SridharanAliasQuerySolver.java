package experiments.dacapo.demand.driven;

import java.util.Set;

import com.google.common.collect.Sets;

import experiments.demand.driven.sridharan.DemandCSPointsTo;
import experiments.demand.driven.sridharan.ManuTimeoutException;
import soot.Local;
import soot.PointsToSet;

public class SridharanAliasQuerySolver extends AliasQuerySolver{
	private Set<Local> crashed = Sets.newHashSet();

	public SridharanAliasQuerySolver(int timeoutMS) {
		super(timeoutMS);
	}

	private PointsToSet getPointsTo(Local l){
		DemandCSPointsTo pts = DemandCSPointsTo.makeWithBudget(75000000, 10000, false);
		DemandCSPointsTo.timeBudget = timeoutMS;
		try {
			if(crashed.contains(l)) {
				throw new SkipQueryException();
			}
			PointsToSet res = pts.reachingObjects(l);
			return res;
		} catch(ManuTimeoutException e) {
			crashed .add(l);
			throw e;
		}
	}

	public boolean internalComputeQuery(AliasQuery q) {
		PointsToSet a = getPointsTo(q.getLocalA());
		PointsToSet b = getPointsTo(q.getLocalB());
		return a.hasNonEmptyIntersection(b);
	}
}
