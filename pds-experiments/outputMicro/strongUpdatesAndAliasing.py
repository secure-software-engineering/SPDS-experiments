


import glob, csv, math
import numpy as np
idealTimeout = 0
idealAPTimeout = 0
idealSeeds = 0
idealAPSeeds = 0
ruleTimeout = 0
ruleSeeds = 0

USE_ARITHMEAN = False

def mean(iterable):
    if len(iterable) == 0:
        return 0;
    if USE_ARITHMEAN:
        return np.mean(iterable)
    a = np.log(iterable)
    return np.exp(a.sum()/len(a))

def getResultsForAnalysis(col, table):
    for fname in glob.glob(col+"/*.csv"):
        print(fname)
        row = ""
        if "IOTest" in fname:
            row = "IO"
        if "VectorTest" in fname:
            row = "Vector"
        if "SignatureTest" in fname:
            row = "Signature"
        if "KeyStoreTest" in fname:
            row = "KeyStore"
        if "URLConnectionTest" in fname:
            row = "URL"
        if "IteratorTest" in fname:
            row = "Iterator"

        visitedMethods = []
        falsePositives = 0
        falseNegatives = 0
        truePositives = 0
        programs = 0
        with open(fname) as f:
            reader = csv.DictReader(f,delimiter = ";")
            for r in reader:
                vm = int(r['VisitedMethods'])
                if vm is 0:
                    visitedMethods.append(1)
                else: 
                    visitedMethods.append(int(r['VisitedMethods']))
                falseNegatives += int(r['False Negatives'])
                falsePositives += int(r['False Positives'])
                truePositives += (int(r['Expected Errors']) - int(r['False Negatives']))
                programs += 1

        entry = {}
        entry["FP"] = falsePositives
        entry["FN"] = falseNegatives
        entry["TP"] = truePositives
        entry["VisitedMethods"] = mean(visitedMethods)
        entry["programs"] = programs
        if row in table:
            d = table[row]
            d[col] = entry
        else: 
            d = {}
            d[col] = entry
            table[row] = d



table = {}

analysis = ["ideal", "ideal-noStrongUpdates", "ideal-noAliasing"]
for x in analysis:
    getResultsForAnalysis(x,table)

def toTex(val, type):
    if val > 3:
        return str(val) + "$\\times$\\"+type +" "
    res = ""
    for i in range(0,val):
        res += "\\"+type
    return res;

header = ["analysis","truepos","falsepos","falseneg","precision","recall"]

file = open("table-microbenchmark-su-al.csv","w")
file.write(";".join(header) + ";\n")
for a in analysis:
    tp = 0
    fp = 0
    fn = 0
    for prop in table:
        print prop 
        print a
        print table[prop][a]
        tp +=  table[prop][a]["TP"]
        fp +=  table[prop][a]["FP"]
        fn +=  table[prop][a]["FN"]
    prec = tp / (float(fp) + tp)
    recall = tp / (float(fn) + tp)
    line = []
    if a == "ideal":
        line.append("\\tsideal")
    if a == "ideal-noStrongUpdates":
        line.append("\\tsnostrongupdate")
    if a == "ideal-noAliasing":
        line.append("\\tsnoaliasing")
    line.append(str(toTex(tp,"TP")))
    line.append(str(toTex(fp,"FP")))
    line.append(str(toTex(fn,"FN")))
    line.append(str(round(prec,2)))
    line.append(str(round(recall,2))) 
    file.write(";".join(line) + ";\n")

