package main.java.runner;

import java.io.IOException;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import main.java.claroline190.Claroline_TestSuite_Selenium;
import main.java.datatype.*;
import main.java.utils.*;
import main.java.config.Settings;

public class TestSuiteRunner {

	public static void main(String[] args) throws IOException {

		// this step runs the test suite and the aspect records the visual
		// execution trace
		Result result1 = JUnitCore.runClasses(Claroline_TestSuite_Selenium.class);

		for (Failure fail : result1.getFailures()) {
			// for each breakage, I save the exception on the filesystem
			//
			UtilsRepair.saveFailures(fail);
			EnhancedException ea = UtilsRepair.saveExceptionFromFailure(fail);

			String path = Settings.testingTestSuiteVisualTraceExecutionFolder
					+ UtilsRepair.capitalizeFirstLetter(ea.getFailedTest()) + Settings.javaExtension;
			String jsonPath = UtilsParser.toJsonPath(path);

			UtilsParser.serializeException(ea, jsonPath);
		}

		// step 3 : run the analysis and repair

	}

	// public void runSingleTest(String test) {
	//
	// if (Settings.INRECORDING) {
	//
	// // I want to record the visual trace of the correct test case
	//
	// // Result result =
	// // JUnitCore.runClasses(Claroline_TestSuite_Selenium.class);
	// // for (Failure fail : result.getFailures()) {
	// // System.out.println(fail.toString());
	// // }
	// // assertTrue(result.wasSuccessful());
	//
	// } else {
	// // I want to run the test case of the next test suite
	//
	// }
	// }
	//
	// public void runAllTests() {
	//
	// if (Settings.INRECORDING) {
	//
	// // I want to record the visual trace of the correct test suite
	//
	// // Result result =
	// // JUnitCore.runClasses(Claroline_TestSuite_Selenium.class);
	// // for (Failure fail : result.getFailures()) {
	// // System.out.println(fail.toString());
	// // }
	// // assertTrue(result.wasSuccessful());
	//
	// } else {
	// // I want to run the next test suite
	// }
	// }

}
