package experiments.field.complexity;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.google.common.base.Stopwatch;

import boomerang.BackwardQuery;
import boomerang.Boomerang;
import boomerang.DefaultBoomerangOptions;
import boomerang.Query;
import boomerang.jimple.Statement;
import boomerang.jimple.Val;
import boomerang.seedfactory.SeedFactory;
import soot.SceneTransformer;
import soot.SootMethod;
import soot.Transformer;
import soot.Unit;
import soot.Value;
import soot.jimple.Stmt;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;
import wpds.impl.Weight.NoWeight;

public class PDSAnalysis extends AbstractAnalysis {

	public PDSAnalysis(String testClass) {
		super(testClass);
	}

	@Override
	protected Transformer createAnalysisTransformer() {
		return new SceneTransformer() {

			protected void internalTransform(String phaseName, @SuppressWarnings("rawtypes") Map options) {
				final SeedFactory<NoWeight> seedFactory = new SeedFactory<NoWeight>() {
					@Override
					public BiDiInterproceduralCFG<Unit, SootMethod> icfg() {
						return getOrCreateICFG();
					}

					@Override
					protected Collection<? extends Query> generate(SootMethod method, Stmt u,
							Collection calledMethods) {
						if (isQueryForStmt(u)) {
							Value val = u.getInvokeExpr().getArg(0);
							return Collections
									.singleton(new BackwardQuery(new Statement(u, method), new Val(val, method)));
						}
						return Collections.emptySet();
					}
				};
				Boomerang solver = new Boomerang(new DefaultBoomerangOptions()) {
					@Override
					public BiDiInterproceduralCFG<Unit, SootMethod> icfg() {
						return getOrCreateICFG();
					}

					@Override
					public SeedFactory<NoWeight> getSeedFactory() {
						return seedFactory;
					}

				};
				Collection<Query> seeds = seedFactory.computeSeeds();
				Stopwatch watch = Stopwatch.createStarted();
				for (Query q : seeds) {
					solver.solve(q);
				}
				analysisTime = watch.elapsed();
				System.out.println("Test (" + testClass + ") took: " + watch.elapsed());
			}
		};
	}

}
