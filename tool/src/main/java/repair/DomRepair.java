//package main.java.repair;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.List;
//import java.util.Scanner;
//
//import org.apache.commons.io.FileUtils;
//import org.junit.runner.JUnitCore;
//import org.junit.runner.Result;
//import org.junit.runner.notification.Failure;
//import org.openqa.selenium.WebDriver;
//import org.xml.sax.SAXException;
//
//import org.opencv.core.Point;
//
//import main.java.claroline.TestLoginAdmin;
//import claroline1811.*;
//import claroline190.*;
//import clarolineDirectBreakage.DirectBreakage;
//import config.Settings;
//import datatype.EnhancedException;
//import datatype.EnhancedTestCase;
//import datatype.HtmlDomTree;
//import datatype.HtmlDomTreeWithRTree;
//import datatype.HtmlElement;
//import datatype.Node;
//import datatype.SeleniumLocator;
//import datatype.Statement;
//import datatype.WebDriverSingleton;
//import parser.ParseTest;
//import utils.UtilsParser;
//import utils.UtilsRepair;
//import utils.UtilsScreenshots;
//import utils.UtilsWater;
//
//public class DomRepair {
//
//	static EnhancedException exception;
//	static String htmlpage; 
//	
//	public static void main(String[] args) throws IOException, SAXException {
//		
//		// path to the broken test case
//		String brokenTest = "DirectBreakage";
//		htmlpage = "index.php.html";
//		
//		String path = Settings.pathToTestSuiteUnderTest + brokenTest + Settings.javaExtension;
//		String jsonPath =  UtilsParser.toJsonPath(path);
//		
//		// do not run the test suite again, if the exception has already been saved
//		if(new File(jsonPath).exists())
//			exception = UtilsParser.readException(jsonPath);
//		else {
//			Result result1 = JUnitCore.runClasses(DirectBreakage.class);
//			for (Failure fail : result1.getFailures()) {
//				UtilsRepair.saveFailures(fail);
//				EnhancedException ea = UtilsRepair.saveExceptionFromFailure(fail);
//				UtilsParser.serializeException(ea, jsonPath);
//			}
//		}
//		
//		// path to the correct test case
//		path = Settings.pathToReferenceTestSuite + TestLoginAdmin.class.getSimpleName() + Settings.javaExtension;
//				
////		// get DOM old (correct version)
//		
//		EnhancedTestCase correctTestCase = ParseTest.parse(path);
//		String currentDirectory = System.getProperty("user.dir");
//		String string = correctTestCase.getStatements().get(Integer.parseInt(exception.getInvolvedLine())).getAnnotatedScreenshot().toString();
//		
//		String dir = currentDirectory + Settings.separator + string.replace(".png", "").replace("Annotated", "2after");
//		String htmlFile = dir + Settings.separator + htmlpage;
//		
//		datatype.WebDriverSingleton instance = WebDriverSingleton.getInstance();
//		instance.loadPage("file:///" + htmlFile);
//		WebDriver driver = instance.getDriver();
//		
//		HtmlDomTree oldDom = new HtmlDomTree(driver, htmlFile);
//		oldDom.buildHtmlDomTree();
//		oldDom.preOrderTraversalRTree();
//		
//		WebDriverSingleton.closeDriver();
//		
//		if (Settings.VERBOSE) {
//			System.out.println("Breakage at line " + exception.getInvolvedLine());
//			System.out.println(exception.getMessage());
//		}
//			
////		"/html/body/div[1]/div[2]/div[1]/div/form/fieldset/input[3]" 
////		"/html/body/div[1]/div[2]/div[1]/div/form/fieldset/button"
////		HtmlElement el = oldDom.searchHtmlDomTreeByXPath("/html/body/div[1]/div[2]/div[1]/div/form/fieldset/button");
////		HtmlElement el = oldDom.searchHtmlDomTreeByXPath("/html[1]/body[1]/div[1]/div[2]/div[1]/div[1]/form[1]/fieldset[1]/input[3]");
//				
////		System.out.println(el.getTagName() + "\t" + el.getXPath());
//
//		// get DOM new (broken version)
//		
//		path = Settings.pathToTestSuiteUnderTest + DirectBreakage.class.getSimpleName() + Settings.javaExtension;
//		EnhancedTestCase brokenTestCase = ParseTest.parse(path);
//		string = brokenTestCase.getStatements().get(Integer.parseInt(exception.getInvolvedLine())).getAnnotatedScreenshot().toString();
//		
//		dir = currentDirectory + Settings.separator + string.replace(".png", "").replace("Annotated", "2after");
//		htmlFile = dir + Settings.separator + "index.html";
//		
//		instance = WebDriverSingleton.getInstance();
//		instance.loadPage("file:///" + htmlFile);
//		driver = instance.getDriver();
//		
////		System.out.println("Type anything to proceed further");
////		Scanner scanner = new Scanner(System.in);
////		scanner.next();
//		
//		HtmlDomTree newDom = new HtmlDomTree(driver, htmlFile);
//		newDom.buildHtmlDomTree();
////		newDom.preOrderTraversalRTree();
//		
////		"/html/body/div[1]/div[2]/div[1]/div/form/fieldset/input[3]" 
////		"/html/body/div[1]/div[2]/div[1]/div/form/fieldset/button"
////		HtmlElement el = newDom.searchHtmlDomTreeByXPath("/html[1]/body[1]/div[1]/div[2]/div[1]/div[1]/form[1]/fieldset[1]/button[1]");
////		System.out.println(el.getTagName() + "\t" + el.getXPath());
//				
//		
////		HtmlElement el = UtilsWater.getNodesByProperty(oldDom, "value", "Enter");
////		System.out.println(el.getXPath());
////		System.out.println(UtilsWater.getNodeByLocator(oldDom, el.getXPath()).getXPath());
//				
////		HtmlElement el = oldDom.searchHtmlDomTreeByXPath("/html[1]/body[1]/div[1]/div[2]/div[1]/div[1]/form[1]/fieldset[1]/input[3]");
//
//				
////		System.out.println(UtilsWater.getNodesByProperty(newDom, "id", "login"));
////		System.out.println(UtilsWater.getNodeByLocator(newDom, "/html[1]/body[1]/div[1]/div[2]/div[1]/div[1]/form[1]/fieldset[1]/button[1]"));
//		
//		String locator = "/html[1]/body[1]/div[2]/table[1]/tbody[1]/tr[1]/td[2]/form[1]/fieldset[1]/input[1]";
//		
//		Water wtr = new Water(brokenTestCase, correctTestCase, exception);
//		wtr.suggestRepair(oldDom, locator, newDom);
//		
//		System.out.println(wtr.getRepairs());
//		
//		WebDriverSingleton.closeDriver();
//
//	}
//	
//	private static void printTestCase(EnhancedTestCase tc) {
//		for (Integer i : tc.getStatements().keySet()) {
//			System.out.println(tc.getStatements().get(i));
//		}
//	}
//
//	private static void printResults(List<Node<HtmlElement>> result, HtmlDomTreeWithRTree rt) {
//		
//		String s = "***** repairs list *****";
//		System.out.println(s);
//		for (Node<HtmlElement> node : result) {
//			System.out.println(node.getData().getXPath());
//			System.out.println(rt.getRects().get(node.getData().getRectId()));
//		}
//		for(int i=0;i<s.length();i++) System.out.print("*");
//		 System.out.print("\n");
//	}
//
//}
