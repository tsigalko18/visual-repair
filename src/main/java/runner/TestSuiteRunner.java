package runner;

import java.io.IOException;

import config.Settings;
import utils.UtilsRunner;

/**
 * The TestSuiteRunner class is suppose to help in automating the task of
 * running the JUnit Selenium test suites that are used as a reference for the
 * new evolved/regressed test suites, that will be instead executed through the
 * VisualAssertionTestRunner class contained in this package
 * 
 * @author astocco
 * @author yrahulkr
 *
 */
public class TestSuiteRunner {

	public TestSuiteRunner() {
		Settings.aspectActive = true;
	}

	public static void main(String[] args) throws IOException {

		/* Claroline example. */
		UtilsRunner.runTest(Settings.testSuiteCorrect, "TestLoginAdmin");

	}

}
