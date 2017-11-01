package runner;

import java.io.IOException;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import config.Settings;
import datatype.EnhancedException;
import utils.UtilsParser;
import utils.UtilsRepair;

public class TestSuiteRunner {

	public static void main(String[] args) throws IOException {

		// String prefix = "claroline190.";
		// String prefix = "addressbook6211.";
		// String prefix = "addressbook825.";
		String prefix = "claroline.";

		// path to the test suite class name
		// String classRunner = "claroline.TestLoginAdmin";
		// String classRunner = "clarolineDirectBreakage.TestLoginAdmin";
		// String classRunner = "addressbook6211.TestUserAdded";

		// String classRunner = prefix + "TestAddCategory";
		// String classRunner = prefix + "TestAddClass";
		// String classRunner = prefix + "TestAddCourse";
		// String classRunner = prefix + "TestAddNewCategory";
		// String classRunner = prefix + "TestAddPhone";
		// String classRunner = prefix + "TestAddUser";
		// String classRunner = prefix + "TestAssignments";
		// String classRunner = prefix + "TestCourseCategoryEdit";
		// String classRunner = prefix + "TestCourseDesc";
		// String classRunner = prefix + "TestDeletingCategory";
		// String classRunner = prefix + "TestEditTextZone";
		// String classRunner = prefix + "TestExercise";
		 String classRunner = prefix + "TestLoginAdmin";
		// String classRunner = prefix + "TestLoginLogout";
		// String classRunner = prefix + "TestModuleSettings";
		// String classRunner = prefix + "TestUserList";
		// String classRunner = prefix + "TestViewProfile";
//		String classRunner = prefix + "TestUserAdded";

		runTest(classRunner);
	}

	private static void runTest(String classRunner) {

		// this step runs the test suite and the aspect records the visual execution
		// trace
		Result result = null;
		try {
			System.out.println("[LOG]\tRunning Test " + classRunner);
			result = JUnitCore.runClasses(Class.forName(classRunner));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		// if tests have failed, save the exception
		if (!result.wasSuccessful()) {

			System.out.println("[LOG]\tTest " + classRunner + " failed, saving the exception");

			// for each breakage, I save the exception on the filesystem
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
