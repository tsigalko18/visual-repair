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
		
		// path to the test suite class name
//		String classRunner = "claroline.TestLoginAdmin";
//		String classRunner = "clarolineDirectBreakage.TestLoginAdmin";
//		String classRunner = "addressbook6211.TestUserAdded";
		String classRunner = "addressbook825.TestUserAdded";
		
		
		// this step runs the test suite and the aspect records the visual execution trace 
		Result result = null;
		try {			
			result = JUnitCore.runClasses(Class.forName(classRunner));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		// if tests have failed, save the exception
		if(!result.wasSuccessful()) {
		
			// for each breakage, I save the exception on the filesystem
			for (Failure fail : result.getFailures()) {
				
				EnhancedException ea = UtilsRepair.saveExceptionFromFailure(fail);
				
				String path = Settings.testingTestSuiteVisualTraceExecutionFolder + UtilsRepair.capitalizeFirstLetter(ea.getFailedTest()) + Settings.JAVA_EXTENSION;
				String jsonPath = UtilsParser.toJsonPath(path);
				
				UtilsParser.serializeException(ea, jsonPath);
			}
		}
		
		System.exit(0);
	}

}
