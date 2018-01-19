package experiments;

import java.io.File;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import pointerbench.PointerBenchAnalysis;
import pointerbench.PointerBenchAnalysisBoomerang;
import pointerbench.PointerBenchResult;

public class PointerBench {

	static List<String> pointerBenchPackagePrefix = Lists.newArrayList("generalJava","cornerCases","collections","basic");
//	static List<String> pointerBenchPackagePrefix = Lists.newArrayList("basic");
	
	public static void main(String... args) {
		List<File> foundClassFiles = Lists.newArrayList();
		String pointerBenchClassesFolder = args[0];
		File file = new File(pointerBenchClassesFolder);
		searchFile(file, foundClassFiles);
		List<PointerBenchResult> boomerangResults = Lists.newArrayList();
		for(File f : foundClassFiles){
			String relative = file.toURI().relativize(f.toURI()).getPath();
			String className = relative.replaceAll(".class", "").replaceAll("/",".");
			if(className.contains("$"))
				continue;
			if(packagePrefixFilter(className)){
				PointerBenchAnalysis pointerBenchAnalysis = new PointerBenchAnalysisBoomerang(args[0], className);
				boomerangResults.add(pointerBenchAnalysis.run());
			}
		}
		System.out.println(Joiner.on("\n").join(boomerangResults));
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
