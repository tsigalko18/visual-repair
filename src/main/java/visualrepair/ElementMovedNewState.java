package visualrepair;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.opencv.core.Point;
import org.openqa.selenium.WebDriver;
import org.xml.sax.SAXException;

import config.Settings;
import datatype.EnhancedException;
import datatype.EnhancedTestCase;
import datatype.HtmlDomTreeWithRTree;
import datatype.HtmlElement;
import datatype.Node;
import datatype.Statement;
import datatype.WebDriverSingleton;
import utils.PHash;
import utils.UtilsGetters;
import utils.UtilsRepair;
import utils.UtilsScreenshots;
import vision.ImageProcessing;

public class ElementMovedNewState {
	
	static {
		nu.pattern.OpenCV.loadShared();
	}

	static List<HtmlElement> searchElementNewState(EnhancedException e, EnhancedTestCase b, EnhancedTestCase c)
			throws SAXException, IOException {

		System.out.println("[LOG]\tApplying repair strategy <searchElementNewState>");

		// detect the broken statement visually
		int brokenStatementLine = detectMismatchVisually(c, b, e);

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
		scanner.close();

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
			// UtilsParser.printResults(result, rt);
		}

		List<HtmlElement> rep = new LinkedList<HtmlElement>();
		for (Node<HtmlElement> htmlElement : result) {
			rep.add(htmlElement.getData());
		}

		return rep;
	}
	
	private static int detectMismatchVisually(EnhancedTestCase correct, EnhancedTestCase broken, EnhancedException e)
			throws IOException {

		// get the visual information for the correct test
		String test = Settings.referenceTestSuiteVisualTraceExecutionFolder + UtilsRepair.capitalizeFirstLetter(correct.getName());
		File[] correctVisualTrace = UtilsGetters.getAnnotatedScreenshots(test);
		File[] correctVisualLocators = UtilsGetters.getVisualLocators(test);

		// get the visual information for the broken test
		test = Settings.testingTestSuiteVisualTraceExecutionFolder + UtilsRepair.capitalizeFirstLetter(broken.getName());
		File[] brokenVisualTrace = UtilsGetters.getAnnotatedScreenshots(test);
		File[] brokenVisualLocators = UtilsGetters.getVisualLocators(test);

		int brokenStateFromExeception = Integer.parseInt(e.getInvolvedLine());
		String path = System.getProperty("user.dir") + Settings.separator;
		ImageProcessing ip = new ImageProcessing();

		int min = Math.min(brokenVisualLocators.length, brokenVisualTrace.length);

		Map<String, Double> statesSimilarityMap = new HashMap<String, Double>();
		Map<String, Double> visualLocatorsSimilarityMap = new HashMap<String, Double>();

		for (int i = 0; i < min; i++) {

			String state = correctVisualTrace[i].getName().substring(0, 2);
			
			statesSimilarityMap.put(state, ip.compareImagesByHistogram(path + correctVisualTrace[i].getPath(),
							path + brokenVisualTrace[i].getPath()));
			
			visualLocatorsSimilarityMap.put(state, PHash.getPHashSimiliarity(path + correctVisualLocators[i].getPath(),
							path + brokenVisualLocators[i].getPath()));
			
			
//			System.out.println("Sim\t" + correctVisualTrace[i].getName() + "\t" + brokenVisualTrace[i].getName() + ":\t"
//					+ ip.compareImagesByHistogram(path + correctVisualTrace[i].getPath(),
//							path + brokenVisualTrace[i].getPath()));

//			System.out.println("PHash\t" + correctVisualTrace[i].getName() + "\t" + brokenVisualTrace[i].getName()
//					+ ":\t" + TestPHash.getPHashSimiliarity(path + correctVisualTrace[i].getPath(),
//							path + brokenVisualTrace[i].getPath()));
//
//			System.out.println();
		}
		
		System.out.println(statesSimilarityMap);
	
		System.out.println(visualLocatorsSimilarityMap);
		
		return Integer.parseInt(getStateWithMinimumSimilarityScore(visualLocatorsSimilarityMap));
		
	}

	private static String getStateWithMinimumSimilarityScore(Map<String, Double> visualLocatorsSimilarityMap) {
		return Collections.min(visualLocatorsSimilarityMap.entrySet(), Comparator.comparingDouble(Entry::getValue)).getKey();
	}

}
