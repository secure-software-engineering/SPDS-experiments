

import glob, csv, math, os.path, sys
import plotly.graph_objs
import numpy as np

MAX_ANALYSIS_TIME = 600000;
MAX_ACCESS_PATH = 0;
MAX_VISITED_METHODS = 1000;
RESULTS_PATH = "typestate/*.csv"

import numpy
def beautifyAnalysisTime(time):
    if int(time) > MAX_ANALYSIS_TIME:
        return MAX_ANALYSIS_TIME
    if int(time) == 0:
        return 1
    return int(time)

def geo_mean(iterable):
   return np.mean(iterable)

def getData(filename, replacement):
    if os.path.isfile(filename.replace("ideal",replacement)):
        with open(filename.replace("ideal",replacement)) as csvfile:
            readerFINK_UNIQUE = csv.DictReader(csvfile,delimiter = ";")
            return list(readerFINK_UNIQUE)
    return []
def toSeconds(ms):
    return (ms/1000).round(1)

def computeAccessPathLength(accessPathString):
    return len(accessPathString.split(","))
results ={}

uniqueSeeds = set()
for fname in glob.glob(RESULTS_PATH):
    print(fname)
    if "ideal" in fname and not "-ap" in fname:
        timesIDEAL = []
        timesIDEAL_AP = []
        timesFINK_UNIQUE = []
        timesFINK_APMUST = []
        timeoutsIDEAL = 0
        containsFieldLoop = 0
        timeoutsIDEAL_AP = 0
        timeoutsFINK_UNIQUE = 0
        maxMemoryIDEAL_AP = 0
        maxMemoryIDEAL = 0
        maxMemoryIDEAL_AP_ARRAY = []
        maxMemoryIDEAL_ARRAY = []
        timeoutsFINK_APMUST = 0
        errorsIDEAL = 0
        errorsFINK_UNIQUE = 0
        errorsFINK_APMUST = 0
        methodsIDEAL_AP = []
        methodsIDEAL  = []
        maxAccessPath  = []
        dataIDEAL_AP = getData(fname,"ideal-ap")
        dataIDEAL =  getData(fname,"ideal")
        for rowIDEAL in dataIDEAL:
            foundInFINK_UNIQUE = False
            for rowIDEAL_AP in dataIDEAL_AP:
                if rowIDEAL_AP['SeedMethod'] == rowIDEAL['SeedMethod'] and rowIDEAL_AP['SeedStatement'] == rowIDEAL['SeedStatement']:
                    if not foundInFINK_UNIQUE:
                        foundInFINK_UNIQUE = True
                        if rowIDEAL['Timedout'] == "true":
                            timeoutsIDEAL += 1
                            timesIDEAL.append(MAX_ANALYSIS_TIME)
                        else:
                            timesIDEAL.append(beautifyAnalysisTime(rowIDEAL['AnalysisTimes']))
                            

                        if rowIDEAL_AP['Timedout'] == "true":
                            timesFINK_UNIQUE.append(MAX_ANALYSIS_TIME)
                            timeoutsFINK_UNIQUE += 1
                        else:
                            timesFINK_UNIQUE.append(beautifyAnalysisTime(rowIDEAL_AP['AnalysisTimes']))
                        maxMemoryIDEAL = max(int(rowIDEAL['MaxMemory']), maxMemoryIDEAL)
                        maxMemoryIDEAL_AP = max(int(rowIDEAL_AP['MaxMemory']), maxMemoryIDEAL_AP)
                        maxMemoryIDEAL_ARRAY.append(maxMemoryIDEAL)
                        maxMemoryIDEAL_AP_ARRAY.append(maxMemoryIDEAL_AP)
                        methodsIDEAL.append(int(rowIDEAL['VisitedMethod']))
                        methodsIDEAL_AP.append(int(rowIDEAL_AP['VisitedMethod']))
                        maxAccessPath.append(computeAccessPathLength(rowIDEAL_AP['MaxAccessPath']))
                        

                        if rowIDEAL['FieldLoop'] == "true":
                            containsFieldLoop += 1

                        if rowIDEAL['Is_In_Error'] == "true":
                            errorsIDEAL += 1
                        if rowIDEAL_AP['Is_In_Error'] == "true":
                            errorsFINK_UNIQUE += 1

        if(len(dataIDEAL) < len(timesIDEAL_AP)):
            print("SOMETHING WENT WRONG")
        if len(timesIDEAL) != 0:
            print(fname.replace("ideal",""))
            print(timesIDEAL)
            print(str(len(timesIDEAL)) + " "+ str(geo_mean(timesIDEAL)))
            print(str(len(timesIDEAL_AP)) + " "+ str(geo_mean(timesIDEAL_AP)))
            print(str(len(timesFINK_UNIQUE)) + " "+ str(geo_mean(timesFINK_UNIQUE)))

            print("SPDS-Times-Methods " + str(numpy.corrcoef(timesIDEAL,methodsIDEAL)))
            print("SPDS-Times-AccessPath " + str(numpy.corrcoef(timesIDEAL,maxAccessPath)))
            print("SPDS-Times-Memory " + str(numpy.corrcoef(timesIDEAL,maxMemoryIDEAL_ARRAY)))

            print("AP-Times-Methods " + str(numpy.corrcoef(timesFINK_UNIQUE,methodsIDEAL_AP)))
            print("AP-Times-AccessPath " + str(numpy.corrcoef(timesFINK_UNIQUE,maxAccessPath)))
            print("AP-Times-Memory " + str(numpy.corrcoef(timesFINK_UNIQUE,maxMemoryIDEAL_AP_ARRAY)))

            outputFileName = ""
            print("IDEAL Errors:" + str(errorsIDEAL))
            if "-IO.csv" in fname:
                outputFileName = "io_ideal_vs_idealap.csv"
            if "-EmptyVector.csv" in fname:
                outputFileName = "vector_ideal_vs_idealap.csv"
            if "-IteratorHasNext.csv" in fname:
                outputFileName = "iterator_ideal_vs_idealap.csv"
            if "-Singature.csv" in fname:
                outputFileName = "signature_ideal_vs_idealap.csv"
            if "-URLConnection.csv" in fname:
                outputFileName = "url_ideal_vs_idealap.csv"
            if "-KeyStore.csv" in fname:
                outputFileName = "keystore_ideal_vs_idealap.csv"
            d = {}
            if outputFileName in results:
                d = results[outputFileName]
            benchmarkName = fname.replace("dacapo.","")
            benchmarkName = benchmarkName.replace("typestate\\","")
            benchmarkName = benchmarkName.replace("org","hsqldb")
            benchmarkName = benchmarkName[0:benchmarkName.find(".")]
            entry = {"benchmark":  benchmarkName, 
                    "ideal": toSeconds(geo_mean(timesIDEAL)),
                    "ideal_ap": toSeconds(geo_mean(timesFINK_UNIQUE)),
                    "total_ideal": toSeconds(np.sum(timesIDEAL)),
                    "total_ideal_ap": toSeconds(np.sum(timesFINK_UNIQUE)),
                    "seeds": len(timesIDEAL),
                    "timeouts_ideal_ap": timeoutsFINK_UNIQUE,
                    "timeouts_ideal": timeoutsIDEAL,
                    "timeouts_fraction_ideal_ap": round(timeoutsFINK_UNIQUE*100/float(len(timesIDEAL)),1),
                    "timeouts_fraction_ideal": round(timeoutsIDEAL*100/float(len(timesIDEAL)),1),
                    "errors_ideal_ap": errorsFINK_UNIQUE,
                    "errors_ideal": errorsIDEAL,
                    "methods_ideal": int(geo_mean(methodsIDEAL)),
                    "methods_ideal_ap": int(geo_mean(methodsIDEAL_AP)),
                    "max_memory_ideal_ap": (maxMemoryIDEAL_AP/(1024*1024)),
                    "max_memory_ideal": (maxMemoryIDEAL/(1024*1024)),
                    "containsFieldLoop": containsFieldLoop,
                    "maxAccessPath": int(geo_mean(maxAccessPath))}
            d[fname] = entry
            results[outputFileName] = d

header = ["benchmark", "ideal","ideal_ap","seeds","timeouts_ideal_ap","timeouts_ideal","errors_ideal_ap","errors_ideal","methods_ideal_ap","methods_ideal", "containsFieldLoop","maxAccessPath", "total_ideal", "total_ideal_ap","timeouts_fraction_ideal_ap","timeouts_fraction_ideal","max_memory_ideal_ap","max_memory_ideal"]

benchmarks = ["antlr", "bloat", "chart", "eclipse", "fop", "hsqldb", "jython", "luindex", "lusearch", "pmd", "xalan"]

def index_containing_substring(diction, substring):
    for key,value in diction.items():
        if substring in key:
              return key
    return -1

for i in results:
    file = open(i,"w")
    for k in header:
        file.write(k+";")
    file.write("index\n")
    index = 1
    avegareRatiosTime = []
    print("SSS")
    print(results[i])
    for j in benchmarks:
        if index_containing_substring(results[i], j) != -1:
            realKey = index_containing_substring(results[i], j)
            for k in header:
                file.write(str(results[i][realKey][k]) + ";")
            avegareRatiosTime.append(float(results[i][realKey]["ideal_ap"])/results[i][realKey]["ideal"])
        else:
        	file.write(j + ";-;-;-;-;-;-;-;-;-;-;-;-;-;0;0;-;-;")
        file.write(str(index)+"\n")
        index += 1
