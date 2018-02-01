package com.ibm.safe.typestate.tests;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ResearchQuestion {
	protected Properties benchProperties = new Properties();
	private Properties generalProperties = new Properties();
	private String benchmarkName;

	public ResearchQuestion() {
		try {
			this.benchLoad();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void benchLoad() throws IOException {
		this.benchmarkName = System.getProperty("benchmark");
		if (benchmarkName == null)
			throw new RuntimeException("Set property -Dbenchmark= as VM argument to select the benchmark");
		String propFileName = getBasePath() + benchmarkName
				+ ".properties";
		InputStream stream = new FileInputStream(propFileName);
		benchProperties.load(stream);
	}

	protected String getBasePath() {
		String path = System.getProperty("benchmarkFolder") + benchmarkName + "/";
		if (path == null)
			throw new RuntimeException("Set property -DbenchmarkFolder= as VM argument");
		return path;
	}

	protected String getMainClass() {
		return benchProperties.getProperty("main_class");
	}

	public String[] getApplicationClasses() {
		String library_jar_files = benchProperties.getProperty("application_includes");
		String[] split = library_jar_files.split(":");
		return split;
	}

	public String getOutputDir() {
		return generalProperties.getProperty("output_dir");
	}

	public String getBenchName() {
		return this.benchmarkName;
	}
}
