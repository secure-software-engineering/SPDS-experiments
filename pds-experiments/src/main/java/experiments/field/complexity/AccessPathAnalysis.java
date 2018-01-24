package experiments.field.complexity;

import java.util.Map;

import com.google.common.base.Stopwatch;

import boomerang.accessgraph.AccessGraph;
import boomerang.ap.AliasFinder;
import boomerang.ap.BoomerangOptions;
import boomerang.cfg.ExtendedICFG;
import boomerang.cfg.IExtendedICFG;
import boomerang.context.NoContextRequester;
import heros.solver.Pair;
import soot.Local;
import soot.SceneTransformer;
import soot.SootMethod;
import soot.Transformer;
import soot.Value;
import soot.jimple.Stmt;

public class AccessPathAnalysis extends AbstractAnalysis {

	public AccessPathAnalysis(String testClass, int timeoutInMS) {
		super(testClass,timeoutInMS);
	}

	@Override
	protected Transformer createAnalysisTransformer() {
		return new SceneTransformer() {

			protected void internalTransform(String phaseName, @SuppressWarnings("rawtypes") Map options) {
				AliasFinder boomerang = new AliasFinder(new BoomerangOptions() {
					@Override
					public long getTimeBudget() {
						return timeoutInMs;
					}
					@Override
					public IExtendedICFG icfg() {
						return new ExtendedICFG(getOrCreateICFG());
					}
				});
				Stopwatch watch = Stopwatch.createStarted();
				boomerang.startQuery();
				Pair<SootMethod, Stmt> findQueryForStatement = findQueryForStatement();
				Stmt stmt = findQueryForStatement.getO2();
				Value arg = stmt.getInvokeExpr().getArg(0);
				boomerang.findAliasAtStmt(new AccessGraph((Local) arg), stmt, new NoContextRequester());
				analysisTime = watch.elapsed();
				System.out.println("Test (" + testClass + ") took: " + watch.elapsed());
			}
		};
	}

}
