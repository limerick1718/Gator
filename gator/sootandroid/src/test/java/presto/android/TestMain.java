/*
 * TestMain.java - part of the GATOR project
 *
 * Copyright (c) 2018 The Ohio State University
 *
 * This file is distributed under the terms described in LICENSE in the
 * root directory.
 */

package presto.android;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class TestMain {
  @Test
  public void testMain() throws Exception {
    String ADK = System.getProperty("user.home") + "/Android/Sdk";
    String apk = System.getProperty("user.home") + "/workspace/android/Simplest/app/build/outputs/apk/debug/app-debug.apk";
    String resDir = System.getProperty("user.home") + "/workspace/android/Simplest/app/build/outputs/apk/debug/app-debug";
    String apk_name = "app-debug.apk";

      String APKTOOL = System.getProperty("user.dir") + "/../tools/apktool_2.3.4.jar";

    runProcess("java -jar " + APKTOOL + " d -f -o " + resDir + " " + apk);

    String[] args = new String[]{
            "-project", apk,
            "-apiLevel", "android-27",
            "-manifestFile", resDir + "/AndroidManifest.xml",
            "-resourcePath", resDir + "/res",
            "-guiAnalysis",
            "-sootandroidDir", System.getProperty("user.dir"),
            "-benchmarkName", apk_name,
            "-sdkDir", ADK,
            "-android", ADK + "/platforms/android-27/android.jar",
            "-listenerSpecFile", "listeners.xml",
            "-wtgSpecFile", "wtg.xml",
            "-clientParam", "print2stdout",
            "-client", "GUIHierarchyPrinterClient"
    };
    Main.main(args);

    runProcess("rm -rf " + resDir);
  }

  private static void printLines(PrintStream stream, InputStream ins) throws Exception {
    String line = null;
    BufferedReader in = new BufferedReader(
            new InputStreamReader(ins));
    while ((line = in.readLine()) != null) {
      stream.println(line);
    }
  }

  private static void runProcess(String command) throws Exception {
    Process pro = Runtime.getRuntime().exec(command);
    printLines(System.out, pro.getInputStream());
    printLines(System.err, pro.getErrorStream());
    pro.waitFor();
  }
}
