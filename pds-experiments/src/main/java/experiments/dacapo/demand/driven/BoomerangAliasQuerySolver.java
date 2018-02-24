package experiments.dacapo.demand.driven;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.beust.jcommander.internal.Sets;

import boomerang.BackwardQuery;
import boomerang.Boomerang;
import boomerang.BoomerangTimeoutException;
import boomerang.DefaultBoomerangOptions;
import boomerang.ForwardQuery;
import boomerang.seedfactory.SeedFactory;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;
import wpds.impl.Weight.NoWeight;

public class BoomerangAliasQuerySolver extends AliasQuerySolver {

	protected final BiDiInterproceduralCFG<Unit, SootMethod> icfg;
	protected final SeedFactory<NoWeight> seedFactory;
	private Boomerang solver;
	private Set<BackwardQuery> crashed = Sets.newHashSet();
	
	public BoomerangAliasQuerySolver(int timeoutMS, BiDiInterproceduralCFG<Unit, SootMethod> icfg, SeedFactory<NoWeight> seedFactory){
		super(timeoutMS);
		this.icfg = icfg;
		this.seedFactory = seedFactory;		
		
	}

	@Override
	protected boolean internalComputeQuery(AliasQuery q) {
		if(q.getLocalA().equals(q.getLocalB()))
			return true;
		Set<ForwardQuery> allocsA = getPointsTo(q.queryA);
		System.out.println(allocsA);
		if(allocsA.isEmpty())
			return false;
		Set<ForwardQuery> allocsB = getPointsTo(q.queryB);
		System.out.println(allocsB);
		for(ForwardQuery a : allocsA){
			if(allocsB.contains(a))
				return true;
		}
		return false;
	}

	
	private Set<ForwardQuery> getPointsTo(BackwardQuery q){
		try {
			if(crashed.contains(q)) {
				throw new SkipQueryException();
			}
			recreateSolver();
			solver.solve(q);
		} catch (BoomerangTimeoutException e) {
			crashed.add(q);
			throw e;
		}
		return solver.getAllocationSites(q);
	}

	private void recreateSolver() {
		DefaultBoomerangOptions options = new DefaultBoomerangOptions() {
			@Override
			public boolean arrayFlows() {
				return true;
			}
			@Override
			public boolean staticFlows() {
				return true;
			}
			
			@Override
			public int analysisTimeoutMS() {
				return BoomerangAliasQuerySolver.this.timeoutMS;
			}
		};
		solver = new Boomerang(options) {
			@Override
			public BiDiInterproceduralCFG<Unit, SootMethod> icfg() {
				return icfg;
			}

			@Override
			public SeedFactory<NoWeight> getSeedFactory() {
				return seedFactory;
			}
		};
	}
	
}
