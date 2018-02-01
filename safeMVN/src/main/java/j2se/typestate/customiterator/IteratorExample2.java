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

package j2se.typestate.customiterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IteratorExample2 {

  public static void main(String[] args) {
    List l1 = new ArrayList();
    List l2 = new ArrayList();
    Iterator iterator = l1.iterator();
    iterator.next();
  }
}
