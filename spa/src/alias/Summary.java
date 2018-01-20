package alias;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import edu.osu.cse.pa.Main;
import edu.osu.cse.pa.spg.AbstractAllocNode;
import edu.osu.cse.pa.spg.AbstractSPGEdge;
import edu.osu.cse.pa.spg.EntryEdge;
import edu.osu.cse.pa.spg.ExitEdge;
import edu.osu.cse.pa.spg.FieldPTEdge;
import edu.osu.cse.pa.spg.LocalVarNode;
import edu.osu.cse.pa.spg.NodeFactory;
import edu.osu.cse.pa.spg.PointsToEdge;
import edu.osu.cse.pa.spg.SymbolicPointerGraph;
import edu.osu.cse.pa.spg.VarNode;
import soot.Body;
import soot.Local;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.ReachableMethods;

/*
 * A typical Summary is a collection of 4-tuples <beg, end, ctxEdges, fldEdges>
 */
public class Summary {

	private static HashMap<SootMethod, Summary> summaries =
		new HashMap<SootMethod, Summary>();
	
//	private static HashSet<String> computing =
//		new HashSet<String>();
	public static HashSet<SootMethod> doNotCompute =
		new HashSet<SootMethod>();
	
	public static HashSet<SootMethod> libSummed =
		new HashSet<SootMethod>();
	public static HashSet<SootMethod> libCallbacks =
		new HashSet<SootMethod>();
	
	public static int RATIO = 4;
	
	private Collection<SummaryRecord> records =
		new ArrayList<SummaryRecord>();

	public static final Summary emptySummary = new Summary();
	private Summary() {
	}
	
	public Collection<SummaryRecord> startsWith(AbstractAllocNode n) {
		Collection<SummaryRecord> res = new ArrayList<SummaryRecord>();
		for (SummaryRecord rec : records) {
			if (rec.beg == n) {
				res.add(rec);
			}
		}
		
		return res;
	}
	
	public Collection<SummaryRecord> endsWith(AbstractAllocNode n) {
		Collection<SummaryRecord> res = new ArrayList<SummaryRecord>();
		for (SummaryRecord rec : records) {
			if (rec.end == n) {
				res.add(rec);
			}
		}
		
		return res;
	}

	/**
	 * This method computes the reachability summary for the specified method. The computation
	 * is an all-pairs CFL-reachability problem over the set of escapable objects. The essence
	 * is to avoid repeatitious analysis on the same method. The summaries of methods are cached
	 * so that we don't need to compute them again and again.
	 * 
	 * It's computed in the following steps:
	 * 	0. Check whether the summary for this specified method has been computed or not. If it
	 * 	   is, the cache result is returned immediately. Otherwise, we need to continue the
	 * 	   remaining steps to actually compute it.
	 *  1. The very first step is to compute the set of escapable objects. Those objects escaping
	 *     from call sites within this method are ignored. Thus, there are two types of escapable
	 *     objects: nodes the parameters and the return variables point to respectively.
	 *  2. The CFL-reachability computation is performed on this set of escapable objects. Special
	 *     cares, as in the normal traversal, should be taken for:
	 *        a. global variables (static fields)
	 *        b. recursive data structures
	 * @param m
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Summary getSummary(SootMethod m) {
		libSummed.add(m);
		// 0. check whether summary already exists
		Summary s = summaries.get(m);
		if (s != null) {
			return s;
		}

//		String sig = m.getSignature();
		
		if (doNotCompute.contains(m)) {
			throw new OutOfBudgetException();
		}		

//		System.out.println("summary-calc: " + m);

		HashSet<AbstractAllocNode> escapableObjs = Util.getEscapableObjects(m);		
		
		if (escapableObjs.size() <= 1) {
			summaries.put(m, emptySummary);
			return emptySummary;
		}
		
		s = new Summary();
		
		// plain graph-reachability pruning		
//		ReachableMethods mtds = Util.getReachables(m);		
//		HashSet<AbstractAllocNode> reachables = Util.getReachables(mtds, escapableObjs);		
		
		// 3. compute summary
		// TODO: design a algorithm to compute the summary
		// 	#1. Search without prune. (n^2)
		//  #2. Could be ( (n^2) / 2 ) due to symmetry.		
		LinkedList<Edge> emptyCtxStk = new LinkedList<Edge>();
		LinkedList<CtxPair> emptyCtxSumm = new LinkedList<CtxPair>();
		LinkedList<FldPair> emptyFldStk = new LinkedList<FldPair>();
		LinkedList<NumberedFldPair> emptyFldSumm = new LinkedList<NumberedFldPair>();		
		LinkedList<SummaryTuple> worklist = new LinkedList<SummaryTuple>();

		for (AbstractAllocNode o : escapableObjs) {
			SummaryTuple tuple = SummaryTuple.getTuple(o, o, emptyCtxStk, emptyCtxSumm, emptyFldStk, emptyFldSumm, 0);
			worklist.add(tuple);
		}
		
		int budget = Util.SPA_BUDGET_NODES * escapableObjs.size();
//		int budget = 100;
		int traversedNodes = 0;
		while (!worklist.isEmpty()) {
			SummaryTuple t = worklist.removeFirst();
			AbstractAllocNode cur = t.getCurrent();
			boolean isSpecial = t.getCtxStk().isEmpty();

			//--- incoming edges
			for (Iterator<AbstractSPGEdge> inIter = cur.getIncomingEdges(); inIter.hasNext();) {				
				AbstractSPGEdge e = inIter.next();
				AbstractAllocNode src = e.src();
				SootMethod mtd = src.getMethod();
				
				// escape
				if (isSpecial && e instanceof EntryEdge) {
					continue;
				}

//				if (!reachables.contains(src)) continue;
				
				if (e instanceof ExitEdge) {
				// callbacks
					if (Util.MEASURE_CALLBACK) {						
						if (mtd.getDeclaringClass().isApplicationClass()) {
							libCallbacks.add(m);
						}
					}	
					if(mtd.getDeclaringClass().isApplicationClass() || Summary.ignoreMethod(mtd)) continue;								
				}

				LinkedList<Edge> ctxStk = t.getCtxStk();
				LinkedList<CtxPair> ctxSumm = t.getCtxSumm();
				LinkedList<FldPair> fldStk = t.getFldStk();
				LinkedList<NumberedFldPair> fldSumm = t.getFldSumm();
				int ctxHash = t.getCtxHash();
				
				if (Util.isVisited(ctxSumm, fldSumm, e, ctxHash)) {				
					continue;
				}
								
				// jE9pazU8: we might need to process GVN
				if (Util.pointByGVN(src)) {
					//--- OLD
					doNotCompute.add(m);
					throw new OutOfBudgetException();
					//--- NEW
//					continue;
					//--- END
				}			
				
				if (e instanceof FieldPTEdge) {
					FldPair fp = FldPair.getPair(e, true);
					fldStk = (LinkedList<FldPair>)(fldStk.clone());
					fldStk.addFirst(fp);
					
					fldSumm = (LinkedList<NumberedFldPair>)(fldSumm.clone());
					fldSumm.add(new NumberedFldPair(e, true, ctxHash));
				} else if (e instanceof EntryEdge) {
					// invariant: ctxStk is NOT empty
					// try to match
					if (!ctxStk.isEmpty()) {
						Edge cs1 = ctxStk.peek();						
						Edge cs2 = CallSite.getCallsite(e);
						if (cs1 != cs2) {
							continue;
						} else {
							ctxStk = (LinkedList<Edge>)(ctxStk.clone());
							ctxStk.removeFirst();
						}
					} // when the ctxStk is empty, the current node must be in `objs'
					
					ctxSumm = (LinkedList<CtxPair>)(ctxSumm.clone());
					ctxSumm.add(new CtxPair(e, src, CtxPair.POP));
				} else {	// inverse ExitEdge -> entry
					CtxPair cp = new CtxPair(e, src, CtxPair.PUSH);
					ctxStk = (LinkedList<Edge>)(ctxStk.clone());
					ctxStk.addFirst(CallSite.getCallsite(e));
					
					ctxSumm = (LinkedList<CtxPair>)(ctxSumm.clone());
					ctxSumm.add(cp);
				}
				
				// jE9pazU8: no budget limit needed?
				traversedNodes++;
				if (traversedNodes >= budget) {
					doNotCompute.add(m);
					throw new OutOfBudgetException();
				}

				if (escapableObjs.contains(src)) {
					SummaryRecord rec = new SummaryRecord(t.getHead(), src, ctxSumm, fldSumm);
					s.records.add(rec);
				} else {
					if (!(e instanceof FieldPTEdge)) {
						ctxHash = 3 * ctxHash + e.getReverseId();
					}
					
//					if (topLevel && e instanceof ExitEdge && !Summary.ignoreMethod(mtd)) {
//						applySummary(t.getHead(), src, ctxStk, ctxSumm, fldStk, fldSumm, ctxHash, worklist);
//					} else {
						SummaryTuple toAdd = SummaryTuple.getTuple(t.getHead(), src, ctxStk, ctxSumm, fldStk, fldSumm, ctxHash);
						worklist.add(toAdd);						
//					}					
				}
			} // END incoming edge traverse
			
			//--- outgoing edges
			for (Iterator<AbstractSPGEdge> outIter = cur.getOutgoingEdges(); outIter.hasNext();) {
				

				AbstractSPGEdge e = outIter.next();
				AbstractAllocNode tgt = e.tgt();
				SootMethod mtd = tgt.getMethod();
				
				// escape
				if (isSpecial && e instanceof ExitEdge) {
					continue;
				}
				
				if (e instanceof EntryEdge) {
					// callbacks
					if (Util.MEASURE_CALLBACK) {						
						if (mtd.getDeclaringClass().isApplicationClass()) {
							libCallbacks.add(m);
						}
					}
					if (mtd.getDeclaringClass().isApplicationClass() || Summary.ignoreMethod(mtd)) continue;
				}
				
//				if (!reachables.contains(tgt)) continue;
				
				LinkedList<Edge> ctxStk = t.getCtxStk();
				LinkedList<CtxPair> ctxSumm = t.getCtxSumm();
				LinkedList<FldPair> fldStk = t.getFldStk();
				LinkedList<NumberedFldPair> fldSumm = t.getFldSumm();
				int ctxHash = t.getCtxHash();
				
				if (Util.isVisited(ctxSumm, fldSumm, e, ctxHash)) {
					continue;
				}
				
				// ignore global var node
				if (Util.pointByGVN(tgt)) {
					//--- OLD
					doNotCompute.add(m);
					throw new OutOfBudgetException();	// this is not actually an out of budget case
					//--- NEW
//					continue;
					//--- END
				}

				if (e instanceof FieldPTEdge) {
//					FldPair fp = FldPair.getPair(e, false);
					
					if (!fldStk.isEmpty()) {
						FldPair topFld = fldStk.peek();
						
						FieldPTEdge fpt1 = (FieldPTEdge) e;
        				FieldPTEdge fpt2 = (FieldPTEdge) topFld.getEdge();            				
        				
        				if (!Util.sootFieldEquals(fpt1.getField(), fpt2.getField())) {
        					continue;        					
        				}
        				
        				fldStk = (LinkedList<FldPair>)(fldStk.clone());
        				fldStk.removeFirst();
					}
					
					fldSumm = (LinkedList<NumberedFldPair>)(fldSumm.clone());
					fldSumm.add(new NumberedFldPair(e, false, ctxHash));
				} else if (e instanceof EntryEdge) {
					CtxPair cp = new CtxPair(e, tgt, CtxPair.PUSH);
					
					ctxStk = (LinkedList<Edge>)(ctxStk.clone());
					ctxStk.addFirst(CallSite.getCallsite(e));
					
					ctxSumm = (LinkedList<CtxPair>)(ctxSumm.clone());
					ctxSumm.add(cp);
				} else {
					// invariant: ctxStk is NOT empty
					// try to match
					if (!ctxStk.isEmpty()) {
						Edge cs1 = ctxStk.peek();						
						Edge cs2 = CallSite.getCallsite(e);
						if (cs1 != cs2) {
							continue;
						} else {
							ctxStk = (LinkedList<Edge>)(ctxStk.clone());
							ctxStk.removeFirst();
						}
					}
					ctxSumm = (LinkedList<CtxPair>)(ctxSumm.clone());
					ctxSumm.add(new CtxPair(e, tgt, CtxPair.POP));
				}
				
				// jE9pazU8: we might not need budget limit if the summary computation is not that heavy-weight
				traversedNodes++;
				if (traversedNodes >= budget) {				
					doNotCompute.add(m);
					throw new OutOfBudgetException();
				}		

				if (escapableObjs.contains(tgt)) {
					SummaryRecord rec = new SummaryRecord(t.getHead(), tgt, ctxSumm, fldSumm);
					s.records.add(rec);
				} else {
					if (!(e instanceof FieldPTEdge)) {
						ctxHash = 3 * ctxHash + e.getId();
					}
//					if (topLevel && e instanceof EntryEdge && !Summary.ignoreMethod(mtd)) {
//						applySummary(t.getHead(), tgt, ctxStk, ctxSumm, fldStk, fldSumm, ctxHash, worklist);
//					} else {
						SummaryTuple toAdd = SummaryTuple.getTuple(t.getHead(), tgt, ctxStk, ctxSumm, fldStk, fldSumm, ctxHash);
						worklist.add(toAdd);
//					}
					
				}
			} // END outgoing edge traverse
		}	// END worklist while-loop		

//		removeSig(sig);	// for cascaded summary
		summaries.put(m, s);
		return s;
	}	
	
//	private static void removeSig(String sig) {
//		computing.remove(sig);
//		if (computing.isEmpty()) {
//			NumberedObject.reset();
//		}
//	}
	
	public void clear() {
		records.clear();
	}
	
	public static boolean ignoreMethod(SootMethod mtd) {
		String sig = mtd.getSignature();
		String subsig = mtd.getSubSignature();
		if (
			sig.equals("<java.lang.Object: void <init>()>")
			|| subsig.equals("boolean equals(java.lang.Object)")
			|| subsig.equals("int hashCode()")
//			|| subsig.equals("java.lang.String toString()")
		) {
			return true;
		}
	
		if (Util.getEscapableObjects(mtd).size() <= 1) {
			return true;
		}
		
		return false;
	}
	
	public boolean isEmpty() {
		return records.isEmpty();
	}
//	
//	public void dump() {
//		System.out.println("\n---");
//		System.out.println("[Summary] " + method.getSignature());
//		for (SummaryRecord rec : records) {
//			rec.dump();
//		}
//		System.out.println("---\n");
//	}
}
