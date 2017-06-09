package edu.illinois.reassert.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import spoon.reflect.code.CtInvocation;
import edu.illinois.reassert.AssertFixer;
import edu.illinois.reassert.CodeFixResult;
import edu.illinois.reassert.ReAssert;
import edu.illinois.reassert.RecordedAssertFailure;
import edu.illinois.reassert.UnfixableException;
import edu.illinois.reassert.reflect.Factory;

public class ReAssertTest {
	
	private static final File INPUT_DIR = new File("resources/sample-tests");
	
	@BeforeClass
	public static void deleteOldFixes() throws IOException {
		deleteOldFixes(INPUT_DIR);	
	}
	
	private static void deleteOldFixes(File dir) {
		if (!dir.isDirectory()) {
			return;
		}
		for (File file : dir.listFiles()) {
			if (file.isHidden()) {
				// ignore .svn 
				continue;
			}
			else if (file.isDirectory()) {
				deleteOldFixes(file);
			}
			else if (file.getName().endsWith(ReAssert.FIX_FILE_SUFFIX) 
					|| file.getName().endsWith(ReAssert.SNIPPET_FILE_SUFFIX)) {
				file.delete();
			}
		}
	}
	
	@Before
	public void deleteReAssertWork() {
		new File(".reassert").delete();
	}
	
	private static void assertContentsEqual(File expected, File actual) throws IOException {
		assertEquals(
				FileUtils.readFileToString(expected),
				FileUtils.readFileToString(actual));
	}
	
	private static void assertContentEquals(String expectedContent, File actual) throws IOException {
		assertEquals(
				expectedContent,
				FileUtils.readFileToString(actual));
	}

	@Test
	public void fixSimpleTest() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ReAssert.out = new PrintStream(out);
		ReAssert.main(
				"--sourcepath", INPUT_DIR.getPath(),
				"SimpleTest#test");
		assertContentsEqual(
				new File(INPUT_DIR, "SimpleTest.java.expected"),
				new File(INPUT_DIR, "SimpleTest.java.fix"));
	}

	@Test
	public void testOutDir() throws IOException {
		File outDir = new File(".reassert/output");
		outDir.delete();
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ReAssert.out = new PrintStream(out);
		ReAssert.main(
				"--sourcepath", INPUT_DIR.getPath(),
				"--directory", outDir.getPath(),
				"SimpleTest#test");
		assertContentsEqual(
				new File(INPUT_DIR, "SimpleTest.java.expected"),				
				new File(outDir, new File(INPUT_DIR, "SimpleTest.java.fix").getPath()));
	}
	
	@Test
	public void fixCustomAssert() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ReAssert.out = new PrintStream(out);
		ReAssert.main(
				"--sourcepath", INPUT_DIR.getPath(),
				"--fixers", CustomAssertFixer.class.getName(),
				"CustomAssertTest#testWithCustomAssert",
				"CustomAssertTest#testWithoutCustomAssert");
		assertContentsEqual(
				new File(INPUT_DIR, "CustomAssertTest.java.expected"),
				new File(INPUT_DIR, "CustomAssertTest.java.fix"));
		//System.out.println(out.toString()); // useful for debugging
	}
	public static class CustomAssertFixer extends AssertFixer {
		public CustomAssertFixer(Factory factory) {
			super(factory);
		}

		@Override
		public CodeFixResult fix(
				Method testMethod, 
				CtInvocation<?> assertion, 
				Throwable failureException)
				throws UnfixableException {
			if ("myAssertTrue".equals(assertion.getExecutable().getSimpleName())) {
				// make sure correct test was executed
				assertEquals("public void CustomAssertTest.testWithCustomAssert()", testMethod.toString());
				// make sure assertion was instrumented correctly
				assertTrue(failureException instanceof RecordedAssertFailure);
				// make sure that the recorded value is correct
				assertEquals(false, ((RecordedAssertFailure) failureException).getArgs()[0]);
				// fix the instrumented assertion
				assertion.getExecutable().setSimpleName("myAssertFalse");
				
				return new CodeFixResult(assertion, 
						getFactory().Fragment().replace(assertion, assertion));
			}
			return null;
		}
		
		@Override
		public Collection<String> getMethodsToInstrument() {
			return Arrays.asList(
					"CustomAssertTest#myAssertTrue",
					"CustomAssertTest#myAssertFalse");
		}
	}
	
	@Test
	public void testJUnit4() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ReAssert.out = new PrintStream(out);
		ReAssert.main(
				"--sourcepath", INPUT_DIR.getPath(),
				"--fixlimit", "5",				
				"JUnit4TestInput#succeed",
				"JUnit4TestInput#unfixable",
				"JUnit4TestInput#fixOne",
				"JUnit4TestInput#fixMultiple",
				"JUnit4TestInput#multipleStepFix",
				"JUnit4TestInput#manyLineFix",
				"JUnit4TestInput#semiFixable",
				"JUnit4TestInput#throwsException",
				"JUnit4TestInput#callThrowsException",
				"JUnit4TestInput#callsMethods",
				"JUnit4TestInput#usesOtherFixer",
				"JUnit4TestInput#overFixLimit");
		assertContentsEqual(
				new File(INPUT_DIR, "JUnit4TestInput.java.expected"),
				new File(INPUT_DIR, "JUnit4TestInput.java.fix"));		
	}
	
	@Test
	public void testJUnit3() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ReAssert.out = new PrintStream(out);
		ReAssert.main(
				"--sourcepath", INPUT_DIR.getPath(),
				"--fixlimit", "5",				
				"JUnit3TestInput#testSucceed",
				"JUnit3TestInput#testUnfixable",
				"JUnit3TestInput#testFixOne",
				"JUnit3TestInput#testFixMultiple",
				"JUnit3TestInput#testMultipleStepFix",
				"JUnit3TestInput#testManyLineFix",
				"JUnit3TestInput#testSemiFixable",
				"JUnit3TestInput#testThrowsException",
				"JUnit3TestInput#testCallThrowsException",
				"JUnit3TestInput#testCallsMethods",
				"JUnit3TestInput#testOverFixLimit");
		assertContentsEqual(
				new File(INPUT_DIR, "JUnit3TestInput.java.expected"),
				new File(INPUT_DIR, "JUnit3TestInput.java.fix"));
	}
	
	@Test
	public void testFormatting() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ReAssert.out = new PrintStream(out);
		ReAssert.main(
				"--sourcepath", INPUT_DIR.getPath(),
				"FormattingTest#test");
		assertContentsEqual(
				new File(INPUT_DIR, "FormattingTest.java.expected"),
				new File(INPUT_DIR, "FormattingTest.java.fix"));
	}
	
	@Test
	public void testEmptySourcePath() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();
		ReAssert.out = new PrintStream(out);
		ReAssert.err = new PrintStream(err);
		ReAssert.main(
				"--sourcepath", ":",
				"SimpleTest#test");
		assertEquals("Source path does not exist: \n", err.toString());
	}

	@Test
	public void testBadSourcePath() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();
		ReAssert.out = new PrintStream(out);
		ReAssert.err = new PrintStream(err);
		ReAssert.main(
				"--sourcepath", "FAKE_PATH",
				"SimpleTest#test");
		assertEquals("Source path does not exist: FAKE_PATH\n", err.toString());
	}
	
	@Test
	public void testAdjustLines() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ReAssert.out = new PrintStream(out);
		ReAssert.main(
				"--sourcepath", INPUT_DIR.getPath(),
				"AdjustLinesTest#test",
				"AdjustLinesTest#test2");
		//System.out.println(out.toString());
		assertContentsEqual(
				new File(INPUT_DIR, "AdjustLinesTest.java.expected"),
				new File(INPUT_DIR, "AdjustLinesTest.java.fix"));
	}
	
	@Test
	public void testSnippets() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ReAssert.out = new PrintStream(out);
		ReAssert.main(
				"--sourcepath", INPUT_DIR.getPath(),
				"--snippets",
				"SnippetTest#test");
		assertEquals(
				"Fixing SnippetTest#test\n" +
				"    Used edu.illinois.reassert.assertfixer.AssertEqualsReplaceLiteralFixer\n" +
				"    At resources/sample-tests/SnippetTest.java:9\n" +
				"    Used edu.illinois.reassert.assertfixer.AssertEqualsReplaceLiteralFixer\n" +
				"    At resources/sample-tests/SnippetTest.java:10\n" +
				"    Used edu.illinois.reassert.assertfixer.AssertEqualsReplaceLiteralFixer\n" +
				"    At resources/sample-tests/SnippetTest.java:11\n" +
				"    Fixed!\n" +
				"Fixes saved to resources/sample-tests/SnippetTest.java.130.34.snip\n" +
				"Fixes saved to resources/sample-tests/SnippetTest.java.168.44.snip\n" +
				"Fixes saved to resources/sample-tests/SnippetTest.java.216.34.snip\n", out.toString());
		assertContentEquals(
				"assertEquals(\"actual\", \"actual\")",
				new File(INPUT_DIR, "SnippetTest.java.130.34.snip"));
		assertContentEquals(
				"assertEquals(\"actual\", \"actual\")",
				new File(INPUT_DIR, "SnippetTest.java.168.44.snip"));
		assertContentEquals(
				"assertEquals(\"actual\", \"actual\")",
				new File(INPUT_DIR, "SnippetTest.java.216.34.snip"));
	}
	
	public static class TestOutput {

		@Test
		public void testSucceed() {
			String testMethod = "JUnit4TestInput#succeed";
			String expectedResult = 
				"Fixing JUnit4TestInput#succeed\n" +
				"    Not fixed. Test succeeded without fix.\n";
			testOutput(testMethod, expectedResult);
		}
		
		@Test
		public void testUnfixable() {
			String testMethod = "JUnit4TestInput#unfixable";
			String expectedResult = 
				"Fixing JUnit4TestInput#unfixable\n" +
				"    Not Fixed. No applicable fix strategies.\n";
			testOutput(testMethod, expectedResult);
		}
		
		@Test
		public void testFixOne() {
			String testMethod = "JUnit4TestInput#fixOne";
			String expectedResult = 
				"Fixing JUnit4TestInput#fixOne\n" +
					"    Used edu.illinois.reassert.assertfixer.AssertEqualsReplaceLiteralFixer\n" +
					"    At resources/sample-tests/JUnit4TestInput.java:28\n" +
					"    Fixed!\n" +
					"Fixes saved to resources/sample-tests/JUnit4TestInput.java.fix\n";
			testOutput(testMethod, expectedResult);
		}
	
		@Test
		public void testFixMultiple() {
			String testMethod = "JUnit4TestInput#fixMultiple";
			String expectedResult = 
				"Fixing JUnit4TestInput#fixMultiple\n" +
					"    Used edu.illinois.reassert.assertfixer.AssertEqualsReplaceLiteralFixer\n" +
					"    At resources/sample-tests/JUnit4TestInput.java:33\n" +
					"    Used edu.illinois.reassert.assertfixer.AssertEqualsReplaceLiteralFixer\n" +
					"    At resources/sample-tests/JUnit4TestInput.java:34\n" +
					"    Used edu.illinois.reassert.assertfixer.AssertEqualsReplaceLiteralFixer\n" +
					"    At resources/sample-tests/JUnit4TestInput.java:35\n" +
					"    Fixed!\n" +
					"Fixes saved to resources/sample-tests/JUnit4TestInput.java.fix\n";
			testOutput(testMethod, expectedResult);
		}
	
		@Test
		public void testMultipleStepFix() {
			String testMethod = "JUnit4TestInput#multipleStepFix";
			String expectedResult = 
				"Fixing JUnit4TestInput#multipleStepFix\n" +
					"    Used edu.illinois.reassert.assertfixer.AssertCollectionSizeFixer\n" +
					"    At resources/sample-tests/JUnit4TestInput.java:41\n" +
					"    Used edu.illinois.reassert.assertfixer.AssertEqualsReplaceLiteralFixer\n" +
					"    At resources/sample-tests/JUnit4TestInput.java:41\n" +
					"    Fixed!\n" +
					"Fixes saved to resources/sample-tests/JUnit4TestInput.java.fix\n";
			testOutput(testMethod, expectedResult);
		}
	
		@Test
		public void testManyLineFix() {
			String testMethod = "JUnit4TestInput#manyLineFix";
			String expectedResult = 
				"Fixing JUnit4TestInput#manyLineFix\n" +
					"    Used edu.illinois.reassert.assertfixer.AssertEqualsExpandAccessorsFixer\n" +
					"    At resources/sample-tests/JUnit4TestInput.java:48\n" +
					"    Fixed!\n" +
					"Fixes saved to resources/sample-tests/JUnit4TestInput.java.fix\n";
			testOutput(testMethod, expectedResult);
		}
	
		@Test
		public void testSemiFixable() {
			String testMethod = "JUnit4TestInput#semiFixable";
			String expectedResult = 
				"Fixing JUnit4TestInput#semiFixable\n" +
					"    Used edu.illinois.reassert.assertfixer.AssertEqualsReplaceLiteralFixer\n" +
					"    At resources/sample-tests/JUnit4TestInput.java:53\n" +
					"    Used edu.illinois.reassert.assertfixer.AssertEqualsReplaceLiteralFixer\n" +
					"    At resources/sample-tests/JUnit4TestInput.java:54\n" +
					"    Used edu.illinois.reassert.assertfixer.AssertEqualsReplaceLiteralFixer\n" +
					"    At resources/sample-tests/JUnit4TestInput.java:55\n" +
					"    Not Fixed. No applicable fix strategies.\n" +
					"Fixes saved to resources/sample-tests/JUnit4TestInput.java.fix\n";
			testOutput(testMethod, expectedResult);
		}
	
		@Test
		public void testThrowsException() {
			String testMethod = "JUnit4TestInput#throwsException";
			String expectedResult = 
				"Fixing JUnit4TestInput#throwsException\n" +
					"    Not Fixed. No applicable fix strategies.\n";
			testOutput(testMethod, expectedResult);
		}
	
		@Test
		public void testCallThrowsException() {
			String testMethod = "JUnit4TestInput#callThrowsException";
			String expectedResult = 
				"Fixing JUnit4TestInput#callThrowsException\n" +
					"    Used edu.illinois.reassert.assertfixer.SurroundWithTryCatchFixer\n" +
					"    At resources/sample-tests/JUnit4TestInput.java:66\n" +
					"    Fixed!\n" +
					"Fixes saved to resources/sample-tests/JUnit4TestInput.java.fix\n";
			testOutput(testMethod, expectedResult);
		}
	
		@Test
		public void testCallsMethods() {
			String testMethod = "JUnit4TestInput#callsMethods";
			String expectedResult = 
				"Fixing JUnit4TestInput#callsMethods\n" +
					"    Used edu.illinois.reassert.assertfixer.AssertEqualsReplaceLiteralFixer\n" +
					"    At resources/sample-tests/JUnit4TestInput.java:75\n" +
					"    Used edu.illinois.reassert.assertfixer.AssertEqualsReplaceLiteralFixer\n" +
					"    At resources/sample-tests/JUnit4TestInput.java:78\n" +
					"    Fixed!\n" +
					"Fixes saved to resources/sample-tests/JUnit4TestInput.java.fix\n";
			testOutput(testMethod, expectedResult);
		}
	
		@Test
		public void testUsesOtherFixer() {
			String testMethod = "JUnit4TestInput#usesOtherFixer";
			String expectedResult = 
				"Fixing JUnit4TestInput#usesOtherFixer\n" +
					"    Used edu.illinois.reassert.assertfixer.InvertBooleanAssertFixer\n" +
					"    At resources/sample-tests/JUnit4TestInput.java:83\n" +
					"    Fixed!\n" +
					"Fixes saved to resources/sample-tests/JUnit4TestInput.java.fix\n";
			testOutput(testMethod, expectedResult);
		}
	
		@Test
		public void testOverFixLimit() {
			String testMethod = "JUnit4TestInput#overFixLimit";
			String expectedResult = 
				"Fixing JUnit4TestInput#overFixLimit\n" +
					"    Used edu.illinois.reassert.assertfixer.AssertEqualsReplaceLiteralFixer\n" +
					"    At resources/sample-tests/JUnit4TestInput.java:88\n" +
					"    Used edu.illinois.reassert.assertfixer.AssertEqualsReplaceLiteralFixer\n" +
					"    At resources/sample-tests/JUnit4TestInput.java:89\n" +
					"    Used edu.illinois.reassert.assertfixer.AssertEqualsReplaceLiteralFixer\n" +
					"    At resources/sample-tests/JUnit4TestInput.java:90\n" +
					"    Used edu.illinois.reassert.assertfixer.AssertEqualsReplaceLiteralFixer\n" +
					"    At resources/sample-tests/JUnit4TestInput.java:91\n" +
					"    Used edu.illinois.reassert.assertfixer.AssertEqualsReplaceLiteralFixer\n" +
					"    At resources/sample-tests/JUnit4TestInput.java:92\n" +
					"    Not Fixed. Reached limit on number of fixes.\n" +
					"Fixes saved to resources/sample-tests/JUnit4TestInput.java.fix\n";
			testOutput(testMethod, expectedResult);
		}
	
		private void testOutput(String testMethod, String expectedResult) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ReAssert.out = new PrintStream(out);
			ReAssert.main(
					"--sourcepath", INPUT_DIR.getPath(),
					"--fixlimit", "5",				
					testMethod);
			String result = out.toString();
			assertEquals(expectedResult, result);
		}
	
	}

}
