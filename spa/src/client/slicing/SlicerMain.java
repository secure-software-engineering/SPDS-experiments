package client.slicing;

import iohoister.analysis.DefUseAnalysis;
import iohoister.analysis.DefUseEdge;
import iohoister.analysis.MayAliasAnalysis;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import alias.Summary;
import alias.Util;

import soot.Local;
import soot.MethodOrMethodContext;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootMethod;
import soot.Transform;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.toolkits.scalar.Pair;
import soot.util.queue.QueueReader;

public class SlicerMain extends SceneTransformer {
	private boolean inLibrary(SootMethod m) {
		String p = m.getDeclaringClass().getJavaPackageName();
		return p.startsWith("java.") || p.startsWith("sun.")
				|| p.startsWith("sunw.") || p.startsWith("javax.")
				|| p.startsWith("org.") || p.startsWith("com.");
	}

	@SuppressWarnings("unchecked")
	protected void internalTransform(String arg0, Map arg1) {
		System.out.println("[slicer] slicer starts running...");

		long start = System.currentTimeMillis(); 
		ReachableMethods methods = Scene.v().getReachableMethods();

		QueueReader<MethodOrMethodContext> r = methods.listener();
		while (r.hasNext()) {
			SootMethod mtd = (SootMethod) r.next();
			if (mtd.isConcrete()) {
				Util.tweakBody(mtd);
			}				
		}
		
		MayAliasAnalysis maa = Util.getMayAliasAnalysis();
		
		long maxTimePerMethod = 0;
		String maxMtd = "";
		
		r = methods.listener();
		while (r.hasNext()) {
			long duration = System.currentTimeMillis();
			SootMethod method = (SootMethod) r.next();
			if (!method.isConcrete() || inLibrary(method))
				continue;
			DefUseAnalysis dua = DefUseAnalysis.v(method, Scene.v().getPointsToAnalysis(), maa);
			dua.computeDataDependence();
			duration = (System.currentTimeMillis() - duration);
			if (duration > maxTimePerMethod) {
				maxTimePerMethod = duration;
				maxMtd = method.getSignature();
			}
		}
		
		System.out.println("[TEcH8f5e." + Util.BENCH_NAME + "." + Util.MAY_ALIAS + "." + Util.MANU_MAX_TRAVERSAL +
			"_" + Util.MANU_MAX_PASSES + "] maxTimePerMethod: "  + maxTimePerMethod + "ms, " + maxMtd);
		System.out.println("[TEcH8f5e." + Util.BENCH_NAME + "." + Util.MAY_ALIAS + "." + Util.MANU_MAX_TRAVERSAL +
				"_" + Util.MANU_MAX_PASSES + "] maxAliasTime: "  + Util.maxAliasTime + "ms, " + Util.maxAliasPair);

		long defUseTime = (System.currentTimeMillis() - start);
		System.out.println("[6e9waprU." + Util.BENCH_NAME + "."  + Util.MAY_ALIAS + "." + Util.MANU_MAX_TRAVERSAL +
			"_" + Util.MANU_MAX_PASSES + "] mayAlias: " + DefUseAnalysis.mayAliasTime + "ms, total: " +
			defUseTime + "ms, #defUseEdges: " + DefUseAnalysis.numberOfEdges() +
			", ratio: " + ((double)DefUseAnalysis.mayAliasTime) / ((double)defUseTime) * 100.00 + "%.");

//		if (Util.MEASURE_PRECISION) {
//			System.out.println("[bust32Ec." + Util.MAY_ALIAS + "." + Util.BENCH_NAME + "] " + DefUseAnalysis.mayAliasTime +
//					"#" + defUseTime);
//				System.out.println("[6petH9st." + Util.MAY_ALIAS + "." + Util.BENCH_NAME + "] " + Summary.totalComputeTime + ", " +
//						((double)Summary.totalComputeTime) / ((double)DefUseAnalysis.mayAliasTime) * 100.00 + "%.");
//		}

		if (Util.DUMP_USE_DEF) {
			Map<Stmt, Set<DefUseEdge>> stmtDep = DefUseAnalysis
			.getStmtDataDependence();
			dumpDataDeps(stmtDep);
		}
		
		if (Util.MEASURE_PRECISION) {
			dumpAliasQueries();
			System.out.println("[6aGuchuG." + Util.MAY_ALIAS + "." + Util.BENCH_NAME + "] numberOfEdges,numberOfRevEdges,registeredEdges,duplicateEdges,numFoundEntry,numDefUseEdgeCreated"); 
			System.out.println("[6aGuchuG." + Util.MAY_ALIAS + "." + Util.BENCH_NAME + "]" + DefUseAnalysis.numberOfEdges() +
					", " + DefUseAnalysis.numberOfRevEdges() + ", " + DefUseAnalysis.registeredEdges + ", " + DefUseAnalysis.duplicateEdges + ", " + DefUseAnalysis.numFoundEntry + ", " + DefUseAnalysis.numDefUseEdgeCreated);
		}
				
		System.exit(-1);
	}
	
	private void dumpAliasQueries() {		
		for (Map.Entry<SootMethod, DefUseAnalysis> entry : DefUseAnalysis.instances.entrySet()) {
			SootMethod mtd = entry.getKey();			
			String prefix = "[mE23chet." + Util.MAY_ALIAS + "." + Util.BENCH_NAME + "] " + mtd.getSignature() + "!";
			DefUseAnalysis dua = entry.getValue();
			for (Map.Entry<Pair<Local, Local>, Integer> ent : dua.resultCache.entrySet()) {
				Pair<Local, Local> p = ent.getKey();
				Integer res = ent.getValue();
				Local v1 = p.getO1();
				Local v2 = p.getO2();
				System.out.println(prefix + v1.getName() + "!" + v2.getName() + "!" + res);
			}
		}
	}
	
	private void dumpDataDeps(Map<Stmt, Set<DefUseEdge>> stmtDep) {
		System.out.println("\n=== def use dump");
		for (Iterator<Map.Entry<Stmt, Set<DefUseEdge>>> iter = stmtDep
				.entrySet().iterator(); iter.hasNext();) {
			Map.Entry<Stmt, Set<DefUseEdge>> entry = iter.next();
			Stmt stmt = entry.getKey();
			System.out.println("[due." + Util.MAY_ALIAS + "] stmt: " + stmt);
			Set<DefUseEdge> defUseEdges = entry.getValue();
			for (DefUseEdge due : defUseEdges) {
				System.out.println("[due." + Util.MAY_ALIAS + "]  def: " + due.getDef() + "@" + due.getDefMethod() + ", use: " + due.getUse() + "@" + due.getUseMethod());
			}
			System.out.println();
		}
		System.out.println("\n===");
	}

	private static SlicerMain slicer;

	public static SlicerMain v() {
		if (slicer == null) {
			slicer = new SlicerMain();
		}
		return slicer;
	}
	
	private SlicerMain() {	
	}

	/**
	 * 
	 * @param liargs
	 *            liargs[0]: JDK classes directory; liargs[1]: the directory
	 *            containing the class files to process; liargs[2]: main class
	 */
	public static void main(String[] liargs) {

		SlicerMain pa = SlicerMain.v();

		String phaseName = "wjtp.slicer";
		Transform t = new Transform(phaseName, pa);
		PackManager.v().getPack("wjtp").add(t);

		boolean isPaddle = Util.MAY_ALIAS.equals("paddle");
		String[] args = { "-W",
				"-p",
				"wjop.si",
				"enabled:false",
				"-p",
				phaseName,
				"enabled:true",
				"-p", (isPaddle ? "cg.paddle" : "cg.spark"), "enabled:true" +
				(isPaddle ? ",bdd:true,backend:buddy,context:objsens" : ""),
				"-cp", liargs[0] + java.io.File.pathSeparator + liargs[1], "-f", "n",
				"-allow-phantom-refs", "-process-dir", liargs[1],
				"-main-class", liargs[2], liargs[2], };

		for (int i = 0; i < args.length; i++) {
			System.out.print(args[i] + " ");
		}
		System.out.println();

		soot.Main.main(args);
	}

}
