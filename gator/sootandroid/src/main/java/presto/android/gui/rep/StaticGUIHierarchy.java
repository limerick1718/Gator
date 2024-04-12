/*
 * StaticGUIHierarchy.java - part of the GATOR project
 *
 * Copyright (c) 2018 The Ohio State University
 *
 * This file is distributed under the terms described in LICENSE in the
 * root directory.
 */
package presto.android.gui.rep;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import presto.android.*;
import presto.android.gui.*;
import presto.android.gui.graph.*;
import presto.android.gui.listener.EventType;
import soot.SootClass;
import soot.SootMethod;
import soot.toolkits.scalar.Pair;

import java.util.Map;
import java.util.Set;

public class StaticGUIHierarchy extends GUIHierarchy {
  // Quick hack to save all handler signatures
  public Set<SootMethod> handlerMethods = Sets.newHashSet();

  // Helpers
  GUIAnalysisOutput analysisOutput;
  Flowgraph flowgraph;
  Hierarchy hier = Hierarchy.v();
  JimpleUtil jimpleUtil = JimpleUtil.v();
  PropertyManager prop = PropertyManager.v();
  IDNameExtractor idNameExtractor = IDNameExtractor.v();

  public StaticGUIHierarchy(GUIAnalysisOutput output) {
    this.analysisOutput = output;
    this.flowgraph = output.getFlowgraph();
    build();
  }

  void build() {
    app = Configs.benchmarkName;
    buildActivities();
    buildDialogs();
  }

  void traverseRootViewAndHierarchy(ViewContainer parent, Set<NNode> roots) {
    // Roots & view hierarchy
    if (roots != null && !roots.isEmpty()) {
      for (NNode n : roots) {
        buildView(parent, n);
      }
    }
  }

  SootClass currentActivity;

  void buildActivities() {
    for (SootClass activityClass : analysisOutput.getActivities()) {
      currentActivity = activityClass;
      Activity act = new Activity();
      activities.add(act);
      act.name = activityClass.getName();

      traverseRootViewAndHierarchy(
              act, analysisOutput.getActivityRoots(activityClass));

      // Options menu
      buildOptionsMenu(act, activityClass);
    }
    currentActivity = null;
  }

  void buildDialogs() {
    for (NDialogNode dialogNode : analysisOutput.getDialogs()) {
      Dialog dialog = new Dialog();
      dialogs.add(dialog);
      dialog.name = dialogNode.c.getName();
      dialog.allocStmt = dialogNode.allocStmt.toString();
      dialog.allocMethod = dialogNode.allocMethod.getSignature();
      dialog.allocLineNumber = jimpleUtil.getLineNumber(dialogNode.allocStmt);
      Debug.v().printf(
              "%s @ %s -> %d\n", dialog.allocStmt, dialog.allocMethod, dialog.allocLineNumber);

      traverseRootViewAndHierarchy(
              dialog, analysisOutput.getDialogRoots(dialogNode));
    }
  }

  void buildOptionsMenu(Activity act, SootClass activityClass) {
    NOptionsMenuNode optionsMenu =
            flowgraph.activityClassToOptionsMenu.get(activityClass);
    if (optionsMenu != null) {
      buildView(act, optionsMenu);
      // Add handlers for menu items
      View optionsMenuView = act.views.get(act.views.size() - 1);
      if (!optionsMenuView.type.equals("android.view.Menu")) {
        throw new RuntimeException(optionsMenuView.type + " is not Menu!");
      }
      Set<SootMethod> handlers = analysisOutput.getActivityHandlers(
              activityClass,
              Lists.newArrayList(
                      MethodNames.onMenuItemSelectedSubSig,
                      MethodNames.onOptionsItemSelectedSubSig));
      for (View menuItem : optionsMenuView.views) {
        for (SootMethod m : handlers) {
          EventAndHandler eventAndHandler = new EventAndHandler();
          eventAndHandler.event = EventType.item_selected.toString();
          eventAndHandler.handler = m.getSignature();
          menuItem.eventAndHandlers.add(eventAndHandler);
          handlerMethods.add(m);
        }
      }
    }
  }

  Set<NNode> visitingNodes = Sets.newHashSet();

  void buildView(ViewContainer parent, NNode node) {
    if (!(node instanceof NObjectNode)) {
      throw new RuntimeException(node.toString());
    }
    if (visitingNodes.contains(node)) {
      Logger.trace(this.getClass().getSimpleName(), "[WARNING] Node "
              + node + " already printed. Cycle!!!");
      return;
    }
    visitingNodes.add(node);

    NObjectNode objectNode = (NObjectNode) node;
    Set<NNode> childSet = objectNode.getChildren();
    Set<NContextMenuNode> contextMenuSet =
            analysisOutput.getContextMenus(objectNode);
    boolean noChild = childSet.isEmpty() && contextMenuSet.isEmpty();
    SootClass type = objectNode.getClassType();

    if (objectNode instanceof NInflNode) {
      NInflNode inflNode = (NInflNode) objectNode;
      if (type != null && hier.isMenuItemClass(type)) { // Is it menu item?
        if (!noChild) {
          throw new RuntimeException(
                  "MenuItem: " + inflNode + " is not a leaf!");
        }
//        String title = prop.getSpeciallySeparatedTextOrTitlesOfView(inflNode);
        Set<String> title = prop.getTextsOrTitlesOfView(inflNode);
//        if (title == null) {
//          title = NO_TITLE;
//        }
        View view = new View();
        view.type = type.getName();
//        view.title = title;
        view.title.addAll(title);
//        view.hint = prop.getSpeciallySeparatedHintOfView(inflNode);
        view.hint.addAll(prop.getHintOfView(inflNode));
        Pair<Integer, String> idAndName = getIdAndName(node.idNode);
        view.id = idAndName.getO1().intValue();
        view.idName = idAndName.getO2();
        parent.addChild(view);
        buildEventAndHandlers(view, node);
        visitingNodes.remove(node);
        return;
      }
    }

    // Now, print other types of nodes. First, the open tag.
    Pair<Integer, String> idAndName = getIdAndName(node.idNode);
    View view = new View();
    parent.addChild(view);
    buildEventAndHandlers(view, node);
    view.type = type != null ? type.getName() : "?";
    view.id = idAndName.getO1();
    view.idName = idAndName.getO2();
//    view.title = type != null && node instanceof NInflNode ?
//            prop.getSpeciallySeparatedTextOrTitlesOfView(objectNode) :
//            null;
    if (type != null && node instanceof NInflNode) {
      view.title.addAll(prop.getTextsOrTitlesOfView(objectNode));
    }
//    view.hint = prop.getSpeciallySeparatedHintOfView(objectNode);
    view.hint.addAll(prop.getHintOfView(objectNode));

    // print children
    for (NNode n : objectNode.getChildren()) {
      buildView(view, n);
    }
    // special child
    for (NContextMenuNode contextMenu : contextMenuSet) {
      buildView(view, contextMenu);
      // Add event handlers for menu item in the context menu
      View contextMenuView = view.views.get(view.views.size() - 1);
      if (!contextMenuView.type.equals("android.view.ContextMenu")) {
        throw new RuntimeException(
                contextMenuView.type + " is not ContextMenu!");
      }
      Set<SootMethod> handlers = analysisOutput.getActivityHandlers(
              currentActivity, Lists.newArrayList(MethodNames.onMenuItemSelectedSubSig,
                      MethodNames.onContextItemSelectedSubSig));
      for (View menuItem : contextMenuView.views) {
        for (SootMethod m : handlers) {
          EventAndHandler eventAndHandler = new EventAndHandler();
          eventAndHandler.event = EventType.item_selected.toString();
          eventAndHandler.handler = m.getSignature();
          menuItem.eventAndHandlers.add(eventAndHandler);
          handlerMethods.add(m);
        }
      }
    }

    visitingNodes.remove(node);
  }

  void buildEventAndHandlers(View view, NNode node) {
    NObjectNode guiObject = (NObjectNode) node;
    // Explicit
    Map<EventType, Set<SootMethod>> explicitEventsAndHandlers =
            analysisOutput.getExplicitEventsAndTheirHandlers(guiObject);
    for (Map.Entry<EventType, Set<SootMethod>> entry : explicitEventsAndHandlers.entrySet()) {
      EventType event = entry.getKey();
      for (SootMethod m : entry.getValue()) {
        EventAndHandler eventAndHandler = new EventAndHandler();
        eventAndHandler.event = event.toString();
        eventAndHandler.handler = m.getSignature();
        view.addEventAndHandlerPair(eventAndHandler);
        handlerMethods.add(m);
      }
    }
    // Context menus
    Set<NContextMenuNode> contextMenus =
            analysisOutput.getContextMenus(guiObject);
    for (NContextMenuNode context : contextMenus) {
      SootMethod m = analysisOutput.getOnCreateContextMenuMethod(context);
      EventAndHandler eventAndHandler = new EventAndHandler();
      eventAndHandler.event = EventType.implicit_create_context_menu.toString();
      eventAndHandler.handler = m.getSignature();
      view.addEventAndHandlerPair(eventAndHandler);
      handlerMethods.add(m);
    }
  }

  final String NO_TITLE = "NO_TITLE";
  final String NO_ID_NAME = "NO_ID";
  final Pair<Integer, String> NO_ID = new Pair<Integer, String>(-1, NO_ID_NAME);

  Pair<Integer, String> getIdAndName(NIdNode idNode) {
    if (idNode == null) {
      return NO_ID;
    }
    Integer id = idNode.getIdValue();
    String name = idNode.getIdName();
    if (idNameExtractor.isUnknown(name)) {
      name = NO_ID_NAME;
    }
    if (name.equals(NO_ID_NAME)) {
      return NO_ID;
    } else {
      return new Pair<Integer, String>(id, name);
    }
  }
}
