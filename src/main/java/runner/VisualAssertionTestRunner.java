package runner;

import java.io.IOException;
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
import utils.UtilsRunner;
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

		long startTime = System.currentTimeMillis();

		var.runTestWithVisualAssertion(prefix, className);

		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		System.out.println("\nelapsedTime (s): " + elapsedTime / 1000);
		
	}

	private void runTestWithVisualAssertion(String prefix, String className) throws IOException {

		/* get the path to the test that needs to be verified. */
		String testBroken = UtilsGetters.getTestFile(className, Settings.pathToTestSuiteUnderTest);

		System.out.println("[LOG]\tVerifying test " + prefix + className);

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
		UtilsRunner.runMethod(clazz, inst, "setUp");

		/* retrieve the WebDriver instance. */
		WebDriver driver = (WebDriver) UtilsRunner.runMethod(clazz, inst, "getDriver");

		/* parse the test and create the abstraction. */
		ParseTest pt = new ParseTest(Settings.referenceTestSuiteVisualTraceExecutionFolder);
		EnhancedTestCase etc = pt.parseAndSerialize(testBroken);

		pt.setFolder(Settings.referenceTestSuiteVisualTraceExecutionFolder);
		EnhancedTestCase testCorrect = pt
				.parseAndSerialize(UtilsGetters.getTestFile(className, Settings.pathToReferenceTestSuite));

		/* map of the original statements. */
		Map<Integer, Statement> statementMap = etc.getStatements();

		/* map of the repaired statements. */
		Map<Integer, Statement> repairedTest = new LinkedHashMap<Integer, Statement>();

		/* for each statement. */
		for (Integer statementNumber : statementMap.keySet()) {

			Statement statement = statementMap.get(statementNumber);

			System.out.println("[LOG]\tStatement " + statementNumber + ": " + statement.toString());
			System.out.println("[LOG]\tAsserting visual correcteness");

			WebElement webElementFromDomLocator = null;

			Statement newStatement = (Statement) UtilsRepair.deepClone(statement);

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
				System.out.println("[LOG]\tCause: Non-selection of elements by the locator " + statement.getDomLocator()
						+ " in the current DOM state. Applying visual detection of the web element");

				/*
				 * if the element is not found it can either be:
				 * 
				 * 1. on the same state (current default strategy) 2. another state (requires
				 * local crawling) 3. absent (we delete the statement)
				 * 
				 */

				webElementFromDomLocator = UtilsVisualRepair.visualAssertWebElement(driver, webElementFromDomLocator,
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
				}

				if (webElementFromDomLocator == null) {
					/*
					 * at this point, if the visual check is failing, webElementFromDomLocator
					 * should be set to null?
					 */
					// webElementFromDomLocator = null;
					System.err.println("[LOG]\tStatement " + statementNumber + " still broken.");
				} else {

					/* repair locator. */
					newStatement.setDomLocator(webElementFromDomLocator);

					/* add the repaired statement to the test. */
					repairedTest.put(statementNumber, newStatement);
				}

			}

			if (webElementFromDomLocator != null) {

				webElementFromDomLocator = UtilsVisualRepair.visualAssertWebElement(driver, webElementFromDomLocator,
						testCorrect, statementNumber);

				/* repair locator. */
				newStatement.setDomLocator(webElementFromDomLocator);

				try {
					/* after ascertaining the right element, perform the action. */
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
							System.out.println("[LOG]\tSuggested new value for assertion: " + "\""
									+ webElementFromDomLocator.getText() + "\"");

							/* repair assertion value. */
							newStatement.setValue(webElementFromDomLocator.getText());
						}

					}

					/* add the repaired statement to the test. */
					repairedTest.put(statementNumber, newStatement);

				} catch (Exception ex) {
					ex.printStackTrace();
					// Apply repair strategies
					break;
				}
			}
			// else {
			// System.out.println("[LOG]\tVisual correctness and repair failed");
			// }

			System.out.println();

		}

		System.out.println("[LOG]\toriginal test case");
		UtilsRepair.printTestCaseWithLineNumbers(etc);

		EnhancedTestCase temp = (EnhancedTestCase) UtilsRepair.deepClone(etc);
		temp.replaceStatements(repairedTest);

		System.out.println("[LOG]\trepaired test case");
		UtilsRepair.printTestCaseWithLineNumbers(temp);

		UtilsRunner.cleanup(clazz, inst);

		Runtime rt = Runtime.getRuntime();
		rt.exec("killall firefox-bin");

	}

}
