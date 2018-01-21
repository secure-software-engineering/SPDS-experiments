package experiments.field.complexity;

import java.io.File;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;

import com.beust.jcommander.internal.Lists;

import heros.solver.Pair;
import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Transformer;
import soot.Unit;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import soot.options.Options;

public abstract class AbstractAnalysis {

	
	protected final String testClass;
	private JimpleBasedInterproceduralCFG icfg;
	protected Duration analysisTime;

	public AbstractAnalysis(String testClass){
		this.testClass = testClass;
		initializeSootWithEntryPoint();
	}
	
	
	protected JimpleBasedInterproceduralCFG getOrCreateICFG(){
		if(icfg == null){
			icfg = new JimpleBasedInterproceduralCFG(true); 
		}
		return icfg;
	}
	

	public void run(){
		Transform transform = new Transform("wjtp.ifds", createAnalysisTransformer());
		PackManager.v().getPack("wjtp").add(transform);
		PackManager.v().getPack("cg").apply();
		PackManager.v().getPack("wjtp").apply();
	}
	
	protected boolean isQueryForStmt(Stmt s){
		if (!(s.containsInvokeExpr()))
			return false;
		InvokeExpr invokeExpr = s.getInvokeExpr();
		return invokeExpr.getMethod().getName().matches("queryFor");
	}
	
	protected Pair<SootMethod, Stmt> findQueryForStatement(){
		List<Pair<SootMethod,Stmt>> stmts = Lists.newArrayList();
		for(SootClass c: Scene.v().getApplicationClasses()){
			for(SootMethod m : c.getMethods()){
				if(!m.hasActiveBody())
					continue;
				for(Unit unit : m.getActiveBody().getUnits()){
					if(!(unit instanceof Stmt))
						continue;
					Stmt stmt = (Stmt) unit;
					if (isQueryForStmt(stmt))
						stmts.add(new Pair<SootMethod,Stmt>(m,stmt));
				}
			}
		}
		
		if(stmts.size() > 1){
			throw new RuntimeException("Selection of statement is not unique.");
		}
		if(stmts.size() == 1){
			return stmts.get(0);
		}
		throw new RuntimeException("No statement found that invoke the method queryFor.");
	}
	
	protected abstract Transformer createAnalysisTransformer();

	private void initializeSootWithEntryPoint() {
		G.v().reset();
		Options.v().set_whole_program(true);
		Options.v().setPhaseOption("cg.spark", "on");
		Options.v().setPhaseOption("cg.spark", "verbose:true");
		Options.v().set_output_format(Options.output_format_none);
		String userdir = System.getProperty("user.dir");
		String sootCp = userdir + "/target/classes";
		String javaHome = System.getProperty("java.home");
		if (javaHome == null || javaHome.equals(""))
			throw new RuntimeException("Could not get property java.home!");
		sootCp += File.pathSeparator + javaHome + "/lib/rt.jar";
		Options.v().setPhaseOption("cg", "trim-clinit:false");
		Options.v().set_no_bodies_for_excluded(true);
		Options.v().set_allow_phantom_refs(true);

		List<String> includeList = new LinkedList<String>();
		includeList.add("java.lang.*");
		includeList.add("java.util.*");
		includeList.add("java.io.*");
		includeList.add("sun.misc.*");
		includeList.add("java.net.*");
		includeList.add("javax.servlet.*");
		includeList.add("javax.crypto.*");

		Options.v().set_include(includeList);

		Options.v().setPhaseOption("jb", "use-original-names:true");

//		Options.v().set_exclude(excludedPackages());
		Options.v().set_soot_classpath(sootCp);
		// Options.v().set_main_class(this.getTargetClass());
		Scene.v().addBasicClass(testClass, SootClass.BODIES);
		Scene.v().loadNecessaryClasses();
		SootClass c = Scene.v().forceResolve(testClass, SootClass.BODIES);
		if (c != null) {
			c.setApplicationClass();
		}
	}
	
	public Duration getAnalysisTime(){
		return analysisTime;
	}
}
