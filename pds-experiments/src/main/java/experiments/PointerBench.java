package experiments;

import java.io.File;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import pointerbench.alias.PointerBenchAliasAnalysis;
import pointerbench.alias.PointerBenchAnalysisAliasBoomerang;
import pointerbench.alias.PointerBenchAnalysisAliasSridharan;
import pointerbench.pointsto.PointerBenchAnalysis;
import pointerbench.pointsto.PointerBenchAnalysisBoomerang;
import pointerbench.pointsto.PointerBenchAnalysisSridharan;
import pointerbench.pointsto.PointerBenchResult;

public class PointerBench {

//	static List<String> pointerBenchPackagePrefix = Lists.newArrayList("generalJava","cornerCases","collections","basic");
	static List<String> pointerBenchPackagePrefix = Lists.newArrayList("basic");
	
	public static void main(String... args) {
		List<File> foundClassFiles = Lists.newArrayList();
		String pointerBenchClassesFolder = args[0];
		File file = new File(pointerBenchClassesFolder);
		searchFile(file, foundClassFiles);
		List<PointerBenchResult> boomerangResultsPtsTo = Lists.newArrayList();
		List<PointerBenchResult> sridharanResultsPtsTo = Lists.newArrayList();
		List<PointerBenchResult> boomerangResultsAlias = Lists.newArrayList();
		List<PointerBenchResult> sridharanResultsAlias = Lists.newArrayList();
		for(File f : foundClassFiles){
			String relative = file.toURI().relativize(f.toURI()).getPath();
			String className = relative.replaceAll(".class", "").replaceAll("/",".");
			if(className.contains("$"))
				continue;
			if(!className.contains("basic.ReturnValue2"))
				continue;
			if(packagePrefixFilter(className)){
				PointerBenchAnalysis pointerBenchAnalysis = new PointerBenchAnalysisBoomerang(args[0], className);
				boomerangResultsPtsTo.add(pointerBenchAnalysis.run());
//				
//				PointerBenchAnalysis sridharanAnalysis = new PointerBenchAnalysisSridharan(args[0], className);
//				sridharanResultsPtsTo.add(sridharanAnalysis.run());
//				

				PointerBenchAliasAnalysis boomerangAliasAnalysis= new PointerBenchAnalysisAliasBoomerang(args[0], className);
				boomerangResultsAlias.add(boomerangAliasAnalysis.run());
//				
//				PointerBenchAliasAnalysis sridharanAliasAnalysis = new PointerBenchAnalysisAliasSridharan(args[0], className);
//				sridharanResultsAlias.add(sridharanAliasAnalysis.run());
			}
		}
		System.out.println("Boomerang Points-To Results");
		System.out.println(Joiner.on("\n").join(boomerangResultsPtsTo));
		System.out.println("Sridharan Points-To Results");
		System.out.println(Joiner.on("\n").join(sridharanResultsPtsTo));
		System.out.println("Boomerang Alias Results");
		System.out.println(Joiner.on("\n").join(boomerangResultsAlias));
		System.out.println("Sridharan Alias Results");
		System.out.println(Joiner.on("\n").join(sridharanResultsAlias));
	}

	private static boolean packagePrefixFilter(String className) {
		for(String prefix : pointerBenchPackagePrefix){
			if(className.contains(prefix))
				return true;
		}
		return false;
	}

	private static void searchFile(File file, List<File> foundFiles) {
		if (file.isDirectory()) {
			File[] arr = file.listFiles();
			for (File f : arr) {
				searchFile(f, foundFiles);
			}
		} else {
			if (file.getName().contains(".class")) {
				foundFiles.add(file);
			}
		}
	}
}
