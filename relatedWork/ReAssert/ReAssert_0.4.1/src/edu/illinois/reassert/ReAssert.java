package edu.illinois.reassert;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.output.NullOutputStream;

import spoon.reflect.cu.SourcePosition;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.ParseException;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.UnflaggedOption;
import com.martiansoftware.jsap.stringparsers.ClassStringParser;
import com.martiansoftware.jsap.stringparsers.FileStringParser;
import com.martiansoftware.jsap.stringparsers.IntegerStringParser;

/**
 * Allows one to fix tests from the command line. Run with no arguments for usage.
 * 
 * @author <a href="http://www.cs.uiuc.edu/homes/bdaniel3/">Brett Daniel</a>
 */
public class ReAssert {

	public static PrintStream out = System.out;
	public static PrintStream err = System.err;
	
	public static final String FIX_FILE_SUFFIX = ".fix"; // TODO: make a command line argument
	public static final String SNIPPET_FILE_SUFFIX = ".snip"; // TODO: make a command line argument
	
	private static final String SOURCE_PATH_FLAG = "sourcepath";
	private static final String METHODS_FLAG = "method";
	private static final String FIX_LIMIT_FLAG = "fixlimit";
	private static final String TEST_OUTPUT_FLAG = "testoutput";
	private static final String TEST_ERRORS_FLAG = "testerrors";
	private static final String OUTPUT_DIRECTORY_FLAG = "directory";
	private static final String FIXERS_FLAG = "fixers";
	private static final String SNIPPETS_FLAG = "snippets";
	
	@SuppressWarnings("unchecked")
	public static void main(String... args) {
		JSAP jsap = defineArgs();
		JSAPResult parsed = jsap.parse(args);
				
		if (!parsed.success()) {
			Iterator<?> errors = parsed.getErrorMessageIterator();
			while (errors.hasNext()) {
				err.println(errors.next());
			}
			err.print("Usage: java ");
			err.print(ReAssert.class.getName());
			err.println(" [OPTIONS]");
			err.println(jsap.getHelp());
			System.exit(1);
		}

		String[] sourcePaths = parsed.getStringArray(SOURCE_PATH_FLAG);
		String[] methodsToFix = parsed.getStringArray(METHODS_FLAG);
		int fixLimit = parsed.getInt(FIX_LIMIT_FLAG);
		File outDir = parsed.getFile(OUTPUT_DIRECTORY_FLAG);
		Class<FixStrategy>[] fixerClasses = parsed.getClassArray(FIXERS_FLAG);
		boolean snippets = parsed.getBoolean(SNIPPETS_FLAG);
		
		PrintStream originalOut = System.out;
		if (parsed.contains(TEST_OUTPUT_FLAG)) {
			File outFile = parsed.getFile(TEST_OUTPUT_FLAG);
			redirectStdout(outFile);
		}
		else {
			System.setOut(new PrintStream(NullOutputStream.NULL_OUTPUT_STREAM));
		}

		PrintStream originalErr = System.err;
		if (parsed.contains(TEST_ERRORS_FLAG)) {
			File errFile = parsed.getFile(TEST_ERRORS_FLAG);
			redirectStderr(errFile);
		}
		// else keep stderr unchanged

		TestFixer fixer = 
			setupTestFixer(sourcePaths, fixerClasses);
		List<FixResult> results = 
			performFixes(fixer, methodsToFix, fixLimit);
		saveFixes(results, outDir, snippets);
		
		// recover streams if redirected
		System.setOut(originalOut);
		System.setErr(originalErr);
	}

	private static void saveFixes(List<FixResult> results, File outDir, boolean snippets) {
		Set<File> savedFiles = new HashSet<File>();
		for (FixResult result : results) {
			try {
				File fixFile;
				if (snippets) {
					fixFile = result.saveSnippet(outDir, SNIPPET_FILE_SUFFIX);
				}
				else {
					fixFile = result.save(outDir, FIX_FILE_SUFFIX);
				}
				if (fixFile != null && !savedFiles.contains(fixFile)) {
					out.print("Fixes saved to ");
					out.println(fixFile);
					savedFiles.add(fixFile);
				}
			} 
			catch (IOException e) {
				err.println("Could not save fix. " + e.getMessage());
			}
		}
	}

	private static TestFixer setupTestFixer(
			String[] sourcePaths,
			Class<FixStrategy>[] fixerClasses) {		
		TestFixer fixer = new TestFixer();
		for (String sourcePath : sourcePaths) {
			if (!new File(sourcePath).exists()) {
				err.println("Source path does not exist: " + sourcePath);
			}
			else {
				fixer.addSourcePath(sourcePath);
			}
		}
		for (Class<FixStrategy> fixerClass : fixerClasses) {
			try {
				fixer.prependFixStrategy(fixerClass);
			}
			catch (InstantiationException e) {
				err.println(e.getMessage());
			}
		}
		return fixer;
	}

	private static List<FixResult> performFixes(
			TestFixer fixer,
			String[] methodsToFix, 
			int fixLimit) {
		List<FixResult> results = new LinkedList<FixResult>();
		for (String method : methodsToFix) {
			out.print("Fixing ");
			out.println(method);
			
			FixResult fixResult = null;
			int i = 0;
			// fix up to the limit or until no fixes are applied
			do {
				try {
					fixResult = fixer.fix(method);		
				}
				catch (Exception e) {
					out.print("    Not Fixed. ");
					String message = e.getMessage();
					if (message == null) {
						message = "Threw " + e.getClass().getName();				
					}
					out.println(message);
					//e.printStackTrace(err); // Useful for debugging
					break;
				}
				if (fixResult == null) {
					if (i == 0) {
						out.println("    Not fixed. Test succeeded without fix.");
					}	
					else {
						out.println("    Fixed!");
					}
					break;
				}
				else {
					out.print("    Used ");
					out.println(fixResult.getAppliedFixer().getClass().getName());
					if (fixResult instanceof CodeFixResult) {
						SourcePosition position = ((CodeFixResult) fixResult).getFixedElement().getPosition();
						String positionString = position.getFile().getPath() + ":" + position.getLine();
						out.print("    At ");						
						out.println(positionString);
					}
				}

				i++;
				results.add(fixResult);
			} while (fixLimit <= 0 || i < fixLimit);

			if (i == fixLimit) {
				// Hit fix limit
				out.println("    Not Fixed. Reached limit on number of fixes.");
			}
		}
		return results;
	}

	private static void redirectStderr(File errFile) {
		try {
			System.setErr(new PrintStream(errFile));
		} 
		catch (FileNotFoundException e) {
			err.println("Cannot write to " + errFile);
			System.exit(1);
		}
	}

	private static void redirectStdout(File outFile) {
		try {
			System.setOut(new PrintStream(outFile));
		} 
		catch (FileNotFoundException e) {
			err.println("Cannot write to " + outFile);
			System.exit(1);
		}
	}

	private static JSAP defineArgs() {
		try {
			JSAP jsap = new JSAP();
			
			FlaggedOption sourcePath = new FlaggedOption(SOURCE_PATH_FLAG);
			sourcePath.setHelp(
					"The path(s) from which to load source files.");
			sourcePath.setLongFlag(SOURCE_PATH_FLAG);
			sourcePath.setShortFlag('s');
			sourcePath.setList(true);
			sourcePath.setRequired(false);
			jsap.registerParameter(sourcePath);
			
			FlaggedOption fixLimit = new FlaggedOption(FIX_LIMIT_FLAG);
			fixLimit.setHelp(
					"The maximum number of fixes per test method. " +
					"Values <= 0 do not limit the number of fixes.");
			fixLimit.setLongFlag(FIX_LIMIT_FLAG);
			fixLimit.setShortFlag('n');
			fixLimit.setRequired(false);
			fixLimit.setStringParser(IntegerStringParser.getParser());
			fixLimit.setDefault("10");
			jsap.registerParameter(fixLimit);
			
			FlaggedOption testOutput = new FlaggedOption(TEST_OUTPUT_FLAG);
			testOutput.setHelp(
					"The file in which to print output from the system under test. " +
					"If omitted, test output is not printed.");
			testOutput.setLongFlag(TEST_OUTPUT_FLAG);
			testOutput.setShortFlag('o');
			testOutput.setRequired(false);
			testOutput.setStringParser(FileStringParser.getParser());
			jsap.registerParameter(testOutput);

			FlaggedOption testErrors = new FlaggedOption(TEST_ERRORS_FLAG);
			testErrors.setHelp(
					"The file in which to print errors from the system under test. " +
					"If omitted, errors are printed to standard error.");
			testErrors.setLongFlag(TEST_ERRORS_FLAG);
			testErrors.setShortFlag('e');
			testErrors.setRequired(false);
			testErrors.setStringParser(FileStringParser.getParser());
			jsap.registerParameter(testErrors);

			FlaggedOption outputDirectory = new FlaggedOption(OUTPUT_DIRECTORY_FLAG);
			outputDirectory.setHelp(
					"The directory in which to save fixed files. If omitted, fixed files " +
					"will be saved in the same directories as the original files. ");
			outputDirectory.setLongFlag(OUTPUT_DIRECTORY_FLAG);
			outputDirectory.setShortFlag('d');
			outputDirectory.setRequired(false);
			FileStringParser dirParser = FileStringParser.getParser();
			dirParser.setMustBeDirectory(true);		
			dirParser.setMustExist(false);
			outputDirectory.setStringParser(dirParser);
			jsap.registerParameter(outputDirectory);
			
			FlaggedOption fixers = new FlaggedOption(FIXERS_FLAG);
			fixers.setHelp(String.format(
					"The fully-qualified class names of fix strategies (implementing %s) to " +
					"use to repair failed tests.  Strategies will be prepended to the start of " +
					"the default list.", FixStrategy.class.getName()));
			fixers.setLongFlag(FIXERS_FLAG);
			fixers.setShortFlag('x');
			fixers.setRequired(false);
			fixers.setList(true);
			fixers.setStringParser(new FixStrategyClassStringParser());
			jsap.registerParameter(fixers);

			Switch snippets = new Switch(SNIPPETS_FLAG);
			snippets.setHelp("Output modified snippets rather than the full modified file");
			snippets.setLongFlag(SNIPPETS_FLAG);			
			jsap.registerParameter(snippets);
			
			UnflaggedOption methods = new UnflaggedOption(METHODS_FLAG);
			methods.setHelp(
					"The test methods to fix. " +
					"The expected format is some.package.SomeClass#someMethod");
			methods.setGreedy(true);
			methods.setList(true);
			methods.setRequired(true);
			// TODO: methods.setStringParser(/*a parser that checks the format of the string*/)
			jsap.registerParameter(methods);
			
			return jsap;
			
		} catch (JSAPException e) {
			// programmer error
			throw new RuntimeException(e);
		}
	}
	
	private static class FixStrategyClassStringParser extends ClassStringParser {
		
		@SuppressWarnings("deprecation")
		public FixStrategyClassStringParser() {
			super();
		}
		
		@Override
		public Object parse(String arg) throws ParseException {			
			Class<?> parsed = (Class<?>) super.parse(arg);
			if (FixStrategy.class.isAssignableFrom(parsed)) {
				return parsed;
			}
			throw new ParseException(String.format(
							"%s does not implement %s",
							arg,
							FixStrategy.class.getName()));
		}
	}
}
