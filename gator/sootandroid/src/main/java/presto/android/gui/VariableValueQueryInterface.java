/*
 * VariableValueQueryInterface.java - part of the GATOR project
 *
 * Copyright (c) 2018 The Ohio State University
 *
 * This file is distributed under the terms described in LICENSE in the
 * root directory.
 */
package presto.android.gui;

import presto.android.gui.graph.NIdNode;
import presto.android.gui.graph.NObjectNode;
import soot.Local;

import java.util.Set;

// For a given GUI-related variable (GUI object, ID, activity, etc), return the
// set of values this variable may reference.
public interface VariableValueQueryInterface {
  Set<NIdNode> idVariableValues(Local local);

  Set<NObjectNode> activityVariableValues(Local local);

  Set<NObjectNode> guiVariableValues(Local local);

  Set<NObjectNode> listenerVariableValues(Local local);
}
