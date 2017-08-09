package visualrepair;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.opencv.core.Point;
import org.openqa.selenium.WebDriver;
import org.xml.sax.SAXException;

import config.Settings;
import datatype.*;
import utils.UtilsRepair;
import utils.UtilsScreenshots;

public class ElementRelocatedSameState {

	static {
		nu.pattern.OpenCV.loadShared();
	}

	private static Scanner scanner = new Scanner(System.in);

	static List<EnhancedTestCase> searchLocatorWithinTheSameState(EnhancedException e, EnhancedTestCase b,
			EnhancedTestCase c) throws SAXException, IOException {

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

		WebDriverSingleton instance = WebDriverSingleton.getInstance();
		instance.loadPage("file:///" + htmlFile);
		WebDriver driver = instance.getDriver();

		/* extra check for the cases when the authentication is needed. */
		System.out.println("Is the web page correctly displayed? [type Y and Enter key to proceed]");
		while (!scanner.next().equals("Y")) {
		}

		long startTime = System.currentTimeMillis();
		
		/* get the screenshot of the web page in the new version. */
		String currentScreenshot = System.getProperty("user.dir") + Settings.separator + "currentScreenshot.png";
		UtilsScreenshots.saveScreenshot(driver, currentScreenshot);

		/* find the best visual match. */
		Point match = UtilsScreenshots.findBestMatchCenter(currentScreenshot, template);

		/* build the RTree for the web page. */
		HtmlDomTreeWithRTree rt = new HtmlDomTreeWithRTree(driver, htmlFile);
		rt.buildHtmlDomTree();
		// rt.preOrderTraversalRTree();

		/* search the web element in the RTree. */
		List<Node<HtmlElement>> result = rt.searchRTreeByPoint((int) match.x, (int) match.y);

		if (Settings.VERBOSE) {
			System.out.println(result.size() + " candidate(s) element found");
		}
		
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		System.out.println("Repairs found in: " + elapsedTime / 1000);

		List<EnhancedTestCase> candidateRepairs = new LinkedList<EnhancedTestCase>();

		for (Node<HtmlElement> htmlElement : result) {

			SeleniumLocator newlocator = null;

			if (htmlElement.getData().getTagName().equals("option")) {
				Node<HtmlElement> option = htmlElement.getParent();
				newlocator = new SeleniumLocator("xpath", option.getData().getXPath());
				newst.setDomLocator(newlocator);
			} else {
				newlocator = new SeleniumLocator("xpath", htmlElement.getData().getXPath());
				newst.setDomLocator(newlocator);
			}
			
			newst.setDomLocator(newlocator);

			EnhancedTestCase temp = UtilsRepair.copyTest(b);
			temp.addAndReplaceStatement(brokenStatementLine, newst);
			candidateRepairs.add(temp);

		}

		return candidateRepairs;

	}

}
