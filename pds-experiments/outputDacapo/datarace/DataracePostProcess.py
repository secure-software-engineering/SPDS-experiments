

import glob, csv, math, os.path, sys
import plotly.graph_objs
import numpy as np
from sets import Set

def getData(filename):
    with open(filename) as csvfile:
        rows = csv.DictReader(csvfile,delimiter = ";")
        return list(rows)
    return []
def toInt(val):
    if val == "true":
        return 1
    return 0

def res(val,total):
    return str(val) +" " + str(round(float(val*100)/total,1)) +"%"
path = "*.csv"

def geo_mean(iterable):
   # return np.mean(iterable)
    if len(iterable) == 0:
        return 0;
    a = np.log(iterable)
    return np.exp(a.sum()/len(a))

def prunedPairs(val,total):
    return total-val;
def improvement(val,total):
    return round(float((total-val)*100)/total,1)


results ={}

for fname in glob.glob(path):
    timeoutsBoomerang = 0
    timeoutsDacong = 0
    timeoutsSridharan = 0

    aliasesBoomerang = 0
    aliasesDacong = 0
    aliasesSridharan = 0
    data = getData(fname)
    rowCount = 0
    for row in data:
        rowCount += 1
        timeoutsBoomerang += toInt(row['Boomerang_timeout'])
        timeoutsDacong += toInt(row['Dacong_timeout'])
        timeoutsSridharan += toInt(row['Sridharan_timeout'])
        if toInt(row['Boomerang_timeout']) == 1:
            aliasesBoomerang += 1
        else:
            aliasesBoomerang += toInt(row['Boomerang_res'])
        if toInt(row['Dacong_timeout']) == 1:
            aliasesDacong += 1
        else:
            aliasesDacong += toInt(row['Dacong_res'])
        if toInt(row['Sridharan_timeout']) == 1:
            aliasesSridharan += 1
        else:
            aliasesSridharan += toInt(row['Sridharan_res'])
 
    entry = {"benchmark":   fname.replace("-datarace.csv",""), 
                "total_pairs": rowCount,
                "boomerang_pairs": prunedPairs(aliasesBoomerang,rowCount),
                "boomerang_improvement": improvement(aliasesBoomerang,rowCount),
                "boomerang_timeouts": timeoutsBoomerang,

                "dacong_pairs": prunedPairs(aliasesDacong,rowCount),
                "dacong_improvement": improvement(aliasesDacong,rowCount),
                "dacong_timeouts": timeoutsDacong,

                "sridharan_pairs": prunedPairs(aliasesSridharan,rowCount),
                "sridharan_improvement": improvement(aliasesSridharan,rowCount),
                "sridharan_timeouts": timeoutsSridharan}
    
    results[fname.replace("-datarace.csv","")] = entry;
    print fname
    print "Aliases (Total: " + str(rowCount) +")"
    print "B: " + res(aliasesBoomerang,rowCount)
    print "D: " + res(aliasesDacong,rowCount)
    print "S: " + res(aliasesSridharan,rowCount)
    print "Timeouts"
    print "B: " + res(timeoutsBoomerang,rowCount)
    print "D: " + res(timeoutsDacong,rowCount)
    print "S: " + res(timeoutsSridharan,rowCount)


avgTimeoutsSridharan = []
avgTimeoutsBoomerang = []
avgTimeoutsDacong = []
for bench in results:
    total = results[bench]["total_pairs"]
    avgTimeoutsDacong.append(float(results[bench]["dacong_timeouts"]*100)/total)
    avgTimeoutsSridharan.append(float(results[bench]["sridharan_timeouts"]*100)/total)
    avgTimeoutsBoomerang.append(float(results[bench]["boomerang_timeouts"]*100)/total)

print("Average Timeouts B:" + str(geo_mean(avgTimeoutsBoomerang)))
print("Average Timeouts S:" + str(geo_mean(avgTimeoutsSridharan)))
print("Average Timeouts D:" + str(geo_mean(avgTimeoutsDacong)))

header = ["benchmark","total_pairs","boomerang_pairs","boomerang_timeouts","boomerang_improvement","dacong_pairs","dacong_timeouts","dacong_improvement","sridharan_pairs","sridharan_timeouts","sridharan_improvement"]

headerString = "rowIndex;"
for colHeader in header:
    headerString += colHeader +";"
print headerString
index = 1
for bench in sorted(results):
    res = str(index) +";"
    for col in header:
        res += str(results[bench][col]) +";"
    print res
    index += 1



