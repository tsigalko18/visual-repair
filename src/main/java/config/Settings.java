package config;

import java.io.File;

public class Settings {

	/* package name of the reference correct test suite. */
	public static String testSuiteCorrect = "addressbook549"; // "claroline" "claroline1811"

	/* package name of the broken/regressed test suite. */
	public static String testSuiteBroken = "addressbook55"; // "clarolineDirectBreakage"; "claroline190";

	/* pause the tool's execution after each statement. */
	public static boolean debugMode = false;

	/*
	 * ******* DO NOT EDIT ANYTHING DOWN HERE *******
	 * **********************************************
	 */
	public static String sep = File.separator;
	public static String projectBaseFolder = "src" + sep + "main" + sep + "java" + sep;
	public static String resourcesFolder = "src" + sep + "main" + sep + "resources" + sep;
	public static String outputDir = "output" + sep;

	/* specify if AspectJ is active. */
	public static boolean aspectActive = false;

	/* folder containing the visual execution trace of the reference test suite. */
	public static String referenceTestSuiteVisualTraceExecutionFolder = outputDir + testSuiteCorrect + sep;

	/* folder containing the visual execution trace of the test suite under test. */
	public static String testingTestSuiteVisualTraceExecutionFolder = outputDir + testSuiteBroken + sep;

	/* path to the test suite used as a reference. */
	public static String pathToReferenceTestSuite = resourcesFolder + testSuiteCorrect + sep;

	/* path to the test suite under test. */
	public static String pathToTestSuiteUnderTest = resourcesFolder + testSuiteBroken + sep;

	/* file extensions. */
	public static String PNG_EXT = ".png";
	public static String HTML_EXTENSION = ".html";
	public static String JAVA_EXTENSION = ".java";
	public static String JSON_EXT = ".json";

	/* regexp. */
	public static final String[] TAGS_BLACKLIST = new String[] { "head", "script", "link", "meta", "style", "canvas" };
	public static final String[] ATTRIBUTES_WHITELIST = new String[] { "id", "name", "class", "title", "alt", "value" };

	public final static String REGEX_FOR_GETTING_ID = "\\*\\[@id=['|\"]?(.+[^'\"])['|\"]?\\]";
	public final static String REGEX_FOR_GETTING_INDEX = "\\[(.+)\\]";

	/* different repair strategies. */
	public static enum RepairMode {
		DOM, VISUAL, HYBRID
	}

	public static boolean VERBOSE = true;
	public static double SIMILARITY_THRESHOLD = 0.5;

}
