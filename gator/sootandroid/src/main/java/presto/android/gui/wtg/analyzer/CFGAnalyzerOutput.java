/*
 * CFGAnalyzerOutput.java - part of the GATOR project
 *
 * Copyright (c) 2018 The Ohio State University
 *
 * This file is distributed under the terms described in LICENSE in the
 * root directory.
 */
package presto.android.gui.wtg.analyzer;

import com.google.common.collect.Multimap;
import presto.android.gui.graph.NObjectNode;
import soot.SootMethod;
import soot.jimple.Stmt;
import soot.toolkits.scalar.Pair;

public class CFGAnalyzerOutput {
  public Multimap<NObjectNode, Pair<Stmt, SootMethod>> targets;
  public boolean avoid;
  public boolean exitSystem;
}
