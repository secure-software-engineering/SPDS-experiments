package iohoister.analysis.lattice;

import java.util.HashSet;
import java.util.Set;

import soot.Local;

public abstract class LatticeElement {
	
	protected Set<Local> locals = new HashSet<Local>();
	
	public abstract LatticeElement meet(LatticeElement le);
	
	public abstract LatticeElement join(LatticeElement le);
	
	public void registerVariable(Local l){
		locals.add(l);
	}
	
	public void unregisterVariable(Local l){
		locals.remove(l);
	}

}
