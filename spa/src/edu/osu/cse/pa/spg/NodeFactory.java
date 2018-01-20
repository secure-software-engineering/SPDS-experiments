package edu.osu.cse.pa.spg;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import soot.Local;
import soot.RefType;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.jimple.ClassConstant;
import soot.jimple.IdentityRef;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.StringConstant;

public class NodeFactory {

	private static NodeFactory instance = null;

	private Map /** <Local, LocalVarNode> */
	local2Var = new HashMap();

	private Map /** <IdentityRef, ParameterVarNode> */
	param2Var = new HashMap();

	private static Map /** <SootField, GlobalVarNode> */
	global2Var = new HashMap();

	private Map /** <NewExpr, AllocNode> */
	new2Alloc = new HashMap();

	private Map /** <Local, Map <SootField, FieldVarNode>> */
	field2Var = new HashMap();

	private Map /** <SootMethod, ReturnVarNode> */
	returnNodes = new HashMap();

	private static Map /** <SootMethod, NodeFactory> */
	factories = new HashMap();

	private NodeFactory(SootMethod sm) {

	}

	public static NodeFactory v(SootMethod sm) {
		instance = (NodeFactory) factories.get(sm);
		if (instance == null) {
			instance = new NodeFactory(sm);
			factories.put(sm, instance);
		}
		return instance;
	}

	public static Iterator factories() {
		return factories.values().iterator();
	}

	public static void remove(SootMethod sm) {
		factories.remove(sm);
	}

	public Iterator localVarNodes() {
		return local2Var.values().iterator();
	}

	public Iterator objsNodes() {
		return new2Alloc.values().iterator();
	}



	public AllocNode makeAllocNode(SootMethod method, NewExpr obj) {
		AllocNode node = new AllocNode(method, obj);
		new2Alloc.put(obj, node);
		return node;
	}

	public ArrayAllocNode makeArrayAllocNode(SootMethod method,
			NewArrayExpr expr) {
		ArrayAllocNode node = new ArrayAllocNode(method, expr);
		new2Alloc.put(expr, node);
		return node;
	}

	public AnySubTypeNode makeAnySubTypeNode(SootMethod method, RefType baseType) {
		AnySubTypeNode node = new AnySubTypeNode(method, baseType);
		return node;
	}

	public ArrayAllocNode makeArrayAllocNode(SootMethod method,
			NewMultiArrayExpr expr) {
		ArrayAllocNode node = new ArrayAllocNode(method, expr);
		new2Alloc.put(expr, node);
		return node;
	}

	public LocalVarNode makeLocalVarNode(SootMethod method, Local l) {
		LocalVarNode node = findLocalVarNode(l);
		if (node != null)
			return node;
		node = new LocalVarNode(method, l);
		local2Var.put(l, node);
		return node;
	}

	public ParameterVarNode makeParamVarNode(SootMethod method, IdentityRef ref) {
		ParameterVarNode node = findParamVarNode(ref);
		if (node != null)
			return node;
		node = new ParameterVarNode(method, ref);
		param2Var.put(ref, node);
		return node;
	}

	public FieldVarNode makeFieldVarNode(SootMethod method, SootField f,
			Local node) {
		FieldVarNode n = findFieldVarNode(node, f);
		if (n != null)
			return n;
		n = new FieldVarNode(method, f, node);
		Map m = (Map) field2Var.get(node);
		if (m == null) {
			m = new HashMap();
			field2Var.put(node, m);
		}
		m.put(f, n);
		return n;
	}

	public ReturnVarNode makeReturnVarNode(SootMethod sm) {
		if (returnNodes.containsKey(sm))
			return (ReturnVarNode) returnNodes.get(sm);
		ReturnVarNode node = new ReturnVarNode(sm);
		returnNodes.put(sm, node);
		return node;
	}

	public GlobalVarNode makeGlobalVarNode(SootMethod method, SootField f) {
		GlobalVarNode node = findGlobalVarNode(f);
		if (node != null)
			return node;
		node = new GlobalVarNode(method, f);
		global2Var.put(f, node);

		SootClass sc = f.getDeclaringClass();
		if (sc.declaresMethod("void <clinit>()")) {
			SootMethod m = sc.getMethod("void <clinit>()");
			if (!SymbolicPointerGraph.getClinits().contains(m)) {
				SymbolicPointerGraph.getClinits().add(m);
			}
		}

		return node;
	}

	public FieldVarNode makeArrayElementVarNode(SootMethod method, Local l) {
		FieldVarNode node = findFieldVarNode(l, ArrayElementField.v());
		if (node != null)
			return node;
		node = new FieldVarNode(method, ArrayElementField.v(), l);
		Map m = (Map) field2Var.get(l);
		if (m == null) {
			m = new HashMap();
			field2Var.put(l, m);
		}
		m.put(ArrayElementField.v(), node);
		return node;
	}

	// public ContextVarNode makeContextVarNode(InstantiationGraph g, VarNode v,
	// AbstractRelativeContext context) {
	// ContextVarNode node = new ContextVarNode(g, v, context);
	// return node;
	// }
	//
	// public ContextAllocNode makeContextAllocNode(InstantiationGraph g,
	// AbstractAllocNode v, AbstractRelativeContext context) {
	// ContextAllocNode node = new ContextAllocNode(g, v, context);
	// return node;
	// }

	public LocalVarNode findLocalVarNode(Local l) {
		return (LocalVarNode) local2Var.get(l);
	}
	
	public LocalVarNode findLocalVarNodefromName(String name){
		for(Iterator locals = local2Var.keySet().iterator(); locals.hasNext();){
			Local l = (Local)locals.next();
			if(l.getName().equals(name)){
				return (LocalVarNode) local2Var.get(l);
			}
		}
		return null;
	}

	public ReturnVarNode findReturnVarNode(SootMethod sm) {
		return (ReturnVarNode) returnNodes.get(sm);
	}

	public FieldVarNode findFieldVarNode(Local base, SootField f) {
		Map m = (Map) field2Var.get(base);
		if (m != null) {
			FieldVarNode node = (FieldVarNode) m.get(f);

			return node;
		}
		return null;
	}

	public ParameterVarNode findParamVarNode(IdentityRef ref) {
		return (ParameterVarNode) param2Var.get(ref);
	}

	public static GlobalVarNode findGlobalVarNode(SootField f) {
		return (GlobalVarNode) global2Var.get(f);
	}

	public AllocNode findAllocNode(NewExpr newexpr) {
		return (AllocNode) new2Alloc.get(newexpr);
	}

	public ArrayAllocNode finArrayAllocNode(NewArrayExpr expr) {
		ArrayAllocNode node = (ArrayAllocNode) new2Alloc.get(expr);
		return node;
	}

	public ArrayAllocNode finArrayAllocNode(NewMultiArrayExpr expr) {
		ArrayAllocNode node = (ArrayAllocNode) new2Alloc.get(expr);
		return node;
	}

}
