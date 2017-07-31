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
import parser.ParseTest;
import utils.FileFilters;
import utils.UtilsGetters;
import utils.UtilsParser;

public class Main {

	static EnhancedException exception;
	static EnhancedTestCase testCorrect;
	static EnhancedTestCase testBroken;
	static List<EnhancedTestCase> repairs;

	public static void main(String[] args) throws JsonSyntaxException, IOException, SAXException {

		File testSuiteFolder = new File(Settings.testingTestSuiteVisualTraceExecutionFolder);

		File[] tests = testSuiteFolder.listFiles(FileFilters.directoryFilter);

		Map<String, File> m = UtilsParser.convertToHashMap(tests);

		for (File file : m.values()) {

			if (UtilsGetters.isTestBroken(file)) {

				System.out.println("[LOG]\tTest " + file.getName());

				/* load the exception. */
				File ex = UtilsGetters.getExceptionFile(file);
				exception = UtilsParser.readException(ex.getAbsolutePath());

				if (Settings.VERBOSE) {
					System.out.println("[LOG]\tBreakage at line " + exception.getInvolvedLine());
					System.out.print("[LOG]\t" + exception.getMessage());
				}

				/* load the broken test. */
				String name = file.getName();
				ParseTest pt = new ParseTest(Settings.testingTestSuiteVisualTraceExecutionFolder);
				testBroken = pt.parseAndSerialize(UtilsGetters.getTestFile(name, Settings.pathToTestSuiteUnderTest));

				/* load the correct test. */
				pt.setFolder(Settings.referenceTestSuiteVisualTraceExecutionFolder);
				testCorrect = pt.parseAndSerialize(UtilsGetters.getTestFile(name, Settings.pathToReferenceTestSuite));

				/* apply repair algorithms. */
				repairs = RepairStrategies.suggestRepair(exception, testBroken, testCorrect);

			}

		}

	}

}
