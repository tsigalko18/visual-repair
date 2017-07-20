package main.java.repair;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.xml.sax.SAXException;

import com.google.gson.JsonSyntaxException;

import main.java.config.Settings;
import main.java.datatype.EnhancedException;
import main.java.datatype.EnhancedTestCase;
import main.java.datatype.HtmlElement;
import main.java.datatype.Statement;
import main.java.parser.ParseTest;
import main.java.utils.FileFilters;
import main.java.utils.UtilsParser;

public class Main {

	static EnhancedException exception;
	static EnhancedTestCase testCorrect;
	static EnhancedTestCase testBroken;
	static List<HtmlElement> repairs;

	public static void main(String[] args) throws JsonSyntaxException, IOException, SAXException {

		File testSuiteFolder = new File(Settings.testingTestSuiteVisualTraceExecutionFolder);

		File[] tests = testSuiteFolder.listFiles(FileFilters.directoryFilter);

		Map<String, File> m = UtilsParser.convertToHashMap(tests);

		for (File file : m.values()) {

			if (FileFilters.isTestBroken(file)) {

				System.out.println("[LOG]\tTest " + file.getName());

				// load the exception
				File ex = FileFilters.getExceptionFile(file);
				exception = UtilsParser.readException(ex.getAbsolutePath());

				if (Settings.VERBOSE) {
					System.out.println("[LOG]\tBreakage at line " + exception.getInvolvedLine());
					System.out.print("[LOG]\t" + exception.getMessage());
				}

				// load the broken test
				testBroken = ParseTest
						.parse(FileFilters.getTestFile(file.getName(), Settings.pathToTestSuiteUnderTest));

				// System.out.println(testBroken);

				// load the correct test
				testCorrect = ParseTest
						.parse(FileFilters.getTestFile(file.getName(), Settings.pathToReferenceTestSuite));

				// System.out.println(testCorrect);

				// // get the broken statement
				// Statement oldst =
				// testCorrect.getStatements().get(Integer.parseInt(exception.getInvolvedLine()));
				//
				// // get the correct statement in the correct version
				// Statement newst =
				// testBroken.getStatements().get(Integer.parseInt(exception.getInvolvedLine()));
				//
				// System.out.println(oldst);
				// System.out.println(newst);

				repairs = RepairMain.suggestRepair(exception, testBroken, testCorrect);

				System.out.println(repairs);
			}

		}

	}

}
