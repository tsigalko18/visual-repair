package runner;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import config.Settings;
import datatype.EnhancedTestCase;
import datatype.Statement;
import parser.ParseTest;
import utils.UtilsGetters;
import utils.UtilsRepair;
import utils.UtilsVisualRepair;

/**
 * The VisualAssertionTestRunner class runs the new evolved/regressed JUnit
 * Selenium test suites and uses the visual execution traces captured previously
 * to verify the correctness of the statements prior to their execution. In case
 * of mismatches, automatic repair techniques are triggered.
 * 
 * @author astocco
 *
 */
public class VisualAssertionTestRunner {

	public VisualAssertionTestRunner() {
		/*
		 * aspectJ must be disable here. TODO: eventually enable it in the future to
		 * re-create the new visual execution trace
		 */
		Settings.aspectActive = false;
	}

	public static void main(String[] args)
			throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {

		/* package name. */
		String prefix = "clarolineDirectBreakage.";

		/* class name. */
		String className = "TestLoginAdmin";

		VisualAssertionTestRunner var = new VisualAssertionTestRunner();

		var.runTestWithVisualAssertion(prefix, className);
	}

	private void runTestWithVisualAssertion(String prefix, String className) {

		/* get the path to the test that needs to be verified. */
		String testBroken = UtilsGetters.getTestFile(className, Settings.pathToTestSuiteUnderTest);

		Class<?> clazz = null;
		Object inst = null;

		try {
			clazz = Class.forName(prefix + className);
			inst = clazz.newInstance();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (InstantiationException e2) {
			e2.printStackTrace();
		} catch (IllegalAccessException e3) {
			e3.printStackTrace();
		}

		/* run the setup method (typically opens the browser). */
		runMethod(clazz, inst, "setUp");

		/* retrieve the WebDriver instance. */
		WebDriver driver = (WebDriver) runMethod(clazz, inst, "getDriver");

		/* parse the test and create the abstraction. */
		ParseTest pt = new ParseTest(Settings.referenceTestSuiteVisualTraceExecutionFolder);
		EnhancedTestCase etc = pt.parseAndSerialize(testBroken);

		pt.setFolder(Settings.referenceTestSuiteVisualTraceExecutionFolder);
		EnhancedTestCase testCorrect = pt
				.parseAndSerialize(UtilsGetters.getTestFile(className, Settings.pathToReferenceTestSuite));

		/* maintains a map of the original statements. */
		Map<Integer, Statement> statementMap = etc.getStatements();

		/* maintains a map of the repaired statements. */
		Map<Integer, Statement> repairedTest = new LinkedHashMap<Integer, Statement>();

		/* for each statement. */
		for (Integer statementNumber : statementMap.keySet()) {

			Statement statement = statementMap.get(statementNumber);

			System.out.println("[LOG]\tStatement " + statementNumber + ": " + statement.toString());
			System.out.println("[LOG]\tAsserting visual correcteness");

			WebElement webElementFromDomLocator = null;

			try {

				/* try to poll the DOM looking for the web element. */
				webElementFromDomLocator = UtilsVisualRepair.retrieveWebElementFromDomLocator(driver,
						statement.getDomLocator());

			} catch (NoSuchElementException Ex) {

				/*
				 * if NoSuchElementException is captured, it means that I'm incurred into a
				 * non-selection that would lead to a direct breakage in the test.
				 */

				System.out.println("[LOG]\tDirect breakage detected at line " + statement.getLine());
				System.out.println("[LOG]\tNon-selection of statement " + statement.getSeleniumAction());
				System.out.println("[LOG]\tLocator " + statement.getDomLocator()
						+ " not found in the current state. Applying visual detection of the web element");

				/*
				 * if the element is not found it can either be:
				 * 
				 * 1. on the same state (current default strategy) 2. another state (requires
				 * local crawling) 3. absent (we delete the statement)
				 * 
				 */

				webElementFromDomLocator = UtilsVisualRepair.searchWithinTheSameState(driver, webElementFromDomLocator,
						testCorrect, statementNumber);

				/*
				 * actually the local crawling step might also check whether the step is no
				 * longer possible.
				 */
				if (webElementFromDomLocator == null) {
					webElementFromDomLocator = UtilsVisualRepair.localCrawling(); // stub method
				}

				if (webElementFromDomLocator == null) {
					webElementFromDomLocator = UtilsVisualRepair.removeStatement(); // stub method
				} else {
					/*
					 * at this point, if the visual check is failing, webElementFromDomLocator
					 * should be set to null???
					 */
					webElementFromDomLocator = null;
				}

			}

			if (webElementFromDomLocator != null) {

				webElementFromDomLocator = UtilsVisualRepair.visualAssertWebElement(driver, webElementFromDomLocator,
						testCorrect, statementNumber, statement);

				Statement newStatement = (Statement) UtilsRepair.deepClone(statement);
				newStatement.setDomLocator(webElementFromDomLocator);

				try {
					// after ascertaining the right element, perform the action
					if (statement.getAction().equalsIgnoreCase("click")) {

						webElementFromDomLocator.click();

					} else if (statement.getAction().equalsIgnoreCase("sendkeys")) {

						webElementFromDomLocator.sendKeys(statement.getValue());

					} else if (statement.getAction().equalsIgnoreCase("selectByVisibleText")) {

						new Select(webElementFromDomLocator).selectByVisibleText(statement.getValue());

					} else if (statement.getAction().equalsIgnoreCase("getText")) {

						if (webElementFromDomLocator.getText() == statement.getValue()) {
							System.out.println("[LOG]\tAssertion value correct");
							System.out.println(statement.toString());
						} else {
							System.out.println(
									"[LOG]\tAssertion value incorrect: " + "\"" + webElementFromDomLocator.getText()
											+ "\"" + " <> " + "\"" + statement.getValue() + "\"");
							newStatement.setValue(webElementFromDomLocator.getText());
						}

					}

					// add the repaired statement to the test
					repairedTest.put(statementNumber, newStatement);

				} catch (Exception ex) {
					ex.printStackTrace();
					// Apply repair strategies
					break;
				}
			} else {
				System.out.println("[LOG]\tVisual correctness and repair failed");
			}

			System.out.println();

		}

		System.out.println("[LOG]\toriginal test case");
		UtilsRepair.printTestCaseWithLineNumbers(etc);

		EnhancedTestCase temp = (EnhancedTestCase) UtilsRepair.deepClone(etc);
		temp.replaceStatements(repairedTest);

		System.out.println("[LOG]\trepaired test case");
		UtilsRepair.printTestCaseWithLineNumbers(temp);

		driver.close();

		// cleanup(clazz, inst);
		// runMethod(clazz, inst, "tearDown");
		// pt.runTest(etc, testBroken);
		// System.exit(1);

		// Result result = null;
		// try {
		// System.out.println("[LOG]\tRunning Test " + classRunner);
		//
		// result = JUnitCore.runClasses(Class.forName(classRunner));
		// } catch (ClassNotFoundException e) {
		// e.printStackTrace();
		// }
		//
		// // if tests have failed, save the exception
		// if (!result.wasSuccessful()) {
		//
		// System.out.println("[LOG]\tTest " + classRunner + " failed, saving the
		// exception");
		//
		// // for each breakage, I save the exception on the filesystem
		// for (Failure fail : result.getFailures()) {
		//
		// EnhancedException ea = UtilsRepair.saveExceptionFromFailure(fail);
		//
		// String path = Settings.testingTestSuiteVisualTraceExecutionFolder
		// + UtilsRepair.capitalizeFirstLetter(ea.getFailedTest()) +
		// Settings.JAVA_EXTENSION;
		// String jsonPath = UtilsParser.toJsonPath(path);
		//
		// UtilsParser.serializeException(ea, jsonPath);
		// }
		// } else {
		// System.out.println("[LOG]\tTest " + classRunner + " passed");
		// }

		System.exit(0);
	}

	private static Object runMethod(Class<?> clazz, Object inst, String methodName) {

		Object result = null;

		try {

			Method[] allMethods = clazz.getDeclaredMethods();
			for (Method m : allMethods) {
				if (m.getName().equalsIgnoreCase(methodName)) {
					m.setAccessible(true);
					result = m.invoke(inst, null);
				}
			}

		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static void cleanup(Class<?> clazz, Object inst) {
		runMethod(clazz, inst, "tearDown");
		System.exit(1);
	}

}
