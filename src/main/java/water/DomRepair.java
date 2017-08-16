package water;

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
import utils.UtilsRepair;

public class DomRepair {

	static List<EnhancedTestCase> repairs;
	static EnhancedException exception;
	static String htmlpage;
	static EnhancedTestCase testBroken;
	static EnhancedTestCase testCorrect;
	static boolean checkOnBrowser = true;

	public static void main(String[] args) throws JsonSyntaxException, IOException, SAXException {

		File testSuiteFolder = new File(Settings.testingTestSuiteVisualTraceExecutionFolder);

		File[] tests = testSuiteFolder.listFiles(FileFilters.directoryFilter);

		Map<String, File> m = UtilsParser.convertToHashMap(tests);

		System.out.println("[LOG]\tRunning WATER");
		System.out.println("[LOG]\tcheckOnBrowser is set to " + checkOnBrowser);

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

				long startTime = System.currentTimeMillis();

				/* apply repair algorithms. */
				Water wt = new Water(testBroken, testCorrect, exception, checkOnBrowser);
				repairs = wt.suggestRepair();

				long stopTime = System.currentTimeMillis();
				long elapsedTime = stopTime - startTime;
				System.out.println(repairs.size() + " repairs found in " + elapsedTime / 1000 + " s");

				for (int i = 0; i < repairs.size(); i++) {
					System.out.println("Repaired Test #" + i);
					UtilsRepair.printTestCaseWithLineNumbers(repairs.get(i));
				}

			}

		}

		
		System.exit(0);

	}

}
