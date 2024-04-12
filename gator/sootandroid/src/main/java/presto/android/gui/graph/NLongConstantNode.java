/*
 * NLongConstantNode.java - part of the GATOR project
 *
 * Copyright (c) 2018 The Ohio State University
 *
 * This file is distributed under the terms described in LICENSE in the
 * root directory.
 */

package presto.android.gui.graph;

import soot.Scene;
import soot.SootClass;

public class NLongConstantNode extends NObjectNode {
  public Long value;

  @Override
  public SootClass getClassType() {
    return Scene.v().getSootClass("java.lang.Long");
  }

  @Override
  public String toString() {
    return "LongConst[" + value + "]" + id;
  }
}
