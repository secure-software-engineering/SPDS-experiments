package edu.osu.cse.pa.spg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import edu.osu.cse.pa.spg.symbolic.Assert;
import edu.osu.cse.pa.spg.symbolic.SymbolicExceptionObject;
import edu.osu.cse.pa.spg.symbolic.SymbolicGlobalObject;
import edu.osu.cse.pa.spg.symbolic.SymbolicInstanceFieldObject;
import edu.osu.cse.pa.spg.symbolic.SymbolicObject;
import edu.osu.cse.pa.spg.symbolic.SymbolicParamObject;
import edu.osu.cse.pa.spg.symbolic.SymbolicReturnedObject;

import soot.AnySubType;
import soot.PrimType;
import soot.ArrayType;
import soot.Local;
import soot.NullType;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.BinopExpr;
import soot.jimple.CastExpr;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.ClassConstant;
import soot.jimple.DefinitionStmt;
import soot.jimple.DynamicInvokeExpr;
import soot.jimple.FieldRef;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityRef;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InstanceOfExpr;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.MonitorStmt;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.NopStmt;
import soot.jimple.ParameterRef;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.TableSwitchStmt;
import soot.jimple.ThisRef;
import soot.jimple.ThrowStmt;
import soot.jimple.UnopExpr;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.util.ArraySet;

public class SymbolicPointerGraph {
	@SuppressWarnings("unchecked")
	private static Map /** <SootMethod, SymbolicPointerGraph> */
	graphs = new HashMap();

	private SootMethod method;

	@SuppressWarnings("unchecked")
	private Map /** <VarNode, Set<VarNode>> */
	var_edges = new HashMap();

	@SuppressWarnings("unchecked")
	private Map /** <AbstractAllocNode, set<VarNode>> */
	obj_edges = new HashMap();

	@SuppressWarnings("unchecked")
	private Map /** <Stmt, List> */
	cs2Targets = new HashMap(); // call site to targets map

	@SuppressWarnings("unchecked")
	private Map /** <VarNode, Set<AbstractAllocNode>> */
	varPtset = new HashMap();

	@SuppressWarnings("unchecked")
	private Set /** <VarNode> */
	receivers = new HashSet();

	@SuppressWarnings("unchecked")
	private Map /** <AbstractAllocNode, Map<SootField, Set<AbstractAllocNode>>> */
	fieldPtset = new HashMap();

	@SuppressWarnings("unchecked")
	private Map /** <LocalVarNode, Set<FieldVarNode>> */
	vartofields = new HashMap();	// base -> {base.f}

	@SuppressWarnings("unchecked")
	private static Map /** <GlobalVarNode, SymbolicGlobalObject> */
	global2symbolic = new HashMap();

	@SuppressWarnings("unchecked")
	private static Set /* SootMethod* */clinits = new HashSet();

	// private Map /** <SymbolicObject, Set<Field>> */
	// symbolicObj2fields = new HashMap();

	@SuppressWarnings("unchecked")
	private List /** <SymbolicObject> */
	headObjects = new ArrayList();

	@SuppressWarnings("unchecked")
	private Set /** SymbolicObjects or AbstractAllocNode */
	upwardEscapedObjs = new HashSet();

	@SuppressWarnings("unchecked")
	private Map /* <FieldVarNode, Set<VarNode>> */
	fieldVarEdges = new HashMap();

	/* This is for RTA */
	@SuppressWarnings("unchecked")
	private static Map /** <RefType, Set<CallGraphEdge>> */
	pending_calls = new HashMap();

	@SuppressWarnings("unchecked")
	private Set /** <LocalVarNode> */
	returnVars = new HashSet();
	
//	@SuppressWarnings({ "unused", "unchecked" })
//	private Set /** <SymbolicReturnedObject */
//	retObjsForSumm = new HashSet();

	@SuppressWarnings("unchecked")
	private Set /** SymbolicObjects or AbstractAllocNode */
	downwardEscapedObjs = new HashSet();

	@SuppressWarnings("unchecked")
	private Set /* <VarNode>* */
	rootsofDownwardEscape = new HashSet();	

	@SuppressWarnings("unchecked")
	private static Set /** <RefType> */
	possibleTypes = new HashSet();

	private SymbolicExceptionObject exceptionObj = null;
	
	@SuppressWarnings("unchecked")
	public static SymbolicPointerGraph v(SootMethod sm) {
		// int id = IDManager.getMethodID(sm);
		if (!graphs.containsKey(sm)) {
			SymbolicPointerGraph spg = new SymbolicPointerGraph(sm);
			graphs.put(sm, spg);
			return spg;
		} else {
			return (SymbolicPointerGraph) graphs.get(sm);
		}
	}

	@SuppressWarnings("unchecked")
	public static Map getPendingCalls() {
		return pending_calls;
	}

	public static void remove(SootMethod sm) {
		graphs.remove(sm);
	}

	@SuppressWarnings("unchecked")
	public static Set getClinits() {
		return clinits;
	}

	// public static void addPendingCall(RefType t, CallGraphEdge e) {
	// Set s = (Set) pending_calls.get(t);
	// if (s == null) {
	// s = new HashSet();
	// pending_calls.put(t, s);
	// }
	// s.add(e);
	// }

	@SuppressWarnings("unchecked")
	public static Map resolvePendingCall(RefType t) {
		Map m = (Map) pending_calls.get(t);
		if (m == null)
			return new HashMap();
		pending_calls.remove(t);
		return m;
	}

	public static boolean hasBeenInstantiated(RefType type) {
		return possibleTypes.contains(type);
	}

	public static SymbolicGlobalObject getSymbolicforGlobal(GlobalVarNode gvn) {
		return (SymbolicGlobalObject) global2symbolic.get(gvn);
	}

	private SymbolicPointerGraph(SootMethod sm) {
		method = sm;
	}

	@SuppressWarnings("unchecked")
	public Set getGlobalVarNodes() {
		return global2symbolic.keySet();
	}

	@SuppressWarnings("unchecked")
	public Set getReturnedVars() {
		return returnVars;
	}

	@SuppressWarnings("unchecked")
	public Map getVarPtSet() {
		return varPtset;
	}

	@SuppressWarnings("unchecked")
	public Set getReceivers() {
		return receivers;
	}

	@SuppressWarnings("unchecked")
	public Map getFieldPtSet() {
		return fieldPtset;
	}

	@SuppressWarnings("unchecked")
	public List getHeadObjects() {
		return headObjects;
	}

	@SuppressWarnings("unchecked")
	public Set getUpwardEscapedObjs() {
		return upwardEscapedObjs;
	}

	@SuppressWarnings("unchecked")
	public Set getRootofDownwardEscapedObjs() {
		return rootsofDownwardEscape;
	}

	@SuppressWarnings("unchecked")
	public Map getCallsite2TargetMap() {
		return cs2Targets;
	}

	public SootMethod getMethod() {
		return method;
	}

	@SuppressWarnings("unchecked")
	public Map getFieldVarEdges() {
		return fieldVarEdges;
	}

	@SuppressWarnings("unchecked")
	public void build() {
		/**
		 * We build flow graph
		 */
		if (!method.hasActiveBody()) {
			method.retrieveActiveBody();
		}
		
		// first of all, flow edges are added by inspecting the statements in the
		// method one by one
		for (Iterator stmts = method.getActiveBody().getUnits().iterator(); stmts
				.hasNext();) {
			Stmt st = (Stmt) stmts.next();
			processStmt(st, method);
		}

		// Set /* <LocalVarNode> */receivers = new HashSet();
		// receiver objects of method invocations are recorded
		for (Iterator cs = cs2Targets.keySet().iterator(); cs.hasNext();) {
			Stmt st = (Stmt) cs.next();
			// Set s = new HashSet();
			if (st.getInvokeExpr() instanceof InstanceInvokeExpr) {
				Local l = (Local) ((InstanceInvokeExpr) st.getInvokeExpr())
						.getBase();
				LocalVarNode lvn = NodeFactory.v(method).findLocalVarNode(l);
				Assert.assertTrue(lvn != null);
				receivers.add(lvn);
			}

			for (int i = 0; i < st.getInvokeExpr().getArgCount(); i++) {
				Value v = st.getInvokeExpr().getArg(i);
				if (isTypeofInterest(v)) {
					VarNode ln;
					if (v instanceof Local) {
						ln = NodeFactory.v(method).findLocalVarNode((Local) v);
						if (ln == null) {
							continue;
						}
						Assert.assertTrue(ln != null);
						receivers.add(ln);
					}
				}
			}

		}

		// compute intra-procedural points-to relation
		computeClosureOverVarEdges();
		computeClosureOverObjEdges();

		// propagate points-to set
		for (Iterator i = obj_edges.keySet().iterator(); i.hasNext();) {
			AbstractNode alloc = (AbstractNode) i.next();
			Set vars = (Set) obj_edges.get(alloc);
			for (Iterator j = vars.iterator(); j.hasNext();) {
				VarNode var = (VarNode) j.next();
				Set objs = (Set) varPtset.get(var);
				if (objs == null) {
					objs = new HashSet();
					varPtset.put(var, objs);
				}
				objs.add(alloc);				
			}
		}

		for (Iterator i = var_edges.keySet().iterator(); i.hasNext();) {
			VarNode node = (VarNode) i.next();
			if (node instanceof FieldVarNode) {
				Set s = (Set) var_edges.get(node);
				Set set = (Set) fieldVarEdges.get(node);
				if (set == null) {
					set = new HashSet();
					fieldVarEdges.put(node, set);
				}
				for (Iterator j = s.iterator(); j.hasNext();) {
					VarNode n = (VarNode) j.next();
					if (n instanceof LocalVarNode) {
						set.add(n);
					}
				}
			}

		}

		// propagate for load and store operations
		List worklist = new ArrayList();
		worklist.addAll(vartofields.keySet());

		while (worklist.size() > 0) {
			VarNode v = (VarNode) worklist.get(0);
			worklist.remove(0);
			Set fs = (Set) vartofields.get(v);
			Set pts = (Set) varPtset.get(v);
			if (pts == null) {
				pts = new HashSet();
				varPtset.put(v, pts);
			}

			int old_size = 0;
			for (Iterator fields = fs.iterator(); fields.hasNext();) {
				FieldVarNode fvn = (FieldVarNode) fields.next();
				Set fvnptset = (Set) varPtset.get(fvn);
				if (fvnptset == null) {
					fvnptset = new HashSet();
					varPtset.put(fvn, fvnptset);
				}

				old_size = fvnptset.size();
				for (Iterator objs = pts.iterator(); objs.hasNext();) {
					AbstractNode aan = (AbstractNode) objs.next();
					Map m = (Map) fieldPtset.get(aan);
					if (m == null) {
						m = new HashMap();
						fieldPtset.put(aan, m);
					}
					Set os = (Set) m.get(fvn.getField());
					if (os == null) {
						os = new HashSet();
						m.put(fvn.getField(), os);
					}
					int old_os_size = os.size();

					// if (aan instanceof SymbolicObject
					// && !hasSymbolicFieldCreated((SymbolicObject) aan,
					// fvn.getField())) {
					// SymbolicInstanceFieldObject obj = new
					// SymbolicInstanceFieldObject(
					// method, fvn.getField());
					// Set s = (Set) symbolicObj2fields.get(aan);
					// if (s == null) {
					// s = new HashSet();
					// symbolicObj2fields.put(aan, s);
					// }
					// s.add(fvn.getField());
					// os.add(obj);
					// }

					os.addAll(fvnptset);	// add (o1,f,o2) for o1 \in varPtSet(v), o2 \in varPtSet(fvn)
					fvnptset.addAll(os);	// 
					if (os.size() > old_os_size) {
						worklist.add(v);
					}
				}
				if (fvnptset.size() > old_size) {
					Set succ = (Set) var_edges.get(fvn);
					if (succ != null) {
						for (Iterator nodes = succ.iterator(); nodes.hasNext();) {
							VarNode vn = (VarNode) nodes.next();
							Set set = (Set) varPtset.get(vn);
							if (set == null) {
								set = new HashSet();
								varPtset.put(vn, set);
							}
							int old = set.size();
							set.addAll(fvnptset);
							if (vartofields.containsKey(vn)
									&& !worklist.contains(vn)
									&& set.size() > old)
								worklist.add(vn);
						}
					}
				}

			}

		}

		// find all FieldVarNode p.a, such that p.a does not have a points-to
		// set yet
		// we need to create a symbolic object for each of them, and propogate
		// the set
		worklist.addAll(vartofields.keySet());
		while (worklist.size() > 0) {
			VarNode vn = (VarNode) worklist.get(0);
			worklist.remove(0);
			Set fs = (Set) vartofields.get(vn);
			for (Iterator itt = fs.iterator(); itt.hasNext();) {
				FieldVarNode fvn = (FieldVarNode) itt.next();
				Set pts = (Set) varPtset.get(fvn);
				int old_pts = pts.size();
				if (succNodesdonothavePTset(fvn)) {
					SymbolicInstanceFieldObject ifo = new SymbolicInstanceFieldObject(
							method, fvn.getField());
					if (pts == null) {
						pts = new HashSet();
						varPtset.put(fvn, pts);
					}
					pts.add(ifo);
				}

				for (Iterator objs = ((Set) varPtset.get(vn)).iterator(); objs
						.hasNext();) {
					AbstractNode an = (AbstractNode) objs.next();
					Map fsmap = (Map) fieldPtset.get(an);
					if (fsmap == null) {
						fsmap = new HashMap();
						fieldPtset.put(an, fsmap);
					}
					Set f = (Set) fsmap.get(fvn.getField());
					if (f == null) {
						f = new HashSet();
						fsmap.put(fvn.getField(), f);
					}
					int old_f = f.size();
					f.addAll(pts);
					pts.addAll(f);
					if (f.size() > old_f)
						worklist.add(vn);
				}
				if (pts.size() > old_pts) {
					Set succ = (Set) var_edges.get(fvn);
					if (succ != null) {
						for (Iterator nodes = succ.iterator(); nodes.hasNext();) {
							VarNode v = (VarNode) nodes.next();

							Set set = (Set) varPtset.get(v);
							if (set == null) {
								set = new HashSet();
								varPtset.put(v, set);
							}
							int old = set.size();
							set.addAll(pts);
							if (vartofields.containsKey(v)
									&& !worklist.contains(v)
									&& set.size() > old)
								worklist.add(v);
						}
					}
				}

			}

		}

		constructSPG();

		// computeEscapeObjects();

	}

	@SuppressWarnings("unchecked")
	private void constructSPG() {
		// first build an intra-procedural points-to graph from the flow graph
//		@SuppressWarnings("unused")
//		Map obj2context = new HashMap();
		// headObjects = spg.getHeadObjects();
		cs2Targets = getCallsite2TargetMap();
		Map varPtSet = getVarPtSet();
		Map fieldPtSet = getFieldPtSet();

		for (Iterator ita = varPtSet.keySet().iterator(); ita.hasNext();) {
			VarNode varnode = (VarNode) ita.next();
			if (varnode instanceof FieldVarNode) {
				continue;
			}

			Set s = (Set) varPtSet.get(varnode);
			for (Iterator objs = s.iterator(); objs.hasNext();) {
				AbstractAllocNode obj = (AbstractAllocNode) objs.next();
				new PointsToEdge(varnode, obj);
			}
		}

		// some sanity check,
		// for (Iterator its = spg.getReceivers().iterator(); its.hasNext();) {
		// VarNode vn = (VarNode) its.next();
		// if (!receiversMap.containsKey(vn)) {
		// System.out.println();
		// }
		// }
		// ReturnVarNode vn = NodeFactory.v(sm).findReturnVarNode(sm);
		// if (vn != null && !varPtSet.containsKey(vn)) {
		// System.out.println();
		// }

		for (Iterator ita = fieldPtSet.keySet().iterator(); ita.hasNext();) {
			AbstractNode objnode = (AbstractNode) ita.next();
			assert objnode instanceof AbstractAllocNode;
			Map m = (Map) fieldPtSet.get(objnode);
			// Map dir = (Map) directions.get(objnode);
			for (Iterator fields = m.keySet().iterator(); fields.hasNext();) {
				SootField sf = (SootField) fields.next();
				Set pts = (Set) m.get(sf);
				// int di = ((Integer) dir.get(sf)).intValue();
				for (Iterator objs = pts.iterator(); objs.hasNext();) {
					AbstractNode tgt = (AbstractNode) objs.next();
					assert tgt instanceof AbstractAllocNode;
					new FieldPTEdge((AbstractAllocNode) objnode, sf,
							(AbstractAllocNode) tgt);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void addEntryAndExitEdges() {
		CallGraph cg = Scene.v().getCallGraph();

		// add entry edges
		for (Iterator<Edge> edges = cg.edgesOutOf(method); edges.hasNext();) {
			Edge e = edges.next();
			// performs certain sanity checks and also makes sure that
			// e.tgt() is concrete.
			if (e.srcStmt() == null)
				continue;
			if (!e.tgt().isConcrete())
				continue;
			if (!e.srcStmt().containsInvokeExpr())
				continue;
			if (!e.srcStmt().getInvokeExpr().getMethod().getSubSignature()
					.equals(e.tgt().getSubSignature()))
				continue;

			// fills in the params map which is essentially pairs of <Actual,
			// Formal>
			Stmt st = e.srcStmt();
			InvokeExpr ie = st.getInvokeExpr();
			Map<Value, Local> params = new HashMap<Value, Local>();
			SootMethod tgt = e.tgt();
			for (int i = 0; i < ie.getArgCount(); i++) {
				Value v = ie.getArg(i);

				Local formalParam = tgt.getActiveBody().getParameterLocal(i);
				if (v.getType() instanceof PrimType) {
					continue;
				}
				if (v instanceof Local) {
					params.put((Local) v, formalParam);
				} else if (v instanceof StringConstant) {
					// StringConstNode scn = StringConstNode.node;
					params.put(v, formalParam);
				} else if (v instanceof ClassConstant) {
					params.put(v, formalParam);
				}
			}
			if (ie instanceof InstanceInvokeExpr) {
				params.put((Local) ((InstanceInvokeExpr) ie).getBase(), tgt
						.getActiveBody().getThisLocal());
			}

			// add the formal-actual binding edges
			for (Map.Entry<Value, Local> entry : params.entrySet()) {
				Value actual = entry.getKey();
				Local formal = entry.getValue();
				VarNode fn = NodeFactory.v(tgt).findLocalVarNode(formal);
				Set<AbstractAllocNode> actualPT = new ArraySet<AbstractAllocNode>();
				if (actual instanceof Local) {
					VarNode vn = NodeFactory.v(method).findLocalVarNode(
							(Local) actual);
					assert vn != null;
					assert (vn.getPointsToEdges().hasNext());
					// TODO: assuming the only case that `vn==null' is that the
					// local
					// var `actual' is null.
					// eg. $r2 = (java.lang.Throwable) null;
					if (vn != null) {
						for (Iterator<PointsToEdge> it = vn.getPointsToEdges(); it
								.hasNext();) {
							PointsToEdge pte = it.next();
							actualPT.add(pte.tgt());
						}
					}
				} else if (actual instanceof StringConstant) {
					actualPT.add(new StringConstNode(method,
							(StringConstant) actual));
				} else if (actual instanceof ClassConstant) {
					actualPT.add(ClassConstNode.node);
				}

				for (AbstractAllocNode n : actualPT) {
					for (Iterator<PointsToEdge> itt = fn.getPointsToEdges(); itt
							.hasNext();) {
						PointsToEdge fte = itt.next();
						new EntryEdge(n, fte.tgt(), e);
					}
				}
			}

			// ---

			// add ExitEdges
			if (e.srcStmt() instanceof AssignStmt) {
				Local lhs = (Local) ((AssignStmt) e.srcStmt()).getLeftOp();
				Set<LocalVarNode> returnedVars = SymbolicPointerGraph.v(tgt)
						.getReturnedVars();

				VarNode ln = NodeFactory.v(method).findLocalVarNode(lhs);
				// TODO: assuming that `ln==null' implies that lhs is not
				// a RefLike var
				// Example: i0 = virtualinvoke r2.<java.util.Vector: int size()>();
				if (ln != null) {
					for (Iterator<PointsToEdge> itt = ln.getPointsToEdges(); itt
							.hasNext();) {
						AbstractAllocNode lhsObj = itt.next().tgt();
						// for (Local r : returnedVars) {
						for (LocalVarNode lvn : returnedVars) {
							Local r = lvn.getLocal();
							VarNode rn = NodeFactory.v(tgt).findLocalVarNode(r);
							for (Iterator<PointsToEdge> it = rn
									.getPointsToEdges(); it.hasNext();) {
								new ExitEdge(it.next().tgt(), lhsObj, e);
							}
						}
					}
				}
			}

		}

	}

	@SuppressWarnings("unchecked")
	private boolean succNodesdonothavePTset(FieldVarNode fvn) {
		// boolean ret = false;
		Set ptset = (Set) varPtset.get(fvn);
		if (ptset == null || ptset.size() == 0) {
			return true;
		}
		
		Set s = (Set) var_edges.get(fvn);
		if (s == null || s.size() == 0)
			return false;
		
		for (Iterator ita = s.iterator(); ita.hasNext();) {
			VarNode vn = (VarNode) ita.next();
			ptset = (Set) varPtset.get(vn);
			if (ptset == null || ptset.size() == 0) {				
				return true;
			}
		}
		
		return false;
	}

	@SuppressWarnings("unchecked")
	public List targets(Stmt s) {
		return (List) cs2Targets.get(s);
	}

	@SuppressWarnings("unchecked")
	public static void resolveCall(InvokeExpr ie, List target_types) {
		if (ie instanceof StaticInvokeExpr || ie instanceof SpecialInvokeExpr || ie instanceof DynamicInvokeExpr) {
			return;
		}

		Local receiver_local = (Local) ((InstanceInvokeExpr) ie).getBase();

		Type t = receiver_local.getType();

		if (t instanceof NullType) {
			return;
		}

		// deal with arrays first
		if (t instanceof ArrayType) {
			SootClass c = Scene.v().getSootClass("java.lang.Object");
			// SootMethod target_m = virtualDispatch(ie.getMethod(), c);
			target_types.add(c.getType());
			// target_types.add(c.getType());
		} else {
			SootClass static_class = ((RefType) t).getSootClass();
			List l;
			if (static_class.isInterface()) {
				l = Scene.v().getActiveHierarchy().getImplementersOf(
						static_class);
			} else {
				l = Scene.v().getActiveHierarchy().getSubclassesOfIncluding(
						static_class);
			}

			for (int i = 0; i < l.size(); i++) {
				SootClass c = (SootClass) l.get(i);
				target_types.add(c.getType());
			}

		}
	}

	@SuppressWarnings("unchecked")
	private void computeEscapeObjects() {

		// examine ExceptionVarNode
		Set objs = (Set) varPtset.get(ExceptionVarNode.node);
		if (objs != null && objs.size() > 1) {
			// more than one object
			// get rid of the SymbolicExceptionObject
			objs.remove(exceptionObj);

			// add objects that are thrown into headObjects;
			// headObjects.addAll(objs);
		}

		// for (Iterator keys = global2symbolic.keySet().iterator(); keys
		// .hasNext();) {
		// GlobalVarNode gvn = (GlobalVarNode) keys.next();
		// Set ns = (Set) varPtset.get(gvn);
		// Assert.assertTrue(ns != null);
		// Set removed = new HashSet();
		// if (ns.size() > 1) {
		// for (Iterator it = ns.iterator(); it.hasNext();) {
		// AbstractNode an = (AbstractNode) it.next();
		// if (an instanceof SymbolicGlobalObject) {
		// removed.add(an);
		// break;
		// }
		// }
		// ns.removeAll(removed);
		// } else {
		// // if (ns.iterator().next() instanceof SymbolicGlobalObject)
		// // headObjects.addAll(ns);
		// }
		// }

		// examine call sites
		// for (Iterator les = returnVars.iterator(); les.hasNext();) {
		// LocalVarNode lvn = (LocalVarNode) les.next();
		// objs = (Set) varPtset.get(lvn);
		// if (objs.size() > 1) {
		// // more than one object
		// // get rid of the SymbolicReturnedObject
		// Set removed = new HashSet();
		// for (Iterator ii = objs.iterator(); ii.hasNext();) {
		// AbstractNode an = (AbstractNode) ii.next();
		// if (an instanceof SymbolicReturnedObject) {
		// removed.add(an);
		// break;
		// }
		// }
		// objs.removeAll(removed);
		// }
		// }

		// add objects that are returned into headObjects
		ReturnVarNode rn = NodeFactory.v(method).findReturnVarNode(method);
		if (rn != null) {
			objs = (Set) varPtset.get(rn);
			headObjects.addAll(objs);
		}

		List worklist = new ArrayList();
		worklist.addAll(headObjects);
		Set visited = new HashSet();
		while (worklist.size() > 0) {
			AbstractNode so = (AbstractNode) worklist.get(0);
			visited.add(so);
			upwardEscapedObjs.add(so);
			worklist.remove(0);
			Map m = (Map) fieldPtset.get(so);
			if (m != null) {
				for (Iterator fields = m.keySet().iterator(); fields.hasNext();) {
					SootField f = (SootField) fields.next();
					Set pts = (Set) m.get(f);
					for (Iterator ptsI = pts.iterator(); ptsI.hasNext();) {
						AbstractNode an = (AbstractNode) ptsI.next();
						if (!worklist.contains(an) && !visited.contains(an)
								&& fieldPtset.containsKey(an))
							worklist.add(an);
						else {
							upwardEscapedObjs.add(an);
						}
					}
				}
			}

		}

		for (Iterator cs = cs2Targets.keySet().iterator(); cs.hasNext();) {
			Stmt st = (Stmt) cs.next();

			InvokeExpr ie = st.getInvokeExpr();
			if (ie instanceof InstanceInvokeExpr) {
				Local l = (Local) ((InstanceInvokeExpr) ie).getBase();
				LocalVarNode lvn = NodeFactory.v(method).findLocalVarNode(l);
				rootsofDownwardEscape.add(lvn);
			}
			for (int i = 0; i < ie.getArgCount(); i++) {
				Value arg = ie.getArg(i);
				if (arg instanceof Local && isTypeofInterest(arg)) {
					LocalVarNode n = NodeFactory.v(method).findLocalVarNode(
							(Local) arg);
					rootsofDownwardEscape.add(n);
				}
			}

			for (Iterator ita = rootsofDownwardEscape.iterator(); ita.hasNext();) {
				LocalVarNode lvn = (LocalVarNode) ita.next();
				Set objset = (Set) varPtset.get(lvn);
				if (objset != null) {
					for (Iterator objss = objset.iterator(); objss.hasNext();) {
						AbstractNode so = (AbstractNode) objss.next();
						Map m = (Map) fieldPtset.get(so);
						if (m != null) {
							for (Iterator fields = m.keySet().iterator(); fields
									.hasNext();) {
								SootField f = (SootField) fields.next();
								Set pts = (Set) m.get(f);
								for (Iterator ptsI = pts.iterator(); ptsI
										.hasNext();) {
									AbstractNode an = (AbstractNode) ptsI
											.next();
									if (!worklist.contains(an)) {
										worklist.add(an);
									}
								}
							}
						}
					}
				}
			}

		}
		visited = new HashSet();
		while (worklist.size() > 0) {
			AbstractNode so = (AbstractNode) worklist.get(0);
			visited.add(so);
			downwardEscapedObjs.add(so);
			worklist.remove(0);
			Map m = (Map) fieldPtset.get(so);
			if (m != null) {
				for (Iterator fields = m.keySet().iterator(); fields.hasNext();) {
					SootField f = (SootField) fields.next();
					Set pts = (Set) m.get(f);
					for (Iterator ptsI = pts.iterator(); ptsI.hasNext();) {
						AbstractNode an = (AbstractNode) ptsI.next();
						if (!worklist.contains(an) && !visited.contains(an)
								&& fieldPtset.containsKey(an))
							worklist.add(an);
						else
							downwardEscapedObjs.add(an);
					}
				}
			}
		}

		// use intra-procedural points-to info to resolve virtual calls
		loop: for (Iterator cs = cs2Targets.keySet().iterator(); cs.hasNext();) {
			Stmt st = (Stmt) cs.next();
			InvokeExpr ie = st.getInvokeExpr();
			Set types = new HashSet();
			if (ie instanceof VirtualInvokeExpr
					|| ie instanceof InterfaceInvokeExpr) {
				Local l = (Local) ((InstanceInvokeExpr) ie).getBase();
				LocalVarNode lvn = NodeFactory.v(method).findLocalVarNode(l);
				Set objset = (Set) varPtset.get(lvn);
				if (objset != null) {
					for (Iterator objss = objset.iterator(); objss.hasNext();) {

						AbstractNode obj = (AbstractNode) objss.next();
						if (!(obj instanceof SymbolicParamObject)
								&& !(obj instanceof AbstractAllocNode)) {
							// can not be resolved

							types.clear();
							continue loop;
						}

						if (upwardEscapedObjs.contains(obj)
								|| downwardEscapedObjs.contains(obj)) {
							// can not be resolved
							types.clear();
							continue loop;
						}

						if (!(obj.getType() instanceof RefType)) {
							types.clear();
							continue loop;
						}
						types.add(obj.getType());
					}
				}
			}
			if (types.size() > 0) {
				List mm = (List) cs2Targets.get(st);
				mm.retainAll(types);
			}
		}

	}

	/**
	 * Ignores certain types of statements, and calls addFlowEdges()
	 * 
	 * @param s
	 * @param sm
	 */
	private void processStmt(Stmt s, SootMethod sm) {
		if (s instanceof ReturnVoidStmt)
			return;
		if (s instanceof GotoStmt)
			return;
		if (s instanceof IfStmt)
			return;
		if (s instanceof TableSwitchStmt)
			return;
		if (s instanceof LookupSwitchStmt)
			return;
		if (s instanceof MonitorStmt)
			return;
		addFlowEdges(s, sm);
	}

	/**
	 * 
	 * @param s
	 * @param sm
	 */
	@SuppressWarnings("unchecked")
	private void addFlowEdges(Stmt s, SootMethod sm) {

		if (s instanceof NopStmt) return;
		
		// call site
		if (s.containsInvokeExpr()) {
			InvokeExpr ie = s.getInvokeExpr();			
			
			// deals with return values (which matters only for AssignStmt
			if (s instanceof AssignStmt) {
				Local lhs = (Local) ((AssignStmt) s).getLeftOp();
				InvokeExpr iie = s.getInvokeExpr();
				SootMethod static_target = iie.getMethod();
				String sig = static_target.getSubSignature();
				String cls = static_target.getDeclaringClass().getName();
				// deals with certain special cases
				// and since they are special, the parameters of them are not
				// handled
				// TODO: read through this part
				if (sig.equals("java.lang.Object newInstance()")
						&& cls.equals("java.lang.Class")) {
					LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm,
							lhs);
					AnySubTypeNode rn = NodeFactory.v(sm).makeAnySubTypeNode(
							sm, (RefType) lhs.getType());

					addObjEdge(rn, ln);

					return;
				}
				if (sig
						.equals("java.lang.Object newInstance(java.lang.Object[])")
						&& cls.equals("java.lang.reflect.Constructor")) {
					LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm,
							lhs);
					AnySubTypeNode rn = NodeFactory.v(sm).makeAnySubTypeNode(
							sm, (RefType) lhs.getType());
					addObjEdge(rn, ln);
					return;
				}
				if (static_target.getSignature().equals("<java.lang.reflect.Array: java.lang.Object newInstance(java.lang.Class,int)>")) {
					LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm, lhs);
					AnySubTypeNode rn = NodeFactory.v(sm).makeAnySubTypeNode(sm, (RefType) lhs.getType());
					addObjEdge(rn, ln);
				}
				if (sig
						.equals("java.lang.Object invoke(java.lang.Object,java.lang.Object[])")
						&& cls.equals("java.lang.reflect.Method")) {
					LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm,
							lhs);
					AnySubTypeNode rn = NodeFactory.v(sm).makeAnySubTypeNode(
							sm, (RefType) lhs.getType());
					addObjEdge(rn, ln);
					return;
				}

				if (sig
						.equals("java.lang.Object newProxyInstance(java.lang.ClassLoader,java.lang.Class[],java.lang.reflect.InvocationHandler)")
						&& cls.equals("java.lang.reflect.Proxy")) {
					LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm,
							lhs);
					AnySubTypeNode rn = NodeFactory.v(sm).makeAnySubTypeNode(
							sm, (RefType) lhs.getType());
					addObjEdge(rn, ln);
					return;
				}

				// ---

				Type rt = static_target.getReturnType();
				if (rt instanceof RefType || rt instanceof ArrayType) {
					SymbolicReturnedObject ro = new SymbolicReturnedObject(sm,
							rt);
					LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm,
							lhs);
					// TODO: returnVars shouldn't be changed here
//					returnVars.add(ln);
					addSymbolicEdge(ro, ln);
				} // Otherwise, the return type is not a reference.

			}

			// ---

			// deals with parameters
			if (s.getInvokeExpr() instanceof InstanceInvokeExpr) {
				Local base = (Local) ((InstanceInvokeExpr) s.getInvokeExpr())
						.getBase();
				LocalVarNode lvn = NodeFactory.v(sm).makeLocalVarNode(sm, base);
				Assert.assertTrue(lvn != null);
				receivers.add(lvn);
			}

			// ---

			// adds the pair <call site, targets> to cs2Targets map
			List list = new ArrayList();
			resolveCall(s.getInvokeExpr(), list);
			cs2Targets.put(s, list);
			return;
		}

		// END call site handling

		// ---

		// case 1: return
		if (s instanceof ReturnStmt) {
			Value v = ((ReturnStmt) s).getOp();

			if (v instanceof Local && isTypeofInterest(v)) {
				LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm,
						(Local) v);
				ReturnVarNode rn = NodeFactory.v(sm).makeReturnVarNode(sm);
				addVarEdge(ln, rn);
				
				// TODO: add returnVars
				returnVars.add(ln);
			}

			if (v instanceof StringConstant) {
				ReturnVarNode rn = NodeFactory.v(sm).makeReturnVarNode(sm);
				addObjEdge(new StringConstNode(sm, (StringConstant) v), rn);
			}
			if (v instanceof ClassConstant) {
				ReturnVarNode rn = NodeFactory.v(sm).makeReturnVarNode(sm);
				addObjEdge(ClassConstNode.node, rn);
			}
			return;
		}

		// case 2: throw
		if (s instanceof ThrowStmt) {
			Local l = (Local) ((ThrowStmt) s).getOp();
			LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm, l);
			addVarEdge(ln, ExceptionVarNode.node);
			return;
		}

		Value lhs = ((DefinitionStmt) s).getLeftOp();
		Value rhs = ((DefinitionStmt) s).getRightOp();

		// case 3: IdentityStmt
		if (s instanceof IdentityStmt) {

			if (rhs instanceof CaughtExceptionRef) {
				LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm,
						(Local) lhs);
				addVarEdge(ExceptionVarNode.node, ln);
				if (exceptionObj == null)
					exceptionObj = new SymbolicExceptionObject(sm);
				// headObjects.add(exceptionObj);
				addSymbolicEdge(exceptionObj, ExceptionVarNode.node);
			}

			if ((rhs instanceof ThisRef || rhs instanceof ParameterRef)
					&& isTypeofInterest(rhs)) {
				LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm,
						(Local) lhs);
				SymbolicParamObject obj = new SymbolicParamObject(sm,
						(IdentityRef) rhs);
				headObjects.add(obj);
				addSymbolicEdge(obj, ln);
			}
			return;
		}

		if (s instanceof AssignStmt) {
			// case 4.1: lhs is array access
			if (lhs instanceof ArrayRef) {
				// if rhs is local
				if (rhs instanceof Local && isTypeofInterest(rhs)) {
					LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm,
							(Local) rhs);

					FieldVarNode node = NodeFactory.v(sm)
							.makeArrayElementVarNode(sm,
									(Local) ((ArrayRef) lhs).getBase());

					LocalVarNode base = NodeFactory.v(sm).makeLocalVarNode(sm,
							((Local) ((ArrayRef) lhs).getBase()));
					Set fields = (Set) vartofields.get(base);
					if (fields == null) {
						fields = new HashSet();
						vartofields.put(base, fields);
					}
					fields.add(node);

					addVarEdge(ln, node);
				}
				// rhs is a string constant
				if (rhs instanceof StringConstant) {

					FieldVarNode node = NodeFactory.v(sm)
							.makeArrayElementVarNode(sm,
									(Local) ((ArrayRef) lhs).getBase());
					addObjEdge(new StringConstNode(sm, (StringConstant) rhs),
							node);
					LocalVarNode base = NodeFactory.v(sm).makeLocalVarNode(sm,
							((Local) ((ArrayRef) lhs).getBase()));
					Set fields = (Set) vartofields.get(base);
					if (fields == null) {
						fields = new HashSet();
						vartofields.put(base, fields);
					}
					fields.add(node);

				}
				if (rhs instanceof ClassConstant) {
					FieldVarNode node = NodeFactory.v(sm)
							.makeArrayElementVarNode(sm,
									(Local) ((ArrayRef) lhs).getBase());
					LocalVarNode base = NodeFactory.v(sm).makeLocalVarNode(sm,
							((Local) ((ArrayRef) lhs).getBase()));
					Set fields = (Set) vartofields.get(base);
					if (fields == null) {
						fields = new HashSet();
						vartofields.put(base, fields);
					}
					fields.add(node);
					addObjEdge(ClassConstNode.node, node);
				}
				return;
			}

			// case 4.2: lhs is a field access
			if (lhs instanceof FieldRef) {

				if (rhs instanceof Local && isTypeofInterest(rhs)) {

					LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm,
							(Local) rhs);
					if (lhs instanceof InstanceFieldRef) {
						Local l = (Local) ((InstanceFieldRef) lhs).getBase();
						FieldVarNode fn = NodeFactory.v(sm).makeFieldVarNode(
								sm, ((FieldRef) lhs).getField(), l);
						LocalVarNode base = NodeFactory.v(sm).makeLocalVarNode(
								sm, l);
						Set fields = (Set) vartofields.get(base);
						if (fields == null) {
							fields = new HashSet();
							vartofields.put(base, fields);
						}
						fields.add(fn);
						addVarEdge(ln, fn);						
					} else {
						GlobalVarNode gn = NodeFactory.v(sm).makeGlobalVarNode(
								sm, ((FieldRef) lhs).getField());

						addVarEdge(ln, gn);
					}

				}
				// if rhs is a string constant
				if (rhs instanceof StringConstant) {
					if (lhs instanceof InstanceFieldRef) {
						Local l = (Local) ((InstanceFieldRef) lhs).getBase();
						FieldVarNode fn = NodeFactory.v(sm).makeFieldVarNode(
								sm, ((FieldRef) lhs).getField(),
								(Local) ((InstanceFieldRef) lhs).getBase());
						LocalVarNode base = NodeFactory.v(sm).makeLocalVarNode(
								sm, l);
						Set fields = (Set) vartofields.get(base);
						if (fields == null) {
							fields = new HashSet();
							vartofields.put(base, fields);
						}
						fields.add(fn);

						addObjEdge(
								new StringConstNode(sm, (StringConstant) rhs),
								fn);
					} else {
						GlobalVarNode gn = NodeFactory.v(sm).makeGlobalVarNode(
								sm, ((FieldRef) lhs).getField());

						addObjEdge(
								new StringConstNode(sm, (StringConstant) rhs),
								gn);
					}
				}
				// if rhs is a class constant
				if (rhs instanceof ClassConstant) {
					if (lhs instanceof InstanceFieldRef) {
						FieldVarNode fn = NodeFactory.v(sm).makeFieldVarNode(
								sm, ((FieldRef) lhs).getField(),
								(Local) ((InstanceFieldRef) lhs).getBase());
						Local l = (Local) ((InstanceFieldRef) lhs).getBase();
						LocalVarNode base = NodeFactory.v(sm).makeLocalVarNode(
								sm, l);
						Set fields = (Set) vartofields.get(base);
						if (fields == null) {
							fields = new HashSet();
							vartofields.put(base, fields);
						}
						fields.add(fn);
						addObjEdge(ClassConstNode.node, fn);
					} else {
						GlobalVarNode gn = NodeFactory.v(sm).makeGlobalVarNode(
								sm, ((FieldRef) lhs).getField());
						addObjEdge(ClassConstNode.node, gn);
					}
				}
				return;
			}

			if (!isTypeofInterest(lhs))
				return;

			// case 4.3: local := local
			if (rhs instanceof Local && isTypeofInterest(rhs)) {
				LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm,
						(Local) lhs);
				LocalVarNode rhs_ln = NodeFactory.v(sm).makeLocalVarNode(sm,
						(Local) rhs);
				addVarEdge(rhs_ln, ln);
				return;
			}

			// case 4.4.1: local := string const
			if (rhs instanceof StringConstant) {
				LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm,
						(Local) lhs);
				addObjEdge(new StringConstNode(sm, (StringConstant) rhs), ln);
				return;
			}
			// case 4.4.2: local := class const
			if (rhs instanceof ClassConstant) {
				LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm,
						(Local) lhs);
				addObjEdge(ClassConstNode.node, ln);
				return;
			}

			// case 4.5: local := new X
			if (rhs instanceof NewExpr) {
				LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm,
						(Local) lhs);
				AllocNode on = NodeFactory.v(sm).makeAllocNode(sm,
						(NewExpr) rhs);
				addObjEdge(on, ln);
				return;
			}

			// case 4.6: new array: e.g. x := new Y[5];
			if (rhs instanceof NewArrayExpr) {
				LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm,
						(Local) lhs);
				ArrayAllocNode an = NodeFactory.v(sm).makeArrayAllocNode(sm,
						(NewArrayExpr) rhs);
				addObjEdge(an, ln);
				return;
			}

			// case 4.7: new multi-dimensional array
			if (rhs instanceof NewMultiArrayExpr) {
				LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm,
						(Local) lhs);
				ArrayAllocNode an = NodeFactory.v(sm).makeArrayAllocNode(sm,
						(NewMultiArrayExpr) rhs);
				addObjEdge(an, ln);
				return;
			}

			// case 4.8: rhs is field access x.f or X.f
			if (rhs instanceof FieldRef && isTypeofInterest(rhs)) {

				LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm,
						(Local) lhs);
				if (rhs instanceof InstanceFieldRef) {

					Local l = (Local) ((InstanceFieldRef) rhs).getBase();
					LocalVarNode base = NodeFactory.v(sm).makeLocalVarNode(sm,
							l);
					Set fields = (Set) vartofields.get(base);
					if (fields == null) {
						fields = new HashSet();
						vartofields.put(base, fields);
					}
					FieldVarNode fn = NodeFactory.v(sm).makeFieldVarNode(sm,
							((FieldRef) rhs).getField(),
							(Local) ((InstanceFieldRef) rhs).getBase());
					fields.add(fn);
					addVarEdge(fn, ln);
				} else {
					GlobalVarNode gn = NodeFactory.v(sm).makeGlobalVarNode(sm,
							((FieldRef) rhs).getField());
					SymbolicGlobalObject o = (SymbolicGlobalObject) global2symbolic
							.get(gn);
					if (o == null) {
						o = new SymbolicGlobalObject(sm, gn);
						global2symbolic.put(gn, o);

					}
					addSymbolicEdge(o, gn);
					addVarEdge(gn, ln);
				}
				return;
			}

			// case 4.9: cast

			if (rhs instanceof CastExpr && isTypeofInterest(rhs)) {
				Value y = ((CastExpr) rhs).getOp();
				// possibleTypes.add(lhs.getType());
				if (y instanceof Local && isTypeofInterest(y)) {
					LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm,
							(Local) lhs);
					LocalVarNode rhs_ln = NodeFactory.v(sm).makeLocalVarNode(
							sm, (Local) y);
					addVarEdge(rhs_ln, ln);
				}
				if (y instanceof StringConstant) {
					LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm,
							(Local) lhs);
					addObjEdge(new StringConstNode(sm, (StringConstant) y), ln);
				}
				if (y instanceof ClassConstant) {
					LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm,
							(Local) lhs);
					addObjEdge(ClassConstNode.node, ln);
				}
				return;
			}

			// case 4.10: rhs is array reference
			if (rhs instanceof ArrayRef && isTypeofInterest(rhs)) {
				LocalVarNode ln = NodeFactory.v(sm).makeLocalVarNode(sm,
						(Local) lhs);
				FieldVarNode vn = NodeFactory.v(sm).makeArrayElementVarNode(sm,
						(Local) ((ArrayRef) rhs).getBase());

				LocalVarNode base = NodeFactory.v(sm).makeLocalVarNode(sm,
						((Local) ((ArrayRef) rhs).getBase()));
				Set fields = (Set) vartofields.get(base);
				if (fields == null) {
					fields = new HashSet();
					vartofields.put(base, fields);
				}
				fields.add(vn);
				addVarEdge(vn, ln);
				return;
			}

			if (rhs instanceof BinopExpr) {
				return;
			}

			if (rhs instanceof UnopExpr) {
				return;
			}

			if (rhs instanceof InstanceOfExpr) {
				return;
			}

			return;

		} // AssignStmt

	} // method addStatementCases

	public static boolean isTypeofInterest(Value v) {
		return (v.getType() instanceof RefType || v.getType() instanceof ArrayType);
	}

	@SuppressWarnings("unchecked")
	private void addVarEdge(VarNode src, VarNode trg) {
		if (src == trg)
			return; // avoid self-cycles
		Set x = (Set) var_edges.get(src);
		if (x == null) {
			x = new HashSet();
			var_edges.put(src, x);
		}
		if (!x.contains(trg)) {
			x.add(trg);
		}
	}

	@SuppressWarnings("unchecked")
	private void addObjEdge(AbstractAllocNode src, VarNode trg) {
		// if (src.getType() instanceof RefType
		// || src.getType() instanceof AnySubType) {
		// Type t = src.getType() instanceof RefType ? src.getType()
		// : ((AnySubType) src.getType()).getBase();
		// if (!possibleTypes.contains(t)) {
		// possibleTypes.add(t);
		// Set s = (Set) pending_calls.get(t);
		// if (s != null) {
		// PointerAnalysis.v().resolvePendingCalls(s);
		// possibleTypes.remove(t);
		//
		// }
		//
		// }
		//
		// }

		if (!canObjFlowToVar(src.getType(), trg.getType()))
			return;
		Set x = (Set) obj_edges.get(src);
		if (x == null) {
			x = new HashSet();
			obj_edges.put(src, x);
		}
		if (!x.contains(trg)) {
			x.add(trg);
		}
	}

	@SuppressWarnings("unchecked")
	private void addSymbolicEdge(SymbolicObject o, VarNode trg) {
		Set x = (Set) obj_edges.get(o);
		if (x == null) {
			x = new HashSet();
			obj_edges.put(o, x);
		}
		x.add(trg);
	}

	/*
	 * use type to verify if the edge is spurious
	 */
	public static boolean canObjFlowToVar(Type obj_t, Type var_t) {

		if (var_t == null)
			return true;

		if (obj_t instanceof AnySubType) {
			return true;
		}

		if (var_t instanceof ArrayType || var_t instanceof RefType)
			;
		else
			return false;

		if (var_t == obj_t)
			return true;

		if (obj_t instanceof RefType && var_t instanceof ArrayType) {

			return false;
		}

		if (obj_t instanceof ArrayType && var_t instanceof RefType) {

			SootClass c = ((RefType) var_t).getSootClass();
			if (c.getName().equals("java.lang.Object")
					|| c.getName().equals("java.lang.Cloneable")
					|| c.getName().equals("java.io.Serializable"))
				return true;
			else {

				return false;
			}
		}

		if (obj_t instanceof ArrayType && var_t instanceof ArrayType) {
			Type obj_elem_t = ((ArrayType) obj_t).getElementType();
			Type var_elem_t = ((ArrayType) var_t).getElementType();
			if (((obj_elem_t instanceof ArrayType) || (obj_elem_t instanceof RefType))
					&& ((var_elem_t instanceof ArrayType) || (var_elem_t instanceof RefType))) {
				boolean res = canObjFlowToVar(obj_elem_t, var_elem_t);
				return res;
			} else {

				return false;
			}
		}

		SootClass obj_c = ((RefType) obj_t).getSootClass();
		SootClass var_c = ((RefType) var_t).getSootClass();
		List l;
		if (var_c.isInterface()) {
			l = Scene.v().getActiveHierarchy().getImplementersOf(var_c);
		} else {
			l = Scene.v().getActiveHierarchy().getSubclassesOfIncluding(var_c);
		}

		return l.contains(obj_c);
	}

	@SuppressWarnings("unchecked")
	private void computeClosureOverVarEdges() {
		Set emptySet = new HashSet();
		for (Iterator ita = var_edges.keySet().iterator(); ita.hasNext();) {
			VarNode vn = (VarNode) ita.next();
			Set s = (Set) var_edges.get(vn);

			for (Iterator nodes = s.iterator(); nodes.hasNext();) {
				VarNode n = (VarNode) nodes.next();
				if (!var_edges.containsKey(n)) {
					emptySet.add(n);
				}
			}
		}

		for (Iterator i = emptySet.iterator(); i.hasNext();) {
			VarNode n = (VarNode) i.next();
			testnode = n;
			var_edges.put(n, new HashSet());
		}

		for (Iterator ita = var_edges.keySet().iterator(); ita.hasNext();) {
			VarNode vn = (VarNode) ita.next();
			// if (!visited.contains(vn))
			computeClosure(vn, var_edges);
		}
	}

	private AbstractNode testnode;
	@SuppressWarnings("unchecked")
	private Stack visited = new Stack();

	@SuppressWarnings("unchecked")
	private Set computed = new HashSet();

	@SuppressWarnings("unchecked")
	private Map scc = new HashMap();

	@SuppressWarnings("unchecked")
	private Set computeClosure(AbstractNode node, Map s) {
		Set succ_set = (Set) s.get(node);

		if (visited.contains(node)) {
			// scc
			Set nodes = (Set) scc.get(node);
			if (nodes == null) {
				nodes = new HashSet();
				scc.put(node, nodes);
			}
			int index = visited.indexOf(node);
			for (int i = index + 1; i < visited.size(); i++)
				nodes.add(visited.get(i));
			if (succ_set == null) {
				succ_set = new HashSet();
				// s.put(node, succ_set);
			}
			return succ_set;
		}
		if (computed.contains(node)) {
			if (succ_set == null) {
				succ_set = new HashSet();
				// s.put(node, succ_set);
			}
			return succ_set;
		}

		computed.add(node);
		visited.push(node);
		Set succ = new HashSet();
		if (succ_set != null) {
			Object[] aa = succ_set.toArray();
			for (int i = 0; i < aa.length; i++) {
				AbstractNode n = (AbstractNode) aa[i];
				succ.add(n);
				Set no = computeClosure(n, s);
				succ.addAll(no);
			}
		} else {
			succ_set = new HashSet();
			s.put(node, succ_set);
		}
		visited.pop();
		if (succ_set != null)
			succ_set.addAll(succ);
		if (scc.containsKey(node)) {
			Set nodes = (Set) scc.get(node);
			for (Iterator ita = nodes.iterator(); ita.hasNext();) {
				AbstractNode an = (AbstractNode) ita.next();
				Set set = (Set) s.get(an);
				set.addAll(succ_set);
			}
		}
		return succ;
	}

	// ---
	private void computeClosureOverObjEdges() {
		for (Iterator i = obj_edges.keySet().iterator(); i.hasNext();) {
			AbstractNode on = (AbstractNode) i.next();
			if (!(on instanceof AbstractAllocNode)
					&& !(on instanceof SymbolicObject)) {
				throw new RuntimeException();
			}
			Set succ_set = (Set) obj_edges.get(on);

			// for each successor node, go through its
			// successors; collect all succesors-of-successors
			Set succ_succ_set = new HashSet();
			for (Iterator succ_i = succ_set.iterator(); succ_i.hasNext();) {
				Set x = (Set) var_edges.get(succ_i.next());
				if (x != null) {
					// look at all elemements of x, and add them
					// to succ_succ_set, using type-based filtering
					for (Iterator jjj = x.iterator(); jjj.hasNext();) {
						VarNode vn = (VarNode) jjj.next();

						if (on instanceof SymbolicObject
								|| canObjFlowToVar(on.getType(), vn.getType()))
							succ_succ_set.add(vn);
					}
				}
			}
			succ_set.addAll(succ_succ_set);
		}
	}
}
