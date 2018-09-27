package alias;

import iohoister.analysis.MayAliasAnalysis;
import iohoister.analysis.DefUseAnalysis.DefaultMayAliasAnalysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import soot.ArrayType;
import soot.Body;
import soot.Local;
import soot.MethodOrMethodContext;
import soot.PatchingChain;
import soot.PointsToAnalysis;
import soot.PointsToSet;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.ReachableMethods;

import edu.osu.cse.pa.Main;
import edu.osu.cse.pa.Timer;
import edu.osu.cse.pa.spg.AbstractAllocNode;
import edu.osu.cse.pa.spg.AbstractSPGEdge;
import edu.osu.cse.pa.spg.FieldPTEdge;
import edu.osu.cse.pa.spg.GlobalVarNode;
import edu.osu.cse.pa.spg.LocalVarNode;
import edu.osu.cse.pa.spg.NodeFactory;
import edu.osu.cse.pa.spg.PointsToEdge;
import edu.osu.cse.pa.spg.SymbolicPointerGraph;
import edu.osu.cse.pa.spg.VarNode;

public class Util {
	
	public static int ctxId = 0;
	
	private static Map<SootMethod, ReachableMethods> reachables = new HashMap<SootMethod, ReachableMethods>();
	
	private static int uniqueTempNumber = 0;
	
	public static long mayAliasTime = 0;
	public static int sparkFalsePairs = 0;
	
	public static String MAY_ALIAS;
	public static String BENCH_NAME;
	
	public static boolean IGNORE_LIBRARY_METHOD;
	public static boolean DEBUG;
	public static boolean DEBUG_SUMMARY;
	public static boolean DUMP_DOT;
	public static boolean TEST_SUMMARY;
	public static boolean TEST_ALIAS;
	public static boolean BASELINE_TEST;
	public static boolean USE_CACHE;
	public static boolean USE_SUMMARY;
	public static boolean DUMP_USE_DEF;
	public static boolean MEASURE_PRECISION;
	
	public static boolean MEASURE_CALLBACK;
	public static boolean MEASURE_SPARK;
	
	public static boolean SOAP12;
	
	public static boolean TEMP_HACK;
	
	// the incoming call graph edge ratio
	public static int SUMM_RATIO;
	public static int TIME_BUDGET;
	
	public static final int DEFAULT_SUMM_RATIO = 3;
	
	public static int MANU_MAX_TRAVERSAL;
	public static int MANU_MAX_PASSES;
	public static int SPA_BUDGET_NODES;	
	
	public static final int DEFAULT_BUDGET_NODES = 75000;
	public static final int DEFAULT_MANU_MAX_TRAVERSAL = 75000;
	public static final int DEFAULT_MANU_MAX_PASSES = 10;
	
	public static long maxAliasTime = 0;
	public static long totalAnalysisTime = 0;
	public static String maxAliasPair = "";

  public static long spgLibMtdsTime;
	
	public static long aliasStart;
	
	public static int traversedNodes;
	
//	public static int numAppMtds;
//	public static int numLibMtds;
	
	public static HashMap<SootMethod, HashSet<AbstractAllocNode>> escapables =
		new HashMap<SootMethod, HashSet<AbstractAllocNode>>();

	public static boolean POINTERBENCH;

	public static boolean TWEAK_BODY = true;
	
//	public static boolean magicDebug = false;
	
//	public static boolean outOfBudget = false;
		
	static {
		MAY_ALIAS = System.getProperty("MayAlias");
		BENCH_NAME = System.getProperty("BenchName");
		
		IGNORE_LIBRARY_METHOD = !("0".equals(System.getProperty("IgnoreLibraryMethod")));
		DEBUG = !("0".equals(System.getProperty("Debug")));
		DEBUG_SUMMARY = !("0".equals(System.getProperty("DebugSummary")));
		
		DUMP_DOT = !("0".equals(System.getProperty("DumpDot")));
		TEST_SUMMARY = !("0".equals(System.getProperty("TestSummary")));
		TEST_ALIAS = !("0".equals(System.getProperty("TestAlias")));
		BASELINE_TEST = !("0".equals(System.getProperty("BaselineTest")));
		USE_CACHE = false;
		USE_SUMMARY = false;		
		DUMP_USE_DEF = !("0".equals(System.getProperty("DumpUseDef")));
		MEASURE_PRECISION = !("0".equals(System.getProperty("MeasurePrecision")));
		MEASURE_CALLBACK = !("0".equals(System.getProperty("MeasureCallback")));
		MEASURE_SPARK = !("0".equals(System.getProperty("MeasureSpark")));
		
		SOAP12 = !("0".equals(System.getProperty("SOAP12")));
		TEMP_HACK = !("0".equals(System.getProperty("TEMP_HACK")));
		
		try {
			TIME_BUDGET = Integer.parseInt(System.getProperty("TimeBudget"));
		} catch (Exception e) {
			TIME_BUDGET = 2;
		}
		try {
			SUMM_RATIO = Integer.parseInt(System.getProperty("SummRatio"));
		} catch (Exception e) {
			SUMM_RATIO = DEFAULT_SUMM_RATIO;
		}
		try {
			MANU_MAX_TRAVERSAL = Integer.parseInt(System.getProperty("ManuMaxTraversal"));
		} catch (Exception e) {
			MANU_MAX_TRAVERSAL = DEFAULT_MANU_MAX_TRAVERSAL;
		}
		try {
			MANU_MAX_PASSES = Integer.parseInt(System.getProperty("ManuMaxPasses"));
		} catch (Exception e) {
			MANU_MAX_PASSES = DEFAULT_MANU_MAX_PASSES;
		}
		
		try {
			SPA_BUDGET_NODES = Integer.parseInt(System.getProperty("SpaBudgetNodes"));
		} catch (Exception e) {
			SPA_BUDGET_NODES = DEFAULT_BUDGET_NODES;
		}
		
		System.out.println("[PARAMETER] SPA_BUDGET_NODES: " + SPA_BUDGET_NODES + ", SUMM_RATIO: " + SUMM_RATIO + ", TIME_BUDGET: " + TIME_BUDGET);
	}
	
	public static boolean traditionalMayAlias(Local var1, SootMethod m1, Local var2, SootMethod m2, PointsToAnalysis pta) {
		if (var1.getName().equals(var2.getName()) && m1.getSignature().equals(m2.getSignature())) {
			return true;
		}		
		
		PointsToSet pts1 = pta.reachingObjects(var1);
		PointsToSet pts2 = pta.reachingObjects(var2);
		// when both empty, var1 & var2 should be the same object
		// to be aliased, which has been covered at the beginning
		if (pts1.isEmpty() || pts2.isEmpty()) {
			return false;
		}
		
		return pts1.hasNonEmptyIntersection(pts2);
	}
	
	public static boolean mayAlias(Local l1, SootMethod m1, Local l2, SootMethod m2, MayAliasAnalysis maa) {
		
		aliasStart = System.currentTimeMillis();
		boolean res = true;
		try {
			res = maa.mayAlias(l1, m1, l2, m2);
		} catch (OutOfMemoryError e) {
			System.out.println("[6e9waprU." + Util.BENCH_NAME + "." + Util.MAY_ALIAS + (Util.USE_SUMMARY ? ".summ" : ".nosumm") + "] OutOfMemory");
			System.exit(0);
		}
		mayAliasTime += (System.nanoTime() - aliasStart);
		return res;		
	}
	
	// FIXME: tweaked temporarily for experiments!!!
	public static boolean isOutOfBudget() {
//		if (Util.TEST_SUMMARY) {
//			return Util.traversedNodes >= Util.SPA_BUDGET_NODES;
//		} else {
//			return (System.nanoTime() - aliasStart) > 1000000 * Util.SUMM_RATIO;	// 1ms
//		}
		if ((System.currentTimeMillis() - aliasStart)>  Util.TIME_BUDGET){
			throw new DacongOutOfBudgetException();
		}	
		return false;
		// 1ms * ratio
//		return (System.nanoTime() - aliasStart) > 100000;	// 0.1ms
//		return (System.nanoTime() - aliasStart) > 100000000;	// 100ms
//		return Util.traversedNodes >= Util.SPA_BUDGET_NODES;
	}
	
	public static MayAliasAnalysis getMayAliasAnalysis() {
		
		if ("spark".equals(MAY_ALIAS)) {
			// 1. spark may alias
			return DefaultMayAliasAnalysis.v(Scene.v().getPointsToAnalysis());
		} else if ("spa".equals(MAY_ALIAS)) {
			// 2. spa may alias
			edu.osu.cse.pa.Main maa = edu.osu.cse.pa.Main.v();
			long start = System.currentTimeMillis();
			maa.buildSPG();
			long delta = System.currentTimeMillis() - start;
			System.out.println("[yUBuce2h." + BENCH_NAME + ".spg] Total: " + delta + " ms, Lib: " + spgLibMtdsTime);
			return maa;
		} else if ("manu".equals(MAY_ALIAS)) {
			// 3. manu may alias
			return ManuMayAliasAnalysis.v();
		} else if ("paddle".equals(MAY_ALIAS)) {
			return PaddleMayAliasAnalysis.v();
		} else if ("random".equals(MAY_ALIAS)) {
			return new RandomMayAliasAnalysis();
		} else {
			return null;
		}
	}
	
	public static void tweakBody(SootMethod m) {
		Body b = m.retrieveActiveBody();
		PatchingChain<Unit> units = b.getUnits();
		Iterator<Unit> stmtIter = units.snapshotIterator();
		
		while (stmtIter.hasNext()) {
			Stmt stmt = (Stmt) stmtIter.next();
			if (stmt.containsInvokeExpr()) {
				InvokeExpr ie = stmt.getInvokeExpr();
				String sig = ie.getMethod().getSignature();
				/*
				 * arraycopy(src, ..., tgt, ...)
				 * 
				 *   s1: temp = src[i];
				 *   s2: tgt[i] = temp;
				 */
				if (sig.equals(
					"<java.lang.System: void arraycopy(java.lang.Object,int,java.lang.Object,int,int)>")) {
					Local src = (Local) ie.getArg(0);
					Local tgt = (Local) ie.getArg(2);
					Type srcType = src.getType();
					Type eleType = null;
					if (srcType instanceof ArrayType) {
						eleType = ((ArrayType)srcType).getArrayElementType();	
					} else {
//						System.out.println(src + "," + tgt + "@" + m);
						continue;
					}
					
					Local temp = Jimple.v().newLocal("__ze8uwAc3_" + uniqueTempNumber++, eleType);
					b.getLocals().addLast(temp);
					AssignStmt s1 = Jimple.v().newAssignStmt(temp, Jimple.v().newArrayRef(src, IntConstant.v(0)));
					AssignStmt s2 = Jimple.v().newAssignStmt(Jimple.v().newArrayRef(tgt, IntConstant.v(0)), temp);
					
					units.insertAfter(s1, stmt);
					units.insertAfter(s2, s1);
				} else if (stmt instanceof AssignStmt && ie instanceof InstanceInvokeExpr &&
					sig.startsWith("<java.lang.StringBuffer: java.lang.StringBuffer append(")) {
//					System.err.println("tweaking StringBuffer...");
					AssignStmt assign = (AssignStmt) stmt;
//					System.out.println("[StringBuffer] " + assign);	
					Value lhs = assign.getLeftOp();
					Local base = (Local) ((InstanceInvokeExpr) ie).getBase();

					AssignStmt s1 = Jimple.v().newAssignStmt(lhs, base);					
					
					units.insertAfter(s1, stmt);
					units.remove(stmt);
				}
			}
		}
	}
	
	public static boolean inLibrary(SootMethod m) {
    	String p = m.getDeclaringClass().getJavaPackageName();
    	return 
    	    p.startsWith("java.") || p.startsWith("sun.")   ||
    	    p.startsWith("sunw.") || p.startsWith("javax.") ||
    	    p.startsWith("org.")  || p.startsWith("com.");
	}
	 
	public static boolean isVisited(LinkedList<CtxPair> ctxSumm, LinkedList<NumberedFldPair> fldSumm, AbstractSPGEdge edge, int ctxHash) {
		if (edge instanceof FieldPTEdge) {
			for (NumberedFldPair p : fldSumm) {				
				if (p.getEdge() == edge && p.getCtxHash() == ctxHash) {
					return true;
				}
			}
			
		} else {
			for (CtxPair cp : ctxSumm) {
				// the condition is relaxed here to merge multiple "identical" call sites
				// during summary computation
				AbstractSPGEdge e = cp.getEdge();
				if (e == edge) return true;
//				if (e.src() == edge.src() && e.tgt() == edge.tgt()) {
//					return true;
//				}
				
			}
		}		
		
		return false;
	}
	
	public static boolean isVisitedOld(LinkedList<CtxPair> ctxSumm, LinkedList<NumberedFldPair> fldSumm, AbstractSPGEdge edge, int ctxHash) {
		if (edge instanceof FieldPTEdge) {
			for (NumberedFldPair p : fldSumm) {				
				if (p.getEdge() == edge && p.getCtxHash() == ctxHash) {
					return true;
				}
			}
			
		} else {
			for (CtxPair cp : ctxSumm) {
				if (cp.getEdge() == edge) {
					return true;
				}
			}
		}		
		
		return false;
	}
	
	public static boolean pointByGVN(AbstractAllocNode n) {		
		for (Iterator<VarNode> iter = n.getPointBy(); iter.hasNext(); ) {
			VarNode vn = iter.next();
			if (vn instanceof GlobalVarNode) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean sootFieldEquals(SootField f1, SootField f2) {
		if (f1 == f2) return true;	// TODO: for ArrayElementField, might be wrong
		try {
			return f1.toString().equals(f2.toString());
		} catch (Exception ex) {
			return false;
		}
	}
	
	
	public static ReachableMethods getReachables(SootMethod m) {
		ReachableMethods mtds = reachables.get(m);

		if (mtds == null) {
			Collection<MethodOrMethodContext> entryPoints = new ArrayList<MethodOrMethodContext>();
			entryPoints.add(m);
			if(Util.POINTERBENCH ) {
				mtds = new ReachableMethods(Scene.v().getCallGraph(), entryPoints);
			}else{
				mtds = new ReachableMethods(Scene.v().getCallGraph(),Scene.v().getEntryPoints());
			}
			mtds.update();
			
//			CallGraph cg = Scene.v().getCallGraph();
//			mtds = new HashSet<SootMethod>();
//			mtds.add(m);
//			LinkedList<SootMethod> worklist = new LinkedList<SootMethod>();
//			worklist.add(m);
//			while (!worklist.isEmpty()) {
//				SootMethod sm = worklist.removeFirst();
//				for (Iterator<Edge> iter = cg.edgesOutOf(sm); iter.hasNext();) {
//					Edge e = iter.next();
//					SootMethod tgt = e.tgt();
//					if (!mtds.contains(tgt)) {
//						mtds.add(tgt);
//						worklist.add(tgt);
//					}
//				}
//			}
			reachables.put(m, mtds);
		}	
		
		return mtds;
	}	

	/*
	 * Determines whether we can reach n2 starting from n1 with call graph constraints
	 * only.
	 */
	public static boolean reachable(AbstractAllocNode n1, AbstractAllocNode n2) {
		SootMethod m1 = n1.getMethod();
		ReachableMethods mtds = getReachables(m1);
		SootMethod m2 = n2.getMethod();
		return mtds.contains(m2);	// the over-simplified implementation
//		
//		if (!mtds.contains(m2)) return false;
//
//		LinkedList<AbstractAllocNode> stk = new LinkedList<AbstractAllocNode>();
//		stk.push(n1);
//		while (!stk.isEmpty()) {
//			AbstractAllocNode cur = stk.pop();
//			for (Iterator<AbstractSPGEdge> inIter = cur.getIncomingEdges(); inIter.hasNext(); ) {
//				AbstractSPGEdge edge = inIter.next();
//				AbstractAllocNode node = edge.src();
//				if (node == cur) continue;	// FIXME: merging recursive calls lead to imprecision
//				SootMethod mtd = node.getMethod();
//				if (mtds.contains(mtd)) {
//					stk.push(node);
//				}
//			}
//			
//			for (Iterator<AbstractSPGEdge> outIter = cur.getOutgoingEdges(); outIter.hasNext(); ) {
//				AbstractSPGEdge edge = outIter.next();
//			}
//		}
	}
	
	public static boolean compatibleClass(SootClass sc1, SootClass sc2) {		
		return subclassOf(sc1, sc2) || subclassOf(sc2, sc1);
	}
	
	public static boolean subclassOf(SootClass sc1, SootClass sc2) {
		if (sc1.equals(sc2)) return true;		
		if (sc1.getName().equals("java.lang.Object")) return false;
		
		if (subclassOf(sc1.getSuperclass(), sc2)) return true;
		
		for (SootClass sc : sc1.getInterfaces()) {
			if (subclassOf(sc, sc2)) return true;
		}
		
		return false;
	}
	
	public static HashSet<AbstractAllocNode> getReachables(ReachableMethods mtds, Collection<AbstractAllocNode> objs) {
		HashSet<AbstractAllocNode> result = new HashSet<AbstractAllocNode>(objs);
		LinkedList<AbstractAllocNode> stk = new LinkedList<AbstractAllocNode>(objs);	
		
		while (!stk.isEmpty()) {
			AbstractAllocNode cur = stk.removeFirst();
			if (!result.contains(cur)) {
				for (Iterator<AbstractSPGEdge> inIter = cur.getIncomingEdges(); inIter.hasNext(); ) {
					AbstractSPGEdge edge = inIter.next();
					AbstractAllocNode node = edge.src();
					SootMethod mtd = node.getMethod();
					if (mtds.contains(mtd) && !result.contains(node)) {
						result.add(node);
						stk.addFirst(node);
					}
				}
				
				for (Iterator<AbstractSPGEdge> outIter = cur.getOutgoingEdges(); outIter.hasNext(); ) {
					AbstractSPGEdge edge = outIter.next();
					AbstractAllocNode node = edge.tgt();
					SootMethod mtd = node.getMethod();					
					if (mtds.contains(mtd) && !result.contains(node)) {
						result.add(node);
						stk.addFirst(node);
					}
				}
			}
		}
		
		return result;
	}

	public static HashSet<AbstractAllocNode> getEscapableObjects(SootMethod m) {
		
		HashSet<AbstractAllocNode> escapableObjs = escapables.get(m);
		if (escapableObjs != null) {
			return escapableObjs;
		}
			
		escapableObjs = new HashSet<AbstractAllocNode>();
		escapables.put(m, escapableObjs);
		SymbolicPointerGraph spg = SymbolicPointerGraph.v(m);
		
		// 1. get the parameter objects
		Body b = m.retrieveActiveBody();
		NodeFactory fact = NodeFactory.v(m);
		if (!m.isStatic()) {
			VarNode thisNode =  fact.findLocalVarNode(b.getThisLocal());
			for (Iterator<PointsToEdge> iter = thisNode.getPointsToEdges(); iter.hasNext();) {
				AbstractAllocNode o = iter.next().tgt();
				if (!escapableObjs.contains(o)) escapableObjs.add(o);
			}
		}
		for (int i = 0; i < m.getParameterCount(); i++) {
			Local formal = b.getParameterLocal(i);
			VarNode fn = fact.findLocalVarNode(formal);
			if (null == fn) {
				continue;
			}
			for (Iterator<PointsToEdge> iter = fn.getPointsToEdges(); iter.hasNext();) {
				AbstractAllocNode o = iter.next().tgt();
				if (!escapableObjs.contains(o)) escapableObjs.add(o);
			}
		}
//		
//		if (escapableObjs.isEmpty()) {
//
//			return s;
//		}
		
		// 2. get the return object
		Set<LocalVarNode> returnedVars = spg.getReturnedVars();
		for (LocalVarNode lvn : returnedVars) {			
			for (Iterator<PointsToEdge> iter = lvn.getPointsToEdges(); iter.hasNext();) {
				PointsToEdge e = iter.next();
				AbstractAllocNode o = e.tgt();
				if (!escapableObjs.contains(o)) escapableObjs.add(o);	
			}
		}
		
		return escapableObjs;
	}	

//	private static int edgeUID = 0;
//	private static HashMap<Edge, Integer> edgeToUID = new HashMap<Edge, Integer>();
//	private static HashMap<Integer, Edge> uidToEdge = new HashMap<Integer, Edge>();
//	public static int getUIDByEdge(Edge e) {
//		Integer i = edgeToUID.get(e);
//		if (i == null) {
//			i = new Integer(edgeUID++);
//			edgeToUID.put(e, i);
//			uidToEdge.put(i, e);
//		}
//		
//		return i.intValue();
//	}
//	
//	public static Edge getEdgeByUID(int uid) {
//		return uidToEdge.get(uid);
//	}
//	
//	private static int fldUID = 0;
//	private static HashMap<SootField, Integer> fldToUID = new HashMap<SootField, Integer>();
//	private static HashMap<Integer, SootField> uidToFld = new HashMap<Integer, SootField>();
//	public static int getUIDByFld(SootField fld) {
//		Integer i = fldToUID.get(fld);
//		if (i == null) {
//			i = new Integer(fldUID++);
//			fldToUID.put(fld, i);
//			uidToFld.put(i, fld);
//		}
//		
//		return i;
//	}
//	public static SootField getFldByUID(int uid) {
//		return uidToFld.get(uid);
//	}
}
