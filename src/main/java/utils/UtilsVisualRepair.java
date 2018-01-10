package utils;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.opencv.core.Point;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import config.Settings;
import datatype.EnhancedTestCase;
import datatype.SeleniumLocator;

public class UtilsVisualRepair {

	/**
	 * Procedure to verify a DOM element locator by means of a visual locator.
	 */
	public static WebElement visualAssertWebElement(WebDriver driver, WebElement webElementFromDomLocator,
			EnhancedTestCase testCorrect, Integer i) {

		String visualLocator = null;
		WebElement webElementFromVisualLocator = null;

		/* retrieve the visual locator. */
		try {
			visualLocator = testCorrect.getStatements().get(i).getVisualLocator().toString();
		} catch (NullPointerException e) {
			System.out.println("[ERROR]\tVisual locator not found in " + testCorrect.getStatements().get(i));
			System.out.println("[ERROR]\tRe-run the TestSuiteRunner on " + testCorrect.getPath());
			System.exit(1);
		}

		/* retrieve the web element visually. */
		webElementFromVisualLocator = UtilsVisualRepair.retrieveWebElementFromVisualLocator(driver, visualLocator);

		if (webElementFromVisualLocator == null) {

			System.err.println("[LOG]\tElement not found (visually) in the state. Visual assertion failed.");

		} else if (webElementFromDomLocator == null) {

			System.out.println("[LOG]\tApplied visual repair");
			System.out.println("[LOG]\tNew repaired element is " + webElementFromVisualLocator);
			webElementFromDomLocator = webElementFromVisualLocator;

		} else if (!UtilsVisualRepair.areWebElementsEquals(webElementFromDomLocator, webElementFromVisualLocator)) {

			System.out.println("[LOG]\tChance of propagated breakage at line " + i);
			System.out.println("[LOG]\tDOM locator and visual locator target two different elements");

			// System.out.println(webElementFromVisualLocator);
			// System.out.println(webElementFromDomLocator);

			/* I trust the element found by the visual locator. Is that correct? */
			System.out.println("[LOG]\tApplied visual repair");
			System.out.println("[LOG]\tNew repaired element is " + webElementFromVisualLocator);
			webElementFromDomLocator = webElementFromVisualLocator;

		} else {

			System.out.println("[LOG]\tDOM locator and visual locator target the same element");
			System.out.println("[LOG]\tVisual verification succeeded");

		}

		return webElementFromDomLocator;
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

		// Point bestMatch = UtilsComputerVision.findBestMatchCenter(currentScreenshot,
		// visualLocator);
		Point bestMatch = null;
		if(Settings.HYBRID) {
			List<Point> allMatches = UtilsTemplateMatching.featureDetectorAndTemplateMatching_dom(currentScreenshot, visualLocator);
			bestMatch = getBestMatch(allMatches);
		}
		else {
			bestMatch = UtilsTemplateMatching.featureDetectorAndTemplateMatching(currentScreenshot, visualLocator);
		}
		
		if (bestMatch == null) {

			FileUtils.deleteQuietly(new File(currentScreenshot));
			return null;

		} else {

			String xpathForMatches = UtilsXPath.getXPathFromLocation(bestMatch, driver);
			System.out.println("XPath for match: " + xpathForMatches);
			WebElement fromVisual = driver.findElement(By.xpath(xpathForMatches));

			FileUtils.deleteQuietly(new File(currentScreenshot));

			return fromVisual;
		}

	}

	private static Point getBestMatch(List<Point> allMatches) {
		// TODO Auto-generated method stub
		if(allMatches == null)
			return null;
		
		return null;
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
