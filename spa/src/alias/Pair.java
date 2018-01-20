package alias;

public class Pair<E1, E2> {
	public E1 first;
	public E2 second;

	public Pair(E1 e1, E2 e2) {
		first = e1;
		second = e2;
	}

	public boolean equals(Object other) {
		if (other instanceof Pair) {
			Pair p = (Pair) other;
			return first.equals(p.first) && second.equals(p.second);
		} else
			return false;
	}

	@Override
	public int hashCode() {
		// TODO: might not be a good one
		return 7 * first.hashCode() + 13 * second.hashCode();
	}
}
