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
package com.ibm.safe.cha;

import java.io.IOException;

import com.ibm.safe.internal.exceptions.SafeException;
import com.ibm.safe.options.CommonOptions;
import com.ibm.safe.perf.PerformanceTracker;
import com.ibm.safe.perf.PerformanceTracker.Stages;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.client.AbstractAnalysisEngine;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.MonitorUtil.IProgressMonitor;
import com.ibm.wala.util.NullProgressMonitor;
import com.ibm.wala.util.debug.Assertions;

/**
 * @TODO: Should clean out the extension of AbstractAnalysisEngine
 * because it now implies the ability to get the callgraph builder.
 * While doable here, that is confusing.  
 */
public class J2SEClassHierarchyEngine extends AbstractAnalysisEngine {

  private CommonOptions commonOptions;

  public J2SEClassHierarchyEngine(final CommonOptions commonOptions, final PerformanceTracker domoPerfoTracker) {
    this.perfoTracker = domoPerfoTracker;
    this.commonOptions = commonOptions;
    this.progressMonitor = null;
  }

  // --- Overridden methods

  public IClassHierarchy buildClassHierarchy() {
    try {
      if (this.perfoTracker != null) {
        this.perfoTracker.startTracking(Stages.CHA.toString());
      }

      super.scope = this.commonOptions.getOrCreateAnalysisScope();
      setClassHierarchy(createClassHierarchyInstance());
    } catch (SafeException except) {
      throw new RuntimeException("Unable to build class hierarchy.", except);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
	} finally {
      if (this.perfoTracker != null) {
        this.perfoTracker.stopTracking(Stages.CHA.toString());
      }
    }
    return getClassHierarchy();
  }

  // --- Private code

  protected final PerformanceTracker getPerformanceTracker() {
    return this.perfoTracker;
  }

  private ClassHierarchy createClassHierarchyInstance() throws SafeException {
      try {
		return ClassHierarchy.make(getScope(), new NullProgressMonitor());
	} catch (ClassHierarchyException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return null;
  }

  private final PerformanceTracker perfoTracker;

  private final IProgressMonitor progressMonitor;

  public static boolean isApplicationClass(IClass klass) {
    boolean result = true;
    ClassLoaderReference loaderRef = klass.getClassLoader().getReference();
    if (loaderRef.equals(ClassLoaderReference.Extension) || loaderRef.equals(ClassLoaderReference.Primordial)) {
      result = false;
    }
    return result;
  }

  @Override
  protected CallGraphBuilder getCallGraphBuilder(IClassHierarchy cha, AnalysisOptions options, AnalysisCache cache) {
    Assertions.UNREACHABLE("Not meant to build a call graph through ClassHierarchyEngine!");
    return null;
  }
}
