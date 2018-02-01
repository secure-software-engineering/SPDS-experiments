/*******************************************************************************
 * Copyright (c) 2004-2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*********************************************************************
 * Name: IteratorExample1.java
 * Description: A correct usage of iterator.
 * Expected Result: this case should not report any alarms.
 * Author: Eran Yahav (eyahav)
 *********************************************************************/

package j2se.typestate.iterator;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Unique engine will succeed only with fine-grained intraprocedural live
 * analysis
 */
public class IteratorExample16 {

	public static void main(String[] args) {
		List list = new LinkedList();
		list.add(new Object());
		java.util.Iterator iterator = list.iterator();
		iterator.hasNext();
		iterator.next();
		foo(iterator);
	}

	private static void foo(Iterator iterator) {
		iterator.next();		
	}

}
