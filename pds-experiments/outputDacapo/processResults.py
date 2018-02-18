


import glob
path = "*.csv"
idealTimeout = 0
idealAPTimeout = 0
idealSeeds = 0
idealAPSeeds = 0
ruleTimeout = 0
ruleSeeds = 0
for fname in glob.glob(path):
    print(fname)
    with open(fname) as f:
    	content = f.readlines()
    	for line in content:
    		array = line.split(";")
    		analysis = array[0]
    		timeout = array[6]
    		rule = array[1]
    		if analysis == "ideal":
    			idealSeeds += 1
    			if timeout == "true":
    				idealTimeout += 1

    		if analysis == "ideal-ap":
    			idealAPSeeds += 1
    			if timeout == "true":
    				idealAPTimeout += 1

    		if analysis == "ideal" and rule == "EmptyVector":
    			ruleSeeds += 1
    			if timeout == "true":
    				ruleTimeout += 1

print(idealAPTimeout)
print(idealAPSeeds)
print(idealTimeout)
print(idealSeeds)


print(ruleTimeout)
print(ruleSeeds)