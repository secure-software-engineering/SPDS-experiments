

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
    #return str(np.mean(iterable))
    if len(iterable) == 0:
        return str(0);
    a = np.log(iterable)
    return str(np.exp(a.sum()/len(a)))
def getData(filename, replacement):
    if os.path.isfile(filename.replace("ideal-ap",replacement)):
        with open(filename.replace("ideal-ap",replacement)) as csvfile:
            readerFINK_UNIQUE = csv.DictReader(csvfile,delimiter = ";")
            return list(readerFINK_UNIQUE)
    return []

path = "*.csv"
for fname in glob.glob(path):
    #if "dacapo.bloat.Main2-ideal-ap-IO" in fname:
    if "ideal-ap" in fname:
        timesIDEAL = []
        timesIDEAL_AP = []
        timesFINK_UNIQUE = []
        timesFINK_APMUST = []
        dataIDEAL_AP = getData(fname,"ideal-ap")
        dataIDEAL =  getData(fname,"ideal")
        dataFINK_UNIQUE = getData(fname,"fink-unique")
        dataFINK_APMUST = getData(fname,"fink-apmust")
        for rowIDEAL_AP in dataIDEAL_AP:
            foundInFINK_UNIQUE = False
            for rowFINK_UNIQUE in dataFINK_UNIQUE:
                if rowFINK_UNIQUE['SeedMethod'] == rowIDEAL_AP['SeedMethod']:
                    if not foundInFINK_UNIQUE:
                        timesFINK_UNIQUE.append(beautifyAnalysisTime(rowFINK_UNIQUE['AnalysisTimes']))
                        timesIDEAL_AP.append(beautifyAnalysisTime(rowIDEAL_AP['AnalysisTimes']))
                    foundInFINK_UNIQUE = True
            if foundInFINK_UNIQUE:
                foundInFINK_APMUST = False
                for rowFINK_APMUST in dataFINK_APMUST:                    
                    if rowFINK_APMUST['SeedMethod'] == rowIDEAL_AP['SeedMethod']:
                        if not foundInFINK_APMUST:
                            timesFINK_APMUST.append(beautifyAnalysisTime(rowFINK_APMUST['AnalysisTimes']))
                            foundInFINK_APMUST = True
                for rowIDEAL in dataIDEAL:
                    if (rowIDEAL['SeedMethod'] == rowIDEAL_AP['SeedMethod'] and rowIDEAL['SeedStatement'] == rowIDEAL_AP['SeedStatement']):
                        timesIDEAL.append(beautifyAnalysisTime(rowIDEAL['AnalysisTimes']))
        if(len(dataIDEAL) < len(timesIDEAL_AP)):
            print "SOMETHING WENT WRONG"
        if len(timesIDEAL_AP) != 0:
            print fname.replace("ideal-ap","")
            print str(len(timesIDEAL)) + " "+ geo_mean(timesIDEAL)
            print str(len(timesIDEAL_AP)) + " "+ geo_mean(timesIDEAL_AP)
            print str(len(timesFINK_UNIQUE)) + " "+ geo_mean(timesFINK_UNIQUE)
            print str(len(timesFINK_APMUST)) + " "+ geo_mean(timesFINK_APMUST)
