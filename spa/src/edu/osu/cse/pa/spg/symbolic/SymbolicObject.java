package edu.osu.cse.pa.spg.symbolic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import edu.osu.cse.pa.spg.AbstractAllocNode;
import edu.osu.cse.pa.spg.AbstractNode;
import edu.osu.cse.pa.spg.SymbolicPointerGraph;

import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;

public abstract class SymbolicObject extends AbstractAllocNode{

	protected Type basicType;

	protected List typelist;
	
	//protected SymbolicObject orig = null;



	public SymbolicObject(Type basicType, SootMethod m) 
	{
		super(m);
		this.basicType = basicType;
		//orig = this;
	}


	

	public List getPossibleTypes() {
		if (typelist == null) {
			typelist = new ArrayList();
			if (basicType instanceof RefType) {
				SootClass sc = ((RefType) basicType).getSootClass();
				List l;
				if (!sc.isInterface()) {
					l = Scene.v().getActiveHierarchy()
							.getSubclassesOfIncluding(sc);
				} else {
					l = Scene.v().getActiveHierarchy().getImplementersOf(sc);
				}
				for (int i = 0; i < l.size(); i++) {
					sc = (SootClass) l.get(i);
					typelist.add(sc.getType());
				}

			} else {
				typelist.add(basicType);
				typelist.add(Scene.v().getSootClass("java.lang.Object")
						.getType());
			}
		}

		return typelist;
	}



	public Type getType() {
		return basicType;
	}
}
