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
package j2se.typestate.security;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.Provider;

/**
 * Should not produce a warning.
 * 
 * @author egeay
 */
public final class KeyStoreExample3 {

  public static void main(final String[] args) {
    try {
      final KeyStore keyStore = KeyStore.getInstance("JKS");
      if(keyStore != null)
    	  keyStore.load(new FileInputStream("C:/temp/myKey.dat"), getPassword());
      // ... Some code
      Provider size = keyStore.getProvider();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static char[] getPassword() {
    return new char[] { 'm', 'y', 'p', 'a', 's', 's' };
  }

}
