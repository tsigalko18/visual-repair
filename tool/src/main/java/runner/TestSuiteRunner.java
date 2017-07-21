package main.java.runner;

import java.io.IOException;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import main.java.config.Settings;
import main.java.datatype.EnhancedException;
import main.java.utils.UtilsParser;
import main.java.utils.UtilsRepair;

public class TestSuiteRunner {

	public static void main(String[] args) throws IOException {
		
		// path to the test suite class name
//		String classRunner = "main.java.claroline.TestLoginAdmin";
		String classRunner = "main.java.clarolineDirectBreakage.TestLoginAdmin";
		
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
				
				UtilsRepair.saveFailures(fail);
				EnhancedException ea = UtilsRepair.saveExceptionFromFailure(fail);
				
				String path = Settings.testingTestSuiteVisualTraceExecutionFolder + UtilsRepair.capitalizeFirstLetter(ea.getFailedTest()) + Settings.javaExtension;
				String jsonPath = UtilsParser.toJsonPath(path);
				
				UtilsParser.serializeException(ea, jsonPath);
			}
		}
	}

}
