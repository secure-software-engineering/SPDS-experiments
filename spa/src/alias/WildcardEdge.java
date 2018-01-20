package alias;

import java.util.ArrayList;

import soot.SootField;

import edu.osu.cse.pa.spg.FieldPTEdge;

public class WildcardEdge extends FieldPTEdge {

	private ArrayList<FieldPTEdge> cycle;
	private int pos;	

	public WildcardEdge(ArrayList<FieldPTEdge> cycle, int pos) {
		super();
		this.cycle = cycle;
		this.pos = pos;
	}

	public ArrayList<FieldPTEdge> getCycleEdges() {
		return cycle;
	}
	
	public boolean match(SootField fld) {
		FieldPTEdge current = cycle.get(pos);
		return Util.sootFieldEquals(current.getField(), fld);
	}
	
	public void forward() {
		pos = (cycle.size() + pos - 1) % cycle.size();
	}
	
	public int getPos() {
		return pos;
	}
	
	public Object clone() {
		ArrayList<FieldPTEdge> newCycle = (ArrayList<FieldPTEdge>) cycle.clone();
		WildcardEdge we = new WildcardEdge(newCycle, pos);
		return we;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer("cycle: ");
		for (FieldPTEdge e : cycle) {
			sb.append(e.getField().getSignature() + ", ");
		}
		
		return sb.toString();
	}
}
