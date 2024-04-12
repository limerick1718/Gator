/*
 * NStringBuilderNode.java - part of the GATOR project
 *
 * Copyright (c) 2018 The Ohio State University
 *
 * This file is distributed under the terms described in LICENSE in the
 * root directory.
 */

package presto.android.gui.graph;

import com.google.common.collect.Sets;
import soot.SootMethod;
import soot.jimple.Expr;
import soot.jimple.Stmt;

import java.util.Set;

public class NStringBuilderNode extends NAllocNode {
  public Set<String> possibleValues = Sets.newConcurrentHashSet();
  Stmt stmt;
  public SootMethod inMethod;

  public NStringBuilderNode(Expr e, Stmt stmt, SootMethod method) {
    super();
    this.e = e;
    this.stmt = stmt;
    this.inMethod = method;
    this.possibleValues.add("");
  }

  @Override
  public String toString() {
    String vals = String.join(",", possibleValues);
    return "StringBuilder[" + (vals.isEmpty() ? "<empty-string>" : vals) + "]" + id;
  }
}
