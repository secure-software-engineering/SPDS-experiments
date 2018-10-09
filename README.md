# Artifact for Paper "Context-, Flow- and Field-Sensitive Data-Flow Analysis using Synchronized Pushdown Systems"

This repository contains all necessary software artifacts to reproduce the evaluation of the paper.

## General overview over the maven-projects

### Original/Baseline Implementations we compare to:
- *boomerang*: Original implementation of Boomerang based on access graphs/access paths (as presented at ECOOP'16). The  access path/graph data structure is found in class `boomerang.accessgraph.AccessGraph`. The class field `KLimiting` defines the different k-limits (in the default, k = -1, Boomerang uses AccessGraph and avoids k-limiting)
- *ideal*: Original implementation of IDEal based on access graphs/access paths (as presented at OOPSLA'17). It extends *boomerang* and uses the same data structure for the heap model.

### Implementations for SPDS
- *WPDS*: Implementation of a single Weighted Pushdown System (includes WeightedPAutomaton and the implementation of PostStar)
- *synchronizedPDS*: Implementation for Synchronized Pushdown Systems (SPDS). The main class `sync.pds.solver.SyncPDSSolver` instantiates the two (W)PDS for field and call-PDS and synchronizes their results. 
- *boomerangPDS*: Builds on top of *synchronizedPDS* and instantiates multiple SPDS (one for each boomerang.ForwardQuery, i.e., object allocation or boomerang.BackwardQuery, i.e., variable for which one wants know the allocation site). This implementation is similar to the access graph-based version of Boomerang (see project *boomerang*) but uses SPDS instead.  
- *idealPDS*: Implementation that implements the logic for IDEal based on *boomerangPDS*, i.e., SPDS, instead of access graphs.

### Code used for Evaluation
- *pds-experiments*: Contains the code to execute the experiments presented in RQ1-RQ3. 


## Running the Artifact

*Important Note*: During the last two month, we have made some major refactoring and improvements on the code basis. We managed to further improve on some numbers as they were presented in the submitted and reviewed version of the paper. This artifact contains the improved version which means the performance numbers are not *exactly* reproducible. We will update all numbers in the paper for the camera-ready version in accordance to the results contained in this packaged artifact. 


### Reproducing RQ1 (Figure 10)
In RQ1 we compare the models of SPDS to access path with k-limiting with a k-limit of k = 1,2,3,4,5 and to the access graph model based on Boomerang queries. As target program we use synthetic programs (in the paper referred to by EXPL_n) as sketched in Fig. 9.
These synthetic target programs EXPL_2 to EXPL_20 are found in package `experiments.field.complexity.benchmark`. It lists classes `FieldsNLongTest` that represent EXPL_N. All target programs contain a call site that calls `queryFor`. For all analyses (SPDS, k-limited access path analysis, access graph based analysis) a Boomerang query starting at the call site for the first parameter is triggered. The queries compute the allocation of the first parameter that reaches the call site.

To run this experiments for all classes `FieldsNLongTest` and all heap models (SPDS, k-liminting, access graph), simply execute the class `experiments.main.FieldExplosion`. The experiments starts with N = 2, triggers the query for all heap models and then increases N (until N = 20). Each query receives a budget of 100 seconds to terminate (see class member TIMEOUT_IN_MS). To remove potential high variance in analysis times, this process is repeated 5 times (as defined by class member NUMBER_OF_ITERATIONS).  

Once the analysis terminates, the results are printed to file `FieldExplosion-ApVsPds.csv` in the root folder of the project.

Note, if you want to speed-up the analysis, we recommend to reduce the NUMBER_OF_ITERATIONS to 1 and the query budget to 10 seconds, i.e., TIMEOUT_IN_MS = 10 * 100. The complete run should then only take about 5 minutes.

The resulting `FieldExplosion-ApVsPds.csv` file contains a table. The headers are:
`testCaseName;PushdownSystem;AccessPath_-1;AccessPath_1;AccessPath_2;AccessPath_3;AccessPath_4;AccessPath_5`

The first columns lists the respective target program (EXPL_N) that was analyzed. The row for each program lists the analysis time in seconds depending on the configuration listed in the header. The column PushdownSystem refers to the implementation based on SPDS (*boomerangPDS*). The remaining columns lists the analysis times based on the projects *boomerang* configured with the respective k-limits (i.e., column AccessPath_-1 is the data for access graphs.)


### Reproducing RQ2 + RQ3 (Table 5 + Figure 11)

In RQ2 and RQ3 we compare SPDS to the access graph heap model based on a typestate analysis implemented using IDEal. For this experiment we use real world programs and execute the typestate analysis on all programs of the DaCapo 2006 benchmark suite.

To run this experiment, execute the class `experiments.main.TypestateDacapoAnalysis` it runs the (SPDS-based and access graph based) typestate analysis for a set of typestate "rules" across all DaCapo benchmark programs. The program requires the path to the DaCapo benchmark folder as input. In this artifact, the path is `../dacapo/`. For each configuration (heapmodel+typestaterule+program), the analysis is executed in its own process (granted 12GB heap) to continue in the case of a crash of a configuration (e.g., when the analysis runs out of memory). 

The main class delegates invocation to `IDEALvsIDEALAPDacapoRunner` which in turn instantiates the classes ` IDEALRunner` (using SPDS) and `IDEALAPRunner` (using access graphs). The latter two classes setup the analyses for writing output to `csv` files and instantiate the typestate analysis (implemented in projects *idealPDS* and *ideal*).

The typestate "rules" (Vector, Iterator and IO) are defined as form of finite state machines in the project *idealPDS* in the package in package `typestate.impl.statemachines`. The pattern called Iterator in the Table 5 of the submission is defined in class `typestate.impl.statemachines.HasNextStateMachine`. The pattern Vector is the class `typestate.impl.statemachines.VectorStateMachine`. The pattern referred to by IO in Table 5 matches to several input/output related finite state machines encoding the pattern. For the exact matching see class `experiments.dacapo.IDEALvsIDEALAPDacapoRunner` and `experiments.dacapo.Util`.

The analysis generates several `.csv` files into `outputDacapo/typestate`. 

Note: The whole analysis takes about **24 hours to complete**. Therefore, we have packaged precomputed results into this artifact. The `.csv` files are found in `outputDacapo/typestate-precomputed`. If you do not want to wait for the completion of the whole experiment, simply copy the results of the `-precomputed` folder into the `typestate` folder without the suffix.

#### Generating Table 5

Once the analysis on DaCapo is completed, we can obtain Table 5 from the data in `outputDacapo/typestate`. 
Open a terminal, and execute the command from within the folder `outputDacapo` of project *pds-experiments*:
 `python evaluation_idealVsIdeal_ap.py`
 
 The python scripts uses the `csv` files in `outputDacapo/typestate` and generate three `csv` files that contain all data of Table 5.  
 - `io_ideal_vs_idealap.csv`
 - `vector_ideal_vs_idealap.csv`
 - `iterator_ideal_vs_idealap.csv`

Open the files in LibreOffice, the files represent the data for Table 5.

#### Generating Figure 11

From the data in `outputDacapo/typestate` we can also obtain Figure 11.
Open a terminal, and execute the command from within the folder `outputDacapo` of project *pds-experiments*:
 `python evaluation_apVsPds-bubbles.py`
 
 The python scripts uses the `csv` files in `outputDacapo/typestate` and generate two `csv` files that contain the data for Figure 11.  
 - `pds_bubbles.csv`: Data for the left side of Figure 11 (IDEal_P)
 - `ap_bubbleas.csv`: Data for the right side of Figure 11 (IDEal)
 
 Open the files in LibreOffice, the files represent the data for Figure 11.
 
