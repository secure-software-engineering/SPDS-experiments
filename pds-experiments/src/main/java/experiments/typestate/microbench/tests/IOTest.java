package experiments.typestate.microbench.tests;

import com.ibm.safe.internal.exceptions.SafeException;

import experiments.typestate.microbench.TypestateRegressionUnit;

public class IOTest extends AbstractTestCase {

	public void testFileOutputStreamExample1() throws SafeException, Exception {
		TypestateRegressionUnit test = new TypestateRegressionUnit(
				"j2se.typestate.output_stream.FileOutputStreamExample1", 1);
		test.selectTypestateRule("OutputStreamCloseThenWrite");
		run(test);
	}

	public void testPipedOutputStreamExample1() throws SafeException, Exception {
		TypestateRegressionUnit test = new TypestateRegressionUnit(
				"j2se.typestate.output_stream.PipedOutputStreamExample1", 1);
		test.selectTypestateRule("PipedOutputStream");
		run(test);
	}

	public void testPipedOutputStreamExample2() throws SafeException, Exception {
		TypestateRegressionUnit test = new TypestateRegressionUnit(
				"j2se.typestate.output_stream.PipedOutputStreamExample2", 0);
		test.selectTypestateRule("PipedOutputStream");
		run(test);
	}

	public void testFileOutputStreamExample2() throws SafeException, Exception {
		TypestateRegressionUnit test = new TypestateRegressionUnit(
				"j2se.typestate.output_stream.FileOutputStreamExample2", 1);
		test.selectTypestateRule("OutputStreamCloseThenWrite");
		run(test);
	}

	public void testFileOutputStreamExample3() throws SafeException, Exception {
		TypestateRegressionUnit test = new TypestateRegressionUnit(
				"j2se.typestate.output_stream.FileOutputStreamExample3", 1);
		test.selectTypestateRule("OutputStreamCloseThenWrite");
		run(test);
	}

	public void testFileInputStreamExample1() throws SafeException, Exception {
		TypestateRegressionUnit test = new TypestateRegressionUnit(
				"j2se.typestate.input_stream.FileInputStreamExample1", 1);
		test.selectTypestateRule("InputStreamCloseThenRead");
		run(test);
	}

	public void testPipedInputStreamExample1() throws SafeException, Exception {
		TypestateRegressionUnit test = new TypestateRegressionUnit(
				"j2se.typestate.input_stream.PipedInputStreamExample1", 1);
		test.selectTypestateRule("PipedInputStream");
		run(test);
	}

	public void testPipedInputStreamExample2() throws SafeException, Exception {
		TypestateRegressionUnit test = new TypestateRegressionUnit(
				"j2se.typestate.input_stream.PipedInputStreamExample2", 0);
		test.selectTypestateRule("PipedInputStream");
		run(test);
	}

	public void testPipedInputStreamExample3() throws SafeException, Exception {
		TypestateRegressionUnit test = new TypestateRegressionUnit(
				"j2se.typestate.input_stream.PipedInputStreamExample3", 0);
		test.selectTypestateRule("PipedInputStream");
		run(test);
	}

	public void testFileInputStreamExample2() throws SafeException, Exception {
		TypestateRegressionUnit test = new TypestateRegressionUnit(
				"j2se.typestate.input_stream.FileInputStreamExample2", 1);
		test.selectTypestateRule("InputStreamCloseThenRead");
		run(test);
	}

	public void testPrintStreamExample1() throws SafeException, Exception {
		TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.printStream.PrintStreamExample1", 1);
		test.selectTypestateRule("PrintStream");
		run(test);
	}

	public void testPrintStreamExample2() throws SafeException, Exception {
		TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.printStream.PrintStreamExample2", 1);
		test.selectTypestateRule("PrintStream");
		run(test);
	}

	public void testPrintWriterExample1() throws SafeException, Exception {
		TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.printWriter.PrintWriterExample1", 1);
		test.selectTypestateRule("PrintWriter");
		run(test);
	}

	public void testPrintWriterExample2() throws SafeException, Exception {
		TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.printWriter.PrintWriterExample2", 0);
		test.selectTypestateRule("PrintWriter");
		run(test);
	}
}
