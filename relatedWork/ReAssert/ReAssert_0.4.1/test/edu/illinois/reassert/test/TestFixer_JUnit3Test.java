package edu.illinois.reassert.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;

import spoon.reflect.code.CtBlock;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtType;
import edu.illinois.reassert.CodeFixResult;
import edu.illinois.reassert.TestFixer;
import edu.illinois.reassert.UnfixableException;
import edu.illinois.reassert.testutil.IgnoredInner;

public class TestFixer_JUnit3Test {

	private static final String TEST_DIR = "test";
	
	@Test
	public void testAssertEquals() throws UnfixableException, IOException {
		fix(ToFixAssertEquals.class);
	}
	@RunWith(IgnoredInner.class)
	public static class ToFixAssertEquals extends TestCase {
		public void test() {
			assertEquals(1, 2);
		}
		public void fix() {
			assertEquals(2, 2);
		}
	}
	
	@Test
	public void testAssertEquals_Strings() throws UnfixableException, IOException {
		fix(ToFixAssertEquals_Strings.class);
	}
	@RunWith(IgnoredInner.class)
	public static class ToFixAssertEquals_Strings extends TestCase {
		public void test() {
			assertEquals("expected", "actual");
		}
		public void fix() {
			assertEquals("actual", "actual");
		}
	}

	@Test
	public void testAssertTrue() throws UnfixableException, IOException {
		fix(ToFixAssertTrue.class);
	}
	@RunWith(IgnoredInner.class)
	public static class ToFixAssertTrue extends TestCase {
		public void test() {
			assertTrue(false);
		}
		public void fix() {
			assertFalse(false);
		}
	}

	@Test
	public void testAssertFalse() throws UnfixableException, IOException {
		fix(ToFixAssertFalse.class);
	}
	@RunWith(IgnoredInner.class)
	public static class ToFixAssertFalse extends TestCase {
		public void test() {
			assertFalse(true);
		}
		public void fix() {
			assertTrue(true);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void fix(Class<?> testClass) throws UnfixableException, IOException {
		TestFixer fixer = new TestFixer();
		fixer.addSourcePath(TEST_DIR);
		
		CodeFixResult result = (CodeFixResult) fixer.fix(testClass, "test");
		if (result == null) {
			fail("No method was fixed");
		}
		CtElement fixedElement = result.getFixedElement();
		CtBlock actualFix = fixedElement.getParent(CtExecutable.class).getBody();
		CtBlock expectedFix = fixedElement.getParent(CtType.class).getMethod("fix").getBody();
		assertEquals(expectedFix.toString(), actualFix.toString());
	}
	

}
