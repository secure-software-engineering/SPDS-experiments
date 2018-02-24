package experiments.dacapo.demand.driven;

import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

import boomerang.BoomerangTimeoutException;
import experiments.demand.driven.sridharan.ManuTimeoutException;

public abstract class AliasQuerySolver {
	Stopwatch watch = Stopwatch.createUnstarted();
	protected final int timeoutMS;
	
	public AliasQuerySolver(int timeoutMS) {
		this.timeoutMS = timeoutMS;
	}
	protected AliasQueryExperimentResult computeQuery(AliasQuery q) {
		if(watch.isRunning())
			watch.stop();
		watch.reset();
		watch.start();
		try {
			boolean sol = internalComputeQuery(q);
			watch.stop();
			return new AliasQueryExperimentResult(q, sol, watch.elapsed(TimeUnit.MILLISECONDS),  watch.elapsed(TimeUnit.MILLISECONDS) > timeoutMS);
		} catch(Exception e) {
			if(!(e instanceof ManuTimeoutException) && !(e instanceof BoomerangTimeoutException) && !(e instanceof SkipQueryException)) {
				e.printStackTrace();
			}
		}
		return new AliasQueryExperimentResult(q, true, -1, true);
	}
	
	protected abstract boolean internalComputeQuery(AliasQuery q);
}
