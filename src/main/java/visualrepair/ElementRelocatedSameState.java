package visualrepair;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.opencv.core.Point;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.xml.sax.SAXException;

import config.Settings;
import datatype.EnhancedException;
import datatype.EnhancedTestCase;
import datatype.HtmlDomTreeWithRTree;
import datatype.HtmlElement;
import datatype.Node;
import datatype.SeleniumLocator;
import datatype.Statement;
import datatype.WebDriverSingleton;
import utils.UtilsRepair;
import utils.UtilsScreenshots;

public class ElementRelocatedSameState {

	static {
		nu.pattern.OpenCV.loadShared();
	}

	private static Scanner scanner = new Scanner(System.in);

	static List<EnhancedTestCase> searchLocatorWithinTheSameState(EnhancedException e, EnhancedTestCase b,
			EnhancedTestCase c, boolean check) throws SAXException, IOException, CloneNotSupportedException {

		System.out.println("[LOG]\tApplying visual repair strategy <searchLocatorWithinTheSameState>");

		/* get the line responsible for the breakage. */
		int brokenStatementLine = Integer.parseInt(e.getInvolvedLine());

		/* get the statement in the correct test case. */
		Statement oldst = c.getStatements().get(brokenStatementLine);

		/* get the statement in the broken test case. */
		Statement newst = b.getStatements().get(brokenStatementLine);

		/* get the visual locator of the statement in the correct test case. */
		String template = oldst.getVisualLocator().toString();

		/* open the web page of the new version. */
		String htmlFile;
		if (newst.getDomBefore() == null) {
			htmlFile = newst.getDomAfter().getAbsolutePath();
		} else
			htmlFile = newst.getDomBefore().getAbsolutePath();
		
		/* html page to be cleaned. */
		String theHtmlPage = newst.getDomAfter().getName();
		
		String htmlFileCleaned = htmlFile.toString();
		/* encode URL. */
		theHtmlPage = java.net.URLEncoder.encode(theHtmlPage, "UTF-8");
		
		htmlFileCleaned = htmlFileCleaned.substring(0, htmlFile.lastIndexOf("/") + 1);
		htmlFileCleaned = htmlFileCleaned.concat(theHtmlPage);
		
		WebDriverSingleton instance = WebDriverSingleton.getInstance();
		instance.loadPage("file:///" + htmlFileCleaned);
		WebDriver driver = instance.getDriver();

		HtmlDomTreeWithRTree rt = null;

		/* build the RTree for the web page. */
		if (check) {
			/* extra check for the cases when the authentication is needed. */
			System.out.println("Is the web page correctly displayed? [type Y and Enter key to proceed]");
			while (!scanner.next().equals("Y")) {
			}

			String newFileName = htmlFile.replace(".html", "-temp.html");

			File newPageSource = new File(newFileName);
			try {
				FileUtils.write(newPageSource, driver.getPageSource());
			} catch (UnhandledAlertException ex) {

				// TODO: try to understand how to manage the errors after the popup box
//				driver.switchTo().alert().accept();
			}

			rt = new HtmlDomTreeWithRTree(driver, newFileName);
			rt.buildHtmlDomTree();

			FileUtils.deleteQuietly(newPageSource);

		} else {
			rt = new HtmlDomTreeWithRTree(driver, htmlFile);
			rt.buildHtmlDomTree();
		}

		/* get the screenshot of the web page in the new version. */
		String currentScreenshot = System.getProperty("user.dir") + Settings.separator + "currentScreenshot.png";
		UtilsScreenshots.saveScreenshot(driver, currentScreenshot);
		
		WebDriverSingleton.closeDriver();

		/* find the best visual matches. */
		List<Point> matches = UtilsScreenshots.returnAllMatches(currentScreenshot, template);

//		List<Point> matches = new LinkedList<Point>();
//		matches.add(UtilsScreenshots.findBestMatchCenter(currentScreenshot, template));

		/* find the corresponding rectangles. */
		Set<Node<HtmlElement>> results = new HashSet<Node<HtmlElement>>();
//		List<Node<HtmlElement>> results = new ArrayList<Node<HtmlElement>>();
		for (Point point : matches) {
			results.addAll(rt.searchRTreeByPoint((int) point.x, (int) point.y));
		}

		if (Settings.VERBOSE) {
			System.out.println(results.size() + " candidate(s) element found");
			for (Node<HtmlElement> node : results) {
				System.out.println(node.getData().getXPath());
			}
		}

		List<EnhancedTestCase> candidateRepairs = new LinkedList<EnhancedTestCase>();

		/* for each element, generate all possible locators. */
		for (Node<HtmlElement> htmlElement : results) {

			List<SeleniumLocator> locators = new LinkedList<SeleniumLocator>();

			// generate a set of locators for the element, if possible
			if (htmlElement.getData().getTagName().equals("option")) {
				Node<HtmlElement> option = htmlElement.getParent();
				locators = UtilsRepair.generateAllLocators(option.getData());
			} else {
				locators = UtilsRepair.generateAllLocators(htmlElement.getData());
			}

			for (SeleniumLocator seleniumLocator : locators) {

				EnhancedTestCase temp = (EnhancedTestCase) UtilsRepair.deepClone(b);
				Statement newStatementWithNewLocator = temp.getStatements().get(brokenStatementLine);
				newStatementWithNewLocator.setDomLocator(seleniumLocator);
				temp.addAndReplaceStatement(brokenStatementLine, newStatementWithNewLocator);

				candidateRepairs.add(temp);

			}

		}

		return candidateRepairs;

	}

}
