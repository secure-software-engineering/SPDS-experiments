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
package com.ibm.safe.j2se.typestate;

import com.ibm.safe.internal.exceptions.SafeException;

import microbench.AbstractTestCase;
import microbench.TypestateRegressionUnit;

public final class URLConnectionTest extends AbstractTestCase {

  public void testURLConnectionExample1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.URLConnection.URLConnectionExample1", 0);
    test.selectTypestateRule("URLConnection");
    run(test);
  }

  public void testURLConnectionExample2() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.URLConnection.URLConnectionExample2", 1);
    test.selectTypestateRule("URLConnection");
    run(test);
  }
}
