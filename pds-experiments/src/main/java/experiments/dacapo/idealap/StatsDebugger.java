package experiments.dacapo.idealap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Multimap;

import boomerang.accessgraph.AccessGraph;
import boomerang.ap.AliasResults;
import boomerang.cfg.ExtendedICFG;
import heros.solver.PathEdge;
import ideal.ap.AnalysisSolver;
import ideal.ap.IFactAtStatement;
import ideal.debug.IDebugger;
import ideal.pointsofaliasing.PointOfAlias;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import typestate.ap.ConcreteState;
import typestate.ap.TypestateDomainValue;

public class StatsDebugger implements IDebugger<TypestateDomainValue<ConcreteState>> {

	private final ExtendedICFG icfg;
	private final Stopwatch phase1Watch = Stopwatch.createUnstarted();
	private final Stopwatch phase2Watch = Stopwatch.createUnstarted();
	private final Stopwatch boomerangWatch = Stopwatch.createUnstarted();
	private Set<SootMethod> visitedMethods = new HashSet<>();
	private boolean timedout = false;

	public StatsDebugger(ExtendedICFG icfg) {
		this.icfg = icfg;
	}

//	@Override
//	public void computedSeeds(Map<PathEdge<Unit, AccessGraph>, EdgeFunction<TypestateDomainValue<ConcreteState>>> seeds) {
//		for(PathEdge<Unit,AccessGraph> pe : seeds.keySet()){
//			System.out.println("Seed " + pe.getTarget() +"€" + icfg.getMethodOf(pe.getTarget()));
//		}
//	}

	@Override
	public void beforeAnalysis() {
	}

	@Override
	public void startWithSeed(IFactAtStatement seed) {
		String timeStamp = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
		log("Alive tick {}", timeStamp);
		System.out.println(String.format("Starting with seed in %s",  icfg.getMethodOf(seed.getStmt()) +" "+ seed + "€"));
		System.out.println(String.format("Alive tick %s", timeStamp));
		timedout = false;
		visitedMethods.clear();
	}

	private void log(String format, Object... args) {
	}

	private void log(String format) {
	}

	@Override
	public void startPhase1WithSeed(IFactAtStatement seed, AnalysisSolver<TypestateDomainValue<ConcreteState>> solver) {
		phase1Watch.reset();
		phase2Watch.reset();
		boomerangWatch.reset();
		
		phase1Watch.start();
	}

	@Override
	public void startPhase2WithSeed(IFactAtStatement seed, AnalysisSolver<TypestateDomainValue<ConcreteState>> solver) {
		log("===== START IDE PHASE ===== ");
		
		phase2Watch.start();
	}

	@Override
	public void finishPhase1WithSeed(IFactAtStatement seed, AnalysisSolver<TypestateDomainValue<ConcreteState>> solver) {
		phase1Watch.stop();
	}

	@Override
	public void finishPhase2WithSeed(IFactAtStatement seed, AnalysisSolver<TypestateDomainValue<ConcreteState>> solver) {
		phase2Watch.stop();
	}

	@Override
	public void finishWithSeed(PathEdge<Unit, AccessGraph> seed, boolean timeout, boolean isInErrorState, AnalysisSolver<TypestateDomainValue<ConcreteState>> solver) {
	}

	@Override
	public void afterAnalysis() {
	}

	@Override
	public void startAliasPhase(Set<PointOfAlias<TypestateDomainValue<ConcreteState>>> pointsOfAlias) {
	}

	@Override
	public void startForwardPhase(Set<PathEdge<Unit, AccessGraph>> worklist) {
	}

	@Override
	public void onAliasesComputed(AccessGraph boomerangAccessGraph, Unit curr, AccessGraph d1, AliasResults res) {
		if(boomerangWatch.isRunning())
			boomerangWatch.stop();
	}

	@Override
	public void onAliasTimeout(AccessGraph boomerangAccessGraph, Unit curr, AccessGraph d1) {
		if(boomerangWatch.isRunning())
			boomerangWatch.stop();
	}

	@Override
	public void beforeAlias(AccessGraph boomerangAccessGraph, Unit curr, AccessGraph d1) {
		boomerangWatch.start();
	}

	@Override
	public void detectedStrongUpdate(Unit callSite, AccessGraph fact) {
	}

	@Override
	public void onAnalysisTimeout(IFactAtStatement seed) {
		timedout = true;
	}

	@Override
	public void solvePOA(PointOfAlias<TypestateDomainValue<ConcreteState>> p) {
	}


	@Override
	public void onNormalPropagation(AccessGraph sourceFact, Unit curr, Unit succ,AccessGraph d2) {
		SootMethod m = icfg.getMethodOf(curr);
		visitedMethods.add(m);
	}

	@Override
	public void addSummary(SootMethod methodToSummary, PathEdge<Unit, AccessGraph> summary) {
	}
	
	@Override
	public void normalFlow(Unit start, AccessGraph startFact, Unit target, AccessGraph targetFact) {
		assert targetFact.isStatic() || targetFact.getBase() != null;
	}

	@Override
	public void callFlow(Unit start, AccessGraph startFact, Unit target, AccessGraph targetFact) {
		assert targetFact.isStatic() || targetFact.getBase() != null;
	}

	@Override
	public void callToReturn(Unit start, AccessGraph startFact, Unit target, AccessGraph targetFact) {
		assert targetFact.isStatic() || targetFact.getBase() != null;
	}

	@Override
	public void returnFlow(Unit start, AccessGraph startFact, Unit target, AccessGraph targetFact) {
		assert targetFact.isStatic() || targetFact.getBase() != null;
	}

	@Override
	public void setValue(Unit start, AccessGraph startFact, TypestateDomainValue<ConcreteState> value) {
	}

	@Override
	public void killAsOfStrongUpdate(AccessGraph d1, Unit callSite, AccessGraph callNode, Unit returnSite,
			AccessGraph returnSiteNode) {
	}

	@Override
	public void indirectFlowAtWrite(AccessGraph source, Unit curr, AccessGraph target) {
		
	}

	@Override
	public void indirectFlowAtCall(AccessGraph source, Unit curr, AccessGraph target) {
		
	}

	@Override
	public void onSeedFinished(IFactAtStatement seed, AnalysisSolver<TypestateDomainValue<ConcreteState>> solver) {
		Multimap<Unit, AccessGraph> endPathOfPropagation = solver.getEndPathOfPropagation();
		boolean error = false;
		for(Entry<Unit, AccessGraph> e : endPathOfPropagation.entries()){
			 TypestateDomainValue<ConcreteState> resultAt = solver.resultAt(e.getKey(), e.getValue());
			 if(resultAt != null)
				 for(ConcreteState s : resultAt.getStates()){
					 if(s.isErrorState()){
						 error = true;
					 }	
				 }
		}
		long phase1Time = phase1Watch.elapsed(TimeUnit.MILLISECONDS);
		long phase2Time = phase2Watch.elapsed(TimeUnit.MILLISECONDS);
//		long aliasTime = boomerangWatch.elapsed(TimeUnit.MILLISECONDS);
		long totalTime = (phase1Time + phase2Time);
		long propagationCount = solver.propagationCount;

		File file = new File(System.getProperty("outputCsvFile"));
		boolean fileExisted = file.exists();
		FileWriter writer;
		try {
			writer = new FileWriter(file, true);
			if(!fileExisted)
				 writer.write(
                         "Analysis;Rule;Seed;SeedStatement;SeedMethod;SeedClass;Is_In_Error;Timedout;AnalysisTimes;PropagationCount;Phase1Time;Phase2Time;VisitedMethod;ReachableMethods;\n");
				SootMethod method = icfg.getMethodOf(seed.getStmt());
			String line = String.format("%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;\n","ideal-ap",	System.getProperty("ruleIdentifier"),seed, seed.getStmt(),method,method.getDeclaringClass(), error,timedout,totalTime,propagationCount,phase1Time,phase2Time,visitedMethods.size(), Scene.v().getReachableMethods().size());
			writer.write(line);
			writer.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
					
	}
}
