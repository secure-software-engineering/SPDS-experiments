

import glob, csv, math
import plotly.graph_objs
import numpy as np

MAX_ANALYSIS_TIME = 30000;
MAX_ACCESS_PATH = 0;
MAX_VISITED_METHODS = 0;
USE_ARITHMEAN = True

def beautifyAnalysisTime(time):
    if int(time) > MAX_ANALYSIS_TIME:
        return MAX_ANALYSIS_TIME
    return time
def computeAccessPathLength(accessPathString):
    return len(accessPathString.split(","))

path = "*.csv"
methodAP = []
timesAP = []
methodPDS = []
timesPDS = []
ap_length = []
for fname in glob.glob(path):
    if "ideal-ap" in fname:
        with open(fname) as csvfile:
            reader = csv.DictReader(csvfile,delimiter = ";")
            for row in reader:
                timesAP.append(beautifyAnalysisTime(row['AnalysisTimes']))
                vm = int(row['VisitedMethod'])
                methodAP.append(vm)
                if vm > MAX_VISITED_METHODS:
                    MAX_VISITED_METHODS = int(math.ceil(vm / 100.0)) * 100;
                ap = computeAccessPathLength(row['MaxAccessPath']) 
                ap_length.append(ap)
                if ap > MAX_ACCESS_PATH:
                    MAX_ACCESS_PATH = ap
                with open(fname.replace("ideal-ap","ideal")) as csvfilePDS:
                    readerPDS = csv.DictReader(csvfilePDS,delimiter = ";")
                    for rowPDS in readerPDS:
                        if rowPDS['SeedClass'] == row['SeedClass'] and rowPDS['SeedMethod'] == row['SeedMethod'] and row['SeedStatement'] == rowPDS['SeedStatement']:
                            timesPDS.append(beautifyAnalysisTime(rowPDS['AnalysisTimes']))
#                            methodPDS.append(rowPDS['VisitedMethod'])

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



## Compute Heatmaps
numberOfBucketsForTimes = 5

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
        print yBucketIndex
        print accessPathLength
        print data[yBucketIndex]
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



def mean(iterable):
    if len(iterable) == 0:
        return 0;
    if USE_ARITHMEAN:
        return np.mean(iterable)
    a = np.log(iterable)
    return np.exp(a.sum()/len(a))

def plotHeatMapVisitedMethodsTimes(analysisTimes, visitedMethods, filename ):
    bucketRangeAccessPath = int(math.floor(MAX_ACCESS_PATH/numberOfBucketsForTimes)) + 1
    bucketRangeMethods = MAX_VISITED_METHODS/numberOfBucketsForTimes
    data = []
    for i in range(0,numberOfBucketsForTimes+1):
        zero = []
        for j in range(0,numberOfBucketsForTimes+1):
            zero.append([])
        data.append(zero)
    xBucketIndex = 0
    for t in analysisTimes:
        time = int(t)
        visitedMethodBucket = int(math.floor(int(visitedMethods[xBucketIndex])/bucketRangeMethods))
        accessPathBucket = int(math.floor(int(ap_length[xBucketIndex])/bucketRangeAccessPath))
        print accessPathBucket
        if int(t) != 0:
            data[visitedMethodBucket][accessPathBucket].append(time)
        xBucketIndex += 1

    avgData = []
    textData = []
    for i in range(0,numberOfBucketsForTimes+1):
        avgDataRow = []
        textDataRow = []
        for j in range(0,numberOfBucketsForTimes+1):
            avgDataRow.append(mean(data[i][j]))
            textDataRow.append(len(data[i][j]))
        avgData.append(avgDataRow)
        textData.append(textDataRow)
    print avgData
    trace = plotly.graph_objs.Heatmap(text=avgData, z=textData)
    plotly.offline.plot([trace], filename=filename)

plotHeatMapVisitedMethodsTimes(timesAP,methodAP,"plot-heatmap-visitedMethod-accesspath")
plotHeatMapVisitedMethodsTimes(timesPDS,methodAP,"plot-heatmap-visitedMethod-pds")

