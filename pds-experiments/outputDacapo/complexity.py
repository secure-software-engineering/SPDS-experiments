

import glob, csv, math;
def factorial(n):
    if n == 1 or n == 0:
        return 1
    else:
        return n * factorial(n-1)

totalK = 9
totalKLimit = {}
print "n;pds;ap_setbased;ap_klimit_1;ap_klimit_2;ap_klimit_3;ap_klimit_4;ap_klimit_5;ap_klimit_6;ap_klimit_7;ap_klimit_8;"
for n in range(2,21):
    total = 0
    for k in range(1,totalK):
        totalKLimit[k] = 0
    for i in range(0,n):
        total += factorial(n)/factorial(i)

    for k in range(1,totalK):
        for i in range(0,k+1):
            totalKLimit[k] += 2*math.pow(n,i)
    total += math.pow(2,n)
    pdsTotal = math.pow(n,2) + n;
    s = ""
    for k in range(1,totalK):
        s += ";"+ str(totalKLimit[k])
    print str(n) + ";" + str(pdsTotal) + ";"+ str(total) + s