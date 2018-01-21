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
package experiments.typestate.microbench.tests;

import com.ibm.safe.internal.exceptions.SafeException;

import experiments.typestate.microbench.TypestateRegressionUnit;

public final class SignatureTest extends AbstractTestCase {

  public void testSignatureExample1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.security.SignatureExample1", 1);
    test.selectTypestateRule("Signature");
    run(test);
  }


  public void testSignatureExample2() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.security.SignatureExample2", 0);
    test.selectTypestateRule("Signature");
    run(test);
  }


  public void testSignatureExample3() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.security.SignatureExample3", 1);
    test.selectTypestateRule("Signature");
    run(test);
  }


  public void testSignatureExample4() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.security.SignatureExample4", 0);
    test.selectTypestateRule("Signature");
    run(test);
  }

  public void testSignatureExample5() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.security.SignatureExample5", 1);
    test.selectTypestateRule("Signature");
    run(test);
  }

  public void testSignatureExample6() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.security.SignatureExample6", 1);
    test.selectTypestateRule("Signature");
    run(test);
  }

}
