package edu.illinois.reassert.test;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import edu.illinois.reassert.ReAssert;
import edu.illinois.reassert.testutil.FixChecker;
import edu.illinois.reassert.testutil.ReAssertFixer;

public class ReAssertFixerTest {

	final String INPUT_DIR = "resources/sample-tests";
	
	@Test
	public void testReplaceAtFix() throws IOException {
		final File fixFile = new File(INPUT_DIR, "ReAssertFixerTestInput.java.fix");
		fixFile.delete();
		final File expFile = new File(INPUT_DIR, "ReAssertFixerTestInput.java.expected");

		// FixChecker expects normal test directory; input file's is different
		String tmp = FixChecker.SOURCE_DIR;		
		FixChecker.SOURCE_DIR = INPUT_DIR;
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ReAssert.out = new PrintStream(out);
		ReAssert.main(
				"--sourcepath", INPUT_DIR,
				"--fixers", ReAssertFixer.class.getName(),
				"ReAssertFixerTestInput#test",
				"ReAssertFixerTestInput#test2");
		//System.out.println(out.toString());
		
		// Restore TEST_DIR
		FixChecker.SOURCE_DIR = tmp;

		assertEquals(
				FileUtils.readFileToString(expFile), 
				FileUtils.readFileToString(fixFile));
		assertEquals(
				"Fixing ReAssertFixerTestInput#test\n" +
				"    Used edu.illinois.reassert.testutil.ReAssertFixer\n" +
				"    At resources/sample-tests/ReAssertFixerTestInput.java:18\n" +
				"    Fixed!\n" +
				"Fixing ReAssertFixerTestInput#test2\n" +
				"    Used edu.illinois.reassert.testutil.ReAssertFixer\n" +
				"    At resources/sample-tests/ReAssertFixerTestInput.java:27\n" +
				"    Fixed!\n" +
				"Fixes saved to resources/sample-tests/ReAssertFixerTestInput.java.fix\n", 
				out.toString());
	}
}
