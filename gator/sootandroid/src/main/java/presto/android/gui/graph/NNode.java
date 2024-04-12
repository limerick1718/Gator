/*
 * NNode.java - part of the GATOR project
 *
 * Copyright (c) 2018 The Ohio State University
 *
 * This file is distributed under the terms described in LICENSE in the
 * root directory.
 */
package presto.android.gui.graph;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import presto.android.Logger;
import soot.jimple.Stmt;

import java.util.*;

public abstract class NNode implements Comparable<NNode> {
  public static boolean verbose = false;
  private static int nextId = 0;
  public static int numberOfEdges = 0;
  public int id;

  // The flow graph node representing the widget id - could be NWidgetIdNode,
  // or null for nodes without ids (or sometimes, weirdly, NLayoutIdNode)
  public NIdNode idNode;

  // The displayed text for this GUI object. Right now, only valid for the
  // NInflNode nodes representing menu items.
  private Set<NNode> textNodes;

  // The displayed hint for GUI object.
  private Set<NNode> hintNodes;

  public NNode() {
    nextId++;
    id = nextId;
  }

  // NOTE(tony): "alias" nodes/paths
  protected ArrayList<NNode> succ;
  protected ArrayList<NNode> pred;

  // Anyone whose 'parent' is this obj. Used only in
  // NViewAllocNode, NInflNode, and NActivityNode
  protected Set<NNode> children;
  protected Set<NNode> parents;

  public synchronized Collection<NNode> getSuccessors() {
    if (succ == null || succ.isEmpty()) {
      return Collections.emptyList();
    } else {
      return Lists.newArrayList(succ);
    }
  }

  public synchronized int getNumberOfSuccessors() {
    return (succ == null ? 0 : succ.size());
  }

  public synchronized NNode getSuccessor(int index) {
    return succ.get(index);
  }

  public synchronized Collection<NNode> getPredecessors() {
    if (pred == null || pred.isEmpty()) {
      return Collections.emptyList();
    } else {
      return Lists.newArrayList(pred);
    }
  }

  public synchronized int getNumberOfPredecessors() {
    return (pred == null ? 0 : pred.size());
  }

  public synchronized NNode getPredecessor(int index) {
    return pred.get(index);
  }

  public synchronized boolean hasChild(NNode child) {
    if (children == null) {
      return false;
    }
    return children.contains(child);
  }

  public synchronized Iterator<NNode> getParents() {
    if (parents == null || parents.isEmpty()) {
      return Collections.emptyIterator();
    } else {
      return parents.iterator();
    }
  }

  public synchronized Set<NNode> getChildren() {
    if (children == null) {
      return Collections.emptySet();
    } else {
      return children;
    }
  }

  public synchronized void removeEdgeTo(NNode target) {
    if (succ != null && succ.contains(target)) {
      succ.remove(target);
      numberOfEdges--;
    } else {
      if (target.pred != null && target.pred.contains(this)) {
        throw new RuntimeException("Broken edge " + this + "===>" + target);
      }
      return;
    }
    if (target.pred == null || !target.pred.contains(this)) {
      throw new RuntimeException("Broken edge " + this + "===>" + target);
    }
    target.pred.remove(this);
  }

  public synchronized void addEdgeTo(NNode x) {
    addEdgeTo(x, null);
  }

  public synchronized void addEdgeTo(NNode x, Stmt s) {
    if (succ == null) {
      succ = Lists.newArrayListWithCapacity(4);
    }
    if (!succ.contains(x)) {
      succ.add(x);
      numberOfEdges++;
    } else {
      return;
    }

    if (s == null) {
      Logger.trace(this.getClass().getSimpleName(), this + " ==> " + x);
    } else {
      Logger.trace(this.getClass().getSimpleName(), this + " ==> " + x + " [" + s + "]");
    }

    // predecessors
    if (x.pred == null) {
      x.pred = Lists.newArrayListWithCapacity(4);
    }
    if (x.pred.contains(this)) {
      throw new RuntimeException();
    }
    x.pred.add(this);
  }

  public synchronized void addParent(NNode p) {
    if (p == this) {
      throw new RuntimeException("p.addView(p) for " + p);
    }
    if (p == null) {
      return;
    }
    if (parents == null) {
      parents = Sets.newHashSetWithExpectedSize(1);
    }
    parents.add(p);
    Logger.trace(this.getClass().getSimpleName(), this + " [p]==> " + p);

    if (p.children == null) {
      p.children = Sets.newHashSet();
    }
    p.children.add(this);
  }

  public synchronized boolean addTextNode(NNode text) {
    if (textNodes == null) {
      textNodes = Sets.newHashSet();
    }
    return textNodes.add(text);
  }

  public synchronized Iterator<NNode> getTextNodes() {
    if (textNodes == null) {
      return Collections.emptyIterator();
    }
    return textNodes.iterator();
  }

  public synchronized boolean addHintNode(NNode text) {
    if (hintNodes == null) {
      hintNodes = Sets.newHashSet();
    }
    return hintNodes.add(text);
  }

  public synchronized Iterator<NNode> getHintNodes() {
    if (hintNodes == null) {
      return Collections.emptyIterator();
    }
    return hintNodes.iterator();
  }

  @Override
  public int compareTo(NNode o) {
    return this.id - o.id;
  }
}
