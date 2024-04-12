/*
 * GUIAnalysisOutput.java - part of the GATOR project
 *
 * Copyright (c) 2018 The Ohio State University
 *
 * This file is distributed under the terms described in LICENSE in the
 * root directory.
 */
package presto.android.gui;

import presto.android.gui.graph.*;
import presto.android.gui.listener.EventType;
import soot.Local;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.Stmt;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface GUIAnalysisOutput {
  // === Results
  Flowgraph getFlowgraph();

  FixpointSolver getSolver();

  Map<NOpNode, Set<NNode>> operationNodeAndReceivers();

  Map<NOpNode, Set<NNode>> operationNodeAndParameters();

  Map<NOpNode, Set<NNode>> operationNodeAndResults();

  Map<NOpNode, Set<NNode>> operationNodeAndListeners();

  Set<NOpNode> operationNodes(Class<? extends NOpNode> klass);

  VariableValueQueryInterface getVariableValueQueryInterface();

  // === Activities
  Set<SootClass> getActivities();

  SootClass getMainActivity();

  Set<NNode> getActivityRoots(SootClass activity);

  Set<SootMethod> getLifecycleHandlers(SootClass activity);

  Set<SootMethod> getActivityHandlers(SootClass activity,
                                      List<String> subsigs);

  // === Menus
  NOptionsMenuNode getOptionsMenu(SootClass activity);

  boolean isExplicitShowOptionsMenuCall(Stmt s);

  Set<NOptionsMenuNode> explicitlyTriggeredOptionsMenus(Stmt s);

  void getContextMenus(NObjectNode view, Set<NContextMenuNode> result);

  Set<NContextMenuNode> getContextMenus(NObjectNode view);

  SootMethod getOnCreateContextMenuMethod(NContextMenuNode contextMenu);

  boolean isExplicitShowContextMenuCall(Stmt s);

  Set<NContextMenuNode> explicitlyTriggeredContextMenus(Stmt s);

  Set<SootMethod> getMenuHandlers(SootClass activity);

  Set<SootMethod> getMenuCreationHandlers(SootClass activity);

  // === Dialogs
  Set<NDialogNode> getDialogs();

  Set<NNode> getDialogRoots(NDialogNode dialog);

  Set<SootMethod> getDialogCreationHandlers(SootClass activity);

  boolean isDialogShow(Stmt s);

  Set<NDialogNode> dialogsShownBy(Stmt s);

  Set<Stmt> getDialogShows(NDialogNode dialog);

  boolean isDialogDismiss(Stmt s);

  Set<NDialogNode> dialogsDismissedBy(Stmt s);

  Set<Stmt> getDialogDimisses(NDialogNode dialog);

  Set<SootMethod> getDialogLifecycleHandlers(NDialogNode dialog);

  Set<SootMethod> getOtherEventHandlersForDialog(NDialogNode dialog);

  // === Views & Handlers
  Map<EventType, Set<SootMethod>> getAllEventsAndTheirHandlers(NObjectNode guiObject);

  Map<EventType, Set<SootMethod>> getExplicitEventsAndTheirHandlers(NObjectNode guiObject);

  Map<EventType, Set<SootMethod>> getImplicitEventsAndTheirHandlers(NObjectNode guiObject);

  Set<EventType> getAllSupportedEvents(NObjectNode guiObject);

  Set<NSetListenerOpNode> getCallbackRegistrations(NObjectNode guiObject);

  Set<NSetListenerOpNode> getCallbackRegistrations(NObjectNode guiObject, EventType eventType);

  Set<SootMethod> getEventHandlers(NObjectNode guiObject, EventType eventType);

  boolean isCallbackRegistration(Stmt s);

  Local getViewLocal(SootMethod handler);

  Local getListenerLocal(SootMethod handler);

  SootMethod getRealHandler(SootMethod fakeHandler);

  // === Measurements
  long getRunningTimeInNanoSeconds();

  void setRunningTimeInNanoSeconds(long runningTimeInNanoSeconds);

  String getAppPackageName();

  boolean isLifecycleHandler(SootMethod handler);
}
