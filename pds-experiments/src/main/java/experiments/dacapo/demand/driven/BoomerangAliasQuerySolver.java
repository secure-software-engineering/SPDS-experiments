package experiments.dacapo.demand.driven;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;

import com.beust.jcommander.internal.Sets;

import boomerang.Boomerang;
import boomerang.BoomerangTimeoutException;
import boomerang.DefaultBoomerangOptions;
import boomerang.debugger.Debugger;
import boomerang.debugger.IDEVizDebugger;
import boomerang.results.BackwardBoomerangResults;
import boomerang.BackwardQuery;
import boomerang.ForwardQuery;
import boomerang.seedfactory.SeedFactory;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;
import wpds.impl.Weight.NoWeight;

public class BoomerangAliasQuerySolver extends AliasQuerySolver {

	public static boolean VISUALIZATION = false;
	protected final BiDiInterproceduralCFG<Unit, SootMethod> icfg;
	protected final SeedFactory<NoWeight> seedFactory;
	private Boomerang solver;
	private Set<BackwardQuery> crashed = Sets.newHashSet();
	private int query = 0;
	
	public BoomerangAliasQuerySolver(int timeoutMS, BiDiInterproceduralCFG<Unit, SootMethod> icfg, SeedFactory<NoWeight> seedFactory){
		super(timeoutMS);
		this.icfg = icfg;
		this.seedFactory = seedFactory;		
	}

	@Override
	protected boolean internalComputeQuery(AliasQuery q) {
		if(q.getLocalA().equals(q.getLocalB()))
			return true;
		Set<ForwardQuery> allocsA = getPointsTo(q.queryA,q);
		if(allocsA.isEmpty())
			return false;
//		System.out.println(allocsA);
		Set<ForwardQuery> allocsB = getPointsTo(q.queryB,q);
//		System.out.println(allocsB);
		for(ForwardQuery a : allocsA){
			if(allocsB.contains(a))
				return true;
		}
		return false;
	}

	
	private Set<ForwardQuery> getPointsTo(BackwardQuery q, AliasQuery aliasQuery){
		if(crashed.contains(q)) {
			throw new SkipQueryException();
		}
		recreateSolver(q,aliasQuery);
		BackwardBoomerangResults<NoWeight> res = solver.solve(q);
		solver.debugOutput();
		if(res.isTimedout()) {
			crashed.add(q);
		}
		return res.getAllocationSites().keySet();
	}

	private void recreateSolver(BackwardQuery q, AliasQuery aliasQuery) {
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
			public Debugger createDebugger() {
				if(!VISUALIZATION) {
					return new Debugger<>();
				}
				File ideVizFile = new File(
						"target/IDEViz/"+aliasQuery.toString() + "/"+ q+".json");
				if (!ideVizFile.getParentFile().exists()) {
					try {
						Files.createDirectories(ideVizFile.getParentFile().toPath());
					} catch (IOException e) {
						throw new RuntimeException("Was not able to create directories for IDEViz output!" +  ideVizFile.getParentFile().toPath());
					}
				}
				return new IDEVizDebugger(ideVizFile,icfg);
			}
			@Override
			public SeedFactory<NoWeight> getSeedFactory() {
				return seedFactory;
			}
		};
		query++;
	}
	
}
