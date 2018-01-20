package alias;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import soot.jimple.toolkits.callgraph.Edge;

import edu.osu.cse.pa.spg.AbstractAllocNode;
import edu.osu.cse.pa.spg.AbstractSPGEdge;

public class SummaryTuple {
	private static Set<SummaryTuple> tuples = new HashSet<SummaryTuple>();	
	
	private AbstractAllocNode head;			// the head node of the summary chain
	private AbstractAllocNode cur;			// the current node in the traversal
	private LinkedList<Edge> ctxStk;		// the stack used for matching contexts
	private LinkedList<CtxPair> ctxSumm;	// the list to keep the context summary
	private LinkedList<FldPair> fldStk;
	private LinkedList<NumberedFldPair> fldSumm;	// the list to keep the field summary
	private int ctxHash;
//	private HashSet<AbstractSPGEdge> visited;	// the set to keep the visited nodes
	
	private SummaryTuple(AbstractAllocNode head, AbstractAllocNode cur, LinkedList<Edge> ctxStk, LinkedList<CtxPair> ctxSumm,
		LinkedList<FldPair> fldStk, LinkedList<NumberedFldPair> fldSumm, int ctxHash) {
		this.head = head;
		this.cur = cur;
		this.ctxStk = ctxStk;
		this.ctxSumm = ctxSumm;
		this.fldStk = fldStk;
		this.fldSumm = fldSumm;
		this.ctxHash = ctxHash;
//		this.visited = visited;
	}
		
	public static SummaryTuple getTuple(AbstractAllocNode head, AbstractAllocNode cur, LinkedList<Edge> ctxStk, LinkedList<CtxPair> ctxSumm,
		LinkedList<FldPair> fldStk, LinkedList<NumberedFldPair> fldSumm, int ctxHash) {
		SummaryTuple tuple = new SummaryTuple(head, cur, ctxStk, ctxSumm, fldStk, fldSumm, ctxHash);
		tuples.add(tuple);
		return tuple;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer(head.toString());
		sb.append("," + cur.toString());
		sb.append("," + ctxSumm.toString());
		sb.append("," + fldSumm.toString());
//		sb.append("," + visited.toString());
		
		return sb.toString();
	}
		
	public AbstractAllocNode getHead() {
		return head;
	}
	
	public AbstractAllocNode getCurrent() {
		return cur;
	}
	
	public LinkedList<Edge> getCtxStk() {
		return ctxStk;
	}
	
	public LinkedList<CtxPair> getCtxSumm() {
		return ctxSumm;
	}
	
	public LinkedList<FldPair> getFldStk() {
		return fldStk;
	}
	
	public LinkedList<NumberedFldPair> getFldSumm() {
		return fldSumm;
	}
	
	public int getCtxHash() {
		return ctxHash;
	}
	
//	public HashSet<AbstractSPGEdge> getVisited() {
//		return visited;
//	}
	
	public static void clear() {
		tuples.clear();
	}
}
