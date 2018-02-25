package experiments.dacapo.demand.driven;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import boomerang.BackwardQuery;
import boomerang.Query;
import boomerang.jimple.Statement;
import boomerang.jimple.Val;
import boomerang.seedfactory.SeedFactory;
import experiments.dacapo.SootSceneSetupDacapo;
import heros.solver.Pair;
import soot.Local;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootField;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.Stmt;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import wpds.impl.Weight.NoWeight;

public class DataraceClientExperiment extends SootSceneSetupDacapo {
	

	 public static void main(String[] args) {
		DataraceClientExperiment expr = new DataraceClientExperiment(args[0], args[1]);
		expr.run();
	 }


	protected Set<Pair<Local, Stmt>> queries = Sets.newHashSet();
	private JimpleBasedInterproceduralCFG icfg;
	private int TIMEOUT_IN_MS = 1000;
	protected Multimap<SootField, BackwardQuery> fieldToQuery = HashMultimap.create();

	public DataraceClientExperiment(String benchmarkFolder, String benchFolder) {
		super(benchmarkFolder, benchFolder);
	}

	public void run() {
		setupSoot();
		Transform transform = new Transform("wjtp.ifds", new SceneTransformer() {

			protected void internalTransform(String phaseName, @SuppressWarnings("rawtypes") Map options) {

				icfg = new JimpleBasedInterproceduralCFG(false);
				System.out.println("Application Classes: " + Scene.v().getApplicationClasses().size());
				final SeedFactory<NoWeight> seedFactory = new SeedFactory<NoWeight>() {
					@Override
					public BiDiInterproceduralCFG<Unit, SootMethod> icfg() {
						return icfg;
					}

					@Override
					protected Collection<? extends Query> generate(SootMethod method, Stmt u,
							Collection calledMethods) {
						if(!method.hasActiveBody())
							return Collections.emptySet();
						if (u.containsFieldRef() && !method.isJavaLibraryMethod()) {
							FieldRef fieldRef = u.getFieldRef();
							if (u instanceof AssignStmt && fieldRef instanceof InstanceFieldRef) {
								InstanceFieldRef ifr = (InstanceFieldRef) fieldRef;
								BackwardQuery q = new BackwardQuery(new Statement(u, method),
										new Val(ifr.getBase(), method));
								fieldToQuery.put(ifr.getField(), q);
								return Collections.singleton(q);
							}
						}
						return Collections.emptySet();
					}

				};
				Collection<Query> seeds = seedFactory.computeSeeds();
				System.out.println("Points-To Queries: " + seeds.size());
				Set<AliasQuery> dataraceQueries = Sets.newHashSet();
				Set<BackwardQuery> excludeDoubles = Sets.newHashSet();
				int skipped = 0;
				for (SootField field : fieldToQuery.keySet()) {
					Collection<BackwardQuery> queries = fieldToQuery.get(field);
					for (BackwardQuery q1 : queries) {
						excludeDoubles.add(q1);
						for (BackwardQuery q2 : queries) {
							if (excludeDoubles.contains(q2))
								continue;
							if (isDataracePair(q1.stmt(), q2.stmt())) {
								AliasQuery q = new AliasQuery(q1, q2);
								if (!sparkReportsDataRace(new AliasQuery(q1, q2))) {
									skipped++;
									continue;
								}
								dataraceQueries.add(q);
							}
						}
					}
				}
				BoomerangAliasQuerySolver bSolver = new BoomerangAliasQuerySolver(TIMEOUT_IN_MS,icfg, seedFactory);
				DacongAliasQuerySolver dSolver = new DacongAliasQuerySolver(TIMEOUT_IN_MS);
				SridharanAliasQuerySolver sSolver = new SridharanAliasQuerySolver(TIMEOUT_IN_MS);
				System.out.println("Solving queries "+  dataraceQueries.size() + " skipped, cause spark reports false:" + skipped);
				
				int solved = 0;
				for (AliasQuery q : dataraceQueries) {
					System.out.println(String.format("Status, #Solved queries: %s ", solved));
					AliasQueryExperimentResult bRes = bSolver.computeQuery(q);
					AliasQueryExperimentResult dRes = dSolver.computeQuery(q);
					AliasQueryExperimentResult sRes = sSolver.computeQuery(q);
					solved++;
					File f = new File("outputDataraceDacapo" + File.separator+ DataraceClientExperiment.this.getBenchName() + "-datarace.csv");
					if (!f.getParentFile().exists()) {
						try {
							Files.createDirectories(f.getParentFile().toPath());
						} catch (IOException e) {
							throw new RuntimeException("Was not able to create directories for IDEViz output!");
						}
					}
					FileWriter writer;
					try {
						if (!f.exists()) {
							writer = new FileWriter(f);
							LinkedList<String> header = Lists.newLinkedList();
							header.add("QueryA");
							header.add("QueryB");
							header.add("Boomerang_res");
							header.add("Boomerang_time(ms)");
							header.add("Boomerang_timeout");
							header.add("Sridharan_res");
							header.add("Sridharan_time(ms)");
							header.add("Sridharan_timeout");
							header.add("Dacong_res");
							header.add("Dacong_time(ms)");
							header.add("Dacong_timeout");
							writer.write(Joiner.on(";").join(header));
							writer.write("\n");
						} else {
							writer = new FileWriter(f, true);
						}
						LinkedList<Object> row = Lists.newLinkedList();

						row.add(q.queryA);
						row.add(q.queryB);
						row.add(bRes.queryResult);
						row.add(bRes.analysisTimeMs);
						row.add(bRes.timeout);
						row.add(sRes.queryResult);
						row.add(sRes.analysisTimeMs);
						row.add(sRes.timeout);
						row.add(dRes.queryResult);
						row.add(dRes.analysisTimeMs);
						row.add(dRes.timeout);
						writer.write(Joiner.on(";").join(row));
						writer.write("\n");
						writer.flush();
						writer.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			}

		});

		PackManager.v().getPack("wjtp").add(transform);
		PackManager.v().getPack("cg").apply();
		PackManager.v().getPack("wjtp").apply();
	}

	protected boolean sparkReportsDataRace(AliasQuery q) {
		Local a = q.getLocalA();
		Local b = q.getLocalB();
		if(a.equals(b)) {
			return false;
		}
		return Scene.v().getPointsToAnalysis().reachingObjects(a)
				.hasNonEmptyIntersection(Scene.v().getPointsToAnalysis().reachingObjects(b));
	}

	protected boolean isDataracePair(Statement s1, Statement s2) {
		boolean a = isFieldLoad(s1.getUnit().get());
		boolean b = isFieldLoad(s2.getUnit().get());;
		return !(a && b);
	}

	private boolean isFieldLoad(Stmt s) {
		if (s instanceof AssignStmt) {
			AssignStmt as = (AssignStmt) s;
			return as.getRightOp() instanceof InstanceFieldRef;
		}
		return false;
	}

}
