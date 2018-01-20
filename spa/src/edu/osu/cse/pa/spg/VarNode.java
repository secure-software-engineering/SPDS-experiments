package edu.osu.cse.pa.spg;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import soot.SootMethod;
import soot.Type;
import soot.util.ArraySet;

public abstract class VarNode extends AbstractNode {

	private Set<PointsToEdge> pointsToEdges = new ArraySet<PointsToEdge>();

	VarNode(SootMethod sm) {
		super(sm);
	}

	public void addPointsToEdge(PointsToEdge pointsToEdge) {
		pointsToEdges.add(pointsToEdge);
	}

	public Iterator<PointsToEdge> getPointsToEdges() {
		return pointsToEdges.iterator();
	}

}
