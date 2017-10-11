package runner;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.opencv.core.Point;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import config.Settings;
import datatype.EnhancedTestCase;
import datatype.SeleniumLocator;
import datatype.Statement;
import parser.ParseTest;
import utils.UtilsGetters;
import utils.UtilsScreenshots;
import utils.UtilsXPath;

public class VisualAssertionTestRunner {

	public static void main(String[] args)
			throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {

		// String prefix = "clarolineDirectBreakage.";
		String prefix = "clarolineDirectBreakage.";

		String className = "TestLoginAdmin";

		String classRunner = prefix + className;

		// this step runs the test suite and the aspect records the visual execution
		// trace

		String testBroken = UtilsGetters.getTestFile(className, Settings.pathToTestSuiteUnderTest);
		// String testBroken = UtilsGetters.getTestFile(className, "src/main/resources/"
		// + prefix.replace(".", ""));
		Class<?> clazz = null;
		Object inst = null;

		try {
			clazz = Class.forName(classRunner);
			inst = clazz.newInstance();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (InstantiationException e2) {
			e2.printStackTrace();
		} catch (IllegalAccessException e3) {
			e3.printStackTrace();
		}

		Object ret = runMethod(clazz, inst, "setUp");

		WebDriver driver = (WebDriver) runMethod(clazz, inst, "getDriver");

		ParseTest pt = new ParseTest(Settings.referenceTestSuiteVisualTraceExecutionFolder);
		EnhancedTestCase etc = pt.parseAndSerialize(testBroken);

		pt.setFolder(Settings.referenceTestSuiteVisualTraceExecutionFolder);
		EnhancedTestCase testCorrect = pt
				.parseAndSerialize(UtilsGetters.getTestFile(className, Settings.pathToReferenceTestSuite));

		Map<Integer, Statement> statementMap = etc.getStatements();

		for (Integer I : statementMap.keySet()) {

			Statement statement = statementMap.get(I);

			System.out.println("[LOG]\tStatement " + I + ": " + statement.toString());
			System.out.println("[LOG]\tAsserting visual correcteness");

			String visualLocatorPerfect = null;
			String visualLocatorLarge = null;
			WebElement webElementFromDomLocator = null;
			WebElement webElementFromVisualLocatorPerfect = null;
			WebElement webElementFromVisualLocatorLarge = null;

			try {
				webElementFromDomLocator = retrieveWebElementFromDomLocator(driver, statement.getDomLocator());
			} catch (NoSuchElementException Ex) {

				System.out.println("[LOG]\tDirect breakage detected at line " + statement.getLine());
				System.out.println("[LOG]\tLocator " + statement.getDomLocator()
						+ " not found in the current state. Applying visual detection of the web element");

				visualLocatorPerfect = testCorrect.getStatements().get(I).getVisualLocatorPerfect().toString();
				visualLocatorLarge = testCorrect.getStatements().get(I).getVisualLocatorLarge().toString();

				webElementFromVisualLocatorPerfect = retrieveWebElementFromVisualLocator(driver, visualLocatorPerfect);
				webElementFromVisualLocatorLarge = retrieveWebElementFromVisualLocator(driver, visualLocatorLarge);

				/* there is disagreement between the visual locators. */
				if (!areWebElementsEquals(webElementFromVisualLocatorPerfect, webElementFromVisualLocatorLarge)) {
					System.out.println("[LOG]\tThe two visual locators target two different elements");
					System.out.println("[LOG]\tApplying proximity procedure");

					/* might be the wrong element. */
					System.out.println("[LOG]\tApplied (suboptimal) visual repair");
					webElementFromDomLocator = webElementFromVisualLocatorLarge;
				} else {
					/* any of the visual locator is ok. */
					System.out.println("[LOG]\tApplied visual repair");
					System.out.println("[LOG]\tRepaired element is " + webElementFromVisualLocatorPerfect);
					webElementFromDomLocator = webElementFromVisualLocatorPerfect;
				}
			}

			if (webElementFromDomLocator != null) {

				/* check the visual locators. */
				visualLocatorPerfect = testCorrect.getStatements().get(I).getVisualLocatorPerfect().toString();
				webElementFromVisualLocatorPerfect = retrieveWebElementFromVisualLocator(driver, visualLocatorPerfect);

				visualLocatorLarge = testCorrect.getStatements().get(I).getVisualLocatorLarge().toString();
				webElementFromVisualLocatorLarge = retrieveWebElementFromVisualLocator(driver, visualLocatorLarge);

				/* there is disagreement between the visual locators. */
				if (!areWebElementsEquals(webElementFromVisualLocatorPerfect, webElementFromVisualLocatorLarge)) {

					System.out.println("[LOG]\tThe two visual locators target two different elements");
					System.out.println("[LOG]\tApplying proximity procedure");
					webElementFromDomLocator = applyProximityVoting((JavascriptExecutor) driver,
							webElementFromDomLocator, webElementFromVisualLocatorPerfect,
							webElementFromVisualLocatorLarge);
				}

				if (!areWebElementsEquals(webElementFromDomLocator, webElementFromVisualLocatorLarge)
						|| !areWebElementsEquals(webElementFromDomLocator, webElementFromVisualLocatorPerfect)) {

					System.out.println("[LOG]\tChance of propagated breakage at line " + statement.getLine());
					System.out.println("[LOG]\tDOM locator and visual locator target two different elements");

					System.out.println(webElementFromVisualLocatorLarge);
					System.out.println(webElementFromVisualLocatorPerfect);
					System.out.println(webElementFromDomLocator);

					/* DECIDE WHAT TO DO HERE: which one should I trust? */
					webElementFromDomLocator = webElementFromVisualLocatorPerfect;
				} else {
					System.out.println("[LOG]\tVisual Assertion correct");
				}
				try {
					// after ascertaining the right element, perform the action
					if (statement.getAction().equalsIgnoreCase("click")) {
						webElementFromDomLocator.click();
					}
					if (statement.getAction().equalsIgnoreCase("sendkeys")) {
						webElementFromDomLocator.sendKeys(statement.getValue().replaceAll("\"", ""));
					}
					if (statement.getAction().equalsIgnoreCase("selectByVisibleText")) {
						new Select(webElementFromDomLocator).selectByVisibleText(statement.getValue());
					}

				} catch (Exception Ex) {
					Ex.printStackTrace();
					// Apply repair strategies
					break;
				}
			}

		}
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

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

	private static WebElement applyProximityVoting(JavascriptExecutor js, WebElement webElementFromDomLocator,
			WebElement webElementFromVisualLocatorPerfect, WebElement webElementFromVisualLocatorLarge) {

		String xpathVisualLocatorPerfect = UtilsXPath.generateXPathForWebElement(webElementFromVisualLocatorPerfect,
				"");
		String xpathVisualLocatorLarge = UtilsXPath.generateXPathForWebElement(webElementFromVisualLocatorLarge, "");

		String xpathDomLocator = UtilsXPath.generateXPathForWebElement(webElementFromDomLocator, "");

		if (xpathDomLocator.equals(xpathVisualLocatorPerfect)) {
			System.out.println("[LOG]\tVisually verified with perfectly cropped visual locator");
			return webElementFromVisualLocatorPerfect;
		} else if (xpathDomLocator.equals(xpathVisualLocatorLarge)) {
			System.out.println("[LOG]\tVisually verified with largely cropped visual locator");
			return webElementFromVisualLocatorLarge;
		} else {
			System.out.println("[LOG]\tNot visually verified");
			return webElementFromVisualLocatorPerfect;
		}
	}

	private static boolean areWebElementsEquals(WebElement webElementFromDomLocator,
			WebElement webElementFromVisualLocator) {

		return webElementFromDomLocator.equals(webElementFromVisualLocator);

	}

	private static WebElement retrieveWebElementFromVisualLocator(WebDriver driver, String visualLocator) {

		String currentScreenshot = System.getProperty("user.dir") + Settings.separator + "currentScreenshot.png";
		UtilsScreenshots.saveScreenshot(driver, currentScreenshot);

		Point matches = UtilsScreenshots.findBestMatchCenter(currentScreenshot, visualLocator);
		// returnAllMatches(currentScreenshot, template);

		// System.out.println(matches);

		String xpathForMatches = UtilsXPath.getXPathFromLocation(matches, driver);

		// System.out.println("XPath for match: " + xpathForMatches);
		WebElement fromVisual = driver.findElement(By.xpath(xpathForMatches));
		return fromVisual;
	}

	private static WebElement retrieveWebElementFromDomLocator(WebDriver driver, SeleniumLocator domSelector) {

		String strategy = domSelector.getStrategy();
		String locator = domSelector.getValue();
		WebElement element = null;

		if (strategy.equalsIgnoreCase("xpath")) {
			element = driver.findElement(By.xpath(locator));
		} else if (strategy.equalsIgnoreCase("name")) {
			element = driver.findElement(By.name(locator));
		} else if (strategy.equalsIgnoreCase("id")) {
			element = driver.findElement(By.id(locator));
		} else if (strategy.equalsIgnoreCase("linkText")) {
			element = driver.findElement(By.linkText(locator));
		} else if (strategy.equalsIgnoreCase("cssSelector")) {
			element = driver.findElement(By.cssSelector(locator));
		}

		return element;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public static void cleanup(Class<?> clazz, Object inst) {
		runMethod(clazz, inst, "tearDown");
		// pt.runTest(etc, testBroken);
		System.exit(1);
	}

}
