package alias;

import edu.osu.cse.pa.spg.AbstractSPGEdge;

public class FldPair extends NumberedObject {
//	private static Set<AbstractSPGEdge> edges = new HashSet<AbstractSPGEdge>();
	
	private AbstractSPGEdge edge;
	private boolean bar;
	
	protected FldPair(AbstractSPGEdge e, boolean b) {
		this.edge = e;
		this.bar = b;
	}
	
	public static FldPair getPair(AbstractSPGEdge e, boolean b) {
		return new FldPair(e, b);
	}
	
	public AbstractSPGEdge getEdge() {
		return edge;
	}
	
	public boolean isBar() {
		return bar;
	}
	
//	public static void clear() {
//		edges.clear();
//	}
}
