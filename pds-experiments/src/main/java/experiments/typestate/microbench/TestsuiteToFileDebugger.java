package experiments.typestate.microbench;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Multimap;

import boomerang.accessgraph.AccessGraph;
import boomerang.ap.AliasResults;
import boomerang.cfg.ExtendedICFG;
import heros.solver.PathEdge;
import ideal.ap.AnalysisSolver;
import ideal.ap.IFactAtStatement;
import ideal.debug.IDEVizDebugger;
import ideal.debug.IDebugger;
import ideal.pointsofaliasing.PointOfAlias;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import typestate.ap.ConcreteState;
import typestate.ap.TypestateDomainValue;

public class TestsuiteToFileDebugger implements IDebugger<TypestateDomainValue<ConcreteState>> {

	private final ExtendedICFG icfg;
	private long phase1Propagations;
	private long phase2Propagations;
	private long propagationCount;
	private Set<SootMethod> visitedMethods = new HashSet<SootMethod>();
	private int errorStates;
	private IDEVizDebugger<TypestateDomainValue<ConcreteState>> visDebugger;

	public TestsuiteToFileDebugger(ExtendedICFG icfg, boolean vis) {
		this.icfg = icfg;
		if(!new File("IDEViz").exists()){
			new File("IDEViz").mkdirs();
		}
		if(vis)
			visDebugger= new IDEVizDebugger<TypestateDomainValue<ConcreteState>>(new File("IDEViz/"+Scene.v().getMainClass()+".json"), icfg);
	}
	
//
//	@Override
//	public void computedSeeds(Map<PathEdge<Unit, AccessGraph>, EdgeFunction<TypestateDomainValue<ConcreteState>>> seeds) {
//		log("====== COMPUTED SEEDS, FOUND: {}", seeds.size());
//		System.out.println("====== COMPUTED SEEDS, FOUND: " + seeds.size());
//		Multiset<Type> count = HashMultiset.create();
//		for (PathEdge<Unit, AccessGraph> e : seeds.keySet()) {
//			System.out.println(e.getTarget() +  " -> in " + icfg.getMethodOf(e.getTarget()));
//			count.addAll(e.factAtTarget().getType());
//		}
//
//		int i = 0;
//		for (Type type : Multisets.copyHighestCountFirst(count).elementSet()) {
//			if (i < 40) {
//				System.out.println(type + ": " + count.count(type) + " " + (type instanceof RefType));
//				if (type instanceof RefType) {
//					RefType refType = (RefType) type;
//				}
//				i++;
//			}
//		}
//		;
//	}
// com
	@Override
	public void beforeAnalysis() {
		// System.out.println(Scene.v().getMainMethod().getActiveBody());
		System.out.println("ReachableMethods: " + computeReachableMethods());
		propagationCount = 0;
	}

	@Override
	public void startWithSeed(IFactAtStatement seed) {
		String timeStamp = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
		System.out.println(String.format("Alive tick %s", timeStamp));
	}


	@Override
	public void startPhase1WithSeed(IFactAtStatement seed, AnalysisSolver<TypestateDomainValue<ConcreteState>> solver) {
	}

	@Override
	public void startPhase2WithSeed(IFactAtStatement seed, AnalysisSolver<TypestateDomainValue<ConcreteState>> solver) {
	}

	@Override
	public void finishPhase1WithSeed(IFactAtStatement seed, AnalysisSolver<TypestateDomainValue<ConcreteState>> solver) {
		this.phase1Propagations = solver.propagationCount;
		System.out.println("PropagationCount P1: " + phase1Propagations);
	}

	@Override
	public void finishPhase2WithSeed(IFactAtStatement seed, AnalysisSolver<TypestateDomainValue<ConcreteState>> solver) {
		this.phase2Propagations = solver.propagationCount;
		System.out.println("PropagationCount P2: " + phase2Propagations);
	}


	@Override
	public void onSeedFinished(IFactAtStatement seed, AnalysisSolver<TypestateDomainValue<ConcreteState>> solver) {
		propagationCount += phase1Propagations+phase2Propagations;
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
		propagationCount += phase1Propagations+phase2Propagations;
		if(error){
			errorStates++;
		}

	}

	@Override
	public void afterAnalysis() {
		String output = System.getProperty("outputCsvFile");
		if (output != null && !output.equals("")) {
			File file = new File(output);
			boolean existed = file.exists();
			try {
			FileWriter writer = new FileWriter(file, true);
			if (!existed)
				writer.write("Method;ESGNodes;VisitedMethods;ActualErrors;ExpectedErrors;\n");
			writer.write(String.format("%s;%s;%s;%s;%s;\n", System.getProperty("method"), propagationCount, visitedMethods.size(), errorStates, System.getProperty("expectedFinding")));
			writer.close();
			visitedMethods.clear();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else{
			assertEquals("Wrong number of Findings computed!", Integer.parseInt(System.getProperty("expectedFinding")),errorStates);
		}

		if(visDebugger != null)
			visDebugger.afterAnalysis();

	}
	private int computeReachableMethods() {
		return Scene.v().getReachableMethods().size();
	}

	@Override
	public void startAliasPhase(Set<PointOfAlias<TypestateDomainValue<ConcreteState>>> pointsOfAlias) {
	}

	@Override
	public void startForwardPhase(Set<PathEdge<Unit, AccessGraph>> worklist) {
	}

	@Override
	public void onAliasesComputed(AccessGraph boomerangAccessGraph, Unit curr, AccessGraph d1, AliasResults res) {
	}

	@Override
	public void onAliasTimeout(AccessGraph boomerangAccessGraph, Unit curr, AccessGraph d1) {
	}

	@Override
	public void beforeAlias(AccessGraph boomerangAccessGraph, Unit curr, AccessGraph d1) {
	}


	@Override
	public void detectedStrongUpdate(Unit callSite, AccessGraph fact) {
	}

	@Override
	public void onAnalysisTimeout(IFactAtStatement seed) {
		errorStates++;
	}

	@Override
	public void solvePOA(PointOfAlias<TypestateDomainValue<ConcreteState>> p) {
	}

	@Override
	public void onNormalPropagation(AccessGraph sourceFact, Unit curr, Unit succ,AccessGraph d2) {
		visitedMethods.add(icfg.getMethodOf(curr));
	}


	@Override
	public void indirectFlowAtWrite(AccessGraph source, Unit curr, AccessGraph target) {
	}

	@Override
	public void indirectFlowAtCall(AccessGraph source, Unit curr, AccessGraph target) {
		
	}@Override
	public void addSummary(SootMethod methodToSummary, PathEdge<Unit, AccessGraph> summary) {
		if(visDebugger != null)
			visDebugger.addSummary(methodToSummary, summary);
	}

	@Override
	public void normalFlow(Unit start, AccessGraph startFact, Unit target, AccessGraph targetFact) {
		assert targetFact.isStatic() || targetFact.getBase() != null;
		if(visDebugger != null)
			visDebugger.normalFlow(start, startFact, target, targetFact);
	}

	@Override
	public void callFlow(Unit start, AccessGraph startFact, Unit target, AccessGraph targetFact) {
		assert targetFact.isStatic() || targetFact.getBase() != null;
		if(visDebugger != null)
			visDebugger.callFlow(start, startFact, target, targetFact);
	}

	@Override
	public void callToReturn(Unit start, AccessGraph startFact, Unit target, AccessGraph targetFact) {
		assert targetFact.isStatic() || targetFact.getBase() != null;
		if(visDebugger != null)
			visDebugger.callToReturn(start, startFact, target, targetFact);
	}

	@Override
	public void returnFlow(Unit start, AccessGraph startFact, Unit target, AccessGraph targetFact) {
		assert targetFact.isStatic() || targetFact.getBase() != null;
		if(visDebugger != null)
			visDebugger.returnFlow(start, startFact, target, targetFact);
	}

	@Override
	public void setValue(Unit start, AccessGraph startFact, TypestateDomainValue<ConcreteState> value) {
		if(visDebugger != null)
			visDebugger.setValue(start, startFact, value);
	}

	@Override
	public void killAsOfStrongUpdate(AccessGraph d1, Unit callSite, AccessGraph callNode, Unit returnSite,
			AccessGraph returnSiteNode) {
		if(visDebugger != null)
			visDebugger.killAsOfStrongUpdate(d1, callSite, callNode, returnSite, returnSiteNode);
	}

	@Override
	public void finishWithSeed(PathEdge<Unit, AccessGraph> seed, boolean timeout, boolean isInErrorState,
			AnalysisSolver<TypestateDomainValue<ConcreteState>> solver) {
		// TODO Auto-generated method stub
		
	}

}
