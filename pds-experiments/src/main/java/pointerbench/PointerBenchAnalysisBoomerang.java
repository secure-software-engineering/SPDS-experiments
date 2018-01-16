package pointerbench;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Sets;

import boomerang.Boomerang;
import boomerang.DefaultBoomerangOptions;
import boomerang.ForwardQuery;
import boomerang.Query;
import boomerang.jimple.Statement;
import boomerang.jimple.Val;
import boomerang.seedfactory.SeedFactory;
import boomerang.solver.AbstractBoomerangSolver;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;
import sync.pds.solver.EmptyStackWitnessListener;
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

	private void compare(Collection<Query> expectedResults, Set<Node<Statement, Val>> results) {
		System.out.println("Boomerang Results: " + results);
		System.out.println("Expected Results: " + expectedResults);
		Collection<Node<Statement, Val>> falseNegativeAllocationSites = new HashSet<>();
		for (Query res : expectedResults) {
			if (!results.contains(res.asNode()))
				falseNegativeAllocationSites.add(res.asNode());
		}
		Collection<? extends Node<Statement, Val>> falsePositiveAllocationSites = new HashSet<>(results);
		for (Query res : expectedResults) {
			falsePositiveAllocationSites.remove(res.asNode());
		}

		String answer = (falseNegativeAllocationSites.isEmpty() ? "" : "\nFN:" + falseNegativeAllocationSites)
				+ (falsePositiveAllocationSites.isEmpty() ? "" : "\nFP:" + falsePositiveAllocationSites + "\n");
		if (!falseNegativeAllocationSites.isEmpty()) {
			unsoundErrors.add(new Error(" Unsound results for:" + answer));
		}
		if (!falsePositiveAllocationSites.isEmpty())
			imprecisionErrors.add(new Error(" Imprecise results for:" + answer));
	}

	private Set<Node<Statement, Val>> runQuery() {
		final Set<Node<Statement, Val>> results = Sets.newHashSet();
		for (final Query query : queryForCallSites) {
			DefaultBoomerangOptions options = new DefaultBoomerangOptions() {
				@Override
				public boolean arrayFlows() {
					return true;
				}
				@Override
				public boolean staticFlows() {
					return true;
				}
			};
			Boomerang solver = new Boomerang(options) {
				@Override
				public BiDiInterproceduralCFG<Unit, SootMethod> icfg() {
					return icfg;
				}

				@Override
				public SeedFactory<NoWeight> getSeedFactory() {
					return seedFactory;
				}
			};
			solver.solve(query);
			for (final Entry<Query, AbstractBoomerangSolver<NoWeight>> fw : solver.getSolvers().entrySet()) {
				
				if (fw.getKey() instanceof ForwardQuery) {
					fw.getValue().synchedEmptyStackReachable(query.asNode(),
							new EmptyStackWitnessListener<Statement, Val>() {

								@Override
								public void witnessFound(Node<Statement, Val> allocation) {
									results.add(fw.getKey().asNode());
								}

							});
				}
			}

		}
		return results;
	}
}
