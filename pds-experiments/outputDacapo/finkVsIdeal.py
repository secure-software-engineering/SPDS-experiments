

import glob, csv, math, os.path, sys
import plotly.graph_objs
import numpy as np
from sets import Set

MAX_ANALYSIS_TIME = 30000;
MAX_ACCESS_PATH = 0;
MAX_VISITED_METHODS = 1000;

def beautifyAnalysisTime(time):
    if int(time) > MAX_ANALYSIS_TIME:
        return MAX_ANALYSIS_TIME
    if int(time) == 0:
        return 1
    return int(time)

def geo_mean(iterable):
    return np.mean(iterable)
    #if len(iterable) == 0:
    #    return 0;
    #a = np.log(iterable)
    #return np.exp(a.sum()/len(a))
def getData(filename, replacement):
    if os.path.isfile(filename.replace("ideal-ap",replacement)):
        with open(filename.replace("ideal-ap",replacement)) as csvfile:
            readerFINK_UNIQUE = csv.DictReader(csvfile,delimiter = ";")
            return list(readerFINK_UNIQUE)
    return []
def toSeconds(ms):
    return (ms/1000).round(1)

results ={}

path = "*.csv"
for fname in glob.glob(path):
    #if "dacapo.bloat.Main2-ideal-ap-IO" in fname:
    if "ideal-ap" in fname:
        timesIDEAL = []
        timesIDEAL_AP = []
        timesFINK_UNIQUE = []
        timesFINK_APMUST = []
        timeoutsIDEAL = 0
        timeoutsIDEAL_AP = 0
        timeoutsFINK_UNIQUE = 0
        timeoutsFINK_APMUST = 0
        dataIDEAL_AP = getData(fname,"ideal-ap")
        dataIDEAL =  getData(fname,"ideal")
        dataFINK_UNIQUE = getData(fname,"fink-unique")
        dataFINK_APMUST = getData(fname,"fink-apmust")
        for rowIDEAL_AP in dataIDEAL_AP:
            foundInFINK_UNIQUE = False
            for rowFINK_UNIQUE in dataFINK_UNIQUE:
                if rowFINK_UNIQUE['SeedMethod'] == rowIDEAL_AP['SeedMethod'] and int(rowFINK_UNIQUE['VisitedMethod']) != 0 and int(rowIDEAL_AP['VisitedMethod']) != 0:
                    if not foundInFINK_UNIQUE:
                        timesFINK_UNIQUE.append(beautifyAnalysisTime(rowFINK_UNIQUE['AnalysisTimes']))
                        timesIDEAL_AP.append(beautifyAnalysisTime(rowIDEAL_AP['AnalysisTimes']))
                        if rowIDEAL_AP['Timedout'] == "true":
                            timeoutsIDEAL_AP += 1
                        if rowFINK_UNIQUE['Timedout'] == "true":
                            timeoutsFINK_UNIQUE += 1
                    foundInFINK_UNIQUE = True
            if foundInFINK_UNIQUE:
                foundInFINK_APMUST = False
                for rowFINK_APMUST in dataFINK_APMUST:                    
                    if rowFINK_APMUST['SeedMethod'] == rowIDEAL_AP['SeedMethod']:
                        if not foundInFINK_APMUST:
                            timesFINK_APMUST.append(beautifyAnalysisTime(rowFINK_APMUST['AnalysisTimes']))
                            foundInFINK_APMUST = True
                            if rowFINK_APMUST['Timedout'] == "true":
                                timeoutsFINK_APMUST += 1
                for rowIDEAL in dataIDEAL:
                    if (rowIDEAL['SeedMethod'] == rowIDEAL_AP['SeedMethod'] and rowIDEAL['SeedStatement'] == rowIDEAL_AP['SeedStatement']):
                        timesIDEAL.append(beautifyAnalysisTime(rowIDEAL['AnalysisTimes']))
                        if rowIDEAL['Timedout'] == "true":
                            timeoutsIDEAL += 1
        if(len(dataIDEAL) < len(timesIDEAL_AP)):
            print "SOMETHING WENT WRONG"
        if len(timesIDEAL_AP) != 0:
            print fname.replace("ideal-ap","")
            print str(len(timesIDEAL)) + " "+ str(geo_mean(timesIDEAL))
            print str(len(timesIDEAL_AP)) + " "+ str(geo_mean(timesIDEAL_AP))
            print str(len(timesFINK_UNIQUE)) + " "+ str(geo_mean(timesFINK_UNIQUE))
            print str(len(timesFINK_APMUST)) + " "+ str(geo_mean(timesFINK_APMUST))
            outputFileName = ""
            if "-IO.csv" in fname:
                outputFileName = "io_ideal_vs_fink.csv"
            if "-EmptyVector.csv" in fname:
                outputFileName = "vector_ideal_vs_fink.csv"
            if "-IteratorHasNext.csv" in fname:
                outputFileName = "iterator_ideal_vs_fink.csv"
            d = {}
            if outputFileName in results:
                d = results[outputFileName]
            entry = {"name":   fname, 
                    "ideal": toSeconds(geo_mean(timesIDEAL)),
                    "fink": toSeconds(geo_mean(timesFINK_UNIQUE)),
                    "fink_apmust": toSeconds(geo_mean(timesFINK_APMUST)),
                    "seeds": len(timesIDEAL_AP),
                    "timeouts_fink": timeoutsFINK_UNIQUE,
                    "timeouts_finkapmust": timeoutsFINK_APMUST,
                    "timeouts_ideal": timeoutsIDEAL}
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

header = ["name", "ideal","fink","fink_apmust","seeds","timeouts_fink","timeouts_finkapmust","timeouts_ideal"]

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
        print str(results[i][j]["seeds"]) + "&"
        avegareRatiosTime.append(float(results[i][j]["fink_apmust"])/results[i][j]["ideal"])
        index += 1
    print "Ratio Times: IDEAL vs Fink AP Must:" + str(geo_mean(avegareRatiosTime))
