package alias;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import soot.SootField;
import soot.jimple.toolkits.callgraph.Edge;

import edu.osu.cse.pa.spg.AbstractAllocNode;
import edu.osu.cse.pa.spg.AbstractSPGEdge;
import edu.osu.cse.pa.spg.FieldPTEdge;

public class TraverseTuple {
	private static Set<TraverseTuple> quads = new HashSet<TraverseTuple>();	
	
	private AbstractAllocNode node;
	private LinkedList<Edge> ctxStk;
	private LinkedList<FieldPTEdge> fldStk;
	private HashSet<AbstractSPGEdge> visitedCtxEdges;
	private HashSet<Pair<FldPair, Integer>> visitedFldEdges;	// with context snapshot
	private int ctxHash;
	
	private TraverseTuple(AbstractAllocNode n, LinkedList<Edge> cs, LinkedList<FieldPTEdge> fs, HashSet<AbstractSPGEdge> visitedCtxEdges,
		HashSet<Pair<FldPair, Integer>> visitedFldEdges, int ctxHash) {
		this.node = n;
		this.ctxStk = cs;
		this.fldStk = fs;
		this.visitedCtxEdges = visitedCtxEdges;
		this.visitedFldEdges = visitedFldEdges;
		this.ctxHash = ctxHash;
	}
		
	public static TraverseTuple getTuple(AbstractAllocNode n, LinkedList<Edge> cs, LinkedList<FieldPTEdge> fs, HashSet<AbstractSPGEdge> visitedCtxEdges,
		HashSet<Pair<FldPair, Integer>> visitedFldEdges, int ctxHash) {
		TraverseTuple quad = new TraverseTuple(n, cs, fs, visitedCtxEdges, visitedFldEdges, ctxHash);
		quads.add(quad);
		return quad;		
	}
		
	public AbstractAllocNode getNode() {
		return node;
	}
	
	public LinkedList<Edge> getCtxStk() {
		return ctxStk;
	}
	
	public LinkedList<FieldPTEdge> getFldStk() {
		return fldStk;
	}
	
	public HashSet<AbstractSPGEdge> getVisitedCtxEdges() {
		return visitedCtxEdges;
	}
	
	public HashSet<Pair<FldPair, Integer>> getVisitedFldEdges() {
		return visitedFldEdges;
	}
	
	public int getCtxHash() {
		return ctxHash;
	}
			
	public static void clear() {
		quads.clear();
	}
}
