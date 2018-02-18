

import glob, csv, math, os.path
import plotly.graph_objs
import numpy as np

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
    if len(iterable) == 0:
        return str(0);
    a = np.log(iterable)
    return str(np.exp(a.sum()/len(a)))
path = "*.csv"
print geo_mean([])
for fname in glob.glob(path):
    if "ideal-ap" in fname:
        timesIDEAL = []
        timesIDEAL_AP = []
        timesFINK_UNIQUE = []
        timesFINK_APMUST = []
        with open(fname) as csvfile:
            reader = csv.DictReader(csvfile,delimiter = ";")
            for rowIDEAL_AP in reader:
                if os.path.isfile(fname.replace("ideal-ap","fink-unique")):
                    with open(fname.replace("ideal-ap","fink-unique")) as csvfileFINK_UNIQUE:
                        readerFINK_UNIQUE = csv.DictReader(csvfileFINK_UNIQUE,delimiter = ";")
                        for rowFINK_UNIQUE in readerFINK_UNIQUE:
                            if rowFINK_UNIQUE['SeedMethod'] == rowIDEAL_AP['SeedMethod']:
                                timesIDEAL_AP.append(beautifyAnalysisTime(rowIDEAL_AP['AnalysisTimes']))
                                timesFINK_UNIQUE.append(beautifyAnalysisTime(rowFINK_UNIQUE['AnalysisTimes']))
                                with open(fname.replace("ideal-ap","fink-apmust")) as csvfileFINK_APMUST:
                                    readerFINK_APMUST = csv.DictReader(csvfileFINK_APMUST,delimiter = ";")
                                    for rowFINK_APMUST in readerFINK_APMUST:
                                        if rowFINK_APMUST['SeedMethod'] == rowIDEAL_AP['SeedMethod']:
                                            timesFINK_APMUST.append(beautifyAnalysisTime(rowFINK_APMUST['AnalysisTimes']))
                                with open(fname.replace("ideal-ap","ideal")) as csvfileIDEAL:
                                    readerIDEAL = csv.DictReader(csvfileIDEAL,delimiter = ";")
                                    for rowIDEAL in readerIDEAL:
                                        if rowIDEAL['SeedMethod'] == rowIDEAL_AP['SeedMethod']:
                                            print rowIDEAL['Seed']
                                            timesIDEAL.append(beautifyAnalysisTime(rowIDEAL['AnalysisTimes']))

        
        print fname.replace("ideal-ap","")
        print str(len(timesIDEAL)) + " "+ geo_mean(timesIDEAL)
        print str(len(timesIDEAL_AP)) + " "+ geo_mean(timesIDEAL_AP)
        print str(len(timesFINK_UNIQUE)) + " "+ geo_mean(timesFINK_UNIQUE)
        print str(len(timesFINK_APMUST)) + " "+ geo_mean(timesFINK_APMUST)
