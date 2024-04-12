/*
 * NStringBuilderAppendOpNode.java - part of the GATOR project
 *
 * Copyright (c) 2018 The Ohio State University
 *
 * This file is distributed under the terms described in LICENSE in the
 * root directory.
 */

package presto.android.gui.graph;

import soot.SootMethod;
import soot.jimple.Stmt;
import soot.toolkits.scalar.Pair;

public class NStringBuilderAppendOpNode extends NOpNode {
  NVarNode bldrNode;
  NNode strNode;

  public NStringBuilderAppendOpNode(NVarNode bldrNode, NNode strNode, Pair<Stmt, SootMethod> callSite) {
    super(callSite, true);
    bldrNode.addEdgeTo(this);
    this.bldrNode = bldrNode;
    strNode.addEdgeTo(this);
    this.strNode = strNode;
  }

  @Override
  public boolean hasReceiver() {
    return true;
  }

  @Override
  public NVarNode getReceiver() {
    return bldrNode;
  }

  @Override
  public boolean hasParameter() {
    return true;
  }

  @Override
  public NNode getParameter() {
    return strNode;
  }

  @Override
  public boolean hasLhs() {
    return false;
  }
}
