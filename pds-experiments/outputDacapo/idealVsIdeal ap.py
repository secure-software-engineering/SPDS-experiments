

import glob, csv, math, os.path, sys
import plotly.graph_objs
import numpy as np
from sets import Set

MAX_ANALYSIS_TIME = 600000;
MAX_ACCESS_PATH = 0;
MAX_VISITED_METHODS = 1000;
import numpy
def beautifyAnalysisTime(time):
    if int(time) > MAX_ANALYSIS_TIME:
        return MAX_ANALYSIS_TIME
    if int(time) == 0:
        return 1
    return int(time)

def geo_mean(iterable):
   return np.mean(iterable)
   # if len(iterable) == 0:
   #     return 0;
   # a = np.log(iterable)
   # return np.exp(a.sum()/len(a))
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
path = "*.csv"

uniqueSeeds = set()
for fname in glob.glob(path):
    #if "dacapo.xalan.Main2-ideal-IteratorHasNext" in fname:
    print fname
    if "ideal" in fname and not "-ap" in fname and not "_vs_" in fname:
        
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
        dataFINK_UNIQUE = getData(fname,"fink-unique")
        dataFINK_APMUST = getData(fname,"fink-apmust")
        for rowIDEAL_AP in dataIDEAL:
            foundInFINK_UNIQUE = False
            for rowFINK_UNIQUE in dataIDEAL_AP:
                if rowFINK_UNIQUE['SeedMethod'] == rowIDEAL_AP['SeedMethod'] and rowFINK_UNIQUE['SeedStatement'] == rowIDEAL_AP['SeedStatement']:
                    if not foundInFINK_UNIQUE:
                        foundInFINK_UNIQUE = True
                        if rowIDEAL_AP['Timedout'] == "true":
                            timeoutsIDEAL += 1
                            timesIDEAL.append(MAX_ANALYSIS_TIME)
                        else:
                            timesIDEAL.append(beautifyAnalysisTime(rowIDEAL_AP['AnalysisTimes']))
                            

                        if rowFINK_UNIQUE['Timedout'] == "true":
                            timesFINK_UNIQUE.append(MAX_ANALYSIS_TIME)
                            timeoutsFINK_UNIQUE += 1
                        else:
                            timesFINK_UNIQUE.append(beautifyAnalysisTime(rowFINK_UNIQUE['AnalysisTimes']))
                        maxMemoryIDEAL = max(int(rowIDEAL_AP['MaxMemory']), maxMemoryIDEAL)
                        maxMemoryIDEAL_AP = max(int(rowFINK_UNIQUE['MaxMemory']), maxMemoryIDEAL_AP)
                        maxMemoryIDEAL_ARRAY.append(maxMemoryIDEAL)
                        maxMemoryIDEAL_AP_ARRAY.append(maxMemoryIDEAL_AP)
                        methodsIDEAL.append(int(rowIDEAL_AP['VisitedMethod']))
                        methodsIDEAL_AP.append(int(rowFINK_UNIQUE['VisitedMethod']))
                        maxAccessPath.append(computeAccessPathLength(rowFINK_UNIQUE['MaxAccessPath']))
                        

                        if rowIDEAL_AP['FieldLoop'] == "true":
                            containsFieldLoop += 1

                        if rowIDEAL_AP['Is_In_Error'] == "true":
                            errorsIDEAL += 1
                            uniqueSeeds.add(rowIDEAL_AP['Seed'])
                        if rowFINK_UNIQUE['Is_In_Error'] == "true":
                            errorsFINK_UNIQUE += 1
           
        if(len(dataIDEAL) < len(timesIDEAL_AP)):
            print "SOMETHING WENT WRONG"
        if len(timesIDEAL) != 0:
            print fname.replace("ideal","")
            print timesIDEAL
            print str(len(timesIDEAL)) + " "+ str(geo_mean(timesIDEAL))
            print str(len(timesIDEAL_AP)) + " "+ str(geo_mean(timesIDEAL_AP))
            print str(len(timesFINK_UNIQUE)) + " "+ str(geo_mean(timesFINK_UNIQUE))

            print "SPDS-Times-Methods " + str(numpy.corrcoef(timesIDEAL,methodsIDEAL))
            print "SPDS-Times-AccessPath " + str(numpy.corrcoef(timesIDEAL,maxAccessPath))
            print "SPDS-Times-Memory " + str(numpy.corrcoef(timesIDEAL,maxMemoryIDEAL_ARRAY))

            print "AP-Times-Methods " + str(numpy.corrcoef(timesFINK_UNIQUE,methodsIDEAL_AP))
            print "AP-Times-AccessPath " + str(numpy.corrcoef(timesFINK_UNIQUE,maxAccessPath))
            print "AP-Times-Memory " + str(numpy.corrcoef(timesFINK_UNIQUE,maxMemoryIDEAL_AP_ARRAY))

            outputFileName = ""
            print "IDEAL Errors:" + str(errorsIDEAL)
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
          #  if results[outputFileName] == None:
          #      results[outputFileName] = []
          #             
          #  if not os.path.isfile(outputFileName):
          #      file = open(outputFileName,"w")
          #      file.write("name,index,ideal,fink,fink_apmust\n")
          #  file = open(outputFileName,"a") 
          #  num_lines = sum(1 for file in open(outputFileName))
          #  file.write(fname + ","+ str(num_lines) + ","+str(toSeconds(geo_mean(timesIDEAL)))+","+str(toSeconds(geo_mean(timesFINK_UNIQUE)))+","+str(toSeconds(geo_mean(timesFINK_APMUST)))+"\n")
          #  file.close() 

header = ["benchmark", "ideal","ideal_ap","seeds","timeouts_ideal_ap","timeouts_ideal","errors_ideal_ap","errors_ideal","methods_ideal_ap","methods_ideal", "containsFieldLoop","maxAccessPath", "total_ideal", "total_ideal_ap","timeouts_fraction_ideal_ap","timeouts_fraction_ideal","max_memory_ideal_ap","max_memory_ideal"]

for i in results:
    file = open(i,"w")
    for k in header:
        file.write(k+";")
    file.write("index\n")
    index = 1
    inorder = sorted(results[i])
    print i
    avegareRatiosTime = []
    for j in sorted(results[i]):
        for k in header:
            file.write(str(results[i][j][k]) + ";")
        file.write(str(index)+"\n")
        avegareRatiosTime.append(float(results[i][j]["ideal_ap"])/results[i][j]["ideal"])
        index += 1
    print avegareRatiosTime
    print "Ratio Times: IDEAL vs Fink AP Must:" + str(geo_mean(avegareRatiosTime))

for i in uniqueSeeds:
    print i
