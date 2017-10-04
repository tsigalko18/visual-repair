package config;

import java.io.File;

public class Settings {

	/* specify where to find the correct test suite. */
//	 public static String testSuiteCorrect = "addressbook6211";
//	 public static String testSuiteCorrect = "claroline";
	public static String testSuiteCorrect = "claroline1811";

	/* specify where to find the broken/regressed test suite. */
//	 public static String testSuiteBroken = "clarolineDirectBreakage";
//	 public static String testSuiteBroken = "addressbook825";
	public static String testSuiteBroken = "claroline190";

	/*
	 * ******* DO NOT EDIT ANYTHING DOWN HERE *******
	 * **********************************************
	 */
	public static String separator = File.separator;
	public static String projectBaseFolder = "src" + separator + "main" + separator + "java" + separator;
	public static String resourcesFolder = "src" + separator + "main" + separator + "resources" + separator;
	public static String outputFolder = "output" + separator;

	/* folder containing the visual execution trace of the reference test suite. */
	public static String referenceTestSuiteVisualTraceExecutionFolder = outputFolder + testSuiteCorrect + separator;

	/* folder containing the visual execution trace of the test suite under test. */
	public static String testingTestSuiteVisualTraceExecutionFolder = outputFolder + testSuiteBroken + separator;

	/* this is the path to the test suite used as a reference. */
	public static String pathToReferenceTestSuite = resourcesFolder + testSuiteCorrect + separator;

	/* this is the path to the test suite used that might need repair. */
	public static String pathToTestSuiteUnderTest = resourcesFolder + testSuiteBroken + separator;

	/* file extensions. */
	public static String PNG_EXTENSION = ".png";
	public static String HTML_EXTENSION = ".html";
	public static String JAVA_EXTENSION = ".java";
	public static String JSON_EXTENSION = ".json";

	/* those might be deleted in the future. */
	public static final String[] NON_VISUAL_TAGS = new String[] { "head", "script", "link", "meta", "style", "title",
			"canvas" };
	public static final String[] TAGS_BLACKLIST = new String[] { "head", "script", "link", "meta", "style", "canvas" };
	public final static String REGEX_FOR_GETTING_ID = "\\*\\[@id=['|\"]?(.+[^'\"])['|\"]?\\]";
	public final static String REGEX_FOR_GETTING_INDEX = "\\[(.+)\\]";

	public static boolean ANNOTATE = false;
	public static CroppingType CROPPING_METHOD = CroppingType.BOTH;
	public static boolean VERBOSE = true;
	public static double SIMILARITY_THRESHOLD = 0.5;
	
	public enum CroppingType {
		PERFECT, ENLARGED, BOTH
	}

}
