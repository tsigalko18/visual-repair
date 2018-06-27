package runner;

import java.io.IOException;

import config.Settings;
import utils.UtilsRunner;

/**
 * The VisualExecutionTracer class runs a JUnit Selenium test suites and
 * collects the DOM and GUI information pertaining to each statement
 * 
 * @author astocco
 * @author yrahulkr
 *
 */
public class VisualExecutionTracer {

	public VisualExecutionTracer() {
		Settings.aspectActive = true;
	}

	public static void main(String[] args) throws IOException {

		/* Claroline example. */
		UtilsRunner.runTest(Settings.testSuiteCorrect, "TestLoginAdmin");

	}

}
