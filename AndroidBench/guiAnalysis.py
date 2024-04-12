#!/usr/bin/env python3

import os, sys
import json, subprocess, glob
from subprocess import call

class SootGlobalConfig:
    GatorRoot=""
    ADKLocation=""
    AndroidBenchLocation=""
    CurrentWorkingDir=""
    ConfigFile=""
    OutputFolder=""
    jsonData=""
    externalSootCMD=""
    gatorDebugCodeList=[]
    gatorClient=""
    bRunAllCGO = False
    bRunAllEnergy = False
    bSilent = False 
    bDebug = False 
    bExact = False
    pList = []
    jsonBASE_DIR=""
    jsonBASE_PARAM=""
    jsonBASE_CLIENT=""
    jsonBASE_CLIENT_PARAM=""
    paramBASE_DIR=""
    paramBASE_PARAM=""
    paramBASE_CLIENT=""
    paramBASE_CLIENT_PARAM=""
    projListCGO = []
    projListCC = []
    AppPath=""
    AppAPILevel=""

class ProjectS:
    def __init__(
    self,
    projName,
    projPath,
    projParam,
    projClient,
    projClientParam):
        self.name = projName
        self.path = projPath
        self.param = projParam
        self.client = projClient
        self.clientParam = projClientParam
        pass

    def __str__(self):
        curLine = "Name: {0}\nPath: {1}\nParam:{2}\nClient:{3}\nClientParam:{4}\n".format(\
        self.name, self.path, self. param, self.client, self.clientParam)
        return curLine

    def execute(self):
        apkPath = SootGlobalConfig.AndroidBenchLocation + self.path
        if not pathExists(apkPath):
            if (SootGlobalConfig.bDebug):
                print("APK not found for " + apkPath)
            return -1
        dirName = "output"
        if not pathExists(dirName):
            os.mkdir(dirName)
        pathName = SootGlobalConfig.CurrentWorkingDir + "/" + dirName

        pcwd = os.getcwd();
        os.chdir(pathName);
        GatorOptions=""
        if self.client != '':
            GatorOptions = '{0} -client {1} {2}'.format(\
            self.param, self.client, self.clientParam)
        else:
            GatorOptions = self.param
        callList = ["python3",\
                    SootGlobalConfig.GatorRoot+"/gator/gator",\
                    "a",\
                    "-p",\
                    apkPath,\
                    ]
        callList.extend(GatorOptions.split())
        call(callList)
        if (SootGlobalConfig.bDebug):
            print(callList)
        os.chdir(pcwd)
        print(self.name + " FINISHED")
        return 0

def pathExists(pathName):
    if os.access(pathName, os.F_OK):
        return True
    return False;

def fatalError(str):
    print(str)
    sys.exit(1)
    pass

def waitForEnter():
    try:
        input("Press ENTER to continue")
    except:
        pass

def debugOutput():
    print("ConfigFile: "+ SootGlobalConfig.ConfigFile)
    print("GatorRoot: "+ SootGlobalConfig.GatorRoot)
    print("ADKLocation: "+ SootGlobalConfig.ADKLocation)
    print("CurrentWorkingDir: "+ SootGlobalConfig.CurrentWorkingDir)
    print("OutputFolder: "+ SootGlobalConfig.OutputFolder)
    print("Projects: ")
    for item in SootGlobalConfig.projListCGO:
        print(item)
    for item in SootGlobalConfig.projListCC:
        print(item)


def parseSingleProject(key, curData, projList):
    #Not a reserved Keyword
    baseDir = SootGlobalConfig.jsonBASE_DIR
    baseParam = SootGlobalConfig.jsonBASE_PARAM
    baseClient = SootGlobalConfig.jsonBASE_CLIENT
    baseClientParam = SootGlobalConfig.jsonBASE_CLIENT_PARAM
    curName = key
    curPath = ""
    curAPI = ""
    curZip = ""
    curExtraLib = ""
    curParam = ""
    curClient = ""
    curClientParam = ""
    if "abs-path" in curData and curData["abs-path"] != "":
        curPath = curData["abs-path"]
    elif "relative-path" in curData:
        curPath = baseDir + "/" + curData["relative-path"]
    else:
        fatalError("Error: Path is not defined in " + curName)

    if "override-param" in curData and curData["override-param"] != "":
        curParam = curData["override-param"]
    elif "append-param" in curData:
        curParam = baseParam + ' ' + curData["append-param"]
    else:
        curParam = baseParam

    if "override-client" in curData and curData["override-client"] != "":
        curClient = curData["override-client"]
    else:
        curClient = baseClient

    if "override-client-param" in curData and curData["override-client-param"] != "":
        curClientParam = curData["override-client-param"]
    elif "append-client-param" in curData:
        curClientParam = baseClientParam + ' ' + curData["append-client-param"]
    else:
        curClientParam = baseClientParam

    curProj = ProjectS(\
      projName = curName,\
      projPath = curPath,\
      projParam = curParam,\
      projClient = curClient,\
      projClientParam = curClientParam)
    projList.append(curProj)

def parseProjects(jsData, projList):
    for key in jsData:
        #Reserved Keyword?
        if key == "BASE_DIR":
            SootGlobalConfig.jsonBASE_DIR = jsData[key].__str__()
            if len(SootGlobalConfig.paramBASE_DIR) != 0:
                SootGlobalConfig.jsonBASE_DIR = SootGlobalConfig.paramBASE_DIR
        elif key == "BASE_PARAM":
            SootGlobalConfig.jsonBASE_PARAM = jsData[key].__str__()
            if len(SootGlobalConfig.paramBASE_PARAM) != 0:
                SootGlobalConfig.jsonBASE_PARAM = SootGlobalConfig.paramBASE_PARAM
        elif key == "BASE_CLIENT":
            SootGlobalConfig.jsonBASE_CLIENT = jsData[key].__str__()
            if len(SootGlobalConfig.paramBASE_CLIENT) != 0:
                SootGlobalConfig.jsonBASE_CLIENT = SootGlobalConfig.paramBASE_CLIENT
        elif key == "BASE_CLIENT_PARAM":
            SootGlobalConfig.jsonBASE_CLIENT_PARAM = jsData[key].__str__()
            if len(SootGlobalConfig.paramBASE_CLIENT_PARAM) != 0:
                SootGlobalConfig.jsonBASE_CLIENT_PARAM = SootGlobalConfig.paramBASE_CLIENT_PARAM
        pass

    for key in jsData:
        if key == "BASE_DIR" or \
        key == "BASE_PARAM" or \
        key == "BASE_CLIENT" or \
        key == "BASE_CLIENT_PARAM":
            continue
        else:
            #Not a reserved Keyword
            curData = jsData[key]
            parseSingleProject(key, curData, projList)

def loadJSON(fileName):
    global jsonData
    fileFD = None
    try:
        fileFD = open(fileName, "r")
    except:
        print("JSON configuration file open error! Abort.")
        sys.exit(0)

    jsonData = json.load(fileFD)
    #print(jsonData)
    return jsonData

def determinGatorRootAndSDKPath():
    print("Determine Root called")
    gatorRoot = os.environ.get("GatorRoot")
    if gatorRoot != None:
        SootGlobalConfig.GatorRoot = gatorRoot
    else:
        SootGlobalConfig.GatorRoot = os.getcwd() + "/.."
        #os.environ['GatorRoot'] = configs.GATOR_ROOT
    adkRoot = os.environ.get("ADK")
    if adkRoot == None:
        adkRoot = os.environ.get("ANDROID_SDK")
    if adkRoot != None:
        SootGlobalConfig.ADKLocation = adkRoot
    else:
        homeDir = os.environ.get("HOME")
        if homeDir == None:
            fatalError("ADK environment variable is not defined")
        if sys.platform == "linux2":
            if pathExists(homeDir + "/Android/Sdk"):
                SootGlobalConfig.ADKLocation = homeDir+"/Android/Sdk"
            else:
                fatalError("ADK environment variable is not defined")
        elif sys.platform == "darwin":
            if pathExists(homeDir + "/Library/Android/sdk"):
                SootGlobalConfig.ADKLocation = homeDir + "/Library/Android/sdk"
            else:
                fatalError("ADK environment variable is not defined")
        pass
    pass


def parseMainParam():
    determinGatorRootAndSDKPath()
    params = sys.argv
    gatorRoot = SootGlobalConfig.GatorRoot
    SootGlobalConfig.CurrentWorkingDir = os.getcwd()
    adkLocation = SootGlobalConfig.ADKLocation
    SootGlobalConfig.AndroidBenchLocation = SootGlobalConfig.GatorRoot + "/AndroidBench"
    i = 0;
    while (i < len(params) - 1):
        i += 1
        val = params[i]
        if val == 'runAll':
            SootGlobalConfig.bRunAllCGO = True
            SootGlobalConfig.bRunAllEnergy = True
            continue
        elif val == 'runAllCGO':
            SootGlobalConfig.bRunAllCGO = True
            continue
        elif val == 'runAllEnergy':
            SootGlobalConfig.bRunAllEnergy = True
            continue
        else:
            SootGlobalConfig.pList.append(val)
        pass
    if SootGlobalConfig.OutputFolder == "":
        SootGlobalConfig.OutputFolder = os.getcwd()


def main():
    parseMainParam()
    print("Loading " + SootGlobalConfig.AndroidBenchLocation + "/cgo.json")
    jsData = loadJSON(SootGlobalConfig.AndroidBenchLocation + "/cgo.json")
    parseProjects(jsData, SootGlobalConfig.projListCGO)
    print("Loading " + SootGlobalConfig.AndroidBenchLocation + "/cc16.json")
    jsData = loadJSON(SootGlobalConfig.AndroidBenchLocation + "/cc16.json")
    parseProjects(jsData, SootGlobalConfig.projListCC)
    if SootGlobalConfig.bDebug:
        debugOutput()
    if SootGlobalConfig.bRunAllCGO:
        for curItem in SootGlobalConfig.projListCGO:
            ret = curItem.execute()
            if (not SootGlobalConfig.bSilent) and (ret == 0):
                waitForEnter()

    if SootGlobalConfig.bRunAllEnergy:
        for curItem in SootGlobalConfig.projListCC:
            ret = curItem.execute()
            if (not SootGlobalConfig.bSilent) and (ret == 0):
                waitForEnter()

    if len(SootGlobalConfig.pList) > 0:
        for curStr in SootGlobalConfig.pList:
            for curItem in SootGlobalConfig.projListCGO:
                if (SootGlobalConfig.bExact and curStr == curItem.name) or ((not SootGlobalConfig.bExact) and curStr in curItem.name ):
                    curItem.execute()
                    if (not SootGlobalConfig.bSilent) and (ret == 0):
                        waitForEnter()
            for curItem in SootGlobalConfig.projListCC:
                if (SootGlobalConfig.bExact and curStr == curItem.name) or ((not SootGlobalConfig.bExact) and curStr in curItem.name ):
                    curItem.execute()
                    if (not SootGlobalConfig.bSilent) and (ret == 0):
                        waitForEnter()

    print("All Done!")
    pass


if __name__ == "__main__":
    main()
    pass
