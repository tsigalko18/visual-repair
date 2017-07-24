package utils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.ini4j.InvalidFileFormatException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.runner.notification.Failure;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import config.Settings;
import datatype.EnhancedException;
import datatype.EnhancedTestCase;
import datatype.HtmlDomTreeWithRTree;
import datatype.HtmlElement;
import datatype.Node;
import datatype.SeleniumLocator;
import japa.parser.ast.stmt.Statement;

public class UtilsParser {

	public static Gson gson = new GsonBuilder().setPrettyPrinting().create();

	/**
	 * Auxiliary method to get the value for the get statement
	 * 
	 * @param st
	 * @return
	 */
	public static String getUrlFromDriverGet(Statement st) {
		String s = st.toString();
		s = s.substring(10); // remove driver.get(
		s = s.substring(1, s.length() - 2); // remove );
		return s;
	}

	/**
	 * Auxiliary method to get the value for the sendKeys statement
	 * 
	 * @param st
	 * @return
	 */
	public static String getValueFromSendKeys(Statement st) {

		String s = st.toString();
		int begin = s.indexOf("sendKeys(");
		s = s.substring(begin, s.length()); // leave only sendKeys("admin");
		s = s.substring(9, s.length() - 2); // leave only "admin"
		return s;

	}

	/**
	 * Auxiliary method to get the screenshot file
	 * 
	 * @param st
	 * @return
	 * @throws Exception
	 */
	public static File getScreenshot(String name, int beginLine, String type) throws Exception {

		String p = Settings.referenceTestSuiteVisualTraceExecutionFolder + name + Settings.separator;

		File dir = new File(p);
		File[] listOfFiles = dir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String n) {
				// return true;
				return (n.startsWith(Integer.toString(beginLine)) && n.endsWith(Settings.PNG_EXTENSION)
						&& n.contains(name) && n.contains(type));
			}
		});

		if (listOfFiles.length == 0) {
			return null;
		} else if (listOfFiles.length == 1) {
			return listOfFiles[0];
		} else {
			throw new Exception("[LOG]\tToo many files retrieved");
		}

	}

	/**
	 * Auxiliary method to get the HTML file
	 * 
	 * @param st
	 * @return
	 * @throws Exception
	 */
	public static File getHTMLDOMfile(String name, int beginLine, String type, String useExtension) throws Exception {

		String p;

		p = Settings.testingTestSuiteVisualTraceExecutionFolder + name + Settings.separator + beginLine + "-" + type
				+ "-" + name + "-" + beginLine;

		// if(Settings.INRECORDING){
		// p = Settings.referenceTestSuiteVisualTraceExecutionFolder + name +
		// Settings.separator + beginLine + "-" + type + "-" + name + "-" + beginLine;
		// } else {
		// p = Settings.testingTestSuiteVisualTraceExecutionFolder + name +
		// Settings.separator + beginLine + "-" + type + "-" + name + "-" + beginLine;
		// }

		File dir = new File(p);
		File[] listOfFiles = dir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String n) {
				return (n.endsWith(".html"));
			}
		});

		if (listOfFiles == null || listOfFiles.length == 0) {
			return null;
		} else {
			return listOfFiles[0];
		}

	}

	/**
	 * get class name from path e.g. src/clarolineDirectBreakage/DirectBreakage.java
	 * => DirectBreakage
	 * 
	 * @param arg
	 * @return
	 */
	public static String getClassNameFromPath(String arg) {
		return arg.substring(arg.lastIndexOf("/") + 1).replace(".java", "");
	}

	/**
	 * auxiliary method to extract the DOM locator used by the web element
	 * 
	 * @param webElement
	 * @return
	 */
	public static SeleniumLocator getDomLocator(Statement st) {

		String domLocator = st.toString(); // driver.findElement(By.xpath(".//*[@id='loginBox']/form/fieldset/input[4]")).click();
		domLocator = domLocator.substring(domLocator.indexOf("By"), domLocator.length()); // By.id("login")).sendKeys("admin");
		domLocator = domLocator.substring(domLocator.indexOf("By"), domLocator.indexOf(")") + 1); // By.id("login")
		domLocator = domLocator.replace("By.", ""); // id("login")
		String strategy = domLocator.split("\\(")[0].trim();
		String value = domLocator.split("\\(")[1];
		value = value.substring(0, value.length() - 1).replaceAll("\"", "").trim();

		return new SeleniumLocator(strategy, value);
	}

	public static String getValueFromSelect(Statement st) {

		String value = st.toString(); // new
										// Select(driver.findElement(By.id("course_category"))).selectByVisibleText("(SC)
										// Sciences");
		value = value.substring(value.indexOf("selectBy"), value.length()); // selectByVisibleText("(SC)
																			// Sciences");
		value = value.substring(value.indexOf("(") + 1, value.indexOf("\");") + 1); // (SC)
																					// Sciences
		return value;
	}

	public static String getAssertion(Statement st) {

		// assertTrue(driver.findElement(By.xpath("//*[@class='userName']")).getText().contains("John
		// Doe"));
		String a = st.toString();
		int begin = a.indexOf("assert");
		int end = a.indexOf("(", begin);
		a = a.substring(begin, end);
		return a;
	}

	/**
	 * return the predicate used in the assertion
	 * 
	 * @param st
	 * @return
	 */
	public static String getPredicate(Statement st) {

		// assertTrue(driver.findElement(By.xpath("//*[@class='userName']")).getText().contains("John
		// Doe"));
		if (st.toString().contains("assert") && st.toString().contains("getText()")) {
			String a = st.toString();
			int begin = a.indexOf("getText().");
			a = a.substring(begin + "getText().".length(), a.length() - 2); // contains("John
																			// Doe")
			return a;
		}
		return "";
	}

	/**
	 * Save the test case in JSON format
	 * 
	 * @param tc
	 * @param path
	 */
	public static void serializeTestCase(EnhancedTestCase tc, String path, String folder) {

		// src/clarolineDirectBreakage/DirectBreakage.java
		// testSuite/DirectBreakage/DirectBreakage.json

		int lastSlash = path.lastIndexOf("/");
		int end = path.indexOf(".java");
		String testName = path.substring(lastSlash + 1, end);
		String newPath = folder + testName + Settings.separator + testName + Settings.JSON_EXTENSION;

		try {
			FileUtils.write(new File(newPath), gson.toJson(tc));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Save the exception in JSON format
	 * 
	 * @param tc
	 * @param path
	 */
	public static void serializeException(EnhancedException ex, String path) {

		try {
			FileUtils.write(new File(path), gson.toJson(ex, EnhancedException.class));
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (Settings.VERBOSE)
			System.out.println("[LOG]\tException saved: " + path);
	}

	/**
	 * Save the exception in JSON format
	 * 
	 * @param tc
	 * @param path
	 */
	public static void serializeHtmlDomTree(HtmlDomTreeWithRTree h, String path) {

		try {
			FileUtils.write(new File(path), gson.toJson(h, HtmlDomTreeWithRTree.class));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static String toJsonPath(String path) {
		// src/clarolineDirectBreakage/DirectBreakage.java
		// testSuite/DirectBreakage/exception.json

		int lastSlash = path.lastIndexOf("/");
		int end = path.indexOf(".java");
		String testName = path.substring(lastSlash + 1, end);
		String newPath = Settings.testingTestSuiteVisualTraceExecutionFolder + testName + Settings.separator
				+ "exception" + Settings.JSON_EXTENSION;
		return newPath;
	}

	/**
	 * Save the exception in JSON format
	 * 
	 * @param tc
	 * @param path
	 * @throws IOException
	 * @throws JsonSyntaxException
	 */
	public static EnhancedException readException(String path) throws JsonSyntaxException, IOException {

		if (!path.endsWith(Settings.JSON_EXTENSION)) {
			throw new InvalidFileFormatException("[ERROR]\tInvalid file extension");
		} else if (!path.contains("exception")) {
			throw new InvalidFileFormatException("[ERROR]\tInvalid exception file");
		}

		File f = new File(path);
		EnhancedException ea = gson.fromJson(FileUtils.readFileToString(f), EnhancedException.class);
		return ea;
	}

	/**
	 * Given an HTML element, retrieve its XPath
	 * 
	 * @param js
	 *            Selenium JavascriptExecutor object to execute javascript
	 * @param element
	 *            Selenium WebElement corresponding to the HTML element
	 * @return XPath of the given element
	 */
	public static String getElementXPath(JavascriptExecutor js, WebElement element) {
		return (String) js
				.executeScript("var getElementXPath = function(element) {" + "return getElementTreeXPath(element);"
						+ "};" + "var getElementTreeXPath = function(element) {" + "var paths = [];"
						+ "for (; element && element.nodeType == 1; element = element.parentNode)  {" + "var index = 0;"
						+ "for (var sibling = element.previousSibling; sibling; sibling = sibling.previousSibling) {"
						+ "if (sibling.nodeType == Node.DOCUMENT_TYPE_NODE) {" + "continue;" + "}"
						+ "if (sibling.nodeName == element.nodeName) {" + "++index;" + "}" + "}"
						+ "var tagName = element.nodeName.toLowerCase();"
						+ "var pathIndex = (\"[\" + (index+1) + \"]\");" + "paths.splice(0, 0, tagName + pathIndex);"
						+ "}" + "return paths.length ? \"/\" + paths.join(\"/\") : null;" + "};"
						+ "return getElementXPath(arguments[0]);", element);
	}

	public static Element getElementFromXPathJava(String xPath, Document doc) throws IOException {
		String xPathArray[] = xPath.split("/");
		ArrayList<String> xPathList = new ArrayList<String>();

		for (int i = 0; i < xPathArray.length; i++) {
			if (!xPathArray[i].isEmpty()) {
				xPathList.add(xPathArray[i]);
			}
		}

		Element foundElement = null;
		Elements elements;
		int startIndex = 0;

		String id = getElementId(xPathList.get(0));
		if (id != null && !id.isEmpty()) {
			foundElement = doc.getElementById(id);
			if (foundElement == null)
				return null;
			elements = foundElement.children();
			startIndex = 1;
		} else {
			elements = doc.select(xPathList.get(0).replaceFirst(Settings.REGEX_FOR_GETTING_INDEX, ""));
		}
		for (int i = startIndex; i < xPathList.size(); i++) {
			String xPathFragment = xPathList.get(i);
			int index = getSiblingIndex(xPathFragment);
			boolean found = false;

			// strip off sibling index in square brackets
			xPathFragment = xPathFragment.replaceFirst(Settings.REGEX_FOR_GETTING_INDEX, "");

			for (Element element : elements) {
				if (found == false && xPathFragment.equalsIgnoreCase(element.tagName())) {
					// check if sibling index present
					if (index > 1) {
						int siblingCount = 0;
						for (Element siblingElement = element
								.firstElementSibling(); siblingElement != null; siblingElement = siblingElement
										.nextElementSibling()) {
							if ((siblingElement.tagName().equalsIgnoreCase(xPathFragment))) {
								siblingCount++;
								if (index == siblingCount) {
									foundElement = siblingElement;
									found = true;
									break;
								}
							}
						}
						// invalid element (sibling index does not exist)
						if (found == false)
							return null;
					} else {
						foundElement = element;
						found = true;
					}
					break;
				}
			}

			// element not found
			if (found == false) {
				return null;
			}

			elements = foundElement.children();
		}
		return foundElement;
	}

	private static int getSiblingIndex(String xPathElement) {
		String value = getValueFromRegex(Settings.REGEX_FOR_GETTING_INDEX, xPathElement);
		if (value == null)
			return -1;
		return Integer.parseInt(value);
	}

	private static String getElementId(String xPathElement) {
		return getValueFromRegex(Settings.REGEX_FOR_GETTING_ID, xPathElement);
	}

	public static String getValueFromRegex(String regex, String str) {
		Pattern p = Pattern.compile(regex, Pattern.DOTALL);
		Matcher m = p.matcher(str);
		if (m.find()) {
			return m.group(1);
		}
		return null;
	}

	public static boolean isPointInRectangle(int x, int y, int left, int top, int width, int height,
			boolean isBorderIncluded) {

		if (isBorderIncluded) {
			if (x >= left && y >= top && x <= (left + width) && y <= (top + height))
				return true;
		} else {
			if (x > left && y > top && x < (left + width) && y < (top + height))
				return true;
		}
		return false;
	}

	// OK
	public static String getFailedTestFromFailure(Failure f) {
		String s = f.getTestHeader().substring(0, f.getTestHeader().indexOf("("));
		return s;
	}

	// OK
	public static String getExceptionFromFailure(Failure f) {
		String s = f.getException().toString().substring(0,
				f.getException().toString().indexOf("For documentation", 0));
		return s;
	}

	// OK
	public static String getMessageFromFailure(Failure f) {

		String s;

		if (f.getMessage().contains("Cannot locate element with text:")) {
			s = f.getMessage().toString().substring(0, f.getException().toString().indexOf("For documentation", 0));
			s = s.substring(0, s.indexOf("For documentation"));
		} else {
			// s = f.getMessage().toString().substring(0,
			// f.getException().toString().indexOf(":", 0));
			s = f.getMessage().toString().substring(0, f.getMessage().toString().indexOf("Command"));
		}

		return s;
	}

	// OK
	public static String getLineFromFailure(Failure f) {
		String s = f.getTrace();
		int begin = s.indexOf(getFailedTestFromFailure(f), 0);
		s = s.substring(begin, s.indexOf(System.getProperty("line.separator"), begin));
		return s.replaceAll("\\D+", "");
	}

	public static Map<String, File> convertToHashMap(File[] tests) {

		Map<String, File> m = new HashMap<String, File>();
		for (File test : tests)
			m.put(test.getName(), test);
		return m;
	}

	public static void printResults(List<Node<HtmlElement>> result, HtmlDomTreeWithRTree rt) {

		String s = "***** candidate list *****";
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
