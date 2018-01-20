package iohoister.analysis;

import soot.SootMethod;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.Edge;

public class DefUseEdge {
	private Stmt use;
	private Stmt def;
	private boolean isBasePointerDefUse;
	private boolean isReturnEdge;
	private boolean isParamEdge;
	private boolean isHeapAccess;
	private SootMethod useMethod;
	private SootMethod defMethod;
	private Edge callEdge;

	public DefUseEdge(Stmt use, Stmt def, SootMethod useMethod,
			SootMethod defMethod) {
		DefUseAnalysis.numDefUseEdgeCreated++;
		this.use = use;
		this.def = def;
		isBasePointerDefUse = false;
		isReturnEdge = false;
		isParamEdge = false;
		callEdge = null;
		isHeapAccess = false;
		this.useMethod = useMethod;
		this.defMethod = defMethod;
	}

	public void setHeapAccess() {
		isHeapAccess = true;
	}

	public boolean isHeapAccess() {
		return isHeapAccess;
	}

	public SootMethod getUseMethod() {
		return useMethod;
	}

	public SootMethod getDefMethod() {
		return defMethod;
	}

	public void setPointer() {
		isBasePointerDefUse = true;
	}

	public boolean equals(Object o) {
		if (!(o instanceof DefUseEdge))
			return false;
		DefUseEdge e = (DefUseEdge) o;
		if (use == e.use && def == e.def)
			return true;
		return false;
	}

	public boolean isBasePointerDefUse() {
		return isBasePointerDefUse;
	}

	public Stmt getUse() {
		return use;
	}

	public Stmt getDef() {
		return def;
	}

	public void setIsParam(Edge call) {
		isParamEdge = true;
		this.callEdge = call;
	}

	public void setIsReturn(Edge call) {
		isReturnEdge = true;
		this.callEdge = call;
	}

	public boolean isReturnEdge() {
		return isReturnEdge;
	}

	public boolean isParamEdge() {
		return isParamEdge;
	}

	public Edge getCallEdge() {
		return callEdge;
	}

}
