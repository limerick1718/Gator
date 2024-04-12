/*
 * GUIAnalysis.java - part of the GATOR project
 *
 * Copyright (c) 2019 The Ohio State University
 *
 * This file is distributed under the terms described in LICENSE in the
 * root directory.
 */
package presto.android.gui;

import com.google.common.collect.Sets;
import presto.android.Configs;
import presto.android.Hierarchy;
import presto.android.Logger;
import presto.android.xml.XMLParser;
import presto.android.Timer;

import java.util.Set;

/**
 * The entrypoint class for our GUI analysis. See run() for details.
 */
public class GUIAnalysis {
  private static final String TAG = GUIAnalysis.class.getSimpleName();
  public static final String DEFAULT_CLIENT_PACKAGE = "presto.android.gui.clients";

  public Hierarchy hier;
  public XMLParser xmlParser;

  public Set<Integer> allLayoutIds = Sets.newHashSet();
  public Set<Integer> allMenuIds = Sets.newHashSet();
  public Set<Integer> allWidgetIds = Sets.newHashSet();
  public Set<Integer> allStringIds = Sets.newHashSet();

  public Flowgraph flowgraph;
  public FixpointSolver fixpointSolver;
  public VariableValueQueryInterface variableValueQueryInterface;

  GUIAnalysis(Hierarchy hier, XMLParser xmlParser) {
    this.hier = hier;
    this.xmlParser = xmlParser;
  }

  private static GUIAnalysis instance;

  public static synchronized GUIAnalysis v() {
    if (instance == null) {
      instance = new GUIAnalysis(Hierarchy.v(), XMLParser.Factory.getXMLParser());
    }
    return instance;
  }

  /**
   * Populate ID containers with information from XML files, and print out some statistics as a
   * sanity check. The ID containers are used in the construction of flowgraph.
   */
  public void populateIDContainers() {
    // First, the layout ids
    allLayoutIds.addAll(xmlParser.getApplicationLayoutIdValues());
    allLayoutIds.addAll(xmlParser.getSystemLayoutIdValues());
    // Next, the menu ids (similarly to layouts, could be inflated):
    allMenuIds.addAll(xmlParser.getApplicationMenuIdValues());
    allMenuIds.addAll(xmlParser.getSystemMenuIdValues());
    // And the widget ids
    allWidgetIds.addAll(xmlParser.getApplicationRIdValues());
    allWidgetIds.addAll(xmlParser.getSystemRIdValues());
    allStringIds.addAll(xmlParser.getStringIdValues());
    Logger.verb(
            TAG,
            "[XML] Layout Ids: "
                    + allLayoutIds.size()
                    + ", Menu Ids: "
                    + allMenuIds.size()
                    + ", Widget Ids: "
                    + allWidgetIds.size()
                    + ", String Ids: "
                    + allStringIds.size());
    Logger.verb(TAG, "[XML] MainActivity: " + xmlParser.getMainActivity());
  }

  public void run() {
    Logger.verb(TAG, "[GUIAnalysis] Start");
    long startTime = System.nanoTime();

    // 0. Populate IDs
    populateIDContainers();

    // 1. Build flow graph
    flowgraph = new Flowgraph(hier, allLayoutIds, allMenuIds, allWidgetIds, allStringIds);
    flowgraph.build();

    //Added for Experiment
    /*System.out.println("<TIME>:" + (Timer.duration() / 1000.0) + " second");
    Runtime.getRuntime().gc();
    Runtime.getRuntime().gc();
    Runtime.getRuntime().gc();
    System.out.println("<MEMORY>:" +
            ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 /
                    1024) + " MB");
    System.exit(0);*/

    // 2. Fix-point computation
    fixpointSolver = new FixpointSolver(flowgraph);
    fixpointSolver.solve();

    // Added for Experiment
    /*System.out.println("<TIME>:" + (Timer.duration() / 1000.0) + " second");
    Runtime.getRuntime().gc();
    Runtime.getRuntime().gc();
    Runtime.getRuntime().gc();
    System.out.println("<MEMORY>:" +
            ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 /
                    1024) + " MB");
    System.exit(0);*/

    // 3. Variable value query interface
    variableValueQueryInterface = DemandVariableValueQuery.v(flowgraph, fixpointSolver);

    // 4. Construct the output
    GUIAnalysisOutput output = new DefaultGUIAnalysisOutput(this);

    long estimatedTime = System.nanoTime() - startTime;
    output.setRunningTimeInNanoSeconds(estimatedTime);
    Logger.verb(TAG, "[GUIAnalysis] End: " + (estimatedTime * 1.0e-09) + " sec");

    // 5. Client analyses
    executeClientAnalyses(output);

    if (!Configs.flowgraphOutput.isEmpty()) {
      flowgraph.dump(Configs.flowgraphOutput);
    }
  }

  void executeClientAnalyses(GUIAnalysisOutput output) {
    Set<GUIAnalysisClient> clients = Sets.newHashSet();
    readClientAnalysisSpecification(clients);

    for (GUIAnalysisClient client : clients) {
      Class<? extends GUIAnalysisClient> clientClass = client.getClass();
      String clientName;
      if (clientClass.getPackage().getName().equals(DEFAULT_CLIENT_PACKAGE)) {
        clientName = clientClass.getSimpleName();
      } else {
        clientName = clientClass.getName();
      }

      Logger.verb(TAG, "[" + clientName + "] Start");
      long startTime = System.nanoTime();
      client.run(output);
      long estimatedTime = System.nanoTime() - startTime;
      Logger.verb(TAG, "[" + clientName + "] End: " + (estimatedTime * 1.0e-09) + " sec");
    }
  }

  // TODO(tony): use fancy annotation mechanism to manage client specification
  // so that shorter names can be registered with clients.
  // NOTE(tony): we assume each client is specified once (i.e., no duplication)
  void readClientAnalysisSpecification(Set<GUIAnalysisClient> clients) {
    for (String clientSpec : Configs.clients) {
      Class<?> klass = null;
      try {
        klass = Class.forName(clientSpec);
      } catch (Exception e1) {
        if (!clientSpec.contains(".")) {
          // prepend default package
          try {
            klass = Class.forName(DEFAULT_CLIENT_PACKAGE + "." + clientSpec);
          } catch (Exception ex) {
            // ignore
          }
        }
      }
      if (klass == null) {
        Logger.warn(TAG, "Cannot find client `" + clientSpec + "'");
      } else {
        Object newInstance = null;
        try {
          newInstance = klass.newInstance();
        } catch (Exception e) {
          Logger.warn(TAG, "Cannot create an instance for `" + clientSpec + "'");
        }
        if (newInstance != null) {
          if (newInstance instanceof GUIAnalysisClient) {
            clients.add((GUIAnalysisClient) newInstance);
          } else {
            Logger.warn(TAG, "`" + clientSpec + "' does not implement GUIAnalysisClient");
          }
        }
      }
    }
  }
}
