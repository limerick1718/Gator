/*
 * NFakeType1Node.java - part of the GATOR project
 *
 * Copyright (c) 2018 The Ohio State University
 *
 * This file is distributed under the terms described in LICENSE in the
 * root directory.
 */

package presto.android.gui.wtg.flowgraph;

import soot.Scene;
import soot.SootClass;

/**
 * Created by zero on 7/24/15.
 */
public class NFakeType1Node extends NSpecialNode {
  SootClass fakeClass;

  private NFakeType1Node() {
    fakeClass = new SootClass("presto.android.gui.stubs.PrestoFakeType1NodeClass");
    Scene.v().addClass(fakeClass);
  }

  public String toString() {
    return "FAKE_TYPE1_NODE[]" + id;
  }

  @Override
  public SootClass getClassType() {
    return fakeClass;
  }

  public final static NFakeType1Node NODE = new NFakeType1Node();
}
