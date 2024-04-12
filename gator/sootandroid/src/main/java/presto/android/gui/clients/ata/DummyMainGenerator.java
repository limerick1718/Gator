/*
 * DummyMainGenerator.java - part of the GATOR project
 *
 * Copyright (c) 2018 The Ohio State University
 *
 * This file is distributed under the terms described in LICENSE in the
 * root directory.
 */
package presto.android.gui.clients.ata;

import soot.SootMethod;

public interface DummyMainGenerator {
  String DUMMY_MAIN_CLASSNAME = "DummyMain80467581";

  SootMethod generateDummyMain(String mainActivity, ActivityTransitionGraph atg);

  SootMethod generateDummyMain(String mainActivity, ActivityTransitionGraph atg, ActivityStackTransitionGraph astg);
}
