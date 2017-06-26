package main.java.repair;

import java.awt.AWTException;
import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.opencv.core.Point;
import org.openqa.selenium.WebDriver;
import org.xml.sax.SAXException;

import main.java.claroline.TestLoginAdmin;
import main.java.clarolineDirectBreakage.DirectBreakage;
import main.java.config.Settings;
import main.java.datatype.*;
import main.java.parser.ParseTest;
import main.java.utils.*;

public class VisualRepair {

	static EnhancedException exception;

	public static void main(String[] args) throws IOException, SAXException, HeadlessException, AWTException {

		String path = Settings.buggyTestSuitePath + DirectBreakage.class.getSimpleName() + Settings.javaExtension;
		String jsonPath = UtilsParser.toJsonPath(path);

		if (new File(jsonPath).exists())
			exception = UtilsParser.readException(jsonPath);
		else {
			Result result1 = JUnitCore.runClasses(DirectBreakage.class);
			for (Failure fail : result1.getFailures()) {
				UtilsRepair.saveFailures(fail);
				EnhancedException ea = UtilsRepair.saveExceptionFromFailure(fail);
				UtilsParser.serializeException(ea, jsonPath);
			}
		}

		// step 3 : create test case model

		EnhancedTestCase brokenTestCase = ParseTest.parse(path);

		path = Settings.correctTestSuitePath + TestLoginAdmin.class.getSimpleName() + Settings.javaExtension;

		EnhancedTestCase correctTestCase = ParseTest.parse(path);

		// step 4 : root cause analysis
		// find the defect. Need to parse the Failure message

		// get the broken statement of the test
		Statement oldst = correctTestCase.getStatements().get(Integer.parseInt(exception.getInvolvedLine()));

		// get the correspondent statement in the correct version of the same
		// test
		Statement newst = brokenTestCase.getStatements().get(Integer.parseInt(exception.getInvolvedLine()));

		if (Settings.verbose) {
			System.out.println("Breakage at line " + exception.getInvolvedLine());
			System.out.println(exception.getMessage());
		}

		// get the screenshot of the new page
		String image = newst.getAnnotatedScreenshot().toString(); // "full2.png";

		// get the visual locator on the old page
		String template = oldst.getVisualLocator().toString(); // "template2.png";

		File originalFile = new File("testSuite/DirectBreakage/original.png");
		File imageFile = new File(image);

		FileUtils.copyFile(imageFile, originalFile);

		String currentDirectory = System.getProperty("user.dir");
		String string = brokenTestCase.getStatements().get(Integer.parseInt(exception.getInvolvedLine()))
				.getAnnotatedScreenshot().toString();

		String dir = currentDirectory + Settings.separator + string.replace(".png", "").replace("Annotated", "2after");
		String htmlFile = dir + Settings.separator + "index.html";

		WebDriverSingleton instance = WebDriverSingleton.getInstance();
		instance.loadPage("file:///" + htmlFile);
		WebDriver driver = instance.getDriver();

		// screenshot here
		String currentScreenshot = currentDirectory + Settings.separator + "currentScreenshot.png";
		UtilsScreenshots.saveScreenshot(driver, currentScreenshot);

		// find a list of visual matches using all matching algorithms
		// List<Point> matches =
		// UtilsScreenshots.returnAllMatchesForAllAlgorithms(currentScreenshot,
		// template);
		//
		// HtmlDomTree rt = new HtmlDomTree(driver, htmlFile);
		// rt.buildHtmlDomTree();
		// rt.preOrderTraversalRTree();
		//
		// for (Point match : matches) {
		//
		// System.out.println("Best match = (" + match.x + ", " + match.y +
		// ")");
		//
		// match = UtilsScreenshots.findBestMatchCenter(currentScreenshot,
		// template);
		// System.out.println("Best match center at = (" + match.x + ", " +
		// match.y + ")");
		//
		// List<Node<HtmlElement>> result = rt.searchRTreeByPoint((int) match.x,
		// (int) match.y);
		// printResults(result, rt);
		// }

		// find best visual match
		Point match = UtilsScreenshots.findBestMatchCenter(currentScreenshot, template);

		// build RTree for the HTML page
		HtmlDomTreeWithRTree rt = new HtmlDomTreeWithRTree(driver, htmlFile);
		rt.buildHtmlDomTree();
		rt.preOrderTraversalRTree();

		// search element in the RTree
		List<Node<HtmlElement>> result = rt.searchRTreeByPoint((int) match.x, (int) match.y);
		// printResults(result, rt);

		WebDriverSingleton.closeDriver();
		FileUtils.copyFile(originalFile, new File(image));

		SeleniumLocator newlocator = new SeleniumLocator("xpath", result.get(0).getData().getXPath());
		newst.setDomLocator(newlocator);

		brokenTestCase.addStatement(Integer.parseInt(exception.getInvolvedLine()), newst);

		printTestCase(brokenTestCase);

		// save back to Java and run it

	}

	private static void printTestCase(EnhancedTestCase tc) {
		for (Integer i : tc.getStatements().keySet()) {
			System.out.println(tc.getStatements().get(i));
		}
	}

	private static void printResults(List<Node<HtmlElement>> result, HtmlDomTreeWithRTree rt) {

		String s = "***** repairs list *****";
		System.out.println(s);
		for (Node<HtmlElement> node : result) {
			System.out.println(node.getData().getXPath());
			System.out.println(rt.getRects().get(node.getData().getRectId()));
		}
		for (int i = 0; i < s.length(); i++)
			System.out.print("*");
		System.out.print("\n");
	}

}
