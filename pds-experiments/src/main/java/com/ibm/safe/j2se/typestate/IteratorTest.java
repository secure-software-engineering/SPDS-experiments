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

public final class IteratorTest extends AbstractTestCase {

	public void testIteratorExample1() throws SafeException, Exception {
		TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample1", 0);
		test.selectTypestateRule("IteratorHasNext");
		run(test);
	}

	public void testIteratorExample2() throws SafeException, Exception {
		TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample2", 0);
		test.selectTypestateRule("IteratorHasNext");
		run(test);
	}

	public void testIteratorExample3() throws SafeException, Exception {
		TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample3", 1);
		test.selectTypestateRule("IteratorHasNext");
		run(test);
	}

	public void testIteratorExample4() throws SafeException, Exception {
		TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample4", 0);
		test.selectTypestateRule("IteratorHasNext");
		run(test);
	}

	public void testIteratorExample5() throws SafeException, Exception {
		TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample5", 1);
		test.selectTypestateRule("IteratorHasNext");
		run(test);
	}

	public void testIteratorExample6() throws SafeException, Exception {
		TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample6", 1);
		test.selectTypestateRule("IteratorHasNext");
		run(test);
	}
	public void testIteratorExample7() throws SafeException, Exception {
		TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample7", 1);
		test.selectTypestateRule("IteratorHasNext");
		run(test);
	}public void testIteratorExample8() throws SafeException, Exception {
		TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample8", 1);
		test.selectTypestateRule("IteratorHasNext");
		run(test);
	}
	public void testIteratorExample9() throws SafeException, Exception {
		TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample9", 0);
		test.selectTypestateRule("IteratorHasNext");
		run(test);
	}

	public void testIteratorExample10() throws SafeException, Exception {
		TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample10", 0);
		test.selectTypestateRule("IteratorHasNext");
		run(test);
	}

	public void testIteratorExample11() throws SafeException, Exception {
		TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample11", 0);
		test.selectTypestateRule("IteratorHasNext");
		run(test);
	}
	public void testIteratorExample12() throws SafeException, Exception {
		TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample12", 0);
		test.selectTypestateRule("IteratorHasNext");
		run(test);
	}

	public void testIteratorExample13() throws SafeException, Exception {
		TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample13", 0);
		test.selectTypestateRule("IteratorHasNext");
		run(test);
	}

	public void testIteratorExample14() throws SafeException, Exception {
		TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample14", 0);
		test.selectTypestateRule("IteratorHasNext");
		run(test);
	}
	public void testIteratorExample15() throws SafeException, Exception {
		TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample15", 0);
		test.selectTypestateRule("IteratorHasNext");
		run(test);
	}
	public void testIteratorExample16() throws SafeException, Exception {
		TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample16", 1);
		test.selectTypestateRule("IteratorHasNext");
		run(test);
	}
	public void testIteratorExample17() throws SafeException, Exception {
		TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample17", 1);
		test.selectTypestateRule("IteratorHasNext");
		run(test);
	}
}