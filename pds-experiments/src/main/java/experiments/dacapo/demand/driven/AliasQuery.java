package experiments.dacapo.demand.driven;

import boomerang.BackwardQuery;
import soot.Local;

public class AliasQuery {
	final BackwardQuery queryA;
	final BackwardQuery queryB;

	public AliasQuery(BackwardQuery queryA, BackwardQuery queryB) {
		this.queryA = queryA;
		this.queryB = queryB;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((queryA == null) ? 0 : queryA.hashCode());
		result = prime * result + ((queryB == null) ? 0 : queryB.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AliasQuery other = (AliasQuery) obj;
		if (queryA == null) {
			if (other.queryA != null)
				return false;
		} else if (!queryA.equals(other.queryA))
			return false;
		if (queryB == null) {
			if (other.queryB != null)
				return false;
		} else if (!queryB.equals(other.queryB))
			return false;
		return true;
	}

	public Local getLocalA() {
		return (Local) queryA.var().value();
	}
	public Local getLocalB() {
		return (Local) queryB.var().value();
	}
	
	@Override
	public String toString() {
		return queryA.toString()+queryB;
	}
	
}