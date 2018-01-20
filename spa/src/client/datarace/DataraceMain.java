package client.datarace;

import iohoister.analysis.ArrayFieldRef;
import iohoister.analysis.MayAliasAnalysis;
import iohoister.analysis.DefUseAnalysis.DefaultMayAliasAnalysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

import alias.ManuMayAliasAnalysis;
import alias.RandomMayAliasAnalysis;
import alias.Summary;
import alias.Util;

import edu.osu.cse.pa.Main;
import edu.osu.cse.pa.spg.NodeFactory;

import soot.Local;
import soot.MethodOrMethodContext;
import soot.PackManager;
import soot.RefLikeType;
import soot.RefType;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Transform;
import soot.Type;
import soot.Value;
import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.toolkits.scalar.Pair;
import soot.util.queue.QueueReader;

public class DataraceMain extends SceneTransformer {
	
	private Map<SootField, HashSet<Pair<DefinitionStmt, SootMethod>>> instanceFieldWrites =
		new HashMap<SootField, HashSet<Pair<DefinitionStmt, SootMethod>>>();
	private Map<SootField, HashSet<Pair<DefinitionStmt, SootMethod>>> instanceFieldReads =
		new HashMap<SootField, HashSet<Pair<DefinitionStmt, SootMethod>>>();
	
//	private Map<SootField, HashSet<Pair<DefinitionStmt, SootMethod>>> staticFieldWrites =
//		new HashMap<SootField, HashSet<Pair<DefinitionStmt, SootMethod>>>();
//	private Map<SootField, HashSet<Pair<DefinitionStmt, SootMethod>>> staticFieldReads =
//		new HashMap<SootField, HashSet<Pair<DefinitionStmt, SootMethod>>>();
	
	private Map<SootField, HashSet<Pair<DefinitionStmt, SootMethod>>> arrayElementWrites =
		new HashMap<SootField, HashSet<Pair<DefinitionStmt, SootMethod>>>();
	private Map<SootField, HashSet<Pair<DefinitionStmt, SootMethod>>> arrayElementReads =
		new HashMap<SootField, HashSet<Pair<DefinitionStmt, SootMethod>>>();
	
	
	private static DataraceMain theInst;

	private static long numRaces = 0;
	
	private int mTrue = 0;
	private int mFalse = 0;

	public static DataraceMain v() {
		if (theInst == null) {
			theInst = new DataraceMain();		
		}
		return theInst;
	}
	/**
	 * @param args
	 */
	public static void main(String[] liargs) {
		DataraceMain pa = DataraceMain.v();
		String phaseName = "wjtp.racer";
		Transform t = new Transform(phaseName, pa);
		PackManager.v().getPack("wjtp").add(t);

		boolean isPaddle = Util.MAY_ALIAS.equals("paddle");
		String[] args = { "-W",
				"-p",
				"wjop.si",
				"enabled:false",
//				"-p",
//				"wjtp",
//				"enabled:false",
				"-p",
				phaseName,
				"enabled:true",
				// "-p", "cg.spark", (pre_computed_callgraph ? "enabled:true" :
				// "enabled:false"),
				// "-p", "cg.spark", "rta:true",
				"-p", (isPaddle ? "cg.paddle" : "cg.spark"), "enabled:true" +
//				(isPaddle ? ",bdd:true,backend:javabdd,context:objsens" : "")
				(isPaddle ? ",bdd:true,backend:buddy,pre-jimplify:true,context:kobjsens,k:1,context-heap:true,total-context-counts:false,context-counts:false" : ""),
				"-cp", liargs[0] + java.io.File.pathSeparator + liargs[1], "-f", "n",
				"-allow-phantom-refs", "-process-dir", liargs[1],
				"-main-class", liargs[2], liargs[2], };

		for (int i = 0; i < args.length; i++) {
			System.out.print(args[i] + " ");
		}
		System.out.println();
		
		soot.Main.main(args);
	}	
	
	/*
	 * Traverse reachable methods, and for each method:
	 *   * build read/write sets for each allocation site
	 *   * query pairs of elements (one from read, one from write OR
	 *     both from write) to see whether they may alias. if they do
	 *     then there may be data races between them.
	 *   * prune: must alias analysis on locks (optional)
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void internalTransform(String phaseName, Map options) {
		Util.totalAnalysisTime = System.currentTimeMillis();

//		int numAppCls = Scene.v().getApplicationClasses().size();
//		int numLibCls = Scene.v().getLibraryClasses().size();
//		System.out.printf("numAppCls: %d, numLibCls: %d\n", numAppCls, numLibCls);
		
		if (Util.MEASURE_SPARK) {
			CallGraph cg = Scene.v().getCallGraph();
			QueueReader<Edge> edges = cg.listener();
			HashSet<SootMethod> libMtds = new HashSet<SootMethod>();
			HashSet<SootMethod> appMtds = new HashSet<SootMethod>();
			while (edges.hasNext()) {
				Edge e = edges.next();
				SootMethod src = e.getSrc().method();
				
				if (src.getDeclaringClass().isLibraryClass()) {
					libMtds.add(src);
				} else {
					appMtds.add(src);
				}
				
				SootMethod tgt = e.getTgt().method();
				if (tgt.getDeclaringClass().isLibraryClass()) {
					libMtds.add(tgt);
				} else {
					appMtds.add(tgt);
				}
			}
			
			int lib = libMtds.size();
			int total = lib + appMtds.size();
			float ratio = (float) lib / (float) total;
			System.out.printf("[6e9waprU] %s & %d & %d (%f\\%%)\n", Util.BENCH_NAME, total, lib, ratio * 100);
			
			System.exit(0);
		}
		
		if (Util.TEMP_HACK) {
			int numAppMtds = 0;
			int numLibMtds = 0;
			ReachableMethods methods = Scene.v().getReachableMethods();
			
			QueueReader<MethodOrMethodContext> r = methods.listener();
			while (r.hasNext()) {
				SootMethod method = (SootMethod) r.next();
				// skip non-concrete methods
				if (!method.isConcrete()) {
					continue;
				}
				if (method.getDeclaringClass().isApplicationClass()) {
					numAppMtds++;
				} else {
					numLibMtds++;
				}
			}
			String prefix = "[6e9waprU." + Util.BENCH_NAME + "." + Util.MAY_ALIAS + (Util.USE_SUMMARY ? ".summ" : ".nosumm") + "] ";
			System.out.printf("%snumAppMtds: %d, numLibMtds: %d, libRatio: %f\n",
					prefix, numAppMtds, numLibMtds, (double) numLibMtds / (double)(numLibMtds + numAppMtds));
			System.exit(0);
		}
		
		for (SootClass sc : Scene.v().getApplicationClasses()) {
//		for (SootClass sc : Scene.v().getClasses()) {
//			boolean isApp = sc.isApplicationClass();
			for (SootMethod mtd : sc.getMethods()) {
				if (!mtd.isConcrete()) {
					continue;
				}
				
//				Util.tweakBody(mtd);
				
				processMethod(mtd);
			}
		}

		MayAliasAnalysis maa = Util.getMayAliasAnalysis();		
		if (Util.SOAP12) {
			if (Util.USE_CACHE) {
				throw new RuntimeException("SOAP12 should not use caching!!!");
			}
			raceDetection(instanceFieldWrites, instanceFieldReads, maa);
			raceDetection(arrayElementWrites, arrayElementReads, maa);
		}
		if (Util.USE_SUMMARY && Util.MEASURE_CALLBACK) {
			int libSummed = Summary.libSummed.size();
			int libCallbacks = Summary.libCallbacks.size();
			double ratio = ((double) libCallbacks / (double) libSummed);
			
			// traverse call graph
			int cbs = 0;
			CallGraph cg = Scene.v().getCallGraph();
			for (SootMethod m : Summary.libSummed) {
				Iterator<Edge> outEdges = cg.edgesOutOf(m);
				while (outEdges.hasNext()) {
					Edge e = outEdges.next();
					if (e.tgt().getDeclaringClass().isApplicationClass()) {
						cbs++;
						break;
					}
				}
			}
			
			String prefix = "[6e9waprU." + Util.BENCH_NAME + "." + Util.MAY_ALIAS + "] ";
			System.out.println(prefix + "libSummed: " + libSummed + ", libCallbacks: " + libCallbacks
					+ ", ratio: " + ratio + ", cg_cbs: " + cbs);
			System.out.println("=== START DUMP CALLBACKS");
			for (SootMethod sm : Summary.libCallbacks) {
				System.out.println(sm);
			}
			System.out.println("=== END DUMP CALLBACKS");
			System.exit(0);
		}
		
		Util.mayAliasTime = 0;
		numRaces = 0;
		Util.sparkFalsePairs = 0;
//		Util.numAppMtds = 0;
//		Util.numLibMtds = 0;
		long start = System.currentTimeMillis();
		
		raceDetection(instanceFieldWrites, instanceFieldReads, maa);
		raceDetection(arrayElementWrites, arrayElementReads, maa);
		long end = System.currentTimeMillis();
		
		Util.totalAnalysisTime = System.currentTimeMillis() - Util.totalAnalysisTime;
		int queries = instanceFieldWrites.size() * (instanceFieldWrites.size() + instanceFieldReads.size()) +
			arrayElementWrites.size() * (arrayElementWrites.size() + arrayElementReads.size());
		String prefix = "[6e9waprU." + Util.BENCH_NAME + "." + Util.MAY_ALIAS + (Util.USE_SUMMARY ? ".summ" : ".nosumm") + "] ";
		System.out.println(prefix + "Total Time: " + (end - start) + ", Time: " + (Util.mayAliasTime / 1000000) + " ms, #SparkFalse: " + Util.sparkFalsePairs + ", #Aliases: " + numRaces + ", #Queries: " + queries);
//		System.out.printf("[END] numAppMtds: %d, numLibMtds: %d\n", Util.numAppMtds, Util.numLibMtds);
		System.exit(-1);
	}
	
	private void raceDetection(Map<SootField, HashSet<Pair<DefinitionStmt, SootMethod>>> writes,
		Map<SootField, HashSet<Pair<DefinitionStmt, SootMethod>>> reads, MayAliasAnalysis maa) {		
		
		for (Map.Entry<SootField, HashSet<Pair<DefinitionStmt, SootMethod>>> entry : writes.entrySet()) {
			SootField writeFld = entry.getKey();
			HashSet<Pair<DefinitionStmt, SootMethod>> readSet = reads.get(writeFld);
			HashSet<Pair<DefinitionStmt, SootMethod>> writeSet = entry.getValue();
			
			for (Pair<DefinitionStmt, SootMethod> p1 : writeSet) {
				DefinitionStmt s1 = p1.getO1();
				SootMethod m1 = p1.getO2();
				
				InstanceFieldRef f1 = (InstanceFieldRef) s1.getLeftOp();
				Local l1 = (Local) f1.getBase();
				
				for (Pair<DefinitionStmt, SootMethod> p2 : writeSet) {
					DefinitionStmt s2 = p2.getO1();
					SootMethod m2 = p2.getO2();
					if (s1 == s2) {
						numRaces++;
						continue;
					}
									
					InstanceFieldRef f2 = (InstanceFieldRef) s2.getLeftOp();					
					Local l2 = (Local) f2.getBase();
					boolean res = Util.mayAlias(l1, m1, l2, m2, maa);
					if (Util.DEBUG && Util.MAY_ALIAS.equals("spa")) {
						boolean manu = ManuMayAliasAnalysis.v().mayAlias(l1, m1, l2, m2);
						if (res != manu) {
							if (manu) mTrue++;
							else mFalse++;
							
							System.out.println("[diff] manu: " + manu + ", " + s1 + "@" + m1 + ", " + s2 + "@" + m2);
							System.out.println("--- " + m1);
							CallGraph cg = Scene.v().getCallGraph();
							Iterator<Edge> edges = cg.edgesInto(m1);
							while (edges.hasNext()) {
								System.out.println("  " + edges.next().src());
							}
							System.out.println("--- " + m2);
							edges = cg.edgesInto(m2);
							while (edges.hasNext()) {
								System.out.println("  " + edges.next().src());
							}							
						}
					}
					if (res) {
//						if (DEBUG) {
//							System.out.println("[race] " + s1 + "@" + m1 + ", " + s2 + "@" + m2);
//														
//						}
						numRaces++;
					}
				}
				
				if (readSet == null) continue;
				
				for (Pair<DefinitionStmt, SootMethod> p2 : readSet) {
					DefinitionStmt s2 = p2.getO1();
					SootMethod m2 = p2.getO2();
					InstanceFieldRef f2 = (InstanceFieldRef) s2.getRightOp();
					Local l2 = (Local) f2.getBase();
					boolean res = Util.mayAlias(l1, m1, l2, m2, maa);
					if (Util.DEBUG && Util.MAY_ALIAS.equals("spa")) {
						boolean manu = ManuMayAliasAnalysis.v().mayAlias(l1, m1, l2, m2);
						if (res != manu) {
							if (manu) mTrue++;
							else mFalse++;

							System.out.println("[diff] manu: " + manu + ", " + s1 + "@" + m1 + ", " + s2 + "@" + m2);
							System.out.println("--- " + m1);
							CallGraph cg = Scene.v().getCallGraph();
							Iterator<Edge> edges = cg.edgesInto(m1);
							while (edges.hasNext()) {
								System.out.println("  " + edges.next().src());
							}
							System.out.println("--- " + m2);
							edges = cg.edgesInto(m2);
							while (edges.hasNext()) {
								System.out.println("  " + edges.next().src());
							}	
						}
					}
					if (res) {
//						if (DEBUG) {
//							System.out.println("[race] " + s1 + "@" + m1 + ", " + s2 + "@" + m2);
//						}
						numRaces++;
					}
				}
			}
		}
	}
	
	private void addElement(Map<SootField, HashSet<Pair<DefinitionStmt, SootMethod>>> map, SootField fld,
		DefinitionStmt ds, SootMethod mtd) {
		
		HashSet<Pair<DefinitionStmt, SootMethod>> set = map.get(fld);
		if (set == null) {
			set = new HashSet<Pair<DefinitionStmt, SootMethod>>();
			map.put(fld, set);
		}
		
		set.add(new Pair<DefinitionStmt, SootMethod>(ds, mtd));
	}
	
	private boolean hasExceptionBase(InstanceFieldRef r, SootClass throwable) {
		Type type = r.getBase().getType();
		if (type instanceof RefType) {
			RefType rt = (RefType) type;
			SootClass baseClass = Scene.v().getSootClass(rt.getClassName());
			return (Util.subclassOf(baseClass, throwable));				
		}
		return false;
	}
	@SuppressWarnings("unchecked")
	private void processMethod(SootMethod mtd) {
		if (!mtd.hasActiveBody()) {
			mtd.retrieveActiveBody();
		}
		
		SootClass throwable = Scene.v().getSootClass("java.lang.Throwable");

		for (Iterator stmts = mtd.getActiveBody().getUnits().iterator(); stmts.hasNext();) {
			Stmt st = (Stmt) stmts.next();
			if (!(st.containsFieldRef() && st instanceof DefinitionStmt)) {
				continue;
			}
			
			DefinitionStmt ds = (DefinitionStmt) st;
			Value lhs = ds.getLeftOp();
			Value rhs = ds.getRightOp();

			if (lhs instanceof FieldRef) {	// write				
				SootField fld = ((FieldRef) lhs).getField();				

				if (lhs instanceof InstanceFieldRef) {
					if (hasExceptionBase((InstanceFieldRef)lhs, throwable)) {
						continue;
					}

					if (lhs instanceof ArrayFieldRef) {
						addElement(arrayElementWrites, fld, ds, mtd);						
					} else {
						addElement(instanceFieldWrites, fld, ds, mtd);						
					}					
				} else {  // StaticFieldRef
//					addElement(staticFieldWrites, fld, ds, mtd);
				}
			} else {	// rhs instanceof FieldRef
				SootField fld = ((FieldRef) rhs).getField();

				if (rhs instanceof InstanceFieldRef) {
					if (hasExceptionBase((InstanceFieldRef)rhs, throwable)) {
						continue;
					}					
					if (rhs instanceof ArrayFieldRef) {
						addElement(arrayElementReads, fld, ds, mtd);
					} else {
						addElement(instanceFieldReads, fld, ds, mtd);						
					}										
				} else {  // StaticFieldRef
//					addElement(staticFieldReads, fld, ds, mtd);
				}
			}
		}
	}
}
