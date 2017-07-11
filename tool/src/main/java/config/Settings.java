package main.java.config;

import java.io.File;

public class Settings {
	
	public static boolean INRECORDING = false;
	
	public static String separator = File.separator;
	
	// folder containing the visual execution trace of the reference test suite 
	public static String referenceTestSuiteVisualTraceExecutionFolder 	= "testSuite" + separator;
	
	// folder containing the visual execution trace of the test suite under test 
	public static String testingTestSuiteVisualTraceExecutionFolder 	= "testSuite2" + separator;
	
	// this is the path to the test suite used as a reference
	public static String pathToReferenceTestSuite 	= "src" + separator + "claroline1811" + separator;
	
	// this is the path to the test suite used that might need repair
	public static String pathToTestSuiteUnderTest 	= "src" + separator + "claroline190" + separator;
	
	public static String imageExtension = ".png";
	public static String domExtension 	= ".html";
	public static String javaExtension  = ".java";
	public static String jsonExtension  = ".json";
	
	public static final String[] NON_VISUAL_TAGS = new String[] {"head", "script", "link", "meta", "style", "title", "canvas"};
	public static final String[] TAGS_BLACKLIST = new String[] {"head", "script", "link", "meta", "style", "canvas"};
	public final static String REGEX_FOR_GETTING_ID = "\\*\\[@id=['|\"]?(.+[^'\"])['|\"]?\\]";
	public final static String REGEX_FOR_GETTING_INDEX = "\\[(.+)\\]";
	
	
	public static boolean annotate = false;
	public static boolean VERBOSE = true;
	public static int scale = 10;
	public static double similarityThreshold = 0.5;

}
