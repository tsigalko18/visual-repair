package utils;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import config.Settings;
import datatype.EnhancedException;

public class UtilsRunner {

	public static Result runTestSuite(Class<?> testSuite) {
		Result result = JUnitCore.runClasses(testSuite);
		return result;
	}

	/**
	 * Run a single JUnit test case or an entire test suite if a runner class is
	 * specified
	 * 
	 * @param testSuite
	 * @param testCase
	 */
	public static void runTest(String testSuite, String testCase) {

		/* build the class runner. */
		String classRunner = testSuite + "." + testCase;

		/* run the test programmatically. */
		Result result = null;
		try {
			System.out.println("[LOG]\tRunning Test " + classRunner);
			result = JUnitCore.runClasses(Class.forName(classRunner));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		/* if the test failed, save the exception. */
		if (!result.wasSuccessful()) {

			System.out.println("[LOG]\tTest " + classRunner + " failed, saving the exception");

			/* for each breakage, save the exception on the filesystem. */
			for (Failure fail : result.getFailures()) {

				EnhancedException ea = UtilsRepair.saveExceptionFromFailure(fail);

				String path = Settings.testingTestSuiteVisualTraceExecutionFolder
						+ UtilsRepair.capitalizeFirstLetter(ea.getFailedTest()) + Settings.JAVA_EXTENSION;
				String jsonPath = UtilsParser.toJsonPath(path);

				UtilsParser.serializeException(ea, jsonPath);
			}
		} else {
			System.out.println("[LOG]\tTest " + classRunner + " passed");
		}

		System.exit(0);
	}

}
