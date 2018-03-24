package experiments.pointerbench.pointsto;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;

import boomerang.Boomerang;
import boomerang.DefaultBoomerangOptions;
import boomerang.Query;
import boomerang.jimple.Statement;
import boomerang.jimple.Val;
import boomerang.results.BackwardBoomerangResults;
import boomerang.BackwardQuery;
import boomerang.ForwardQuery;
import boomerang.seedfactory.SeedFactory;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;
import sync.pds.solver.nodes.Node;
import wpds.impl.Weight.NoWeight;

public class PointerBenchAnalysisBoomerang extends PointerBenchAnalysis {

	public PointerBenchAnalysisBoomerang(String pointerBenchClassesPath, String mainClass) {
		super(pointerBenchClassesPath, mainClass);
	}

	@Override
	protected void runAndCompare() {
		Set<Node<Statement, Val>> backwardResults = runQuery();
		compare(allocationSites, backwardResults);
	}

	private void compare(Collection<Query> expectedResults, Set<Node<Statement, Val>> backwardResults) {
		System.out.println("Boomerang Results: " + backwardResults);
		System.out.println("Expected Results: " + expectedResults);
		Collection<Node<Statement, Val>> falseNegativeAllocationSites = new HashSet<>();
		for (Query res : expectedResults) {
			if (!backwardResults.contains(res.asNode()))
				falseNegativeAllocationSites.add(res.asNode());
		}
		Collection<? extends Node<Statement, Val>> falsePositiveAllocationSites = new HashSet<>(backwardResults);
		for (Query res : expectedResults) {
			falsePositiveAllocationSites.remove(res.asNode());
		}

		unsoundErrors = falseNegativeAllocationSites.size();
		imprecisionErrors = falsePositiveAllocationSites.size();
	}

	private Set<Node<Statement, Val>> runQuery() {
		final Set<Node<Statement, Val>> results = Sets.newHashSet();
		for (final Query query : queryForCallSites) {
			Boomerang solver = new Boomerang(new PointerBenchBoomerangOptions()) {
				@Override
				public BiDiInterproceduralCFG<Unit, SootMethod> icfg() {
					return icfg;
				}

				@Override
				public SeedFactory<NoWeight> getSeedFactory() {
					return seedFactory;
				}
			};
			BackwardBoomerangResults<NoWeight> res = solver.solve((BackwardQuery) query);
			for(ForwardQuery q : res.getAllocationSites().keySet()){
				results.add(q.asNode());
			}
		}
		return results;
	}
}
