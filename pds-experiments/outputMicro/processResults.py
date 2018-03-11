


import glob, csv, math
import numpy as np
path = "*.csv"
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


table = {}
for fname in glob.glob(path):
    print(fname)
    col = ""
    if "ideal" in fname:
        col = "ideal"
    if "Unique" in fname:
        col = "fink_unique"
    if "MustNot" in fname:
        col = "fink_mustnot"

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
                truePositives += (int(r['Excepted Errors']) - int(r['False Negatives']))
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

analysis = ["ideal", "fink_unique", "fink_mustnot"]
header = ["property", "idealvisitedmethods","idealtp","idealfp","idealfn","idealprograms","finkuniquevisitedmethods","finkuniquetp","finkuniquefp","finkuniquefn","finkuniqueprograms","finkmustnotvisitedmethods","finkmustnottp","finkmustnotfp","finkmustnotfn","finkmustnotprograms"]
print(";".join(header) +";")
for prop in table:
    line = prop + ";"
    for x in analysis:
        line +=  str(int(table[prop][x]["VisitedMethods"]))+ ";"
        line +=  str(table[prop][x]["TP"])+ ";"
        line +=  str(table[prop][x]["FP"])+ ";"
        line +=  str(table[prop][x]["FN"])+ ";"
        line +=  str(table[prop][x]["programs"])+ ";"
    print(line)
 