/*
 * NIntConstantNode.java - part of the GATOR project
 *
 * Copyright (c) 2018 The Ohio State University
 *
 * This file is distributed under the terms described in LICENSE in the
 * root directory.
 */

package presto.android.gui.graph;

import soot.Scene;
import soot.SootClass;

public class NIntConstantNode extends NObjectNode {
  public Integer value;

  @Override
  public SootClass getClassType() {
    return Scene.v().getSootClass("java.lang.Integer");
  }

  @Override
  public String toString() {
    return "IntConst[" + value + "]" + id;
  }
}
