/*
 * IEdgeFilter.java - part of the GATOR project
 *
 * Copyright (c) 2018 The Ohio State University
 *
 * This file is distributed under the terms described in LICENSE in the
 * root directory.
 */

package presto.android.gui.clients.energy;

import presto.android.gui.graph.NObjectNode;
import presto.android.gui.wtg.ds.WTGEdge;

import java.util.List;
import java.util.Stack;

/**
 * Created by zero on 2/16/16.
 */
public interface IEdgeFilter {
  /***
   * Specify if Edge e should be discarded
   * @param e Current edge
   * @param P Current Path
   * @param S Current WindowStack
   * @return return true if this edge should be discarded. Otherwise
   *         return false
   */
  boolean discard(WTGEdge e, List<WTGEdge> P, Stack<NObjectNode> S);
}
