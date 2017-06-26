package main.java.config;

import java.io.File;

public class Settings {
	
	public static String separator 				= File.separator;
	public static String testSuiteFolder 		= "testSuite" + separator;
	public static String correctTestSuitePath 	= "src" + separator + "claroline" + separator;
	public static String buggyTestSuitePath 	= "src" + separator + "clarolineDirectBreakage" + separator;
	
	public static String imageExtension = ".png";
	public static String domExtension 	= ".html";
	public static String javaExtension  = ".java";
	public static String jsonExtension  = ".json";
	
	public static final String[] NON_VISUAL_TAGS = new String[] {"head", "script", "link", "meta", "style", "title", "canvas"};
	public static final String[] TAGS_BLACKLIST = new String[] {"head", "script", "link", "meta", "style", "canvas"};
	public final static String REGEX_FOR_GETTING_ID = "\\*\\[@id=['|\"]?(.+[^'\"])['|\"]?\\]";
	public final static String REGEX_FOR_GETTING_INDEX = "\\[(.+)\\]";
	
	public static boolean inRecordingMode = true;
	public static boolean annotate = false;
	public static boolean verbose = true;
	public static int scale = 10;
	public static double similarityThreshold = 0.5;

}

//public class Settings {
//	
//	public static String separator 				= File.separator;
//	
//	// reference test suite
//	public static String correctTestSuiteName 	= "claroline186";
//	
//	// evolved test suite
//	public static String brokenTestSuiteName 	= "claroline187";
//	
//	// path to the old correct test suite
//	public static String correctTestSuitePath 	= "src" + separator + correctTestSuiteName + separator;
//	
//	// path to the new (possibly broken) test suite
//	public static String newBrokenTestSuitePath = "src" + separator + brokenTestSuiteName + separator;
//	
//	// path to the folder containing the visual test execution of the correct test suite
//	public static String correctTestSuiteFolder	=  correctTestSuiteName + "-Execution" + separator;
//	
//	// path to the folder containing the visual test execution of the broken test suite
//	public static String brokenTestSuiteFolder 	= brokenTestSuiteName + "" + separator;
//	
//	public static String imageExtension = ".png";
//	public static String htmlExtension 	= ".html";
//	public static String javaExtension  = ".java";
//	public static String jsonExtension  = ".json";
//	
//	public static final String[] NON_VISUAL_TAGS = new String[] {"head", "script", "link", "meta", "style", "title", "canvas"};
//	public static final String[] TAGS_BLACKLIST = new String[] {"head", "script", "link", "meta", "style", "canvas"};
//	public final static String REGEX_FOR_GETTING_ID = "\\*\\[@id=['|\"]?(.+[^'\"])['|\"]?\\]";
//	public final static String REGEX_FOR_GETTING_INDEX = "\\[(.+)\\]";
//	
//	public static boolean isCorrectTestSuiteRunning = true;
//	public static boolean annotate = false;
//	public static boolean verbose = true;
//	public static int scale = 10;
//	public static double similarityThreshold = 0.5;
//
//}
