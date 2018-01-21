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
	
	static long startTime;
	static long stopTime;
	static long elapsedTime;
	
	public TestSuiteRunner() {
		if(Settings.aspectActive == false) {
			Settings.aspectActive = true;
		}
	}

	public static void main(String[] args) throws IOException {

		/* specify the test suite and test name or runner. */
		
		startTime = System.currentTimeMillis();
		
		/* Claroline example */
		//UtilsRunner.runTest(Settings.testSuiteCorrect, "TestLoginAdmin");
		
		/* AddressBook example */
//		Settings.aspectActive = true;
//		UtilsRunner.runTest(Settings.testSuiteCorrect, "AddressBookSearchAddressBookNameTest");
		
//		UtilsRunner.runTest(Settings.testSuiteBroken, "AddressBookSearchAddressBookNameTest");
		
//		Settings.aspectActive = false;
//		UtilsRunner.runTest("addressbook411Repaired", "AddressBookSearchAddressBookNameTest");
		
		stopTime = System.currentTimeMillis();
		elapsedTime = stopTime - startTime;
		System.out.format("\nelapsedTime (s): %.3f", elapsedTime / 1000.0f);

	}

}
