

import glob, csv, math
import plotly.graph_objs
import numpy as np
import plotly.graph_objs as go

## Compute Heatmaps
BUCKET_SIZE = 100

analysisTimes = []
with open("analysis-results.csv") as csvfile:
    reader = csv.DictReader(csvfile,delimiter = ";")
    for row in reader:
        analysisTimes.append(row)



sortedByAnalysisTime = sorted(analysisTimes, key=lambda t: int(t["CryptoAnalysisTime_ms"]))

sortedTimes = []
analyzedObject = []
callGraphReachableMethods = []
cgTimes = []
for t in sortedByAnalysisTime:
    sortedTimes.append(int(t["CryptoAnalysisTime_ms"])/float(1000*60))
    analyzedObject.append(int(t["SeedObjectCount"]))
    callGraphReachableMethods.append(int(t["CallGraphReachableMethods_ActiveBodies"]))
    cgTimes.append(int(t["CallGraphTime_ms"]))




trace1 = go.Scatter(x = np.arange(7300), y=sortedTimes, mode = 'markers')
#plotly.offline.plot([trace1], filename="plot-times.html")

def bucketize(inSet):
    out = [0]
    i = 0;
    bucket = 0;
    for t in inSet:
        i = i+1
        if i > BUCKET_SIZE:
            i = 0
            bucket = bucket+1
            out.append(0)
        out[bucket] = out[bucket] + t
    return out

trace2 = go.Scatter(x = np.arange(73), y=bucketize(analyzedObject), mode = 'markers')
trace3 = go.Scatter(x = np.arange(73), y=bucketize(callGraphReachableMethods), mode = 'markers')
trace4 = go.Scatter(x = np.arange(73), y=bucketize(cgTimes), mode = 'markers')

plotly.offline.plot([trace2, trace3, trace4], filename="plot-correlations.html")


