/*
 * GUIHierarchyPrinterClient.java - part of the GATOR project
 *
 * Copyright (c) 2018 The Ohio State University
 *
 * This file is distributed under the terms described in LICENSE in the
 * root directory.
 */
package presto.android.gui.clients;

import presto.android.Configs;
import presto.android.Logger;
import presto.android.gui.GUIAnalysisClient;
import presto.android.gui.GUIAnalysisOutput;
import presto.android.gui.rep.GUIHierarchy;
import presto.android.gui.rep.StaticGUIHierarchy;

import java.io.File;
import java.io.PrintStream;

public class GUIHierarchyPrinterClient implements GUIAnalysisClient {
  private String TAG = GUIHierarchyPrinterClient.class.getSimpleName();
  GUIAnalysisOutput output;
  GUIHierarchy guiHier;

  private PrintStream out = null;

  @Override
  public void run(GUIAnalysisOutput output) {
    this.output = output;
    guiHier = new StaticGUIHierarchy(output);
    boolean showDialog = true;

    // Init the file io
    for (String param : Configs.clientParams) {
      if (param.equals("print2stdout")) {
        out = System.out;
      } else if (param.equals("nodialog")) {
        showDialog = false;
      }
    }
    if (out == null) {
      try {
        File file = File.createTempFile(Configs.benchmarkName + "-", ".xml");
        Logger.verb(TAG, "XML file: " + file.getAbsolutePath());
        out = new PrintStream(file);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    // Start printing
    guiHier.dumpXML(output, out, showDialog);

    // Finish
    out.flush();
    out.close();
  }
}
