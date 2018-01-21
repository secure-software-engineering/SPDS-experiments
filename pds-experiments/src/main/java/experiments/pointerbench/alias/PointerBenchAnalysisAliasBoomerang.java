package experiments.pointerbench.alias;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;

import boomerang.BackwardQuery;
import boomerang.Boomerang;
import boomerang.DefaultBoomerangOptions;
import boomerang.ForwardQuery;
import boomerang.Query;
import boomerang.jimple.Statement;
import boomerang.jimple.Val;
import boomerang.seedfactory.SeedFactory;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;
import sync.pds.solver.nodes.Node;
import wpds.impl.Weight.NoWeight;

public class PointerBenchAnalysisAliasBoomerang extends PointerBenchAliasAnalysis {

	public PointerBenchAnalysisAliasBoomerang(String pointerBenchClassesPath, String mainClass) {
		super(pointerBenchClassesPath, mainClass);
	}
	
	private Set<ForwardQuery> getPointsTo(BackwardQuery q){
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
		solver.solve(q);
		return solver.getAllocationSites(q);
	}
	
	@Override
	protected boolean computeQuery(AliasQuery q) {
		Set<ForwardQuery> allocsA = getPointsTo(q.queryA);
		Set<ForwardQuery> allocsB = getPointsTo(q.queryB);
		System.out.println(q.queryA);
		System.out.println(allocsA);
		System.out.println(q.queryB);
		System.out.println(allocsB);
		for(ForwardQuery a : allocsA){
			if(allocsB.contains(a))
				return true;
		}
		return false;
	}
}
