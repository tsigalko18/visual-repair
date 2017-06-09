package edu.illinois.reassert.test.assertfixer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.illinois.reassert.CodeFixResult;
import edu.illinois.reassert.TestFixer;
import edu.illinois.reassert.UnfixableException;
import edu.illinois.reassert.assertfixer.AssertEqualsExpandAccessorsFixer;
import edu.illinois.reassert.assertfixer.AssertNullToAssertEqualsFixer;
import edu.illinois.reassert.testutil.Box;
import edu.illinois.reassert.testutil.Fix;
import edu.illinois.reassert.testutil.FixChecker;
import edu.illinois.reassert.testutil.IgnoredInner;

@RunWith(FixChecker.class)
public class AssertNullToAssertEqualsFixerTest {

	@Test
	public void testLiteral() {
		assertNull("actual");
	}
	@Fix("testLiteral")
	public void fixLiteral() {
		assertEquals(null, "actual");
	}
	
	@Test
	public void testVariable() {
		String actual = "actual";
		assertNull(actual);
	}
	@Fix("testVariable")
	public void fixVariable() {
		String actual = "actual";
		assertEquals(null, actual);
	}
	
	@Test
	public void testCompleteFix() throws UnfixableException {
		TestFixer fixer = new TestFixer();
		fixer.addSourcePath(FixChecker.SOURCE_DIR);
		
		String testName = ToFixCompleteFix.class.getName() + "#test";
		CodeFixResult result;
		// convert to assertEquals(null, actual)
		result = (CodeFixResult) fixer.fix(testName);
		assertNotNull(result);
		assertTrue(result.getAppliedFixer() instanceof AssertNullToAssertEqualsFixer);
		// fix assertEquals
		result = (CodeFixResult) fixer.fix(testName);
		assertNotNull(result);
		assertTrue(result.getAppliedFixer() instanceof AssertEqualsExpandAccessorsFixer);
		// successful fix
		result = (CodeFixResult) fixer.fix(testName);
		assertNull(result);
	}
	@RunWith(IgnoredInner.class)
	public static class ToFixCompleteFix {
		@Test
		public void test() {
			Box actual = new Box(5);
			assertNull(actual);
		}
	}
	
}
