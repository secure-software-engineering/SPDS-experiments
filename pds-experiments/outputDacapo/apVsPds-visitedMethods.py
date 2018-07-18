

import glob, csv, math
import plotly.graph_objs
import numpy as np

MAX_ANALYSIS_TIME = 600000;
MAX_ACCESS_PATH = 0;
MAX_VISITED_METHODS = 0;
USE_ARITHMEAN = True
SEEDS = 0


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

def printForTikz(data,fillData,timeoutData):
    print "x,y,r,times,opacity,seeds,timeouts"
    for i in range(0,len(data)):
        for j in range(0,len(data[i])):
            if data[i][j] != 0:
                print str(j+1)+","+str(i+1)+","+str(scale(data[i][j],MAX_ANALYSIS_TIME,0.1,0.5))+","+str((data[i][j]/1000).round(1))+","+str(scale(fillData[i][j],SEEDS,0.1,1))+","+str(fillData[i][j])+","+str(timeoutData[i][j])



def mean(iterable):
    if len(iterable) == 0:
        return 0;
    if USE_ARITHMEAN:
        return np.mean(iterable)
    a = np.log(iterable)
    return np.exp(a.sum()/len(a))

def plotBarChartTimes(analysisTimes):
    bucketRangeTime = MAX_ANALYSIS_TIME/numberOfBucketsForTimes
    data = []
    for i in range(0,numberOfBucketsForTimes):
        data.append(0)
    for t in analysisTimes:
        time = int(t)
        if time == MAX_ANALYSIS_TIME:
            bucket = numberOfBucketsForTimes-1
        else:
            bucket = int(math.floor(time)/bucketRangeTime)
        data[bucket] = data[bucket] + 1

    print "TimeRanges Seeds"
    bucketNo = 0
    for d in data:
        print "["+str(bucketNo*bucketRangeTime/1000) +"-"+str((bucketNo+1)*bucketRangeTime/1000)+"] " + str(d)
        bucketNo += 1
    return plotly.graph_objs.Bar(y=data)
path = "*.csv"
methodAP = []
timesAP = []
timesPDS = []
methodPDS = []
ap_length = []
for fname in glob.glob(path):
    if "ideal-ap" in fname:
        with open(fname) as csvfile:
            reader = csv.DictReader(csvfile,delimiter = ";")
            for row in reader:
                with open(fname.replace("ideal-ap","ideal")) as csvfilePDS:
                    readerPDS = csv.DictReader(csvfilePDS,delimiter = ";")
                    for rowPDS in readerPDS:
                        if rowPDS['SeedClass'] == row['SeedClass'] and rowPDS['SeedMethod'] == row['SeedMethod'] and row['SeedStatement'] == rowPDS['SeedStatement']:
                            if(rowPDS['Timedout'] == "true"):
                                timesPDS.append(MAX_ANALYSIS_TIME)
                            else:
                                timesPDS.append(beautifyAnalysisTime(rowPDS['AnalysisTimes']))
                            if(row['Timedout'] == "true"):
                                timesAP.append(MAX_ANALYSIS_TIME)
                            else:
                                timesAP.append(beautifyAnalysisTime(row['AnalysisTimes']))
                            
                            ap = computeAccessPathLength(row['MaxAccessPath']) 
                            ap_length.append(ap)
                            vm = int(rowPDS['VisitedMethod'])
                            methodAP.append(vm)
                            if vm > MAX_VISITED_METHODS:
                                MAX_VISITED_METHODS = int(math.ceil(vm / 100.0)) * 100;
                            if ap > MAX_ACCESS_PATH:
                                MAX_ACCESS_PATH = ap
#                            methodPDS.append(rowPDS['VisitedMethod'])
#               
SEEDS = len(timesAP)

trace1 = plotly.graph_objs.Scatter3d(
    x = methodAP,
    y = timesAP,
    z = ap_length,
    mode = 'markers',
    name = 'Access Path'
)
trace2 = plotly.graph_objs.Scatter3d(
    x = methodPDS,
    y = timesPDS,
    z = ap_length,
    mode = 'markers',
    name = 'Pushdown Systems',
)
data = [trace1, trace2]
# Plot and embed in ipython notebook!
#plotly.offline.plot(data, filename="plot3d-VisitedMethodAccessPathToTimes.html")    

trace3 = plotly.graph_objs.Scatter(
    x = ap_length,
    y = timesAP,
    text = methodPDS,
    mode = 'markers',
    name = 'Access Path'
)
trace4 = plotly.graph_objs.Scatter(
    x = ap_length,
    y = timesPDS,
    text = methodPDS,
    mode = 'markers',
    name = 'Pushdown Systems'
)
data = [trace3, trace4]
# Plot and embed in ipython notebook!
#plotly.offline.plot(data, filename="plot-AccessPathToTimes.html")    

trace3 = plotly.graph_objs.Scatter(
    x = methodAP,
    y = timesAP,
    text = ap_length,
    mode = 'markers',
    name = 'Access Path'
)
trace4 = plotly.graph_objs.Scatter(
    x = methodPDS,
    y = timesPDS,
    text = ap_length,
    mode = 'markers',
    name = 'Pushdown Systems'
)
data = [trace3, trace4]
# Plot and embed in ipython notebook!
#plotly.offline.plot(data, filename="plot-VisitedMethods.html") 


def plotHeatMapAccessPath(analysisTimes, filename ):
    bucketRange = MAX_ANALYSIS_TIME/numberOfBucketsForTimes
    data = []
    for i in range(0,numberOfBucketsForTimes+1):
        zero = []
        for j in range(0,MAX_ACCESS_PATH+1):
            zero.append(0)
        data.append(zero)
    xBucketIndex = 0
    for t in analysisTimes:
        time = int(t)
        accessPathLength = int(ap_length[xBucketIndex])
        yBucketIndex = int(math.floor(time/bucketRange))
        data[yBucketIndex][accessPathLength] = data[yBucketIndex][accessPathLength]+1
        xBucketIndex += 1

    trace = plotly.graph_objs.Heatmap(z=data)
    plotly.offline.plot([trace], filename=filename)

#plotHeatMapAccessPath(timesAP,"plot-heatmap-accesspath")
#plotHeatMapAccessPath(timesPDS,"plot-heatmap-pds")


def plotHeatMapVisitedMethods(analysisTimes, visitedMethods, filename ):
    bucketRange = MAX_ANALYSIS_TIME/numberOfBucketsForTimes
    bucketRangeMethods = MAX_VISITED_METHODS/numberOfBucketsForTimes
    data = []
    for i in range(0,numberOfBucketsForTimes+1):
        zero = []
        for j in range(0,numberOfBucketsForTimes+1):
            zero.append(0)
        data.append(zero)
    xBucketIndex = 0
    for t in analysisTimes:
        time = int(t)
        visitedMethodBucket = int(math.floor(int(visitedMethods[xBucketIndex])/bucketRangeMethods))
        yBucketIndex = int(math.floor(time/bucketRange))
        data[yBucketIndex][visitedMethodBucket] = data[yBucketIndex][visitedMethodBucket]+1
        xBucketIndex += 1

    trace = plotly.graph_objs.Heatmap(z=data,zauto=False,zmax=20)
    plotly.offline.plot([trace], filename=filename)


#plotHeatMapVisitedMethods(timesAP,methodAP,"plot-heatmap-visitedMethod-accesspath")
#plotHeatMapVisitedMethods(timesPDS,methodAP,"plot-heatmap-visitedMethod-pds")

def plotHeatMapVisitedMethodsTimes(analysisTimes, visitedMethods, filename ):
    bucketRangeAccessPath = int(math.floor(MAX_ACCESS_PATH/numberOfBucketsForTimes)) +1
    bucketRangeMethods = MAX_VISITED_METHODS/numberOfBucketsForTimes
    print MAX_ACCESS_PATH
    data = []
    timeoutData = []
    xLabel = []
    yLabel = []
    for i in range(0,numberOfBucketsForTimes):
        yLabel.append("["+str(i*bucketRangeMethods) +"-"+str((i+1)*bucketRangeMethods)+"]")
        zero = []
        zeroTime = []
        for j in range(0,numberOfBucketsForTimes):
            if j == numberOfBucketsForTimes - 1:
                xLabel.append("["+str(j*bucketRangeAccessPath) +"-"+str(MAX_ACCESS_PATH)+"]")
            else:
                xLabel.append("["+str(j*bucketRangeAccessPath) +"-"+str((j+1)*bucketRangeAccessPath)+"]")
            zero.append([])
            zeroTime.append(0)
        timeoutData.append(zeroTime)
        data.append(zero)
    xBucketIndex = 0
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
    print(filename)
    printForTikz(avgData,textData,timeoutData )
    trace = plotly.graph_objs.Heatmap(text=textData, z=avgData, x = xLabel, y = yLabel)
    #plotly.offline.plot([trace], filename=filename)

plotHeatMapVisitedMethodsTimes(timesAP,methodAP,"plot-heatmap-visitedMethod-accesspath")
plotHeatMapVisitedMethodsTimes(timesPDS,methodAP,"plot-heatmap-visitedMethod-pds")


trace1 = plotBarChartTimes(timesAP)
trace2 = plotBarChartTimes(timesPDS)

plotly.offline.plot([trace1,trace2], filename="plot-barchart.html")

print len(timesAP)
print len(timesPDS)


