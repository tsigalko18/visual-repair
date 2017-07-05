package main.java.repair;

import java.awt.AWTException;
import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

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
import main.java.utils.UtilsParser;
import main.java.utils.UtilsRepair;
import main.java.utils.UtilsScreenshots;

public class VisualRepair {

	static EnhancedException exception;
	static String htmlpage; 

	public static void main(String[] args) throws IOException, SAXException, HeadlessException, AWTException {

		String brokenTest = "TestLoginAdmin";
		htmlpage = "index.php.html";
		
		String path = Settings.pathToTestSuiteUnderTest + brokenTest + Settings.javaExtension;
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

		Settings.inRecordingMode = false;
		
		EnhancedTestCase brokenTestCase = ParseTest.parse(path);
		
		Settings.inRecordingMode = true;

		path = Settings.pathToReferenceTestSuite + TestLoginAdmin.class.getSimpleName() + Settings.javaExtension;

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

		File originalFile = new File("testSuite/" + brokenTest + "/original.png");
		File imageFile = new File(image);

		FileUtils.copyFile(imageFile, originalFile);

		String currentDirectory = System.getProperty("user.dir");
		String string = brokenTestCase.getStatements().get(Integer.parseInt(exception.getInvolvedLine()))
				.getAnnotatedScreenshot().toString();

		String dir = currentDirectory + Settings.separator + string.replace(".png", "").replace("Annotated", "2after");
		String htmlFile = dir + Settings.separator + htmlpage;

		WebDriverSingleton instance = WebDriverSingleton.getInstance();
		instance.loadPage("file:///" + htmlFile);
		WebDriver driver = instance.getDriver();
		
		System.out.println("Type anything to proceed further");
		Scanner scanner = new Scanner(System.in);
		scanner.next();

		// screenshot here
		String currentScreenshot = currentDirectory + Settings.separator + "currentScreenshot.png";
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
		// printResults(result, rt);

		WebDriverSingleton.closeDriver();
		FileUtils.copyFile(originalFile, new File(image));

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
