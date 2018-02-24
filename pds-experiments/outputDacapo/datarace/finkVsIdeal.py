

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
for fname in glob.glob(path):
    timeoutsBoomerang = 0
    timeoutsDacong = 0
    timeoutsSridharan = 0

    aliasesBoomerang = 0
    aliasesDacong = 0
    aliasesSridharan = 0
    data = getData(fname)
    for row in data:
        timeoutsBoomerang += toInt(row['Boomerang_timeout'])
        timeoutsDacong += toInt(row['Dacong_timeout'])
        timeoutsSridharan += toInt(row['Sridharan_timeout'])

        aliasesBoomerang += toInt(row['Boomerang_res'])
        aliasesDacong += toInt(row['Dacong_res'])
        aliasesSridharan += toInt(row['Sridharan_res'])
        #if toInt(row['Boomerang_timeout']) == 0 and toInt(row['Boomerang_res']) == 1 and toInt(row['Dacong_res']) == 0:
            #print row['QueryA']
            #print row['QueryB']
            #print ""
    
    print fname
    print "Aliases (Total: " + str(len(data)) +")"
    print "B: " + res(aliasesBoomerang,len(data))
    print "D: " + res(aliasesDacong,len(data))
    print "S: " + res(aliasesSridharan,len(data))
    print "Timeouts"
    print "B: " + res(timeoutsBoomerang,len(data))
    print "D: " + res(timeoutsDacong,len(data))
    print "S: " + res(timeoutsSridharan,len(data))

