package main.java.visualrepair;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.opencv.core.Point;
import org.openqa.selenium.WebDriver;
import org.xml.sax.SAXException;

import main.java.config.Settings;
import main.java.datatype.*;
import main.java.utils.UtilsRepair;
import main.java.utils.UtilsScreenshots;

public class ElementRelocatedSameState {
	
	static {
		nu.pattern.OpenCV.loadShared();
	}

	static List<HtmlElement> searchLocatorWithinTheSameState(EnhancedException e, EnhancedTestCase b,
			EnhancedTestCase c) throws SAXException, IOException {

		System.out.println("[LOG]\tApplying visual repair strategy <searchLocatorWithinTheSameState>");

		// read the broken statement line from the exception
		int brokenStatementLine = Integer.parseInt(e.getInvolvedLine());

		// get the broken statement
		Statement oldst = c.getStatements().get(brokenStatementLine);

		// get the correct statement in the correct version
		Statement newst = b.getStatements().get(brokenStatementLine);

		// get the visual locator on the old page
		String template = oldst.getVisualLocator().toString();

		String htmlFile;
		if (oldst.getDomBefore() == null) {
			htmlFile = oldst.getDomAfter().getAbsolutePath();
		} else
			htmlFile = oldst.getDomBefore().getAbsolutePath();

		WebDriverSingleton instance = WebDriverSingleton.getInstance();
		instance.loadPage("file:///" + htmlFile);
		WebDriver driver = instance.getDriver();

		System.out.println("If the page is correctly displayed, type anything to proceed further");
		Scanner scanner = new Scanner(System.in);
		scanner.next();

		// screenshot here
		String currentScreenshot = System.getProperty("user.dir") + Settings.separator + "currentScreenshot.png";
		UtilsScreenshots.saveScreenshot(driver, currentScreenshot);

		// find best visual match
		Point match = UtilsScreenshots.findBestMatchCenter(currentScreenshot, template);

		long startTime = System.currentTimeMillis();

		// build RTree for the HTML page
		HtmlDomTreeWithRTree rt = new HtmlDomTreeWithRTree(driver, htmlFile);
		rt.buildHtmlDomTree();
		// rt.preOrderTraversalRTree();

		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		System.out.println("RTree built in: " + elapsedTime / 1000);

		// search element in the RTree
		List<Node<HtmlElement>> result = rt.searchRTreeByPoint((int) match.x, (int) match.y);

		if (Settings.VERBOSE) {
			System.out.println(result.size() + " candidate(s) element found");
		}

		List<HtmlElement> rep = new LinkedList<HtmlElement>();
		
		for (Node<HtmlElement> htmlElement : result) {
			rep.add(htmlElement.getData());
				
			// print repaired test cases
			SeleniumLocator newlocator = null; 
			
			if(result.get(0).getData().getTagName().equals("option")){
				Node<HtmlElement> option = result.get(0).getParent();
				newlocator = new SeleniumLocator("xpath", option.getData().getXPath());
				newst.setDomLocator(newlocator);
			} else {
				newlocator = new SeleniumLocator("xpath", result.get(0).getData().getXPath());
				newst.setDomLocator(newlocator);
			}
			
			newst.setDomLocator(newlocator);
			b.addStatement(Integer.parseInt(e.getInvolvedLine()), newst);

			UtilsRepair.printTestCase(b);
		}
		
		return rep;
	}

}
