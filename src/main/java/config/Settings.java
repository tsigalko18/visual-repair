package config;

import java.io.File;

public class Settings {

	/* package name of the correct test suite. */
	// public static String testSuiteCorrect = "addressbook6211";
	public static String testSuiteCorrect = "claroline";
	// public static String testSuiteCorrect = "claroline1811";

	/* package name of the broken/regressed test suite. */
	public static String testSuiteBroken = "clarolineDirectBreakage";
	// public static String testSuiteBroken = "addressbook825";
	// public static String testSuiteBroken = "claroline190";

	/*
	 * ******* DO NOT EDIT ANYTHING DOWN HERE *******
	 * **********************************************
	 */
	public static String separator = File.separator;
	public static String projectBaseFolder = "src" + separator + "main" + separator + "java" + separator;
	public static String resourcesFolder = "src" + separator + "main" + separator + "resources" + separator;
	public static String outputFolder = "output" + separator;

	/* specify if AspectJ is active. */
	public static boolean aspectActive = true;

	/* folder containing the visual execution trace of the reference test suite. */
	public static String referenceTestSuiteVisualTraceExecutionFolder = outputFolder + testSuiteCorrect + separator;

	/* folder containing the visual execution trace of the test suite under test. */
	public static String testingTestSuiteVisualTraceExecutionFolder = outputFolder + testSuiteBroken + separator;

	/* path to the test suite used as a reference. */
	public static String pathToReferenceTestSuite = resourcesFolder + testSuiteCorrect + separator;

	/* path to the test suite under test. */
	public static String pathToTestSuiteUnderTest = resourcesFolder + testSuiteBroken + separator;

	/* file extensions. */
	public static String PNG_EXTENSION = ".png";
	public static String HTML_EXTENSION = ".html";
	public static String JAVA_EXTENSION = ".java";
	public static String JSON_EXTENSION = ".json";

	/* regexp. */
	public static final String[] TAGS_BLACKLIST = new String[] { "head", "script", "link", "meta", "style", "canvas" };
	public static final String[] ATTRIBUTES_WHITELIST = new String[] { "id", "name", "class", "title", "alt", "value" };

	public final static String REGEX_FOR_GETTING_ID = "\\*\\[@id=['|\"]?(.+[^'\"])['|\"]?\\]";
	public final static String REGEX_FOR_GETTING_INDEX = "\\[(.+)\\]";

	public static boolean HYBRID = false;

	public static boolean VERBOSE = true;
	public static double SIMILARITY_THRESHOLD = 0.5;

}
