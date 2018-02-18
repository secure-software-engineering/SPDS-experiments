


import glob, csv
path = "*.csv"
for fname in glob.glob(path):
    if "ideal-ap" in fname:
        with open(fname) as csvfile:
            reader = csv.DictReader(csvfile,delimiter = ";")
            for row in reader:
                 with open(fname.replace("ideal-ap","ideal")) as csvfilePDS:
                    readerPDS = csv.DictReader(csvfilePDS,delimiter = ";")
                    for rowPDS in readerPDS:
                        if rowPDS['SeedClass'] == row['SeedClass'] and rowPDS['SeedMethod'] == row['SeedMethod'] and row['SeedStatement']:
                            print ""
                            print(rowPDS['AnalysisTimes'])
                            print(row['AnalysisTimes'])