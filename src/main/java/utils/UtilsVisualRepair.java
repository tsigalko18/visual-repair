package utils;

import org.opencv.core.Point;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import config.Settings;
import datatype.EnhancedTestCase;
import datatype.SeleniumLocator;

public class UtilsVisualRepair {

	public static WebElement searchWithinTheSameState(WebDriver driver, WebElement webElementFromDomLocator,
			EnhancedTestCase testCorrect, Integer i) {
	
		String visualLocatorPerfect = null;
		String visualLocatorLarge = null;
		WebElement webElementFromVisualLocatorPerfect = null;
		WebElement webElementFromVisualLocatorLarge = null;
	
		visualLocatorPerfect = testCorrect.getStatements().get(i).getVisualLocatorPerfect().toString();
		visualLocatorLarge = testCorrect.getStatements().get(i).getVisualLocatorLarge().toString();
	
		webElementFromVisualLocatorPerfect = UtilsVisualRepair.retrieveWebElementFromVisualLocator(driver, visualLocatorPerfect);
		webElementFromVisualLocatorLarge = UtilsVisualRepair.retrieveWebElementFromVisualLocator(driver, visualLocatorLarge);
	
		/* there is disagreement between the visual locators. */
		if (!UtilsVisualRepair.areWebElementsEquals(webElementFromVisualLocatorPerfect, webElementFromVisualLocatorLarge)) {
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
		UtilsScreenshots.saveScreenshot(driver, currentScreenshot);
	
		Point matches = UtilsScreenshots.findBestMatchCenter(currentScreenshot, visualLocator);
		// returnAllMatches(currentScreenshot, template);
	
		// System.out.println(matches);
	
		String xpathForMatches = UtilsXPath.getXPathFromLocation(matches, driver);
	
		// System.out.println("XPath for match: " + xpathForMatches);
		WebElement fromVisual = driver.findElement(By.xpath(xpathForMatches));
		return fromVisual;
	}

	public static boolean areWebElementsEquals(WebElement webElementFromDomLocator,
			WebElement webElementFromVisualLocator) {
	
		return webElementFromDomLocator.equals(webElementFromVisualLocator);
	
	}

	public static WebElement removeStatement() {
		// TODO Auto-generated method stub
		return null;
	}

	public static WebElement localCrawling() {
		// TODO Auto-generated method stub
		return null;
	}

	
	
}
