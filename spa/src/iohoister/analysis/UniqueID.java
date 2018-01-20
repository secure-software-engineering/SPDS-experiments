package iohoister.analysis;

import java.util.HashMap;
import java.util.Map;

public class UniqueID {
	private static Map<String, Integer> idMap = new HashMap<String, Integer>();

	public static Integer getUniqueID(String st) {
		Integer id = idMap.get(st);
		if (id == null) {
			idMap.put(st, new Integer(1));
			return new Integer(0);
		} else {
			idMap.put(st, new Integer(id.intValue() + 1));
			return id;
		}

	}

	public static void clear() {
		idMap.clear();
	}

}
