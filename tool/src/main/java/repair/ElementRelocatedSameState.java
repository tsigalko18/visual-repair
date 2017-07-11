package main.java.repair;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.opencv.core.Point;
import org.openqa.selenium.WebDriver;
import org.xml.sax.SAXException;

import main.java.config.Settings;
import main.java.datatype.EnhancedException;
import main.java.datatype.EnhancedTestCase;
import main.java.datatype.HtmlDomTreeWithRTree;
import main.java.datatype.HtmlElement;
import main.java.datatype.Node;
import main.java.datatype.Statement;
import main.java.datatype.WebDriverSingleton;
import main.java.utils.UtilsParser;
import main.java.utils.UtilsScreenshots;
import main.java.config.*;

public class ElementRelocatedSameState {

	static List<HtmlElement> repairs;
	static EnhancedTestCase broken;
	static EnhancedTestCase correct;
	static EnhancedException ex;
	
	static List<HtmlElement> searchLocatorWithinTheSameState(EnhancedException e, EnhancedTestCase b, EnhancedTestCase c) throws SAXException, IOException {
		
		System.out.println("[LOG]\tApplying repair strategy <searchLocatorWithinTheSameState>");
		
		// get the broken statement
		Statement oldst = c.getStatements().get(Integer.parseInt(e.getInvolvedLine()));

		// get the correct statement in the correct version
		Statement newst = b.getStatements().get(Integer.parseInt(e.getInvolvedLine()));
		
		// get the visual locator on the old page
		String template = oldst.getVisualLocator().toString();
		
		String htmlFile;
		if(oldst.getDomBefore() == null){
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
//		rt.preOrderTraversalRTree();
		
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		System.out.println("RTree built in: " + elapsedTime / 1000);

		// search element in the RTree
		List<Node<HtmlElement>> result = rt.searchRTreeByPoint((int) match.x, (int) match.y);
		
		if(Settings.VERBOSE)
			UtilsParser.printResults(result, rt);
		
		List<HtmlElement> rep = new LinkedList<HtmlElement>();
		for (Node<HtmlElement> htmlElement : result) {
			rep.add(htmlElement.getData());
		}
		
		return rep;
	} 
	
}
