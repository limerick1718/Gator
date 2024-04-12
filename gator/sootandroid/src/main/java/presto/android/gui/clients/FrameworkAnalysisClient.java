/*
 * FrameworkAnalysisClient.java - part of the GATOR project
 *
 * Copyright (c) 2018 The Ohio State University
 *
 * This file is distributed under the terms described in LICENSE in the
 * root directory.
 */
package presto.android.gui.clients;

import com.google.common.collect.Sets;
import presto.android.Configs;
import presto.android.gui.GUIAnalysisClient;
import presto.android.gui.GUIAnalysisOutput;
import soot.*;
import soot.jimple.AnyNewExpr;
import soot.jimple.AssignStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.Stmt;

import java.util.Iterator;
import java.util.Set;

public class FrameworkAnalysisClient implements GUIAnalysisClient {
  GUIAnalysisOutput output;

  final String activityInfoClassName = "android.content.pm.ActivityInfo";
  SootClass activityInfoClass;
  Type activityInfoType;

  @Override
  public void run(GUIAnalysisOutput output) {
    this.output = output;
    init();
    for (SootClass c : Scene.v().getLibraryClasses()) {
      if (c.getName().startsWith("java")) {
        continue;
      }
      for (SootMethod m : c.getMethods()) {
        if (!m.isConcrete()) {
          continue;
        }
        processMethod(m);
      }
    }
  }

  void init() {
    activityInfoClass = Scene.v().getSootClass(activityInfoClassName);
    if (activityInfoClass.isPhantom()) {
      throw new RuntimeException(activityInfoClassName
              + " is phantom for android-" + Configs.numericApiLevel);
    }
    activityInfoType = activityInfoClass.getType();
  }

  void processMethod(SootMethod m) {
    Set<InstanceFieldRef> readSet = Sets.newHashSet();
    Set<InstanceFieldRef> writeSet = Sets.newHashSet();

    Body b = m.retrieveActiveBody();
    Iterator<Unit> stmts = b.getUnits().iterator();
    while (stmts.hasNext()) {
      Stmt s = (Stmt) stmts.next();
      if (s instanceof AssignStmt) {
        Value rhs = ((AssignStmt) s).getRightOp();
        if (rhs instanceof InstanceFieldRef) {
          InstanceFieldRef fieldRef = ((InstanceFieldRef) rhs);
          Type baseType = fieldRef.getBase().getType();
          if (baseType.equals(activityInfoType)) {
            System.out.println("  * [READ] " + fieldRef.getField()
                    + " | " + s + " @ " + m);
            readSet.add(fieldRef);
          }
        }
        Value lhs = ((AssignStmt) s).getLeftOp();
        if (lhs instanceof InstanceFieldRef) {
          InstanceFieldRef fieldRef = ((InstanceFieldRef) lhs);
          Type baseType = fieldRef.getBase().getType();
          if (baseType.equals(activityInfoType)) {
            System.out.println("  * [WRITE] " + fieldRef.getField()
                    + " | " + s + " @ " + m);
            writeSet.add(fieldRef);
          }
        }
        if (rhs instanceof AnyNewExpr) {
          Type allocType = rhs.getType();
          if (allocType.equals(activityInfoType)) {
            System.out.println("  * [ALLOC] " + s + " @ " + m);
          }
        }
      } // assignment
    } // statements

    if (!readSet.isEmpty() || !writeSet.isEmpty()) {
      System.out.println("--- " + m);
    }
    for (InstanceFieldRef f : readSet) {
      System.out.println("  * {R} " + f);
    }
    for (InstanceFieldRef f : writeSet) {
      System.out.println("  * {W} " + f);
    }
  }
}
