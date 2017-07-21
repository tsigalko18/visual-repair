package main.java.config;

import java.io.File;

public class Settings {

	// specify where to find the correct test suite
	public static String testSuiteCorrect = "claroline";

	// specify where to find the broken/regressed test suite
	public static String testSuiteBroken = "clarolineDirectBreakage";

	/* ******************************
	 * YOU ARE NOT SUPPOSED TO EDIT 
	 * 		ANYTHING DOWN HERE
	 * ******************************
	 */
	public static String separator = File.separator;
	public static String projectBaseDir = "src" + File.separator + "main" + File.separator + "java";

	// folder containing the visual execution trace of the reference test suite
	public static String referenceTestSuiteVisualTraceExecutionFolder = testSuiteCorrect + separator;

	// folder containing the visual execution trace of the test suite under test
	public static String testingTestSuiteVisualTraceExecutionFolder = testSuiteBroken + separator;

	// this is the path to the test suite used as a reference
	public static String pathToReferenceTestSuite = projectBaseDir + separator + testSuiteCorrect + separator;

	// this is the path to the test suite used that might need repair
	public static String pathToTestSuiteUnderTest = projectBaseDir + separator + testSuiteBroken + separator;

	// file extensions
	public static String imageExtension = ".png";
	public static String domExtension = ".html";
	public static String javaExtension = ".java";
	public static String jsonExtension = ".json";

	// those might be deleted in the future
	public static final String[] NON_VISUAL_TAGS = new String[] { "head", "script", "link", "meta", "style", "title",
			"canvas" };
	public static final String[] TAGS_BLACKLIST = new String[] { "head", "script", "link", "meta", "style", "canvas" };
	public final static String REGEX_FOR_GETTING_ID = "\\*\\[@id=['|\"]?(.+[^'\"])['|\"]?\\]";
	public final static String REGEX_FOR_GETTING_INDEX = "\\[(.+)\\]";

	public static boolean annotate = false;
	public static boolean VERBOSE = true;
	public static int scale = 10;
	public static double similarityThreshold = 0.5;

}
