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
/*
 * Created on Jan 22, 2005
 */
package com.ibm.safe.processors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.ibm.safe.cha.J2SEClassHierarchyEngine;
import com.ibm.safe.reporting.IReporter;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.Predicate;

/**
 * @author Eran Yahav (yahave)
 * 
 */
public class BaseProgramProcessor implements ProgramProcessor {

  private final Predicate<IClass> classFilter;

  private final IReporter reporter;

  /** underlying class hierarchy */
  protected IClassHierarchy classHierarchy;

  /** underlying callgraph */
  protected CallGraph callGraph;

  /**
   * List of ClassProcessors
   */
  protected List<ClassProcessor> classProcessors;

  protected BaseClassProcessor baseClassProcessor;


  public BaseProgramProcessor(IClassHierarchy hierarchy, CallGraph callGraph, IReporter safeReporter, Predicate<IClass> classFilter) {
    assert (hierarchy != null);
    this.classHierarchy = hierarchy;
    this.callGraph = callGraph;
    this.classProcessors = new ArrayList<ClassProcessor>();
    this.baseClassProcessor = new BaseClassProcessor(hierarchy, callGraph);
    this.reporter = safeReporter;
    this.classFilter = classFilter;
    classProcessors.add(baseClassProcessor);
  }

  public void addClassProcessor(ClassProcessor cp) {
    classProcessors.add(cp);
  }

  public BaseClassProcessor getBaseClassProcessor() {
    return baseClassProcessor;
  }

  public void process() throws CancelException {
    final IClass[] classes = getAcceptedClasses();

    for (int i = 0; i < classes.length; ++i) {
      final IClass currentClass = classes[i];
      reporter.process(currentClass);


      for (Iterator<ClassProcessor> it = classProcessors.iterator(); it.hasNext();) {
        ClassProcessor cp = it.next();
        cp.processProlog(currentClass);
        cp.process(currentClass);
        cp.processEpilog(currentClass);
      }

    }
  }

  private IClass[] getAcceptedClasses() {
    final List<IClass> classesList = new LinkedList<IClass>();
    for (IClass currentClass : classHierarchy) {
      if (J2SEClassHierarchyEngine.isApplicationClass(currentClass) && this.classFilter.test(currentClass)) {
        classesList.add(currentClass);
      }
    }
    return classesList.toArray(new IClass[classesList.size()]);
  }

  private String getClassName(final IClass currentClass) {
    return currentClass.getName().toString().substring(1).replace('/', '.');
  }

  public Object getResult() {
    return null;
  }

}
