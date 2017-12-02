package utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;

import org.junit.runner.notification.Failure;
import org.openqa.selenium.WebElement;

import config.Settings;
import datatype.EnhancedException;
import datatype.EnhancedTestCase;
import datatype.HtmlDomTree;
import datatype.HtmlElement;
import datatype.SeleniumLocator;
import parser.ParseTest;

public class UtilsRepair {

	public static EnhancedException saveExceptionFromFailure(Failure f) {

		EnhancedException ea = new EnhancedException();
		ea.setException(UtilsParser.getExceptionFromFailure(f));
		ea.setFailedTest(UtilsParser.getFailedTestFromFailure(f));
		ea.setInvolvedLine(UtilsParser.getLineFromFailure(f));
		ea.setMessage(UtilsParser.getMessageFromFailure(f));
		return ea;

	}

	public static void printFailure(Failure failure) {

		System.out.println("MESSAGE");
		System.out.println("--------------------");
		System.out.println(failure.getMessage());
		System.out.println("--------------------");

		System.out.println("TEST HEADER");
		System.out.println("--------------------");
		System.out.println(failure.getTestHeader());
		System.out.println("--------------------");

		System.out.println("TRACE");
		System.out.println("--------------------");
		System.out.println(failure.getTrace());
		System.out.println("--------------------");

		System.out.println("DESCRIPTION");
		System.out.println("--------------------");
		System.out.println(failure.getDescription());
		System.out.println("--------------------");

		System.out.println("EXCEPTION");
		System.out.println("--------------------");
		System.out.println(failure.getException());
		System.out.println("--------------------");

	}

	public static void saveFailures(Failure fail) throws IOException {

		FileWriter bw = new FileWriter(Settings.referenceTestSuiteVisualTraceExecutionFolder + "exception.txt");

		bw.write("MESSAGE" + "\n");
		bw.write("--------------------" + "\n");
		bw.write(fail.getMessage() + "\n");
		bw.write("--------------------" + "\n");
		bw.write("TEST HEADER" + "\n");
		bw.write("--------------------" + "\n");
		bw.write(fail.getTestHeader() + "\n");
		bw.write("--------------------" + "\n");
		bw.write("TRACE" + "\n");
		bw.write("--------------------" + "\n");
		bw.write(fail.getTrace() + "\n");
		bw.write("--------------------" + "\n");
		bw.write("DESCRIPTION" + "\n");
		bw.write("--------------------" + "\n");
		bw.write(fail.getDescription().toString() + "\n");
		bw.write("--------------------" + "\n");
		bw.write("EXCEPTION" + "\n");
		bw.write("--------------------" + "\n");
		bw.write(fail.getException().toString() + "\n");
		bw.write("--------------------" + "\n");

		bw.close();

	}

	public static String capitalizeFirstLetter(String original) {
		if (original == null || original.length() == 0) {
			return original;
		}
		return original.substring(0, 1).toUpperCase() + original.substring(1);
	}

	public static void printTestCase(EnhancedTestCase tc) {
		for (Integer i : tc.getStatements().keySet()) {
			System.out.println(tc.getStatements().get(i));
		}
	}

	public static void printTestCaseWithLineNumbers(EnhancedTestCase tc) {
		for (Integer i : tc.getStatements().keySet()) {
			System.out.println(tc.getStatements().get(i).getLine() + ":\t" + tc.getStatements().get(i));
		}
	}

	public static int getMinimumValue(File[] afterCorrectTrace, File[] beforeCorrectTrace, File[] afterBrokenTrace,
			File[] beforeBrokenTrace) {

		int min = Math.min(afterCorrectTrace.length, beforeCorrectTrace.length);
		min = Math.min(min, afterBrokenTrace.length);
		min = Math.min(min, beforeBrokenTrace.length);

		return min;
	}

	/**
	 * This method makes a "deep clone" of any object it is given.
	 */
	public static Object deepClone(Object object) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(object);
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);
			return ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static SeleniumLocator generateLocator(HtmlElement htmlElement) {

		SeleniumLocator loc = null;

		// text (<a>), id, name, xpath
		if (htmlElement.getTagName().equalsIgnoreCase("a") && htmlElement.getHtmlAttributes().get("text") != null)
			loc = new SeleniumLocator("linkText", htmlElement.getHtmlAttributes().get("text"));
		else if (htmlElement.getHtmlAttributes().get("id") != null)
			loc = new SeleniumLocator("id", htmlElement.getId());
		else if (htmlElement.getHtmlAttributes().get("name") != null)
			loc = new SeleniumLocator("name", htmlElement.getHtmlAttributes().get("name"));
		else
			loc = new SeleniumLocator("xpath", htmlElement.getXPath());

		return loc;
	}

	public static List<SeleniumLocator> generateAllLocators(HtmlElement htmlElement) {

		List<SeleniumLocator> locs = new LinkedList<SeleniumLocator>();

		// text (<a>), id, name, xpath
		if (htmlElement.getTagName().equalsIgnoreCase("a") && htmlElement.getHtmlAttributes().get("text") != null)
			locs.add(new SeleniumLocator("linkText", htmlElement.getHtmlAttributes().get("text")));

		if (htmlElement.getHtmlAttributes().get("id") != null)
			locs.add(new SeleniumLocator("id", htmlElement.getId()));

		if (htmlElement.getHtmlAttributes().get("name") != null)
			locs.add(new SeleniumLocator("name", htmlElement.getHtmlAttributes().get("name")));

		locs.add(new SeleniumLocator("xpath", htmlElement.getXPath()));

		return locs;
	}

	public static SeleniumLocator getLocators(HtmlDomTree page, WebElement webElementFromDomLocator) {

		String xpath = "/" + UtilsXPath.generateXPathForWebElement(webElementFromDomLocator, "");
		HtmlElement htmlElement = page.searchHtmlDomTreeByXPath(xpath);
		return generateLocator(htmlElement);

	}

	public static void saveTest(String prefix, String className, EnhancedTestCase temp) {

		String oldPath = Settings.resourcesFolder + prefix.replace(".", "/") + className + Settings.JAVA_EXTENSION;
		String newPath = Settings.resourcesFolder + prefix.replace(".", "Repaired/") + className.concat("Repaired")
				+ Settings.JAVA_EXTENSION;

		try {
			temp = ParseTest.parseAndSaveToJava(temp, oldPath, newPath);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
