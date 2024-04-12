/*
 * GUIHierarchy.java - part of the GATOR project
 *
 * Copyright (c) 2018 The Ohio State University
 *
 * This file is distributed under the terms described in LICENSE in the
 * root directory.
 */
package presto.android.gui.rep;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import presto.android.gui.GUIAnalysisOutput;
import presto.android.gui.PropertyManager;
import soot.Scene;
import soot.SootMethod;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GUIHierarchy {
  // Data
  public String app;
  public List<Activity> activities = Lists.newArrayList();
  public List<Dialog> dialogs = Lists.newArrayList();
  private static final int indentation = 2;

  private static void printlnIndented(String s, PrintStream out, int indent) {
    for (int i = 0; i < indent; i++) {
      out.print(" ");
    }
    out.println(s);
  }

  public void dumpXML(GUIAnalysisOutput guiAnalysisOutput, PrintStream out, boolean showDialog) {
    int indent = 0;
    printlnIndented(String.format("<GUIHierarchy app=\"%s\">", this.app), out, indent);
    for (Activity activity : activities) {
      activity.dumpXML(indent + indentation, guiAnalysisOutput, out);
    }
    if (showDialog) {
      for (Dialog dialog : dialogs) {
        dialog.dumpXML(indent + indentation, guiAnalysisOutput, out);
      }
    }
    printlnIndented("</GUIHierarchy>", out, indent);
  }

  public static abstract class ViewContainer {
    protected ArrayList<View> views = Lists.newArrayList();

    public void addChild(View v) {
      views.add(v);
    }

    public List<View> getChildren() {
      return views;
    }

    //TODO:
    public List<View> getChildrenCascade() {
      List<View> retList = Lists.newArrayList();
      retList.addAll(this.views);
      for (View v : this.views) {
        retList.addAll(v.getChildrenCascade());
      }
      return retList;
    }
  }

  public static class EventAndHandler {
    protected String event;
    protected String handler;


    public String getEvent() {
      return event;
    }

    public String getHandler() {
      return handler;
    }

    //TODO:
    public SootMethod getEventHandlerMethod() {
      return null;
    }
  }

  public static class View extends ViewContainer {
    protected String type;
    protected int id;
    protected String idName;
    protected Set<String> title = Sets.newHashSet();
    //public String text;
    protected Set<String> hint = Sets.newHashSet();
    protected ArrayList<EventAndHandler> eventAndHandlers = Lists.newArrayList();

    public void addEventAndHandlerPair(EventAndHandler eventAndHandler) {
      eventAndHandlers.add(eventAndHandler);
    }

    public String getType() {
      return type;
    }

    public int getId() {
      return id;
    }

    public String getIdName() {
      return idName;
    }

    public Set<String> getTitle() {
      return title;
    }

    public Set<String> getHint() {
      return hint;
    }

    public void dumpXML(int indent, GUIAnalysisOutput guiAnalysisOutput, PrintStream out) {
      String type = String.format(" type=\"%s\"", this.type);
      String id = String.format(" id=\"%d\"", this.id);
      String idName = String.format(" idName=\"%s\"", this.idName);
      // TODO(tony): add the text attribute for TextView and so on
      String head = String.format("<View%s%s%s>", type, id, idName);
      printlnIndented(head, out, indent);

      {
        indent += indentation;
        if (this.title != null && type.contains("MenuItem")) {
          for (String t : this.title) {
            for (String s : t.split(PropertyManager.SEPARATOR)) {
              printlnIndented(String.format("<Title>%s</Title>", xmlSafe(s)), out, indent);
            }
          }
        } else if (this.title != null) { // reuse title for text of other views
          for (String t : this.title) {
            for (String s : t.split(PropertyManager.SEPARATOR)) {
              printlnIndented(String.format("<Text>%s</Text>", xmlSafe(s)), out, indent);
            }
          }
        }
        if (this.hint != null) {
          for (String h : this.hint) {
            for (String s : h.split(PropertyManager.SEPARATOR)) {
              printlnIndented(String.format("<Hint>%s</Hint>", xmlSafe(s)), out, indent);
            }
          }
        }
        indent -= indentation;
      }

      if (!this.getChildren().isEmpty()) {
        indent += indentation;
        printlnIndented("<Child>", out, indent);
        // This includes both children and context menus
        for (View child : this.getChildren()) {
          child.dumpXML(indent + indentation, guiAnalysisOutput, out);
        }
        printlnIndented("</Child>", out, indent);
        indent -= indentation;
      }

      {
        // Events and handlers
        for (EventAndHandler eventAndHandler : this.eventAndHandlers) {
          String handler = eventAndHandler.getHandler();
          String safeRealHandler = "";
          if (handler.startsWith("<FakeName_")) {
            SootMethod fake = Scene.v().getMethod(handler);
            SootMethod real = guiAnalysisOutput.getRealHandler(fake);
            safeRealHandler = String.format(
                    " realHandler=\"%s\"", xmlSafe(real.getSignature()));
          }
          printlnIndented(String.format("<EventAndHandler event=\"%s\" handler=\"%s\"%s />",
                  eventAndHandler.getEvent(), xmlSafe(eventAndHandler.getHandler()),
                  safeRealHandler), out, indent + indentation);
        }
      }
      printlnIndented("</View>", out, indent);
    }
  }

  public static class Window extends ViewContainer {
    protected String name;

    public String getName() {
      return name;
    }
  }

  public static class Activity extends Window {

    public void dumpXML(int indent, GUIAnalysisOutput guiAnalysisOutput, PrintStream out) {
      printlnIndented(String.format("<Activity name=\"%s\">", this.name), out, indent);
      for (View rootView : this.getChildren()) {
        rootView.dumpXML(indent + indentation, guiAnalysisOutput, out);
      }
      printlnIndented("</Activity>", out, indent);
    }
  }

  public static class Dialog extends Window {
    protected int allocLineNumber;
    protected String allocStmt;
    protected String allocMethod;

    public int getAllocLineNumber() {
      return allocLineNumber;
    }

    public String getAllocStmt() {
      return allocStmt;
    }

    public String getAllcMethod() {
      return allocMethod;
    }

    public void dumpXML(int indent, GUIAnalysisOutput output, PrintStream out) {
      printlnIndented(String.format("<Dialog name=\"%s\" allocLineNumber=\"%d\" allocStmt=\"%s\" allocMethod=\"%s\">",
              this.name, this.allocLineNumber, xmlSafe(this.allocStmt), xmlSafe(this.allocMethod)), out, indent);

      for (View rootView : this.getChildren()) {
        rootView.dumpXML(indent + indentation, output, out);
      }
      printlnIndented("</Dialog>", out, indent);
    }

  }

  public static String xmlSafe(String s) {
    return s.replaceAll("%", "%%")
            .replaceAll("&", "&amp;")
            .replaceAll("\"", "&quot;")
            .replaceAll("'", "&apos;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;");

  }
}
