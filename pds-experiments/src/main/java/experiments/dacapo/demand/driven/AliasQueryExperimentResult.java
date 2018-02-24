package experiments.dacapo.demand.driven;

public class AliasQueryExperimentResult {
	public final boolean queryResult;
	public final long analysisTimeMs;
	public final boolean timeout;

	public AliasQueryExperimentResult(AliasQuery q, boolean queryResult, long analysisTimeMs, boolean timeout) {
		this.queryResult = queryResult;
		if(!timeout) {
			this.analysisTimeMs = analysisTimeMs;
		} else {
			this.analysisTimeMs = -1;
		}
		this.timeout = timeout;
	}
}
