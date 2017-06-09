package edu.illinois.reassert.test.assertfixer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import spoon.reflect.code.CtBlock;
import spoon.reflect.factory.MethodFactory;
import edu.illinois.reassert.CodeFixResult;
import edu.illinois.reassert.TestFixer;
import edu.illinois.reassert.UnfixableException;
import edu.illinois.reassert.testutil.Box;
import edu.illinois.reassert.testutil.FixChecker;
import edu.illinois.reassert.testutil.IgnoredInner;

public class RemoveNondeterministicFixerTest {

	private TestFixer fixer;
	
	@Before
	public void init() {
		fixer = new TestFixer();
		fixer.addSourcePath(FixChecker.SOURCE_DIR);
	}

	public static class NondeterministicBox extends Box {
		public NondeterministicBox(Object value) {
			super(value);
		}
		
		public long nondeterministic() {
			return System.currentTimeMillis();
		}
	}

	public static class Nondeterministic  {
		public long nondeterministic() {
			return System.currentTimeMillis();
		}
	}
		
	@Test
	public void testSimpleBlock() throws UnfixableException, SecurityException, NoSuchMethodException {
		testRemoveNondeterminism(ToFixSimpleBlock.class, 1);
	}
	@RunWith(IgnoredInner.class)
	public static class ToFixSimpleBlock {
		@Test
		public void test() {
			NondeterministicBox actual = new NondeterministicBox(5);
			assertEquals(null, actual);
		}
		public void fix() {
			NondeterministicBox actual = new NondeterministicBox(5);
			{
				// no nondeterministic accessor here
				assertEquals("NondeterministicBox(5)", actual.toString());
				assertEquals(5, actual.getValue());
			}
		}
	}

	@Test
	public void testNested() throws UnfixableException, SecurityException, NoSuchMethodException {
		testRemoveNondeterminism(ToFixNested.class, 2);
	}
	@RunWith(IgnoredInner.class)
	public static class ToFixNested {
		@Test
		public void test() {
			NondeterministicBox actual = new NondeterministicBox(new NondeterministicBox(5));
			assertEquals(null, actual);
		}
		public void fix() {
			NondeterministicBox actual = new NondeterministicBox(new NondeterministicBox(5));
			{
				assertEquals("NondeterministicBox(NondeterministicBox(5))", actual.toString());
				// no nondeterminism here
				{
					NondeterministicBox nondeterministicbox0 = (NondeterministicBox)(actual.getValue());
					// or here
					assertEquals("NondeterministicBox(5)", nondeterministicbox0.toString());
					assertEquals(5, nondeterministicbox0.getValue());
				}
			}
		}
	}

	@Test
	public void testParallel() throws UnfixableException, NoSuchMethodException {
		testRemoveNondeterminism(ToFixParallel.class, 2);
	}
	@RunWith(IgnoredInner.class) 
	public static class ToFixParallel {
		@Test
		public void test() {
			Pair actual = new Pair();
			assertEquals(null, actual);
		}
		public void fix() {
			Pair actual = new Pair();			
			{
				{
					NondeterministicBox nondeterministicbox0 = (NondeterministicBox)(actual.left());
					assertEquals("NondeterministicBox(5)", nondeterministicbox0.toString());
					assertEquals(5, nondeterministicbox0.getValue());
				}
				{
					NondeterministicBox nondeterministicbox1 = (NondeterministicBox)(actual.right());
					assertEquals("NondeterministicBox(6)", nondeterministicbox1.toString());
					assertEquals(6, nondeterministicbox1.getValue());
				}
			}
		}
	}
	public static class Pair {
		public Object left() {
			return new NondeterministicBox(5);
		}
		public Object right() {
			return new NondeterministicBox(6);
		}
	}
	
	/**
	 * The pretty-printer splits strings containing multiple lines
	 */
	@Test
	public void testMultiline() throws UnfixableException, NoSuchMethodException {
		testRemoveNondeterminism(ToFixMultiline.class, 1);
	}	
	@RunWith(IgnoredInner.class)
	public static class ToFixMultiline {
		@Test
		public void test() {
			MultiLine actual = new MultiLine();
			assertEquals(null, actual);
		}
		public void fix() {
			MultiLine actual = new MultiLine();
			{
				assertEquals("mutliple\nline\nvalue", actual.multiline1());
				assertEquals("mutliple\nline\nvalue", actual.multiline3());
			}
		}
	}
	public static class MultiLine {
		public String multiline1() {
			return "mutliple\nline\nvalue";
		}
		public String multiline2() {
			// nondeterministic
			return System.currentTimeMillis() + "\n" + System.currentTimeMillis();
		}
		public String multiline3() {
			return "mutliple\nline\nvalue";
		}
	}
	
	@Test
	public void testRemoveInCatch() throws UnfixableException, NoSuchMethodException {
		testRemoveNondeterminism(ToFixRemoveInCatch.class, 3);
	}
	@RunWith(IgnoredInner.class)
	public static class ToFixRemoveInCatch {
		@Test
		public void test() {
			throwsException();
		}
		public void fix() {
			try {
				throwsException();
				fail();
			} 
			catch (RuntimeException e) {
				assertNull(e.getCause());
				// no nondeterminism here
			};
		}
		private void throwsException() {
			throw new RuntimeException(String.valueOf(System.currentTimeMillis()));
		}
	}

	@Test
	@Ignore("Known bug. Spoon does not apply snippets correctly.")
	public void testRemoveInCatch_Multiple() throws UnfixableException, NoSuchMethodException {
		testRemoveNondeterminism(ToFixRemoveInCatch_Multiple.class, 6);
	}
	@RunWith(IgnoredInner.class)
	public static class ToFixRemoveInCatch_Multiple {
		@Test
		public void test() {
			throwsException();
			assertTrue(true);
			throwsException();
		}
		public void fix() {
			try {
				throwsException();
				fail();
			} 
			catch (RuntimeException e) {
				assertNull(e.getCause());
				// no nondeterminism here
			};
			assertTrue(true);
			try {
				throwsException();
				fail();
			} 
			catch (RuntimeException e) {
				assertNull(e.getCause());
				// no nondeterminism here
			};
		}
		private void throwsException() {
			throw new RuntimeException(String.valueOf(System.currentTimeMillis()));
		}
	}
	
	@Test
	public void testUnfixableNondeterminism() throws UnfixableException, NoSuchMethodException {
		String testName = ToFixUnfixableNondeterminism.class.getName() + "#test";
		
		CodeFixResult result;
		result = (CodeFixResult) fixer.fix(testName);
		assertNotNull(result);
		result = (CodeFixResult) fixer.fix(testName);
		assertNotNull(result);
		result = (CodeFixResult) fixer.fix(testName);		
		assertNotNull(result);
		// would continue to limit
	}
	@RunWith(IgnoredInner.class)
	public static class ToFixUnfixableNondeterminism {
		@Test
		public void test() {
			{ // not created by ReAssert, so it won't be removed
				assertEquals(null, System.currentTimeMillis());
			}
		}
	}
	
	@Test(expected=UnfixableException.class)
	public void testRemoveAll() throws UnfixableException, NoSuchMethodException {
		testRemoveNondeterminism(ToFixRemoveAll.class, 1);
	}
	@RunWith(IgnoredInner.class)
	public static class ToFixRemoveAll{
		@Test
		public void test() {
			Nondeterministic actual = new Nondeterministic();
			assertEquals(null, actual);
		}
	}

	@Test(expected=UnfixableException.class)
	public void testRemoveAll_WithVariable() throws UnfixableException, NoSuchMethodException {
		testRemoveNondeterminism(ToFixRemoveAll_WithVariable.class, 1);
	}
	@RunWith(IgnoredInner.class)
	public static class ToFixRemoveAll_WithVariable {
		@Test
		public void test() {
			assertEquals(null, new Nondeterministic());
		}
	}
	
	@Test(expected = UnfixableException.class)
	public void testRemoveAll_Nested() throws UnfixableException, NoSuchMethodException {
		testRemoveNondeterminism(ToFixRemoveAll_Nested.class, 2);
	}
	@RunWith(IgnoredInner.class)
	public static class ToFixRemoveAll_Nested {
		@Test
		public void test() {
			Box actual = new Box(new Nondeterministic());
			assertEquals(null, actual);
		}
	}

	private void testRemoveNondeterminism(Class<?> testClass, int fixes)
			throws UnfixableException, NoSuchMethodException {
		String testName = testClass.getName() + "#test";
		
		CodeFixResult result;
		// fix by expansion and introduce nondeterministic assertion
		result = (CodeFixResult) fixer.fix(testName);
//		System.out.println(result.getFixedElement().getParent(CtMethod.class).toString());
		assertNotNull(result);
		for (int i = 0; i < fixes; i++) {
			// fix again and remove nondeterministic assertion
			result = (CodeFixResult) fixer.fix(testName);
//			System.out.println(result.getFixedElement().getParent(CtMethod.class).toString());
			assertNotNull(result);
		}
		// successful fix
		result = (CodeFixResult) fixer.fix(testName);		
		assertNull(result);
		
		// check that "test" and "fix" bodies match
		MethodFactory methodFactory = fixer.getFactory().Method();
		Method expected = testClass.getMethod("fix");
		CtBlock<?> expectedBody = methodFactory.createReference(expected).getDeclaration().getBody();
		Method actual = testClass.getMethod("test");
		CtBlock<?> actualBody = methodFactory.createReference(actual).getDeclaration().getBody();
		assertEquals(expectedBody.toString(), actualBody.toString());
	}
	
}
