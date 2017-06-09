package edu.illinois.reassert.test.assertfixer;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import spoon.reflect.declaration.CtType;
import edu.illinois.reassert.CodeFixResult;
import edu.illinois.reassert.TestFixer;
import edu.illinois.reassert.UnfixableException;
import edu.illinois.reassert.testutil.Box;
import edu.illinois.reassert.testutil.Fix;
import edu.illinois.reassert.testutil.FixChecker;
import edu.illinois.reassert.testutil.IgnoredInner;

@RunWith(FixChecker.class)
public class TraceDeclarationsFixerTest {

	@Test
	public void testTraceVariable() {
		String expected = "expected";
		assertEquals(expected, "actual");
	}
	@Fix("testTraceVariable") 
	public void fixTraceVariable() {
		String expected = "actual";
		assertEquals(expected, "actual");
	}
	
	@Test
	public void testTraceMultipleVariables() {
		String expected = "expected";
		String expected2 = expected;
		String expected3 = expected2;
		assertEquals(expected3, "actual");
	}
	@Fix("testTraceMultipleVariables")
	public void fixTraceMultipleVariables() {
		String expected = "actual";
		String expected2 = expected;
		String expected3 = expected2;
		assertEquals(expected3, "actual");
	}
	
	@Test
	public void testTraceField() throws IOException, UnfixableException {
		TestFixer fixer = new TestFixer();
		fixer.addSourcePath("test");
		CodeFixResult fix = (CodeFixResult) 
			fixer.fix(ToFixTraceField.class, "test");
		CtType<?> fixedClass = fix.getFixedElement().getParent(CtType.class);
		assertEquals("private java.lang.String expected = \"actual\";", fixedClass.getField("expected").toString());
		assertEquals(
				"@org.junit.Test\n" +
				"public void test() {\n" +
				"	java.lang.String expected = ToFixTraceField.this.expected;\n" +
				"	org.junit.Assert.assertEquals(expected ,\"actual\");\n" +
				"}", 
				fixedClass.getMethod("test").toString());
	}
	@RunWith(IgnoredInner.class)
	public static class ToFixTraceField {
		private String expected = "expected";
		@Test
		public void test() {
			String expected = this.expected;
			assertEquals(expected, "actual");
		}
	}
	
	@Test
	public void testTraceParameter() {
		helper("expected");
	}
	@Fix("testTraceParameter")
	public void fixTraceParameter() {
		helper("actual");
	}
	private void helper(String expected) {
		assertEquals(expected, "actual");
	}
	
	@Test
	public void testTraceParameter_InExpression() {
		int i = helperWithReturn("expected");		
	}
	@Fix("testTraceParameter_InExpression")
	public void fixTraceParameter_InExpression() {
		int i = helperWithReturn("actual");
	}
	private int helperWithReturn(String expected) {
		assertEquals(expected, "actual");
		return -1;
	}
	
	@Test
	@Ignore("Known bug")
	public void testTraceParameter_Nested() {
		outer(outer(helperWithReturn("expected")));
	}
	@Fix("testTraceParameter_Nested")
	public void fixTraceParameter_Nested() {
		outer(outer(helperWithReturn("actual")));
	}
	private int outer(int in) {
		return in;
	}
	
	@Test
	public void testMultipleParameters() {
		helper("actual", "expected", "actual");
	}
	@Fix("testMultipleParameters")
	public void fixMultipleParameters() {
		helper("actual", "actual", "actual");
	}
	
	@Test
	public void testMultipleParameters_Last() {
		helper("actual", "actual", "expected");
	}
	@Fix("testMultipleParameters_Last")
	public void fixMultipleParameters_Last() {
		helper("actual", "actual", "actual");
	}
	
	private void helper(String arg0, String arg1, String arg2) {
		assertEquals(arg0, "actual");
		assertEquals(arg1, "actual");
		assertEquals(arg2, "actual");
	}
	
	@Test
	public void testVariableAndParameter() {
		String expected = "expected";
		helper(expected);
	}
	@Fix("testVariableAndParameter")
	public void fixVariableAndParameter() {
		String expected = "actual";
		helper(expected);
	}
	
	/**
	 * Doesn't trace through branches. 
	 * Need deeper analysis for that.
	 * Defaults to replace literal in assertion.   
	 */
	@Test
	public void testBranching() {
		String actual = "actual";
		String expected;
		if (true) {
			expected = "expected";
		}
		else {
			expected = "expected";
		}
		assertEquals(expected, actual);
	}
	@Fix("testBranching")
	public void fixBranching() {
		String actual = "actual";
		String expected;
		if (true) {
			expected = "expected";
		}
		else {
			expected = "expected";
		}
		assertEquals("actual", actual);
	}
	
	/**
	 * Does not trace back for reference types
	 */
	@Test
	public void testNotLiterable() {
		Box expected = new Box(5);
		Box actual = new Box(4);
		assertEquals(expected, actual);
	}
	@Fix("testNotLiterable") 
	public void fixNotLiterable() {
		Box expected = new Box(5);
		Box actual = new Box(4);
		{
			assertEquals("Box(4)" ,actual.toString());
			assertEquals(4 ,actual.getValue());
		}
	}
	
	@Ignore("Currently (2009-05-14) undefined behavior")
	@Test
	public void testMultipleDefinitions() {
		int expected = 1;
		expected = 2;
		assertEquals(expected, 3);
	}
	@Fix("testMultipleDefinitions")
	public void fixMultipleDefinitions() {
		int expected = 1;
		expected = 3;
		assertEquals(expected, 3);
	}

	/**
	 * Cannot modify source of {@link File}.
	 * Default to replace literal
	 */
	@Test
	public void testExternalVariable() {
		String actual = "actual";
		assertEquals(File.separator, actual);
	}
	@Fix("testExternalVariable")
	public void fixExternalVariable() {
		String actual = "actual";
		assertEquals("actual", actual);
	}
	
	@Test
	public void testNestedInvocation() {
		helper("actual".toString(), "expected", "actual");
	}
	@Fix("testNestedInvocation")
	public void fixNestedInvocation() {
		helper("actual".toString(), "actual", "actual");
	}
}
