package alias;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import edu.osu.cse.pa.spg.AbstractAllocNode;

public class AliasCache {
	private static Map<Pair<AbstractAllocNode, AbstractAllocNode>, Boolean> cache
		= new HashMap<Pair<AbstractAllocNode, AbstractAllocNode>, Boolean>();
	
	public static void addAliasInfo(AbstractAllocNode n1, AbstractAllocNode n2, boolean b) {
		Boolean val = getCache(n1, n2);
		if (val == null) {
			cache.put(new Pair<AbstractAllocNode, AbstractAllocNode>(n1, n2), b);
		}
	}
	
	public static Boolean getCache(AbstractAllocNode n1, AbstractAllocNode n2) {
		Iterator<Pair<AbstractAllocNode, AbstractAllocNode>> pairIter = cache.keySet().iterator();
		while (pairIter.hasNext()) {
			Pair<AbstractAllocNode, AbstractAllocNode> pair = pairIter.next();
			if ( (pair.first == n1 && pair.second == n2) ||
				 (pair.first == n2 && pair.second == n1) ) {
				return cache.get(pair);
			}
		}
		return null;
	}	
}
