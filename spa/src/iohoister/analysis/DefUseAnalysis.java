package iohoister.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import alias.ManuMayAliasAnalysis;
import alias.Util;

import iohoister.analysis.lattice.AllocLatticeElement;
import iohoister.analysis.lattice.BottomLatticeElement;
import iohoister.analysis.lattice.FieldRefLatticeElement;
import iohoister.analysis.lattice.LatticeElement;
import iohoister.analysis.lattice.ParamLatticeElement;
import iohoister.analysis.lattice.ReturnLatticeElement;

import soot.Local;
import soot.PointsToAnalysis;
import soot.PointsToSet;
import soot.PrimType;
import soot.Scene;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.ClassConstant;
import soot.jimple.Constant;
import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.IdentityRef;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.ParameterRef;
import soot.jimple.ReturnStmt;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.ThisRef;
import soot.jimple.internal.JimpleLocalBox;
import soot.jimple.spark.pag.ArrayElement;
import soot.jimple.spark.pag.SparkField;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.scalar.Pair;
import soot.util.ArraySet;

public class DefUseAnalysis {

	private SootMethod method;

	private boolean mustAliasRun = false;

	private Set<Stmt> loadStmts = new ArraySet<Stmt>();
	private Set<Stmt> storeStmts = new ArraySet<Stmt>();
	private Set<Stmt> callStmts = new ArraySet<Stmt>();

	private static Map<Local, PointsToSet> basePTCache = new HashMap<Local, PointsToSet>();

	private static Map<SootMethod, ExceptionalUnitGraph> unitGraphs = new HashMap<SootMethod, ExceptionalUnitGraph>();
	// private Set<Stmt> dataSources = new HashSet<Stmt>();

	// ==== data dependence ========
	private Map<Local, Set<Local>> dataDep = new HashMap<Local, Set<Local>>();

	private static Map<Stmt, Set<DefUseEdge>> stmtDep = new HashMap<Stmt, Set<DefUseEdge>>();

	private static Map<Stmt, Set<DefUseEdge>> stmtRevDep = new HashMap<Stmt, Set<DefUseEdge>>();

	// private Map<Stmt, Set<DefUseEdge>> inverseStmtDep = new HashMap<Stmt,
	// Set<DefUseEdge>>();

	// private Set<Local> usefulData = new HashSet<Local>();

	private Map<Stmt, Local> call2FakeLocalMap = new HashMap<Stmt, Local>();

	private Map<Stmt, Map<Local, Set<Local>>> localMustAlias;

	private Set<Stmt> fieldsIn = new ArraySet<Stmt>();

	private Set<Stmt> fieldsOut = new ArraySet<Stmt>();

	private Set<Stmt> returnSummary = new ArraySet<Stmt>();

	private Map<Stmt, Set<Stmt>> fieldSummary = new HashMap<Stmt, Set<Stmt>>();

	private PointsToAnalysis pointsTo;
	
	private MayAliasAnalysis maa;

	private boolean dependenceComputed = false;

	private Map<Local, Set<Stmt>> argDefs = new HashMap<Local, Set<Stmt>>();

	private Set<Stmt> returns = new ArraySet<Stmt>();

	//=== profiling
	
	public static long mayAliasTime = 0;
	
	public HashMap<Pair<Local, Local>, Integer> resultCache;
	
	public static long numFoundEntry = 0;
	
	public static long numDefUseEdgeCreated = 0;
	
	public static long registeredEdges = 0;
	
	public static long duplicateEdges = 0;
	
	//---
	
	public static Map<SootMethod, DefUseAnalysis> instances = new HashMap<SootMethod, DefUseAnalysis>();
	
	public static DefUseAnalysis v(SootMethod m, PointsToAnalysis pta) {
		return v(m, pta, DefaultMayAliasAnalysis.v(pta));
	}

	public static DefUseAnalysis v(SootMethod m, PointsToAnalysis pta, MayAliasAnalysis maa) {
		DefUseAnalysis la = instances.get(m);
		if (la == null) {
			la = new DefUseAnalysis(m, pta, maa);
			instances.put(m, la);
		}
		return la;
	}
	
	public static int numberOfEdges() {
		int res = 0;
		for (Map.Entry<Stmt, Set<DefUseEdge>> entry : stmtDep.entrySet()) {
			res += entry.getValue().size();
		}
		
		return res;
	}
	
	public static int numberOfRevEdges() {
		int res = 0;
		for (Map.Entry<Stmt, Set<DefUseEdge>> entry : stmtRevDep.entrySet()) {
			res += entry.getValue().size();
		}
		
		return res;
	}

	@SuppressWarnings("unchecked")
	public static Set<DefUseEdge> getStmtDependence(Stmt st) {
		Set<DefUseEdge> s = stmtDep.get(st);
		if (s == null)
			return Collections.EMPTY_SET;
		return s;
	}

	@SuppressWarnings("unchecked")
	public static Set<DefUseEdge> getStmtInvDependence(Stmt tgt) {
		Set<DefUseEdge> s = stmtRevDep.get(tgt);
		if (s == null)
			return Collections.EMPTY_SET;
		return s;
	}

	public static void registerEdge(Stmt src, DefUseEdge e) {
		registeredEdges++;
		Set<DefUseEdge> s = stmtDep.get(src);
		if (s == null) {
			s = new ArraySet<DefUseEdge>();
			stmtDep.put(src, s);
		}
		if (s.contains(e)) {
			duplicateEdges++;
		}
		s.add(e);

		s = stmtRevDep.get(e.getDef());
		if (s == null) {
			s = new ArraySet<DefUseEdge>();
			stmtRevDep.put(e.getDef(), s);
		}
		s.add(e);
	}

	public static PointsToSet getPTSet(Local l, PointsToAnalysis pta) {
		PointsToSet pts = basePTCache.get(l);
		if (pts == null) {
			pts = pta.reachingObjects(l);
			basePTCache.put(l, pts);
		}
		return pts;
	}

	private DefUseAnalysis(SootMethod method, PointsToAnalysis pta, MayAliasAnalysis maa) {
		this.method = method;
		this.pointsTo = pta;
		this.maa = maa;
		if (!method.hasActiveBody())
			method.retrieveActiveBody();
	
		if (Util.MEASURE_PRECISION) {
			resultCache = new HashMap<Pair<Local, Local>, Integer>();
		}
	}

	public static Map<Stmt, Set<DefUseEdge>> getStmtDataDependence() {
		return stmtDep;
	}

	public Set<Stmt> getReturnStmts() {
		return returns;
	}

	public Set<Stmt> getFieldsIn() {
		return fieldsIn;
	}

	public Set<Stmt> getFieldsOut() {
		return fieldsOut;
	}

	@SuppressWarnings("unchecked")
	public Set<Stmt> getSummaryForStores(Stmt store) {
		Set<Stmt> ret = fieldSummary.get(store);
		if (ret == null)
			ret = Collections.EMPTY_SET;
		return ret;
	}

	public Set<Stmt> getDefForArg(Local l) {
		return argDefs.get(l);
	}

	public Set<Stmt> getSummaryForReturn() {
		return returnSummary;
	}

	public Set<Stmt> getFieldsOut(PointsToSet pts, SparkField f) {
		Set<Stmt> ret = new ArraySet<Stmt>();
		for (Stmt st : fieldsOut) {
			Local base;
			if (st.containsFieldRef()) {
				if ((InstanceFieldRef) st.getFieldRef().getField() != f)
					continue;
				base = (Local) ((InstanceFieldRef) st.getFieldRef()).getBase();
			} else if (st.containsArrayRef()) {
				if (f != ArrayElement.v())
					continue;
				base = (Local) ((ArrayRef) st.getArrayRef()).getBase();
			} else {
				throw new RuntimeException("Wield Field Ref " + st);
			}

			PointsToSet basePT = basePTCache.get(base);
			if (basePT == null) {
				basePT = pointsTo.reachingObjects(base);
				basePTCache.put(base, basePT);
			}
			if (basePT.hasNonEmptyIntersection(pts))
				ret.add(st);
		}
		return ret;
	}

	public boolean isDependenceComputed() {
		return dependenceComputed;
	}

	@SuppressWarnings("unchecked")
	public void computeDataDependence() {
		if (!dependenceComputed) {
			dependenceComputed = true;
		} else {
			return;
		}

		Map<Stmt, Map<Local, Set<Stmt>>> defInMap = new HashMap<Stmt, Map<Local, Set<Stmt>>>();
		Map<Stmt, Map<Local, Set<Stmt>>> defOutMap = new HashMap<Stmt, Map<Local, Set<Stmt>>>();

		Map<Stmt, Map<FieldRef, Set<Stmt>>> defInMapforFields = new HashMap<Stmt, Map<FieldRef, Set<Stmt>>>();
		Map<Stmt, Map<FieldRef, Set<Stmt>>> defOutMapforFields = new HashMap<Stmt, Map<FieldRef, Set<Stmt>>>();

		ExceptionalUnitGraph g = new ExceptionalUnitGraph(method
				.getActiveBody());

		List stmts = new ArrayList();
		stmts.addAll(g.getHeads());

		Stmt lastStmt = BottomStmt.v();

		// in the very beginning, let us add a fake write to all the heap
		// locations referenced in the method
		Map<FieldRef, Set<Stmt>> fakeDefforFields = new HashMap<FieldRef, Set<Stmt>>();
		defOutMapforFields.put(lastStmt, fakeDefforFields);

		Map<Local, Set<Stmt>> fakeDef = new HashMap<Local, Set<Stmt>>();
		defOutMap.put(lastStmt, fakeDef);

		Set<Local> paramLocals = new ArraySet<Local>();
		try {
			for (Iterator<Unit> i = g.iterator(); i.hasNext();) {
				Stmt stmt = (Stmt) i.next();
				if (stmt.containsFieldRef()) {
					if (stmt instanceof DefinitionStmt) {
						Value lhs = ((DefinitionStmt) stmt).getLeftOp();
						if (lhs instanceof FieldRef)
							storeStmts.add(stmt);
						else {
							loadStmts.add(stmt);
							FieldRef fr = stmt.getFieldRef();
							Set<Stmt> s = new ArraySet<Stmt>();
							s.add(lastStmt);
							fakeDefforFields.put(fr, s);
						}
					}
				}
				if (stmt.containsInvokeExpr()) {
					callStmts.add(stmt);
				}
				if (stmt instanceof DefinitionStmt) {
					Value rhs = ((DefinitionStmt) stmt).getRightOp();
					Value lhs = ((DefinitionStmt) stmt).getLeftOp();
					if (rhs instanceof ParameterRef || rhs instanceof ThisRef) {
						paramLocals.add((Local) lhs);
					}
				}
				if (stmt instanceof ReturnStmt) {
					returns.add(stmt);
				}

			}

			// ===== local must alias analysis =======
			mustAliasAnalysis(g);

			Set<Stmt> visited = new ArraySet<Stmt>();

			// ====== data dependence construction using def-use analysis ====
			while (stmts.size() > 0) {
				Stmt st = (Stmt) stmts.get(0);
				stmts.remove(0);
				Map<Local, Set<Stmt>> defInCurr;
				Map<FieldRef, Set<Stmt>> defInCurrforFields;

				// 0. merge reach-ins at control flow join points
				defInCurr = (Map<Local, Set<Stmt>>) defInMap.get(st);
				if (defInCurr == null) {
					defInCurr = new HashMap<Local, Set<Stmt>>();
					defInMap.put(st, defInCurr);
				}

				defInCurrforFields = (Map<FieldRef, Set<Stmt>>) defInMapforFields
						.get(st);
				if (defInCurrforFields == null) {
					defInCurrforFields = new HashMap<FieldRef, Set<Stmt>>();
					defInMapforFields.put(st, defInCurrforFields);
				}

				List prevStmts = g.getPredsOf(st);
				if (prevStmts.size() == 0) {
					prevStmts = new ArrayList();
					prevStmts.add(BottomStmt.v());
				}

				boolean changed = false;
				for (int j = 0; j < prevStmts.size(); j++) {
					lastStmt = (Stmt) prevStmts.get(j);
					Map<Local, Set<Stmt>> defOutPrev = defOutMap.get(lastStmt);
					Map<FieldRef, Set<Stmt>> defOutPrevforFields = defOutMapforFields
							.get(lastStmt);
					if (mergeReachingDef(st, defOutPrev, defInCurr,
							defOutPrevforFields, defInCurrforFields,
							localMustAlias)) {
						changed = true;
					}
				}

				if (!changed && visited.contains(st))
					continue;

				visited.add(st);
				// create defOutCurr by cloning defInCurr
				defOutMap.remove(st);
				Map<Local, Set<Stmt>> defOutCurr = cloneMap(defInCurr);
				defOutMap.put(st, defOutCurr);
				defOutMapforFields.remove(st);
				Map<FieldRef, Set<Stmt>> defOutCurrforFields = cloneMap(defInCurrforFields);
				defOutMapforFields.put(st, defOutCurrforFields);
				stmts.addAll(g.getSuccsOf(st));

				// 2. let's deal with call site
				if (st.containsInvokeExpr()) {
					for (Iterator<Edge> it = Scene.v().getCallGraph()
							.edgesOutOf(st); it.hasNext();) {
						Edge e = it.next();
						if (e.srcStmt() == null)
							continue;
						if (!st.getInvokeExpr().getMethod().getSubSignature()
								.equals(e.tgt().getSubSignature()))
							continue;

						if (st.getInvokeExpr().getMethod().isAbstract())
							continue;

						// if (st
						// .toString()
						// .equals(
						// "$r7 = interfaceinvoke r1.<java.util.Map: java.util.Set keySet()>()"))
						// {
						// System.out.println("");
						// }

						plugInProceduralSummary(st, e, defInCurr,
								defInCurrforFields, defOutCurr,
								defOutCurrforFields);

					}
				} else {
					dealwithStmt(st, st.getUseBoxes(), st.getDefBoxes(),
							defInCurr, defInCurrforFields, defOutCurr,
							defOutCurrforFields, false, false, null, true);
				}

			}
		} catch (OutOfMemoryError er) {
			System.err
					.println("OutOfMemory occurs when building dependence graph for method "
							+ method);
			throw er;
		}

		// buildDataDependenceSummary();
	}

	private void plugInProceduralSummary(Stmt call, Edge callEdge,
			Map<Local, Set<Stmt>> defInCurr,
			Map<FieldRef, Set<Stmt>> defInCurrforFields,
			Map<Local, Set<Stmt>> defOutCurr,
			Map<FieldRef, Set<Stmt>> defOutCurrforFields) {

		SootMethod tgt = callEdge.tgt();

		if (tgt.isNative() && call instanceof AssignStmt) {
			Value v = ((AssignStmt) call).getLeftOp();
			assert v instanceof Local;
			ValueBox defBox = new JimpleLocalBox((Local) v);

			InvokeExpr ie = call.getInvokeExpr();
			List<ValueBox> argList = new ArrayList<ValueBox>();
			for (int i = 0; i < ie.getArgCount(); i++) {
				Value a = ie.getArg(i);
				if (a instanceof Local) {
					argList.add(new JimpleLocalBox((Local) a));
				}
			}

			if (ie instanceof InstanceInvokeExpr) {
				argList.add(new JimpleLocalBox(
						(Local) ((InstanceInvokeExpr) ie).getBase()));
			}

			dealwithStmt(call, argList, Collections.singletonList(defBox),
					defInCurr, defInCurrforFields, defOutCurr,
					defOutCurrforFields, false, false, null, true);

		}

		if (!tgt.isConcrete())
			return;
		DefUseAnalysis callee = DefUseAnalysis.v(tgt, pointsTo, maa);

		// if (!callee.isDependenceComputed())
		// callee.computeDataDependence();
		// first, let us connect formal and actual parameters
		for (Iterator<Unit> stmts = tgt.getActiveBody().getUnits().iterator(); stmts
				.hasNext();) {
			Stmt identityStmt = (Stmt) stmts.next();
			if (identityStmt instanceof IdentityStmt) {
				Value rhs = ((IdentityStmt) identityStmt).getRightOp();
				if (rhs instanceof ThisRef) {
					Local base = (Local) ((InstanceInvokeExpr) call
							.getInvokeExpr()).getBase();
					ValueBox vb = new JimpleLocalBox(base);
					dealwithStmt(identityStmt, Collections.singletonList(vb),
							Collections.EMPTY_LIST, defInCurr,
							defInCurrforFields, defOutCurr,
							defOutCurrforFields, true, false, callEdge, true);

					Set<Stmt> argDef = argDefs.get(base);
					if (argDef == null) {
						argDef = new ArraySet<Stmt>();
						argDefs.put(base, argDef);
					}
					Set<Stmt> defs = defInCurr.get(base);
					if (defs != null)
						for (Stmt de : defs)
							argDef.add(de);

				} else if (rhs instanceof ParameterRef) {
					int index = ((ParameterRef) rhs).getIndex();
					Value arg = call.getInvokeExpr().getArg(index);
					if (arg instanceof Local) {
						ValueBox vb = new JimpleLocalBox(arg);
						dealwithStmt(identityStmt, Collections
								.singletonList(vb), Collections.EMPTY_LIST,
								defInCurr, defInCurrforFields, defOutCurr,
								defOutCurrforFields, true, false, callEdge,
								true);

						Set<Stmt> argDef = argDefs.get((Local) arg);
						if (argDef == null) {
							argDef = new ArraySet<Stmt>();
							argDefs.put((Local) arg, argDef);
						}
						Set<Stmt> defs = defInCurr.get((Local) arg);
						if (defs != null) {
							for (Stmt st : defs)
								argDef.add(st);
						}

					}
				}
			} else
				break;
		}

		// second, let us connect returned value and return statements
		if (call instanceof AssignStmt) {
			Value v = ((AssignStmt) call).getLeftOp();
			Local intermediate = call2FakeLocalMap.get(call);
			if (intermediate == null) {
				intermediate = Jimple.v().newLocal(
						"fake$" + UniqueID.getUniqueID("call"), v.getType());
				call2FakeLocalMap.put(call, intermediate);
			}

			ValueBox defBox = new JimpleLocalBox(intermediate);
			Set<Stmt> ret = callee.getReturnStmts();
			if (ret.size() == 0) {
				for (Iterator<Unit> units = tgt.getActiveBody().getUnits()
						.iterator(); units.hasNext();) {
					Stmt t = (Stmt) units.next();
					if (t instanceof ReturnStmt)
						ret.add(t);
				}
			}

			for (Stmt t : ret) {
				dealwithStmt(t, Collections.EMPTY_LIST, Collections
						.singletonList(defBox), defInCurr, defInCurrforFields,
						defOutCurr, defOutCurrforFields, false, true, callEdge,
						false);
			}

			Map<Local, Set<Stmt>> mapIn = cloneMap(defOutCurr);

			defBox = new JimpleLocalBox((Local) v);
			ValueBox useBox = new JimpleLocalBox(intermediate);
			dealwithStmt(call, Collections.singletonList(useBox), Collections
					.singletonList(defBox), mapIn, defInCurrforFields,
					defOutCurr, defOutCurrforFields, false, true, callEdge,
					true);

		}

	}

	private void dealwithStmt(Stmt st, List<ValueBox> useBox,
			List<ValueBox> defBox, Map<Local, Set<Stmt>> defInCurr,
			Map<FieldRef, Set<Stmt>> defInCurrforFields,
			Map<Local, Set<Stmt>> defOutCurr,
			Map<FieldRef, Set<Stmt>> defOutCurrforFields, boolean isParam,
			boolean isReturn, Edge callEdge, boolean killEnabled) {
		// 1. build def-use relationships

		// Set<DefUseEdge> defStmts = stmtDep.get(st);

		for (ValueBox b : useBox) {
			Value v = b.getValue();
			if (v instanceof Local) {
				// if (paramLocals.contains(v)) {
				// usefulData.add((Local) v);
				// }

				Set<Stmt> defs = defInCurr.get(v);
				// assert defs != null;
				// if (defs == null) {
				// defs = new ArraySet<Stmt>();
				// defInCurr.put((Local) v, defs);
				// if (!defOutCurr.containsKey(v)) {
				// Set<Stmt> set = new ArraySet<Stmt>();
				// defOutCurr.put((Local) v, set);
				// }
				// }
				if (defs != null) {
					Set<Local> dataDepSet = dataDep.get(v);
					if (dataDepSet == null) {
						dataDepSet = new ArraySet<Local>();
						dataDep.put((Local) v, dataDepSet);
					}

					for (Stmt defSt : defs) {
						// if (defStmts == null) {
						// defStmts = new ArraySet<DefUseEdge>();
						// stmtDep.put(st, defStmts);
						// }
						if (!(defSt instanceof BottomStmt)) {

							Local base = null;
							if (st.containsFieldRef()
									&& st.getFieldRef() instanceof InstanceFieldRef) {
								base = (Local) ((InstanceFieldRef) st
										.getFieldRef()).getBase();
							} else if (st.containsArrayRef())
								base = (Local) ((ArrayRef) st.getArrayRef())
										.getBase();
							DefUseEdge e;

							if (isParam) {
								e = new DefUseEdge(st, defSt, method, callEdge
										.src());
								e.setIsParam(callEdge);
							} else if (isReturn) {
								e = new DefUseEdge(st, defSt, method, callEdge
										.tgt());
								e.setIsReturn(callEdge);
							} else {
								e = new DefUseEdge(st, defSt, method, method);
							}
							if (base == v) {
								e.setPointer();
							}
							registerEdge(st, e);
							// defStmts.add(e);

						}
						// build data dependence among variables
						if (!(defSt instanceof BottomStmt)) {
							for (ValueBox _b : defSt.getUseBoxes()) {
								if (_b instanceof Local)
									dataDepSet.add((Local) _b);
								else if (_b instanceof FieldRef) {
									dataDepSet.add(LocalWrapperforFieldRef
											.v((FieldRef) _b));
								}
							}
						}
					}
				}

			} else if (v instanceof InstanceFieldRef || v instanceof ArrayRef) {
				// Set<Stmt> defs = defInCurrforFields.get(v);
				if (v instanceof ArrayRef) {
					v = ArrayFieldRef.v((ArrayRef) v);
				}

				// deal with base pointer def use

				Local base = (Local) ((InstanceFieldRef) v).getBase();
				Set<Stmt> defs = defInCurr.get(base);
				// if(defs == null)
				// assert (defs != null);

				if (defs != null) {
					Set<Local> dataDepSet = dataDep.get(base);
					if (dataDepSet == null) {
						dataDepSet = new ArraySet<Local>();
						dataDep.put(base, dataDepSet);
					}

					for (Stmt defSt : defs) {
						// if (defStmts == null) {
						// defStmts = new ArraySet<DefUseEdge>();
						// stmtDep.put(st, defStmts);
						// }
						if (!(defSt instanceof BottomStmt)) {
							DefUseEdge e;
							if (isParam) {
								e = new DefUseEdge(st, defSt, method, callEdge
										.src());
								e.setIsParam(callEdge);
							} else if (isReturn) {
								e = new DefUseEdge(st, defSt, method, callEdge
										.tgt());
								e.setIsReturn(callEdge);
							} else {
								e = new DefUseEdge(st, defSt, method, method);
							}

							e.setPointer();
							registerEdge(st, e);
							// defStmts.add(e);

						}
						// build data dependence among variables
						if (!(defSt instanceof BottomStmt)) {
							for (ValueBox _b : defSt.getUseBoxes()) {
								if (_b instanceof Local)
									dataDepSet.add((Local) _b);
								else if (_b instanceof FieldRef) {
									dataDepSet.add(LocalWrapperforFieldRef
											.v((FieldRef) _b));
								}
							}
						}
					}
				}

				// deal with value def use
				for (Map.Entry<FieldRef, Set<Stmt>> entry : defInCurrforFields
						.entrySet()) {
					FieldRef rf = entry.getKey();
					defs = entry.getValue();

					boolean foundEntry = false;
					Local vBase = base;
					Local rfBase = null;
					if (rf.equals(v))
						foundEntry = true;
					else if (rf instanceof InstanceFieldRef
							&& v instanceof InstanceFieldRef) {

						rfBase = (Local) ((InstanceFieldRef) rf).getBase();

						// Here is a may-alias check
						if (rf instanceof ArrayFieldRef
								&& v instanceof ArrayFieldRef) {
							if (mayAlias(vBase, rfBase))
								foundEntry = true;
						} else if (!(rf instanceof ArrayFieldRef)
								&& !(v instanceof ArrayFieldRef)) {
							if (rf.getField().equals(((FieldRef) v).getField())
									&& mayAlias(vBase, rfBase)) {
								foundEntry = true;
							}
						}
					}
					if (foundEntry) {
						numFoundEntry++;
						Local wrapper = LocalWrapperforFieldRef.v(rf);
						Set<Local> s = dataDep.get(wrapper);
						if (s == null) {
							s = new ArraySet<Local>();
							dataDep.put(wrapper, s);
						}

						for (Stmt defSt : defs) {
							// if (defStmts == null) {
							// defStmts = new HashSet<DefUseEdge>();
							// stmtDep.put(st, defStmts);
							// }
							if (!(defSt instanceof BottomStmt)) {
								DefUseEdge e;
								if (isParam) {
									e = new DefUseEdge(st, defSt, method,
											callEdge.src());
									e.setIsParam(callEdge);
								} else if (isReturn) {
									e = new DefUseEdge(st, defSt, method,
											callEdge.tgt());
									e.setIsReturn(callEdge);
								} else {
									e = new DefUseEdge(st, defSt, method,
											method);
								}
								registerEdge(st, e);

							}
							// update variable dependence
							if (defSt instanceof BottomStmt) {
								// usefulData.add(wrapper);
								fieldsIn.add(st);
							} else
								for (ValueBox _b : defSt.getUseBoxes()) {
									if (_b.getValue() instanceof Local)
										s.add((Local) _b.getValue());
									else if (_b.getValue() instanceof FieldRef) {
										s.add(LocalWrapperforFieldRef
												.v((FieldRef) _b.getValue()));
									}
								}

						}
						if (v instanceof ArrayFieldRef) {
							fieldsIn.add(st);
						} else {
							if (s.size() == 0) {
								fieldsIn.add(st);
							} else {
								if (!rf.equals(v)) {
									if (vBase != null && rfBase != null) {
										Set<Local> aliasSet = this
												.getLocalMustAlias(vBase, st);
										if (!aliasSet.contains(rfBase))
											fieldsIn.add(st);
									}

								}
							}
						}

					}
				}

			}
		}

		// 2. perform kills

		for (ValueBox b : defBox) {
			Value v = b.getValue();
			Local localWrapper = null;
			if (v instanceof Local) {
				localWrapper = (Local) v;
				// kill the existing def
				Set<Stmt> s = defOutCurr.get(v);
				if (s == null) {
					s = new ArraySet<Stmt>();
					defOutCurr.put((Local) v, s);
				} else {
					if (killEnabled)
						s.clear();
				}
				s.add(st);

			} else if (v instanceof InstanceFieldRef || v instanceof ArrayRef) {
				if (v instanceof ArrayRef) {
					v = ArrayFieldRef.v((ArrayRef) v);
				}

				localWrapper = LocalWrapperforFieldRef.v((FieldRef) v);
				Set<Stmt> s = defOutCurrforFields.get(v);
				if (s == null) {
					s = new ArraySet<Stmt>();
					defOutCurrforFields.put((FieldRef) v, s);
				} else {
					if (b.getValue() instanceof InstanceFieldRef && killEnabled)
						s.clear();
					// we cannot perform kills for array ref
				}

				s.add(st);

				Local l = (Local) ((InstanceFieldRef) v).getBase();

				Value rhs = (Value) ((AssignStmt) st).getRightOp();
				if (!(rhs instanceof Constant))
					fieldsOut.add(st);

				for (FieldRef fr : defOutCurrforFields.keySet()) {
					boolean baseOK = false;
					if (fr.equals(v))
						continue;
					// here is must alias check
					// enable strong update
					s = defOutCurrforFields.get(fr);
					if (fr instanceof ArrayFieldRef
							&& v instanceof ArrayFieldRef)
						baseOK = true;
					else if (!(fr instanceof ArrayFieldRef)
							&& !(v instanceof ArrayFieldRef)
							&& fr.getField().equals(((FieldRef) v).getField()))
						baseOK = true;

					if (baseOK) {
						Local ba = (Local) (((InstanceFieldRef) fr).getBase());
						if (mustAlias(l, ba, localMustAlias.get(st))) {

							if (!(fr instanceof ArrayRef)) {
								for (Stmt prevDef : s) {
									fieldsOut.remove(prevDef);
								}
								if (killEnabled)
									s.clear();
							}
							s.add(st);
						} else if (mayAlias(l, ba)) {
							s.add(st);
						}
					}
				}

			}

			// add data dependence among defined and used variables
			if (localWrapper != null) {
				Set<Local> dataDepSet = dataDep.get(localWrapper);
				if (dataDepSet == null) {
					dataDepSet = new ArraySet<Local>();
					dataDep.put((Local) localWrapper, dataDepSet);
				}

				for (ValueBox _b : st.getUseBoxes()) {
					Value _v = _b.getValue();
					if (_v instanceof Local)
						dataDepSet.add((Local) _v);
					else if (_v instanceof FieldRef) {
						dataDepSet
								.add(LocalWrapperforFieldRef.v((FieldRef) _v));
					} else if (_v instanceof ArrayRef) {
						FieldRef ref = ArrayFieldRef.v((ArrayRef) _v);
						dataDepSet.add(LocalWrapperforFieldRef.v(ref));
					}
				}
			}

		}
	}

	// construct summary edges
	private void buildDataDependenceSummary() {

		for (Stmt fieldOutStmt : fieldsOut) {
			Set<Stmt> set = new ArraySet<Stmt>();
			fieldSummary.put(fieldOutStmt, set);
		}

		Set<Stmt> union = new ArraySet<Stmt>();
		for (Stmt st : returns)
			union.add(st);
		for (Stmt st : fieldsOut)
			union.add(st);

		for (Stmt stmt : union) {
			Set<Stmt> pairSet;
			if (returns.contains(stmt))
				pairSet = returnSummary;
			else
				pairSet = fieldSummary.get(stmt);

			List<Stmt> worklist = new ArrayList<Stmt>();
			worklist.add(stmt);
			Set<Stmt> visited = new ArraySet<Stmt>();
			while (worklist.size() > 0) {
				Stmt st = worklist.remove(0);
				if (!visited.add(st))
					continue;
				Set<DefUseEdge> deps = getStmtDependence(st);

				if (fieldsIn.contains(st)) {
					if (st.containsFieldRef() || st.containsArrayRef()) {
						pairSet.add(st);
					}
				}

				if (st instanceof IdentityStmt) {
					assert deps == null;
					pairSet.add(st);
				}

				if (deps != null) {
					for (DefUseEdge e : deps) {
						Stmt s = e.getDef();
						if (visited.contains(s))
							continue;
						if (worklist.contains(s))
							continue;
						worklist.add(s);
					}
				} else {
					assert st instanceof AssignStmt;
					Value rhs = ((AssignStmt) st).getRightOp();
					assert rhs instanceof NewExpr
							|| rhs instanceof NewArrayExpr
							|| rhs instanceof NewMultiArrayExpr
							|| rhs instanceof Constant;
					pairSet.add(st);

				}

			}
		}

	}

	// local may alias analysis
	private boolean mayAlias(Local l, Local r) {
		System.out.println("[DEBUG] " + l + "," + r + "@" + method);
		if (l == r) return true;

		long duration = System.currentTimeMillis();
		Pair<Local, Local> p = new Pair<Local, Local>(l, r);
//		Integer resInt = resultCache.get(p);
//		if (resInt != null) {
//			return (resInt.intValue() == 1);
//		}
		boolean res = maa.mayAlias(l, method, r, method);
		duration = (System.currentTimeMillis() - duration);
		mayAliasTime += duration;
		if (duration > Util.maxAliasTime) {
			Util.maxAliasTime = duration;
			Util.maxAliasPair = l + "," + r + "@" + method;
		}
		if (Util.DEBUG && Util.MAY_ALIAS.equals("spa")) {
			boolean manu = ManuMayAliasAnalysis.v().mayAlias(l, method, r, method);
			if (res != manu) {
								
				System.out.println("[diff] manu: " + manu + ", " + l + ", " + r + "@" + method);
				System.out.println("--- " + method);
				CallGraph cg = Scene.v().getCallGraph();
				Iterator<Edge> edges = cg.edgesInto(method);
				while (edges.hasNext()) {
					System.out.println("  " + edges.next().src());
				}											
			}
		}

		if (Util.MEASURE_PRECISION) resultCache.put(p, (res ? 1 : 0));
		return res;
	}

	// deep clone of a hashmap
	private Map cloneMap(Map m) {
		Map newM = new HashMap();
		for (Iterator entries = m.entrySet().iterator(); entries.hasNext();) {
			Map.Entry entry = (Map.Entry) entries.next();
			Object key = entry.getKey();
			Set<Stmt> s = (Set<Stmt>) entry.getValue();
			Set<Stmt> newS = new ArraySet<Stmt>();
			for (Stmt st : s)
				newS.add(st);
			newM.put(key, newS);
		}
		return newM;
	}

	// a conservative intra-procedural must alias analysis
	@SuppressWarnings("unchecked")
	public void mustAliasAnalysis(ExceptionalUnitGraph g) {
		if (mustAliasRun)
			return;
		mustAliasRun = true;
		Map<Stmt, Map<Local, Set<Local>>> alias = new HashMap<Stmt, Map<Local, Set<Local>>>();
		Map<Stmt, Map<Local, LatticeElement>> latticeIn = new HashMap<Stmt, Map<Local, LatticeElement>>();
		Map<Stmt, Map<Local, LatticeElement>> latticeOut = new HashMap<Stmt, Map<Local, LatticeElement>>();
		
		List worklist = new ArrayList<Stmt>();
		worklist.addAll(g.getHeads());

		Set<Stmt> visited = new ArraySet<Stmt>();

		while (worklist.size() > 0) {
			Stmt currStmt = (Stmt) worklist.get(0);
			worklist.remove(0);

			// create currAliasIn if it does not exist
			Map<Local, LatticeElement> currAliasIn = latticeIn.get(currStmt);
			if (currAliasIn == null) {
				currAliasIn = new HashMap<Local, LatticeElement>();
				latticeIn.put(currStmt, currAliasIn);
			}
			List prevList = g.getPredsOf(currStmt);
			boolean changed = false;

			// compute "meet" over incoming control flow edges
			// the result is stored in currAliasIn
			if (prevList.size() == 0)
				changed = true;
			for (int j = 0; j < prevList.size(); j++) {
				Stmt lastStmt = (Stmt) prevList.get(j);
				Map<Local, LatticeElement> lastAliasOut = latticeOut
						.get(lastStmt);
				if (mergeAliasInAndOut(lastAliasOut, currAliasIn, currStmt,
						alias)) {
					changed = true;
				}
			}

			// if no change is made during computing "meet", no need to visit
			// this node again
			if (!changed && visited.contains(currStmt))
				continue;
			visited.add(currStmt);

			// add the CFG successors to worklist
			List list = g.getSuccsOf(currStmt);
			worklist.addAll(list);

			// create currAliasOut by cloning map currAliasIn
			Map<Local, LatticeElement> currAliasOut = new HashMap<Local, LatticeElement>();
			currAliasOut.putAll(currAliasIn);
			latticeOut.put(currStmt, currAliasOut);

			if (!(currStmt instanceof DefinitionStmt))
				continue;
			Value lhs = ((DefinitionStmt) currStmt).getLeftOp();
			if (lhs.getType() instanceof PrimType)
				continue;

			Value rhs = ((DefinitionStmt) currStmt).getRightOp();

			// cases
			LatticeElement element = null;

			// lhs is a local
			if (lhs instanceof Local) {
				if (rhs instanceof ThisRef || rhs instanceof ParameterRef) {
					element = ParamLatticeElement.v((IdentityRef) rhs);
				} else if (rhs instanceof InvokeExpr) {
					element = ReturnLatticeElement.v(currStmt);
				} else if (rhs instanceof Local) {
					element = currAliasOut.get((Local) rhs);
				} else if (rhs instanceof FieldRef) {
					LocalWrapperforFieldRef lr = LocalWrapperforFieldRef
							.v((FieldRef) rhs);
					element = currAliasOut.get(lr);
					// first, let us check if the field reference has already
					// had a lattice element
					if (element == null) {
						// second, check if there exists another field reference
						// that has a lattice element
						// and that must alias rhs
						if (rhs instanceof InstanceFieldRef) {
							for (Map.Entry<Local, LatticeElement> entry : currAliasOut
									.entrySet()) {
								Local l = entry.getKey();
								if (!(l instanceof LocalWrapperforFieldRef)) {
									continue;
								}
								FieldRef fr = ((LocalWrapperforFieldRef) l)
										.getFieldReference();
								if (!(fr instanceof InstanceFieldRef))
									continue;
								if (((FieldRef) rhs).getField() != fr
										.getField())
									continue;
								Local base = (Local) ((InstanceFieldRef) rhs)
										.getBase();
								Local _base = (Local) ((InstanceFieldRef) fr)
										.getBase();
								if (mustAlias(base, _base, alias.get(currStmt)))
									element = entry.getValue();
							}
						}
						// get a new lattice element
						if (element == null)
							element = FieldRefLatticeElement.v((FieldRef) rhs);
					}
				} else if (rhs instanceof StringConstant) {
					element = AllocLatticeElement.v((StringConstant) rhs);
				} else if (rhs instanceof ClassConstant) {
					element = AllocLatticeElement.v((ClassConstant) rhs);
				} else if (rhs instanceof NewExpr) {
					element = AllocLatticeElement.v((NewExpr) rhs);
				} else {
					element = BottomLatticeElement.v();
				}
				// update currAliasOut
				currAliasOut.put((Local) lhs, element);
			}
			// lhs is static field or instance field access
			if (lhs instanceof FieldRef && rhs instanceof Local) {
				LocalWrapperforFieldRef lr = LocalWrapperforFieldRef
						.v((FieldRef) lhs);
				element = currAliasOut.get((Local) rhs);
				currAliasOut.put(lr, element);
			}

		}
		localMustAlias = alias;
		// return alias;
	}

	private Map<Local, Set<Local>> computeAlias(
			Map<Local, LatticeElement> localLatticeMap) {

		// create a new alias map
		Map<Local, Set<Local>> currAliasMap = new HashMap<Local, Set<Local>>();

		// an inverse of localLatticeMap
		Map<LatticeElement, Set<Local>> lattice2Locals = new HashMap<LatticeElement, Set<Local>>();
		boolean changed = false;
		// int numIte = 2;
		do {
			// for(int k = 0; k < numIte; k++)
			changed = false;
			if (localLatticeMap != null)
				for (Map.Entry<Local, LatticeElement> entry : localLatticeMap
						.entrySet()) {
					Local l = entry.getKey();
					LatticeElement e = entry.getValue();
					if (e instanceof BottomLatticeElement)
						continue;
					Set<Local> s = lattice2Locals.get(e);
					if (s == null) {
						s = new ArraySet<Local>();
						lattice2Locals.put(e, s);
					}
					int oldsize = s.size();
					s.add(l);
					if (s.size() > oldsize)
						changed = true;

					// check field reference alias
					if (e instanceof FieldRefLatticeElement
							&& ((FieldRefLatticeElement) e).getFieldReference() instanceof InstanceFieldRef) {
						SootField f = ((FieldRefLatticeElement) e)
								.getFieldReference().getField();
						Local base = (Local) ((InstanceFieldRef) ((FieldRefLatticeElement) e)
								.getFieldReference()).getBase();
						// iterate lattice2Locals to check if there exists a
						// FieldRefLatticeElement that must alias e
						for (Map.Entry<LatticeElement, Set<Local>> _entry : lattice2Locals
								.entrySet()) {
							LatticeElement _e = _entry.getKey();

							if (e == _e)
								continue;
							if (!(_e instanceof FieldRefLatticeElement))
								continue;
							FieldRef _fr = ((FieldRefLatticeElement) _e)
									.getFieldReference();
							if (!(_fr instanceof InstanceFieldRef))
								continue;
							if (_fr.getField() != f)
								continue;

							Local _base = (Local) ((InstanceFieldRef) _fr)
									.getBase();
							LatticeElement be = localLatticeMap.get(base);
							LatticeElement _be = localLatticeMap.get(_base);

							Set<Local> bs = currAliasMap.get(base);
							Set<Local> _bs = currAliasMap.get(_base);
							if (bs != null && _bs != null
									&& !bs.contains(_base)
									&& !_bs.contains(base)
									&& be.meet(_be) == BottomLatticeElement.v())
								continue;
							Set<Local> _s = _entry.getValue();
							oldsize = s.size();
							for (Local _l : _s)
								s.add(_l);
							for (Local _l : s)
								_s.add(_l);
							if (s.size() > oldsize)
								changed = true;
						}
					}
				}

			if (changed) {
				for (Map.Entry<LatticeElement, Set<Local>> entry : lattice2Locals
						.entrySet()) {
					Set<Local> s = entry.getValue();
					if (s.size() > 1) {
						Set<Local> aliasSet = new ArraySet<Local>();
						for (Local l : s)
							aliasSet.add(l);
						for (Local l : s) {
							currAliasMap.put(l, aliasSet);
						}
					}
				}
			}
		} while (changed);
		return currAliasMap;

	}

	// compute meet over incoming CFG edges and store the result in
	// currLatticeIn
	// the return value indicates if there is anything changed
	private boolean mergeAliasInAndOut(
			Map<Local, LatticeElement> lastLatticeOut,
			Map<Local, LatticeElement> currLatticeIn, Stmt st,
			Map<Stmt, Map<Local, Set<Local>>> alias) {
		// if (lastLatticeOut == null || lastLatticeOut.size() == 0)
		// return true;

		boolean changed = false;
		if (currLatticeIn.size() == 0) {
			if (lastLatticeOut != null) {
				currLatticeIn.putAll(lastLatticeOut);
				if (currLatticeIn.size() > 0)
					changed = true;
			}
		} else {

			// get the alias maps
			Map<Local, Set<Local>> lastAliasOut = computeAlias(lastLatticeOut);
			Map<Local, Set<Local>> currAliasIn = computeAlias(currLatticeIn);

			// iterate lastLatticeOut
			if (lastLatticeOut != null)
				for (Map.Entry<Local, LatticeElement> entry : lastLatticeOut
						.entrySet()) {
					Local l = entry.getKey();
					LatticeElement e = entry.getValue();

					LatticeElement _e = currLatticeIn.get(l);
					if (_e == null) {
						changed = true;
						currLatticeIn.put(l, BottomLatticeElement.v());
					} else {
						boolean areAliases = false;
						if (e instanceof FieldRefLatticeElement
								&& _e instanceof FieldRefLatticeElement) {
							FieldRef fr = ((FieldRefLatticeElement) e)
									.getFieldReference();
							FieldRef _fr = ((FieldRefLatticeElement) _e)
									.getFieldReference();
							if (fr instanceof InstanceFieldRef
									&& _fr instanceof InstanceFieldRef
									&& fr.getField() == _fr.getField()) {
								Local base = (Local) ((InstanceFieldRef) fr)
										.getBase();
								Local _base = (Local) ((InstanceFieldRef) _fr)
										.getBase();

								Set as = lastAliasOut.get(base);
								Set _as = currAliasIn.get(_base);
								if (as != null && _as != null)
									if (as.contains(_base)
											&& _as.contains(base))
										areAliases = true;
							}
						}

						if (!areAliases) {
							LatticeElement newe = e.meet(_e);
							if (newe != _e) {
								currLatticeIn.put(l, newe);
								changed = true;
							}
						}
					}

				}
		}
		Map<Local, Set<Local>> mergedAliasIn = computeAlias(currLatticeIn);
		alias.put(st, mergedAliasIn);
		return changed;

	}

	// merge defOutPrev and defInCurr. return true if a new map is generated.
	private boolean mergeReachingDef(Stmt stmt,
			Map<Local, Set<Stmt>> defOutPrev, Map<Local, Set<Stmt>> defInCurr,
			Map<FieldRef, Set<Stmt>> defOutPrevforFields,
			Map<FieldRef, Set<Stmt>> defInCurrforFields,
			Map<Stmt, Map<Local, Set<Local>>> localMustAlias) {
		boolean changed = false;
		// Timer.v("MERGE").startTiming();
		if (defInCurr.size() == 0) {
			if (defOutPrev != null) {
				for (Map.Entry<Local, Set<Stmt>> entry : defOutPrev.entrySet()) {
					Local l = entry.getKey();
					Set<Stmt> s = entry.getValue();
					Set<Stmt> newS = new ArraySet<Stmt>();
					for (Stmt st : s)
						newS.add(st);
					defInCurr.put(l, newS);
				}
				if (defInCurr.size() > 0)
					changed = true;
			}
		} else {
			if (defOutPrev != null) {
				for (Map.Entry<Local, Set<Stmt>> entry : defOutPrev.entrySet()) {
					Local l = entry.getKey();
					Set<Stmt> s = entry.getValue();
					Set<Stmt> currS = defInCurr.get(l);
					if (currS == null) {
						currS = new ArraySet<Stmt>();
						for (Stmt st : s)
							currS.add(st);
						defInCurr.put(l, currS);
						if (currS.size() > 0)
							changed = true;
					} else {
						int oldsize = currS.size();
						for (Stmt st : s)
							currS.add(st);
						if (currS.size() > oldsize)
							changed = true;
					}
				}
			}
		}
		// Timer.v("FIELDS").startTiming();
		// update defInCurrforFields
		if (defInCurrforFields.size() == 0) {
			if (defOutPrevforFields != null) {
				for (Map.Entry<FieldRef, Set<Stmt>> entry : defOutPrevforFields
						.entrySet()) {
					FieldRef l = entry.getKey();
					Set<Stmt> s = entry.getValue();
					Set<Stmt> newS = new ArraySet<Stmt>();
					for (Stmt st : s)
						newS.add(st);
					defInCurrforFields.put(l, newS);
				}
				if (defInCurrforFields.size() > 0)
					changed = true;
			}
		} else {
			if (defOutPrevforFields != null) {
				for (Map.Entry<FieldRef, Set<Stmt>> entry : defOutPrevforFields
						.entrySet()) {
					FieldRef l = entry.getKey();
					Set<Stmt> s = entry.getValue();
					Set<Stmt> currS = defInCurrforFields.get(l);
					if (currS == null) {
						currS = new ArraySet<Stmt>();
						for (Stmt st : s)
							currS.add(st);
						defInCurrforFields.put(l, currS);
						changed = true;

					} else {
						int oldsize = currS.size();
						for (Stmt st : s)
							currS.add(st);
						if (currS.size() > oldsize)
							changed = true;
					}
				}
			}
		}
		// Timer.v("FIELDS").endTiming();
		// Timer.v("FIELDS").getDuration();
		// Timer.v("MERGE").endTiming();
		// Timer.v("MERGE").getDuration();
		return changed;
	}

	public Set<Local> getLocalMustAlias(Local l, Stmt s) {
		if (localMustAlias == null) {
			throw new RuntimeException(
					"Must alias analysis has to be run first!");
		}
		Map<Local, Set<Local>> m = localMustAlias.get(s);
		if (m == null)
			return Collections.singleton(l);
		Set<Local> set = m.get(l);
		if (set == null)
			return Collections.singleton(l);
		set.add(l);
		return set;
	}

	// test aliasing relationship
	private boolean mustAlias(Local l, Local r, Map<Local, Set<Local>> aliasMap) {
		if (l == r)
			return true;
		if (aliasMap == null)
			return false;
		Set s = aliasMap.get(l);
		if (s != null && s.contains(r))
			return true;
		// s = aliasMap.get(r);
		// if (s != null && s.contains(l))
		// return true;
		return false;
	}

	public static ExceptionalUnitGraph getExceptionalGraph(SootMethod m) {
		ExceptionalUnitGraph g = null;
		try {
			g = unitGraphs.get(m);
			if (g == null) {
				g = new ExceptionalUnitGraph(m.getActiveBody());
				unitGraphs.put(m, g);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return g;
	}

	public static class DefaultMayAliasAnalysis implements MayAliasAnalysis {
		private static DefaultMayAliasAnalysis dmaa;
		private PointsToAnalysis pointsTo;
		
		private DefaultMayAliasAnalysis(PointsToAnalysis pta) {
			this.pointsTo = pta;
		}
		
		public static DefaultMayAliasAnalysis v(PointsToAnalysis pta) {
			if (dmaa == null) {
				dmaa = new DefaultMayAliasAnalysis(pta);
			}
			return dmaa;
		}

		public boolean mayAlias(Local v1, SootMethod m1, Local v2, SootMethod m2) {
			return Util.traditionalMayAlias(v1, m1, v2, m2, pointsTo);
//			PointsToSet rol = basePTCache.get(v1);
//			PointsToSet ror = basePTCache.get(v2);
//			if (rol == null) {
//				rol = pointsTo.reachingObjects(v1);
//			}
//			if (ror == null) {
//				ror = pointsTo.reachingObjects(v2);
//			}
//			if (rol.hasNonEmptyIntersection(ror))
//				return true;
//			return false;
		}
	}
}
