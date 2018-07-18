#!/usr/bin/python

import sys, os,subprocess
from subprocess import call
from multiprocessing import Process, Pool

DACAPO_PATH = "/Users/johannesspath/Documents/dacapo/"
JAR = "../build/pds-experiments-0.0.1-SNAPSHOT-jar-with-dependencies.jar"

dacapo = ["antlr", 
	"chart",
	"eclipse",
	"hsqldb",
	"jython",
	"luindex",
	"lusearch",
	"pmd", 
	"fop",
	"xalan", 
	"bloat" 
];
analyses = [
#"ideal",
"ideal-ap",
#"fink-unique" ,
#"fink-apmust"
];
rules = ["IteratorHasNext",
		    #"KeyStore",
		    #"URLConnection",
		    "InputStreamCloseThenRead",
		    "PipedInputStream",
		    "OutputStreamCloseThenWrite",
		    "PipedOutputStream",
		    "PrintStream",
		    "PrintWriter",
		    #"Signature",
		    "EmptyVector"];

def cognicryptScan(arg):
	print("Analyzing " + arg[0] +" : " + arg[1] +":"+arg[2])
	f = open("log-" + arg[0] +" - " + arg[1] +"-"+arg[2]+".txt", "w")
	call(["java", "-Xmx4g","-Xss164m","-cp", os.path.abspath(JAR), "experiments.dacapo.FinkOrIDEALDacapoRunner",arg[0], arg[1], DACAPO_PATH, arg[2]],  stdout=f,stderr=subprocess.STDOUT, timeout=60*60*36)
 
args = []
for analysis in analyses:
	for rule in rules:
		for bench in dacapo:
			args.append([analysis, rule, bench])

if __name__ == '__main__':
	with Pool(2) as p:
		p.map(cognicryptScan, args)
		

