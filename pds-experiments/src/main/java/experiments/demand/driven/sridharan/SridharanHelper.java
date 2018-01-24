package experiments.demand.driven.sridharan;

import java.util.HashSet;

import com.google.common.collect.Sets;

import soot.PointsToSet;
import soot.jimple.spark.ondemand.AllocAndContext;
import soot.jimple.spark.ondemand.AllocAndContextSet;
import soot.jimple.spark.ondemand.WrappedPointsToSet;
import soot.jimple.spark.ondemand.genericutil.ObjectVisitor;
import soot.jimple.spark.pag.AllocNode;
import soot.jimple.spark.sets.PointsToSetInternal;

public class SridharanHelper {

	public static int getPointsToSize(PointsToSet reachingObjects) {
		if (reachingObjects instanceof PointsToSetInternal) {
			PointsToSetInternal i = (PointsToSetInternal) reachingObjects;
			return i.size();
		} else if (reachingObjects instanceof AllocAndContextSet) {
			final HashSet<AllocNode> flat = Sets.newHashSet();
			AllocAndContextSet set = ((AllocAndContextSet) reachingObjects);
			set.forall(new ObjectVisitor<AllocAndContext>() {
				
				@Override
				public void visit(AllocAndContext obj_) {
					if(obj_.alloc.getMethod() == null)
						return;
					if(!obj_.alloc.getMethod().getDeclaringClass().toString().startsWith("java."))
						flat.add(obj_.alloc);
				}
			});
			return flat.size();
		} else if(reachingObjects instanceof WrappedPointsToSet){
			WrappedPointsToSet wrappedPointsToSet = (WrappedPointsToSet) reachingObjects;
			return wrappedPointsToSet.getWrapped().size();
		}else{
			System.out.println("Sure?" + reachingObjects.getClass());
		}
		return 0;
	}

}
