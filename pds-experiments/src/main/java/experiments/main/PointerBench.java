package experiments.main;

import java.io.File;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;

import experiments.pointerbench.alias.PointerBenchAliasAnalysis;
import experiments.pointerbench.alias.PointerBenchAnalysisAliasBoomerang;
import experiments.pointerbench.alias.PointerBenchAnalysisAliasDacong;
import experiments.pointerbench.alias.PointerBenchAnalysisAliasSridharan;
import experiments.pointerbench.pointsto.PointerBenchAnalysis;
import experiments.pointerbench.pointsto.PointerBenchAnalysisBoomerang;
import experiments.pointerbench.pointsto.PointerBenchAnalysisSridharan;
import experiments.pointerbench.pointsto.PointerBenchResult;

public class PointerBench {

	static List<String> pointerBenchPackagePrefix = Lists.newArrayList("basic","generalJava","cornerCases","collections");
//	static List<String> pointerBenchPackagePrefix = Lists.newArrayList("generalJava");
	
	public static void main(String... args) {
		List<File> foundClassFiles = Lists.newArrayList();
		String pointerBenchClassesFolder = args[0];
		File file = new File(pointerBenchClassesFolder);
		searchFile(file, foundClassFiles);
		List<PointerBenchResult> boomerangResultsPtsTo = Lists.newArrayList();
		List<PointerBenchResult> sridharanResultsPtsTo = Lists.newArrayList();
		List<PointerBenchResult> boomerangResultsAlias = Lists.newArrayList();
		List<PointerBenchResult> sridharanResultsAlias = Lists.newArrayList();
		List<PointerBenchResult> dacongResultsAlias = Lists.newArrayList();
		for(File f : foundClassFiles){
			String relative = file.toURI().relativize(f.toURI()).getPath();
			String className = relative.replaceAll(".class", "").replaceAll("/",".");
			if(className.contains("$"))
				continue;
//			if(!className.contains("Set"))
//				continue;
//			if(className.contains("Set"))
//				continue;
			
			if(packagePrefixFilter(className)){
				PointerBenchAnalysis pointerBenchAnalysis = new PointerBenchAnalysisBoomerang(args[0], className);
				boomerangResultsPtsTo.add(pointerBenchAnalysis.run());

				PointerBenchAnalysis sridharanAnalysis = new PointerBenchAnalysisSridharan(args[0], className);
				sridharanResultsPtsTo.add(sridharanAnalysis.run());
				
				PointerBenchAliasAnalysis boomerangAliasAnalysis= new PointerBenchAnalysisAliasBoomerang(args[0], className);
				boomerangResultsAlias.add(boomerangAliasAnalysis.run());
				
				PointerBenchAliasAnalysis sridharanAliasAnalysis = new PointerBenchAnalysisAliasSridharan(args[0], className);
				sridharanResultsAlias.add(sridharanAliasAnalysis.run());
				
				PointerBenchAliasAnalysis dacongAliasAnalysis = new PointerBenchAnalysisAliasDacong(args[0], className);
				dacongResultsAlias.add(dacongAliasAnalysis.run());
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
		System.out.println("Dacong Alias Results");
		System.out.println(Joiner.on("\n").join(dacongResultsAlias));
		Table<String, String, String> texTable = convertToTexTable(boomerangResultsPtsTo,sridharanResultsPtsTo,boomerangResultsAlias,sridharanResultsAlias,dacongResultsAlias);
		computeRecall(texTable,boomerangResultsPtsTo,sridharanResultsPtsTo,boomerangResultsAlias,sridharanResultsAlias,dacongResultsAlias);
		prettyPrint(texTable);
		
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
	
	


	private static Table<String, String, String> convertToTexTable(List<PointerBenchResult> boomerangResultsPtsTo,
			List<PointerBenchResult> sridharanResultsPtsTo, List<PointerBenchResult> boomerangResultsAlias,
			List<PointerBenchResult> sridharanResultsAlias, List<PointerBenchResult> dacongResultsAlias) {
		Table<String,String,String> resultTable = HashBasedTable.create();
		handleResults(resultTable, boomerangResultsPtsTo,"ptsToBoomerang");
		handleResults(resultTable, sridharanResultsPtsTo,"ptsToSridharan");
		handleResults(resultTable, boomerangResultsAlias,"aliasBoomerang");
		handleResults(resultTable, sridharanResultsAlias,"aliasSridharan");
		handleResults(resultTable, dacongResultsAlias,"aliasYan");
		return resultTable;
	}
	private static void handleResults(Table<String, String, String> resultTable,
			List<PointerBenchResult> boomerangResultsPtsTo, String colKey) {
		for(PointerBenchResult r : boomerangResultsPtsTo){
			addToTable(resultTable,r,colKey,r.ptsTruePositives,"\\TP");
		}
		for(PointerBenchResult r : boomerangResultsPtsTo){
			addToTable(resultTable,r,colKey,r.ptsFalsePositives,"\\FP");
		}
		for(PointerBenchResult r : boomerangResultsPtsTo){
			addToTable(resultTable,r,colKey,r.ptsFalseNegatives,"\\FN");
		}
	}
	private static void addToTable(Table<String, String, String> resultTable, PointerBenchResult r, String columnString, int numberOfEntries, String characterToAppend) {
		String rowString = getRowString(r.className);
		String curr = resultTable.get(rowString, columnString);
		if(curr == null)
			curr = "";
		for(int i = 0; i < numberOfEntries; i++)
			curr += characterToAppend;
		resultTable.put(rowString, columnString,curr);
	}
	private static String getRowString(String className) {
		String substring = className.substring(0, className.length()-1);
		return substring.substring(substring.lastIndexOf(".")+1, substring.length());
	}

	private static void prettyPrint(Table<String, String, String> texTable) {
		List<String> header = Lists.newArrayList();
		header.add("Testcase");
		for(String col : texTable.columnKeySet()){
			header.add(col);
		}
		System.out.println(Joiner.on(";").join(header));
		for(String rowKey: texTable.rowKeySet()){
			List<String> line = Lists.newArrayList();
			line.add(rowKey);
			for(String col : texTable.columnKeySet()){
				line.add(texTable.get(rowKey, col));
			}
			System.out.println(Joiner.on(";").join(line));
		}
	}
	

	private static void computeRecall(Table<String, String, String> texTable, List<PointerBenchResult> boomerangResultsPtsTo,
			List<PointerBenchResult> sridharanResultsPtsTo, List<PointerBenchResult> boomerangResultsAlias,
			List<PointerBenchResult> sridharanResultsAlias, List<PointerBenchResult> dacongResultsAlias) {
		texTable.put("Recall", "ptsToBoomerang", computeRecall(boomerangResultsPtsTo));
		texTable.put("Precision", "ptsToBoomerang", computePrecision(boomerangResultsPtsTo));

		texTable.put("Recall", "ptsToSridharan", computeRecall(sridharanResultsPtsTo));
		texTable.put("Precision", "ptsToSridharan", computePrecision(sridharanResultsPtsTo));

		texTable.put("Recall", "aliasBoomerang", computeRecall(boomerangResultsAlias));
		texTable.put("Precision", "aliasBoomerang", computePrecision(boomerangResultsAlias));

		texTable.put("Recall", "aliasSridharan", computeRecall(sridharanResultsAlias));
		texTable.put("Precision", "aliasSridharan", computePrecision(sridharanResultsAlias));

		texTable.put("Recall", "aliasYan", computeRecall(dacongResultsAlias));
		texTable.put("Precision", "aliasYan", computePrecision(dacongResultsAlias));
	}
	
	private static String computePrecision(List<PointerBenchResult> res) {
		int tp = 0;
		int fp = 0;
		for(PointerBenchResult r : res){
			tp += r.ptsTruePositives;
			fp += r.ptsFalsePositives;
		}
		return "" + ((float) tp) /(tp + fp);
	}
	private static String computeRecall(List<PointerBenchResult> res) {
		int tp = 0;
		int fn = 0;
		for(PointerBenchResult r : res){
			tp += r.ptsTruePositives;
			fn += r.ptsFalseNegatives;
		}
		return "" + ((float) tp) /(tp + fn);
	}
}
