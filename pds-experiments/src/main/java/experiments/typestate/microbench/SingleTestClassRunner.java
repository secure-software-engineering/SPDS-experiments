package experiments.typestate.microbench;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

public class SingleTestClassRunner {
	public static void main(String...args) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException{
		String testClassName = args[0];
		if(System.getProperty("analysis") == null || System.getProperty("analysis").equals(""))
			throw new RuntimeException("Add -Danalysis option to JVM!");
		Class<?> forName = Class.forName(testClassName);
		ArrayList<WrappedMethod> methods = new ArrayList<WrappedMethod>();
		for(Method m : forName.getDeclaredMethods()){
			methods.add(new WrappedMethod(m));
		}
		if(!new File("outputMicro").exists())
			new File("outputMicro").mkdirs();
		String outputFile =  "outputMicro/"+System.getProperty("analysis") + "-" + testClassName +  (Util.aliasing() ? "" : "-noAliasing") + (Util.strongUpdates() ? "" : "-noStrongUpdates") + ".csv";
		System.out.println(outputFile);
		for(WrappedMethod m : methods){
			System.out.println(m.getName());
			String javaHome = System.getProperty("java.home");
	        String javaBin = javaHome +
	                File.separator + "bin" +
	                File.separator + "java";
			ProcessBuilder builder = new ProcessBuilder(new String[] {javaBin, "-cp",  System.getProperty("java.class.path"), InvokeAllTestsOfClass.class.getName(),testClassName, System.getProperty("analysis"), m.getName(), outputFile,Boolean.toString(Util.aliasing()),Boolean.toString(Util.strongUpdates())});
			builder.inheritIO();
			Process process;
			try {
				process = builder.start();
				process.waitFor();
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
//		if(System.getProperty("analysis").equals("fink-staged")){
//			computeMeans(outputFile.replace(".csv", "-Unique.csv" ));
//			computeMeans(outputFile.replace(".csv", "-APMustMustNot.csv" ));
//		} else{
//			computeMeans(outputFile);
//		}
	}
	
	public static class WrappedMethod implements Comparable<WrappedMethod>{
		private String name;
		private Method method;
		WrappedMethod(Method m){
			this.method = m;
			this.name = m.getName();
		}
		@Override
		public int compareTo(WrappedMethod o) {
			return o.name.compareTo(this.name);
		}
		Method getMethod(){
			return method;
		}
		public String getName(){
			return name;
		}
	}
	private static void computeMeans(String outputFile) {
		TestResultTable table = new TestResultTable();
		try(BufferedReader reader = new BufferedReader(new FileReader(new File(outputFile)))){
			String ln = null;
			boolean first = true;
			while(null != (ln = reader.readLine())){
				if(first){
					//Skip header
					first = false;
					continue;
				}
				String[] split = ln.split(";");
				TestResultEntry entry = new TestResultEntry(split);
				table.add(entry);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String summaryLine = table.computeMeans();
		try {
			FileWriter writer = new FileWriter(new File(outputFile),true);
			writer.write(summaryLine);
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static class TestResultEntry{
		private String method;
		private int propagationCount;
		private int analysisTime;
		private int visitedMethods;
		private int actualErrors;
		private int expectedErrors;
		public TestResultEntry(String[] split) {
			method = split[0];
			propagationCount = Integer.parseInt(split[1]);
			visitedMethods = Integer.parseInt(split[2]);
			actualErrors = Integer.parseInt(split[3]);
			expectedErrors = Integer.parseInt(split[4]);
		}
		
	}
	private static class TestResultTable{
		private List<TestResultEntry> entries = Lists.newLinkedList();
		void add(TestResultEntry entry){
			entries.add(entry);
		}
		public String computeMeans() {
			List<Integer> visitedMethods = Lists.newLinkedList();
			List<Integer> actualErrors = Lists.newLinkedList();
			List<Integer> expectedErrors = Lists.newLinkedList();
			List<Integer> propagationCounts = Lists.newLinkedList();
			for(TestResultEntry e : entries){
				propagationCounts.add(e.propagationCount);
				visitedMethods.add(e.visitedMethods);
				actualErrors.add(e.actualErrors);
				expectedErrors.add(e.expectedErrors);
			}
			return "---;"+arithmeticMean(propagationCounts) +";"+arithmeticMean(visitedMethods)+";"+sum(actualErrors)+";"+sum(expectedErrors);
		}
	}
	
	private static double geometricMean(List<Integer> input){
		double val = 1;
		int nulls = 0;
		for(Integer i : input){
			if(i != 0)
				val*=i;
			else
				nulls++;
		}
		double exp = (double) 1 / (input.size()-nulls);
		return Math.pow(val,exp);
	}
	
	private static double arithmeticMean(List<Integer> input){
		double val = 0;
		for(Integer i : input){
			val+=i;
		}
		return (double) val / (input.size());
	}
	private static int sum(List<Integer> input){
		int val = 0;
		for(Integer i : input){
				val+=i;
		}
		return val;
	}
}
