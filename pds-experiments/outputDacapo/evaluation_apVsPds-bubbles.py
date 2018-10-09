

import glob, csv, math
import plotly.graph_objs
import numpy as np

MAX_ANALYSIS_TIME = 600000;
MAX_ACCESS_PATH = 0;
MAX_VISITED_METHODS = 0;
OBJECTS = 0


## Compute Heatmaps
numberOfBucketsForTimes = 5

def beautifyAnalysisTime(time):
    if int(time) > MAX_ANALYSIS_TIME:
        return MAX_ANALYSIS_TIME
    return time
def computeAccessPathLength(accessPathString):
    return len(accessPathString.split(","))

def scale(val,valMax,intervalMin,intervalMax):
    diff = intervalMax-intervalMin
    return intervalMin + val/(float(valMax))*diff

def printForTikz(data,fillData,timeoutData, filename):
    file = open(filename,"w")
    file.write("bucket_nesting_depth(x);bucket_visited_methods(y);radius_bubble;average_bucket_analysis_time;bubble_opacity;sum_objects\n")
    for i in range(0,len(data)):
        for j in range(0,len(data[i])):
            if data[i][j] != 0:
                 file.write(str(j+1)+";"+str(i+1)+";"+str(scale(data[i][j],MAX_ANALYSIS_TIME,0.1,0.5))+";"+str((data[i][j]/1000).round(1))+";"+str(scale(fillData[i][j],OBJECTS,0.1,1))+";"+str(fillData[i][j])+";\n")



def mean(iterable):
    if len(iterable) == 0:
        return 0;
    return np.mean(iterable)

path = "typestate/*.csv"
methodAP = []
timesAP = []
timesPDS = []
methodPDS = []
ap_length = []
for fname in glob.glob(path):
    if "ideal-ap" in fname:
        with open(fname) as csvfile:
            reader = csv.DictReader(csvfile,delimiter = ";")
            for rowAP in reader:
                with open(fname.replace("ideal-ap","ideal")) as csvfilePDS:
                    readerPDS = csv.DictReader(csvfilePDS,delimiter = ";")
                    for rowPDS in readerPDS:
                        if rowPDS['SeedClass'] == rowAP['SeedClass'] and rowPDS['SeedMethod'] == rowAP['SeedMethod'] and rowAP['SeedStatement'] == rowPDS['SeedStatement']:
                            if(rowPDS['Timedout'] == "true"):
                                timesPDS.append(MAX_ANALYSIS_TIME)
                            else:
                                timesPDS.append(beautifyAnalysisTime(rowPDS['AnalysisTimes']))
                            if(rowAP['Timedout'] == "true"):
                                timesAP.append(MAX_ANALYSIS_TIME)
                            else:
                                timesAP.append(beautifyAnalysisTime(rowAP['AnalysisTimes']))
                            
                            ap = computeAccessPathLength(rowAP['MaxAccessPath']) 
                            ap_length.append(ap)
                            vm = int(rowPDS['VisitedMethod'])
                            methodAP.append(vm)
                            if vm > MAX_VISITED_METHODS:
                                MAX_VISITED_METHODS = int(math.ceil(vm / 100.0)) * 100;
                            if ap > MAX_ACCESS_PATH:
                                MAX_ACCESS_PATH = ap
OBJECTS = len(timesAP)


def computeBubbles(analysisTimes, visitedMethods, filename ):
    bucketRangeAccessPath = int(math.floor(MAX_ACCESS_PATH/numberOfBucketsForTimes))
    bucketRangeMethods = MAX_VISITED_METHODS/numberOfBucketsForTimes
    data = []
    timeoutData = []
    xLabel = []
    yLabel = []
    #Create Labels
    for i in range(0,numberOfBucketsForTimes +1):
        yLabel.append("["+str(i*bucketRangeMethods) +"-"+str((i+1)*bucketRangeMethods)+"]")
        zero = []
        zeroTime = []
        for j in range(0,numberOfBucketsForTimes + 1):
            if j == numberOfBucketsForTimes - 1:
                xLabel.append("["+str(j*bucketRangeAccessPath) +"-"+str(MAX_ACCESS_PATH)+"]")
            else:
                xLabel.append("["+str(j*bucketRangeAccessPath) +"-"+str((j+1)*bucketRangeAccessPath)+"]")
            zero.append([])
            zeroTime.append(0)
        timeoutData.append(zeroTime)
        data.append(zero)
    xBucketIndex = 0

    #Iterate over data
    for t in analysisTimes:
        time = int(t)
        visitedMethodBucket = int(math.floor(int(visitedMethods[xBucketIndex])/bucketRangeMethods))
        accessPathBucket = int(math.floor(int(ap_length[xBucketIndex])/bucketRangeAccessPath))
        if time != 0:
            data[visitedMethodBucket][accessPathBucket].append(time)
        else:
            data[visitedMethodBucket][accessPathBucket].append(1)     
        if time == MAX_ANALYSIS_TIME:
            timeoutData[visitedMethodBucket][accessPathBucket] = timeoutData[visitedMethodBucket][accessPathBucket] + 1
        xBucketIndex += 1

    avgData = []
    textData = []
    for i in range(0,numberOfBucketsForTimes):
        avgDataRow = []
        textDataRow = []
        for j in range(0,numberOfBucketsForTimes):
            avgDataRow.append(mean(data[i][j]))
            textDataRow.append(len(data[i][j]))
        avgData.append(avgDataRow)
        textData.append(textDataRow)
    printForTikz(avgData,textData,timeoutData,filename)

computeBubbles(timesAP,methodAP,"ap_bubbles.csv")
computeBubbles(timesPDS,methodAP,"pds_bubbles.csv")



