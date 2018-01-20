package alias;

import edu.osu.cse.pa.spg.AbstractAllocNode;
import edu.osu.cse.pa.spg.AbstractSPGEdge;
import edu.osu.cse.pa.spg.EntryEdge;
import edu.osu.cse.pa.spg.ExitEdge;
import soot.jimple.toolkits.callgraph.Edge;

public class CtxPair extends NumberedObject {
	
	public final static int PUSH = 0;
	public final static int POP = 1;
	
	AbstractSPGEdge edge;
	AbstractAllocNode node;
	Edge callsite;
	int action;
	
	public CtxPair(AbstractSPGEdge edge, AbstractAllocNode node, int action) {
		// pre-condition: (action == PUSH) XOR (action == POP)
		
//		if (action != PUSH && action != POP) {
//			throw new RuntimeException("Unknown action " + action + " for CtxPair!");
//		}
		this.action = action;		
		this.edge = edge;
		this.node = node;
		this.callsite = CallSite.getCallsite(edge);				
	}
	
	public AbstractSPGEdge getEdge() {
		return edge;
	}
	
	public AbstractAllocNode getNode() {
		return node;
	}
	
	public Edge getCallsite() {
		return callsite;
	}
	public int getAction() {
		return action;
	}
	

}
