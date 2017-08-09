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

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.WebDriver;
import org.xml.sax.SAXException;

import config.Settings;
import datatype.EnhancedException;
import datatype.EnhancedTestCase;
import datatype.EnhancedWebElement;
import datatype.HtmlDomTree;
import datatype.HtmlElement;
import datatype.Node;
import datatype.SeleniumLocator;
import datatype.Statement;
import datatype.WebDriverSingleton;
import utils.UtilsGetters;
import utils.UtilsParser;
import utils.UtilsRepair;
import vision.ImageProcessing;

public class ElementMovedNewState {

	static {
		nu.pattern.OpenCV.loadShared();
	}

	private static Scanner scanner = new Scanner(System.in);
	
	static List<EnhancedTestCase> searchElementNewState(EnhancedException e, EnhancedTestCase b, EnhancedTestCase c, boolean check)
			throws SAXException, IOException {

		System.out.println("[LOG]\tApplying repair strategy <searchElementNewState>");

		/*
		 * detect the broken statement visually TODO: now basically returns
		 * brokenStatement - 1
		 */
		int brokenStatementLine = detectMismatchVisually(c, b, e);

		/* load HTML page at brokenStatementLine. */

		// get the broken statement
		Statement newst = b.getStatements().get(brokenStatementLine);

		String htmlFile = newst.getDomAfter().getAbsolutePath();

		WebDriverSingleton instance = WebDriverSingleton.getInstance();
		instance.loadPage("file:///" + htmlFile);
		WebDriver driver = instance.getDriver();

		if (check) {
			/* extra check for the cases when the authentication is needed. */
			System.out.println("Is the web page correctly displayed? [type Y and Enter key to proceed]");
			while (!scanner.next().equals("Y")) {
			}
		}

		File newpage = new File(htmlFile);
		FileUtils.write(newpage, driver.getPageSource());

		// build RTree for the HTML page
		HtmlDomTree dt = new HtmlDomTree(driver, htmlFile);
		dt.buildHtmlDomTree();

		/* get list of clickable elements */
		List<Node<HtmlElement>> clickables = new LinkedList<Node<HtmlElement>>();
		UtilsParser.extractClickablesFromHtmlPage(dt, clickables);

		System.out.println("Found " + clickables.size() + " clickables");
		for (Node<HtmlElement> node : clickables) {
			System.out.println(node.getData().getXPath());
		}

		List<EnhancedTestCase> candidateRepairs = new LinkedList<EnhancedTestCase>();

		// System.out.println("ORIGINAL TEST");
		// UtilsRepair.printTestCaseWithLineNumbers(b);

		for (Node<HtmlElement> node : clickables) {
			
			int newStatementLine = brokenStatementLine + 1;
			Statement newStatement = new EnhancedWebElement();
			newStatement.setAction("click");
			newStatement.setValue("");
			newStatement.setLine(newStatementLine);
			newStatement.setDomLocator(new SeleniumLocator("xpath", node.getData().getXPath()));

			EnhancedTestCase temp = UtilsRepair.copyTest(b);
			temp.addStatementAtPosition(newStatementLine, newStatement);
			candidateRepairs.add(temp);
			
		}

		return candidateRepairs;
	}

	private static int detectMismatchVisually(EnhancedTestCase correct, EnhancedTestCase broken, EnhancedException e)
			throws IOException {

		// get the visual information for the correct test
		String test = Settings.referenceTestSuiteVisualTraceExecutionFolder
				+ UtilsRepair.capitalizeFirstLetter(correct.getName());
		File[] afterCorrectTrace = UtilsGetters.getAfterScreenshots(test);
		File[] beforeCorrectTrace = UtilsGetters.getBeforeScreenshots(test);

		Map<String, File> m = UtilsParser.convertToHashMap(afterCorrectTrace);

		// get the visual information for the broken test
		test = Settings.testingTestSuiteVisualTraceExecutionFolder
				+ UtilsRepair.capitalizeFirstLetter(broken.getName());
		File[] afterBrokenTrace = UtilsGetters.getAfterScreenshots(test);
		File[] beforeBrokenTrace = UtilsGetters.getBeforeScreenshots(test);

		int min = UtilsRepair.getMinimumValue(afterCorrectTrace, beforeCorrectTrace, afterBrokenTrace,
				beforeBrokenTrace);

		int brokenStateFromExeception = Integer.parseInt(e.getInvolvedLine());
		String path = System.getProperty("user.dir") + Settings.separator;
		ImageProcessing ip = new ImageProcessing();

		Map<String, Double> beforeStatesSimilarityMap = new HashMap<String, Double>();
		Map<String, Double> afterStatesSimilarityMap = new HashMap<String, Double>();

		String state = null;

		for (int i = 0; i < min; i++) {

			state = beforeCorrectTrace[i].getName().substring(0, 2);

			// System.out.println(state);
			// System.out.println("\t" + beforeCorrectTrace[i].getPath());
			// System.out.println("\t" + beforeBrokenTrace[i].getPath());
			// System.out.println("\tbefore:\t" + ip.compareImagesByHistogram(path +
			// beforeCorrectTrace[i].getPath(),
			// path + beforeBrokenTrace[i].getPath()));
			// System.out.println("\tafter:\t" + ip.compareImagesByHistogram(path +
			// afterCorrectTrace[i].getPath(),
			// path + afterBrokenTrace[i].getPath()));

			beforeStatesSimilarityMap.put(state, ip.compareImagesByHistogram(path + beforeCorrectTrace[i].getPath(),
					path + beforeBrokenTrace[i].getPath()));

			afterStatesSimilarityMap.put(state, ip.compareImagesByHistogram(path + afterCorrectTrace[i].getPath(),
					path + afterBrokenTrace[i].getPath()));

			// beforeStatesSimilarityMap.put(state, PHash.getPHashSimiliarity(path +
			// beforeCorrectTrace[i].getPath(),
			// path + beforeBrokenTrace[i].getPath()));
			//
			// afterStatesSimilarityMap.put(state, PHash.getPHashSimiliarity(path +
			// afterCorrectTrace[i].getPath(),
			// path + afterBrokenTrace[i].getPath()));

			// System.out.println(afterCorrectTrace[i]);
			// System.out.println(afterBrokenTrace[i] + "\n");
			//
			// System.out.println(beforeCorrectTrace[i]);
			// System.out.println(beforeBrokenTrace[i]);
		}

		// System.out.println(beforeStatesSimilarityMap);
		// System.out.println(afterStatesSimilarityMap);

		return Integer.parseInt(state);

	}

	private static String getStateWithMinimumSimilarityScore(Map<String, Double> visualLocatorsSimilarityMap) {
		return Collections.min(visualLocatorsSimilarityMap.entrySet(), Comparator.comparingDouble(Entry::getValue))
				.getKey();
	}

}
