package visualrepair;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.xml.sax.SAXException;

import com.google.gson.JsonSyntaxException;

import config.Settings;
import datatype.EnhancedException;
import datatype.EnhancedTestCase;
import datatype.HtmlElement;
import parser.ParseTest;
import utils.FileFilters;
import utils.UtilsParser;

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
				ParseTest pt = new ParseTest(Settings.referenceTestSuiteVisualTraceExecutionFolder);
				testBroken = pt.parse(FileFilters.getTestFile(file.getName(), Settings.pathToTestSuiteUnderTest));

				// load the correct test
				pt.setFolder(Settings.testingTestSuiteVisualTraceExecutionFolder);
				testCorrect = pt.parse(FileFilters.getTestFile(file.getName(), Settings.pathToReferenceTestSuite));

				// apply repair algorithms
				repairs = RepairStrategies.suggestRepair(exception, testBroken, testCorrect);

			}

		}

	}

}
