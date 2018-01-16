package experiments;

import java.io.File;
import java.util.List;

import com.google.common.collect.Lists;

import pointerbench.PointerBenchAnalysis;
import pointerbench.PointerBenchAnalysisBoomerang;

public class PointerBench {

	static List<String> pointerBenchPackagePrefix = Lists.newArrayList("generalJava","cornerCases","collections","basic");
//	static List<String> pointerBenchPackagePrefix = Lists.newArrayList("basic");
	
	public static void main(String... args) {
		List<File> foundClassFiles = Lists.newArrayList();
		String pointerBenchClassesFolder = args[0];
		searchFile(new File(pointerBenchClassesFolder), foundClassFiles);
		System.out.println(foundClassFiles);
		for(File f : foundClassFiles){
			String className = f.getAbsolutePath().replaceAll(pointerBenchClassesFolder, "").replaceAll(".class", "").replaceAll("/",".");
			if(packagePrefixFilter(className)){
				if(!className.contains("ASD"))
					continue;
				PointerBenchAnalysis pointerBenchAnalysis = new PointerBenchAnalysisBoomerang(args[0], className);
				pointerBenchAnalysis.run();
			}
		}
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
