package edu.osu.cse.pa.spg.symbolic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BooleanExpression {
	@SuppressWarnings("unchecked")
	private List expression;

	@SuppressWarnings("unchecked")
	private Map /* <Integer, Set<Integer>> */indices;

	private int value = UNKNOWN;

	public static final int UNKNOWN = -1;

	public static final int TRUE = 1;

	public static final int FALSE = 0;

	@SuppressWarnings("unchecked")
	public BooleanExpression() {
		expression = new ArrayList();
		indices = new HashMap();
	}

	@SuppressWarnings("unchecked")
	public BooleanExpression clone() {
		BooleanExpression ex = new BooleanExpression();
		for (int i = 0; i < expression.size(); i++) {
			Set s = (Set) expression.get(i);
			Set news = new HashSet();
			ex.expression.add(news);
			for (Iterator ins = s.iterator(); ins.hasNext();) {
				Integer id = (Integer) ins.next();
				Integer newId = new Integer(id.intValue());
				news.add(newId);
				Set ind = (Set) ex.indices.get(newId);
				if (ind == null) {
					ind = new HashSet();
					ex.indices.put(newId, ind);
				}
				ind.add(new Integer(ex.expression.size() - 1));

			}

		}
		return ex;
	}

	@SuppressWarnings("unchecked")
	public void addAnd(int[] conditions) {
		List newList = new ArrayList();

		for (int j = 0; j < conditions.length; j++) {
			if (expression.size() == 0) {
				Set s = new HashSet();
				s.add(new Integer(conditions[j]));
				newList.add(s);
				continue;
			}

			for (int i = 0; i < expression.size(); i++) {
				Set item = (Set) expression.get(i);
				item.add(new Integer(conditions[j]));
				newList.add(item);
				// for (Iterator it = item.iterator(); it.hasNext();) {
				// Integer ind = (Integer) it.next();
				// Set s = (Set) indices.get(ind);
				// if (s == null) {
				// s = new HashSet();
				// indices.put(ind, s);
				// }
				// s.add(new Integer(newList.size() - 1));
				// }
			}

		}
		expression.clear();
		expression = newList;
		update();

	}

	@SuppressWarnings("unchecked")
	public void addOr(BooleanExpression be) {
		for (int j = 0; j < be.expression.size(); j++) {
			Set s = (Set) be.expression.get(j);
			if (!expression.contains(s)) {
				expression.add(s);
			}
//			for (Iterator ite = s.iterator(); ite.hasNext();) {
//				Integer i = (Integer) ite.next();
//				Set set = (Set) indices.get(i);
//				if (set == null) {
//					set = new HashSet();
//					indices.put(i, set);
//				}
//				set.add(new Integer(expression.size() - 1));
//			}
		}
		update();
	}

	@SuppressWarnings("unchecked")
	public void replace(int oldid, int newid) {
		Set s = (Set) indices.get(new Integer(oldid));
		if (s == null)
			return;
		Assert.assertTrue(s != null);
		for (Iterator ite = s.iterator(); ite.hasNext();) {
			Integer index = (Integer) ite.next();
			Set item = (Set) expression.get(index.intValue());
			item.remove(new Integer(oldid));
			item.add(new Integer(newid));
		}

		// indices.remove(new Integer(oldid));
		// indices.put(new Integer(newid), s);
		update();

	}

	@SuppressWarnings("unchecked")
	public int setValue(int id, boolean v) {

		Set in = (Set) indices.get(new Integer(id));
		if (in != null) {
			for (Iterator ids = in.iterator(); ids.hasNext();) {
				Integer item = (Integer) ids.next();
				if (item.intValue() >= expression.size()) {
					update();
				}
				if (item.intValue() >= expression.size()) {
					return value;
				}
				Set s = (Set) expression.get(item.intValue());
				if (v) {
					s.remove(new Integer(id));
					if (s.isEmpty())
						value = 1;

				} else {
					expression.remove(item.intValue());
					if (expression.size() == 0) {
						value = 0;
					}
				}

			}

			// update indices

		}
		update();
		return value;

	}

	@SuppressWarnings("unchecked")
	private void update() {
		indices.clear();

		// merge equivalent int sets
		Set set = new HashSet();
		set.addAll(expression);
		expression.clear();
		expression.addAll(set);
		for (int i = 0; i < expression.size(); i++) {
			Set item = (Set) expression.get(i);
			for (Iterator it = item.iterator(); it.hasNext();) {
				Integer ind = (Integer) it.next();
				Set s = (Set) indices.get(ind);
				if (s == null) {
					s = new HashSet();
					indices.put(ind, s);
				}
				s.add(new Integer(i));
			}
		}
	}

}
