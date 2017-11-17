package runner;

import java.io.IOException;

import config.Settings;
import utils.UtilsRunner;

public class TestSuiteRunner {

	public static void main(String[] args) throws IOException {

		/* specify the test suite and test name or runner. */
		UtilsRunner.runTest(Settings.testSuiteCorrect, "TestLoginAdmin");

	}

}
