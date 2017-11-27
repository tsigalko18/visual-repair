package utils;

import org.opencv.core.Point;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import config.Settings;
import datatype.EnhancedTestCase;
import datatype.SeleniumLocator;
import datatype.Statement;

public class UtilsVisualRepair {

	/**
	 * Procedure to verify a DOM element locator by means of visual locators. The
	 * procedure implies that the DOM locator exists on the test state.
	 */
	public static WebElement visualAssertWebElement(WebDriver driver, WebElement webElementFromDomLocator,
			EnhancedTestCase testCorrect, Integer i, Statement statement) {

		String visualLocatorPerfect = null;
		String visualLocatorLarge = null;
		WebElement webElementFromVisualLocatorPerfect = null;
		WebElement webElementFromVisualLocatorLarge = null;

		/* retrieve the visual locators. */
		try {
			visualLocatorPerfect = testCorrect.getStatements().get(i).getVisualLocatorPerfect().toString();
			visualLocatorLarge = testCorrect.getStatements().get(i).getVisualLocatorLarge().toString();
		} catch (NullPointerException e) {
			System.out.println("[ERROR]\tVisual locator(s) not found in " + testCorrect.getStatements().get(i));
			System.out.println("[ERROR]\tRe-run the TestSuiteRunner on " + testCorrect.getPath());
			System.exit(1);
		}

		webElementFromVisualLocatorPerfect = UtilsVisualRepair.retrieveWebElementFromVisualLocator(driver,
				visualLocatorPerfect);

		webElementFromVisualLocatorLarge = UtilsVisualRepair.retrieveWebElementFromVisualLocator(driver,
				visualLocatorLarge);

		if (webElementFromVisualLocatorPerfect == null && webElementFromVisualLocatorLarge == null) {

			System.err.println("[LOG]\tElement not found (visually) in the state. Visual assertion failed.");

		} else if (webElementFromVisualLocatorPerfect != null && webElementFromVisualLocatorLarge == null) {

			if (!UtilsVisualRepair.areWebElementsEquals(webElementFromDomLocator, webElementFromVisualLocatorLarge)) {

				System.out.println("[LOG]\tChance of propagated breakage at line " + statement.getLine());
				System.out.println("[LOG]\tDOM locator and visual locator target two different elements");

				// System.out.println(webElementFromVisualLocatorLarge);
				// System.out.println(webElementFromDomLocator);

				/* DECIDE WHAT TO DO HERE: which one should I trust? */
				System.out.println(
						"[LOG]\tApplied (suboptimal) visual verification. Trusting perfectly cropped visual locator. Applied visual repair");
				System.out.println("[LOG]\tNew repaired element is " + webElementFromVisualLocatorLarge);
				webElementFromDomLocator = webElementFromVisualLocatorLarge;

			} else {

				System.out.println("[LOG]\tDOM locator and visual locator target the same element");

				// System.out.println(webElementFromVisualLocatorLarge);
				// System.out.println(webElementFromDomLocator);

				System.out.println(
						"[LOG]\tApplied (suboptimal) visual verification with perfectly cropped visual locator. No visual repair applied");
			}

		} else if (webElementFromVisualLocatorPerfect == null && webElementFromVisualLocatorLarge != null) {

			if (!UtilsVisualRepair.areWebElementsEquals(webElementFromDomLocator, webElementFromVisualLocatorLarge)) {

				System.out.println("[LOG]\tChance of propagated breakage at line " + statement.getLine());
				System.out.println("[LOG]\tDOM locator and visual locator target two different elements");

				// System.out.println(webElementFromVisualLocatorLarge);
				// System.out.println(webElementFromDomLocator);

				/* DECIDE WHAT TO DO HERE: which one should I trust? */
				System.out.println(
						"[LOG]\tApplied (suboptimal) visual verification. Trusting largely cropped visual locator. Applied visual repair");
				System.out.println("[LOG]\tNew repaired element is " + webElementFromVisualLocatorLarge);
				webElementFromDomLocator = webElementFromVisualLocatorLarge;
			} else {
				System.out.println("[LOG]\tDOM locator and visual locator target the same element");

				// System.out.println(webElementFromVisualLocatorLarge);
				// System.out.println(webElementFromDomLocator);

				System.out.println(
						"[LOG]\tApplied (suboptimal) visual verification with largely cropped visual locator. No visual repair applied");
			}

		} else if (webElementFromVisualLocatorPerfect != null && webElementFromVisualLocatorLarge != null) {

			/* there is disagreement between the visual locators. */
			if (!UtilsVisualRepair.areWebElementsEquals(webElementFromVisualLocatorPerfect,
					webElementFromVisualLocatorLarge)) {
				/* might be the wrong element. */
				System.out.println("[LOG]\tThe two visual locators target two different elements");
				System.out.println("[LOG]\tApplying proximity procedure");
				System.out.println("[LOG]\tApplied (suboptimal) visual repair");
				webElementFromDomLocator = webElementFromVisualLocatorLarge;

			} else {

				/* any of the visual locator is ok. */
				System.out.println("[LOG]\tThe two visual locators target the same element");
				System.out.println("[LOG]\tApplied (optimal) visual repair");
				System.out.println("[LOG]\tRepaired element is " + webElementFromVisualLocatorPerfect);
				webElementFromDomLocator = webElementFromVisualLocatorPerfect;

			}

		}

		// /* there is disagreement between the visual locators. */
		// if
		// (!UtilsVisualRepair.areWebElementsEquals(webElementFromVisualLocatorPerfect,
		// webElementFromVisualLocatorLarge)) {
		//
		// System.out.println("[LOG]\tThe two visual locators target two different
		// elements");
		// System.out.println("[LOG]\tApplying proximity procedure");
		// webElementFromDomLocator =
		// UtilsVisualRepair.applyProximityVoting((JavascriptExecutor) driver,
		// webElementFromDomLocator, webElementFromVisualLocatorPerfect,
		// webElementFromVisualLocatorLarge);
		// }
		//
		// if (!UtilsVisualRepair.areWebElementsEquals(webElementFromDomLocator,
		// webElementFromVisualLocatorLarge)
		// || !UtilsVisualRepair.areWebElementsEquals(webElementFromDomLocator,
		// webElementFromVisualLocatorPerfect)) {
		//
		// System.out.println("[LOG]\tChance of propagated breakage at line " +
		// statement.getLine());
		// System.out.println("[LOG]\tDOM locator and visual locator target two
		// different elements");
		//
		// // System.out.println(webElementFromVisualLocatorLarge);
		// // System.out.println(webElementFromVisualLocatorPerfect);
		// // System.out.println(webElementFromDomLocator);
		//
		// /* DECIDE WHAT TO DO HERE: which one should I trust? */
		// webElementFromDomLocator = webElementFromVisualLocatorPerfect;
		// } else {
		// System.out.println("[LOG]\tVisual Assertion correct");
		// }

		return webElementFromDomLocator;
	}

	/**
	 * Procedure to find a DOM element locator by means of visual locators. The
	 * procedure implies that the DOM locator has not been found on the test state.
	 */
	public static WebElement searchWithinTheSameState(WebDriver driver, WebElement webElementFromDomLocator,
			EnhancedTestCase testCorrect, Integer i) {

		String visualLocatorPerfect = null;
		String visualLocatorLarge = null;
		WebElement webElementFromVisualLocatorPerfect = null;
		WebElement webElementFromVisualLocatorLarge = null;

		/* retrieve the visual locators. */
		try {
			visualLocatorPerfect = testCorrect.getStatements().get(i).getVisualLocatorPerfect().toString();
			visualLocatorLarge = testCorrect.getStatements().get(i).getVisualLocatorLarge().toString();
		} catch (NullPointerException e) {
			System.out.println("[ERROR]\tVisual locator(s) not found in " + testCorrect.getStatements().get(i));
			System.out.println("[ERROR]\tRe-run the TestSuiteRunner on " + testCorrect.getPath());
			System.exit(1);
		}

		webElementFromVisualLocatorPerfect = UtilsVisualRepair.retrieveWebElementFromVisualLocator(driver,
				visualLocatorPerfect);

		webElementFromVisualLocatorLarge = UtilsVisualRepair.retrieveWebElementFromVisualLocator(driver,
				visualLocatorLarge);

		if (webElementFromVisualLocatorPerfect == null && webElementFromVisualLocatorLarge == null) {

			System.err.println("[LOG]\tElement not found (visually) in the state. Visual assertion failed.");
			return null;

		} else if (webElementFromVisualLocatorPerfect != null && webElementFromVisualLocatorLarge == null) {

			/* only the visual locator perfect is ok. */
			System.out
					.println("[LOG]\tApplied (suboptimal) visual repair based on the perfectly cropped visual locator");
			System.out.println("[LOG]\tRepaired element is " + webElementFromVisualLocatorPerfect);
			webElementFromDomLocator = webElementFromVisualLocatorPerfect;

		} else if (webElementFromVisualLocatorPerfect == null && webElementFromVisualLocatorLarge != null) {

			/* only the visual locator large is ok. */
			System.out.println("[LOG]\tApplied (suboptimal) visual repair based on the largely cropped visual locator");
			System.out.println("[LOG]\tRepaired element is " + webElementFromVisualLocatorLarge);
			webElementFromDomLocator = webElementFromVisualLocatorLarge;

		} else if (webElementFromVisualLocatorPerfect != null && webElementFromVisualLocatorLarge != null) {

			/* there is disagreement between the visual locators. */
			if (!UtilsVisualRepair.areWebElementsEquals(webElementFromVisualLocatorPerfect,
					webElementFromVisualLocatorLarge)) {

				/* might be the wrong element. */
				System.out.println("[LOG]\tThe two visual locators target two different elements");
				System.out.println("[LOG]\tApplying proximity procedure (TODO!!!)");
				System.out.println("[LOG]\tApplied (suboptimal) visual repair");

				/*
				 * TODO: need to save the XPath of each web element during recording, in order
				 * // to apply proximity voting!!! webElementFromDomLocator =
				 * UtilsVisualRepair.applyProximityVoting((JavascriptExecutor) driver,
				 * webElementFromDomLocator, webElementFromVisualLocatorPerfect,
				 * webElementFromVisualLocatorLarge);
				 */

				webElementFromDomLocator = webElementFromVisualLocatorLarge;

			} else {

				/* any of the visual locator is ok. */
				System.out.println("[LOG]\tThe two visual locators target the same element");
				System.out.println("[LOG]\tApplied (optimal) visual repair");
				System.out.println("[LOG]\tRepaired element is " + webElementFromVisualLocatorPerfect);
				webElementFromDomLocator = webElementFromVisualLocatorPerfect;

			}

		}

		return webElementFromDomLocator;

	}

	public static WebElement applyProximityVoting(JavascriptExecutor js, WebElement webElementFromDomLocator,
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
			return webElementFromVisualLocatorLarge;
		}
	}

	public static WebElement retrieveWebElementFromDomLocator(WebDriver driver, SeleniumLocator domSelector) {

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

	public static WebElement retrieveWebElementFromVisualLocator(WebDriver driver, String visualLocator) {

		String currentScreenshot = System.getProperty("user.dir") + Settings.separator + "currentScreenshot.png";
		UtilsComputerVision.saveScreenshot(driver, currentScreenshot);

		Point bestMatch = UtilsComputerVision.findBestMatchCenter(currentScreenshot, visualLocator); 
				//UtilsTemplateMatching.siftAndMultipleTemplateMatching(currentScreenshot, visualLocator, 0.95);

		String xpathForMatches = UtilsXPath.getXPathFromLocation(bestMatch, driver);
		// System.out.println("XPath for match: " + xpathForMatches);

		WebElement fromVisual = driver.findElement(By.xpath(xpathForMatches));
		return fromVisual;
		
//		if (bestMatch == null) {
//			//return null;
//			bestMatch = UtilsComputerVision.findBestMatchCenter(currentScreenshot, visualLocator);
//		}
		
//		if (bestMatch == null) {
//			return null;
//		} else {
//			
//			String xpathForMatches = UtilsXPath.getXPathFromLocation(bestMatch, driver);
//			// System.out.println("XPath for match: " + xpathForMatches);
//
//			WebElement fromVisual = driver.findElement(By.xpath(xpathForMatches));
//			return fromVisual;
//		}

	}

	public static boolean areWebElementsEquals(WebElement webElementFromDomLocator,
			WebElement webElementFromVisualLocator) {

		return webElementFromDomLocator.equals(webElementFromVisualLocator);

	}

	public static WebElement removeStatement() {
		return null;
	}

	public static WebElement localCrawling() {
		return null;
	}

}
