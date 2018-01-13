package utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.openqa.selenium.By;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import config.Settings;
import datatype.EnhancedTestCase;
import datatype.SeleniumLocator;
import datatype.Statement;

public class UtilsVisualRepair {

	/**
	 * Procedure to verify a DOM element locator by means of a visual locator.
	 */
	public static WebElement visualAssertWebElement(WebDriver driver, WebElement webElementFromDomLocator,
			EnhancedTestCase testCorrect, Integer i) {

		Statement statement = null;
		WebElement webElementFromVisualLocator = null;

		/*
		 * try to retrieve the visual locator and raises an exception if it does not
		 * exist on the filesystem.
		 */
		try {

			testCorrect.getStatements().get(i).getVisualLocator().toString();
			statement = testCorrect.getStatements().get(i);

		} catch (NullPointerException e) {

			System.out.println("[ERROR]\tVisual locator not found in " + testCorrect.getStatements().get(i));
			System.out.println("[ERROR]\tRe-run the TestSuiteRunner on " + testCorrect.getPath());
			System.exit(1);

		}

		/* retrieve the web element visually. */
		webElementFromVisualLocator = UtilsVisualRepair.retrieveWebElementFromVisualLocator(driver, statement);

		if (webElementFromVisualLocator == null && webElementFromDomLocator == null) {

			System.err.println(
					"[LOG]\tElement not found by either DOM or visual locators. Visual assertion failed. Stopping execution");
			//System.exit(1);

		} else if (webElementFromVisualLocator == null) {

			System.err.println("[LOG]\tElement not found (visually) in the state. Visual assertion failed.");

		} else if (webElementFromDomLocator == null) {

			System.out.println("[LOG]\tApplied visual repair");
			System.out.println("[LOG]\tNew repaired element is " + webElementFromVisualLocator);
			webElementFromDomLocator = webElementFromVisualLocator;

		} else if (!UtilsVisualRepair.areWebElementsEquals(webElementFromDomLocator, webElementFromVisualLocator)) {

			System.out.println("[LOG]\tChance of propagated breakage at line " + i);
			System.out.println("[LOG]\tDOM locator and visual locator target two different elements");

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

	public static WebElement retrieveWebElementFromVisualLocator(WebDriver driver, Statement statement) {

		String visualLocator = statement.getVisualLocator().toString();
		String currentScreenshot = System.getProperty("user.dir") + Settings.separator + "currentScreenshot.png";
		UtilsComputerVision.saveScreenshot(driver, currentScreenshot);

		Point bestMatch = null;

		if (Settings.HYBRID) {

			Set<Point> allMatches = UtilsTemplateMatching.featureDetectorAndTemplateMatching_dom(currentScreenshot,
					visualLocator);

			WebElement res = getBestMatch(allMatches, driver, statement);
			return res;

		} else {

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

	private static boolean insideSeenRectangles(Point p, List<Rect> rects) {
		for (Rect seenRect : rects) {
			if (p.inside(seenRect))
				return true;
		}
		return false;
	}

	/**
	 * Filter the results obtained by the visual locators.
	 * 
	 * @param allMatches
	 * @param driver
	 * @param statement
	 * @return
	 */
	private static WebElement getBestMatch(Set<Point> allMatches, WebDriver driver, Statement statement) {

		if (allMatches == null)
			return null;

		List<Rect> seenRectangles = new ArrayList<Rect>();
		List<WebElement> distinctWebElements = new ArrayList<WebElement>();

		for (Point match : allMatches) {

			if (insideSeenRectangles(match, seenRectangles))
				continue;

			String xpathForMatch = UtilsXPath.getXPathFromLocation(match, driver);
			//System.out.println(xpathForMatch);
			WebElement webElementForMatch = driver.findElement(By.xpath(xpathForMatch));
			
			// Consider only the leaf elements
			if(UtilsXPath.isLeaf(webElementForMatch)) {
				// check if other points belong to this rectangle
				Rectangle rect = webElementForMatch.getRect();
	
				Rect r = new Rect(rect.x, rect.y, rect.width, rect.height);
	
				seenRectangles.add(r);
				distinctWebElements.add(webElementForMatch);
			}
		}

		/*
		 * Filter results obtained by the visual locators with DOM information. An
		 * alternative might be calculate a similarity score.
		 */

		/* filter by id. */
		List<WebElement> filtered_id = new ArrayList<WebElement>();
		String idattr = statement.getId();
		for (WebElement distinct : distinctWebElements) {
			String id = distinct.getAttribute("id");
			if(id != null) {
				if (id.equalsIgnoreCase(idattr))
					filtered_id.add(distinct);
			}
		}
		if (filtered_id.size() == 1)
			return filtered_id.get(0);

		/* filter by textual content. */
		String textContent = statement.getText();
		List<WebElement> filtered_text = new ArrayList<WebElement>();
		if (!textContent.trim().isEmpty()) {
			for (WebElement elem : distinctWebElements) {
				if (elem.getAttribute("textContent").trim().equalsIgnoreCase(textContent))
					filtered_text.add(elem);
			}
		}
		if (filtered_text.size() == 1)
			return filtered_text.get(0);


		/* filter by name. */
		List<WebElement> filtered_name = new ArrayList<WebElement>();
		String nameattr = statement.getName();
		for (WebElement distinct : distinctWebElements) {
			String name = distinct.getAttribute("name");
			if(name!=null) {
				if (name.equalsIgnoreCase(nameattr))
					filtered_name.add(distinct);
			}
		}
		if (filtered_name.size() == 1)
			return filtered_name.get(0);

		
		
		/* filter by class. */
		List<WebElement> filtered_class = new ArrayList<WebElement>();
		String classattr = statement.getClassAttribute();
		for (WebElement distinct : distinctWebElements) {
			String clazz= distinct.getAttribute("class");
			if(clazz != null) {
				if (clazz.equalsIgnoreCase(classattr))
					filtered_class.add(distinct);
			}
		}
		if (filtered_class.size() == 1)
			return filtered_class.get(0);


		/* filter by XPath. */
		List<WebElement> filtered_xpath = new ArrayList<WebElement>();
		String xpath = statement.getXpath();
		for (WebElement distinct : distinctWebElements) {
			String xp = UtilsXPath.generateXPathForWebElement(distinct, "");
			if (xp.equalsIgnoreCase(xpath))
				filtered_xpath.add(distinct);
		}
		if (filtered_xpath.size() == 1)
			return filtered_xpath.get(0);

		/* filter by tag name. */
		List<WebElement> filtered_tagName = new ArrayList<WebElement>();
		String tagName = statement.getTagName();
		for (WebElement distinct : distinctWebElements) {
			if (distinct.getTagName().equalsIgnoreCase(tagName))
				filtered_tagName.add(distinct);
		}
		if (filtered_tagName.size() == 1)
			return filtered_tagName.get(0);
		
		/* if none of the filters has been applied, null is returned. */
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
