package edu.illinois.reassert.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;
import edu.illinois.reassert.AssertFixer;
import edu.illinois.reassert.CodeFixResult;
import edu.illinois.reassert.FixResult;
import edu.illinois.reassert.RecordedAssertFailure;
import edu.illinois.reassert.TestFixer;
import edu.illinois.reassert.UnfixableException;
import edu.illinois.reassert.assertfixer.InvertBooleanAssertFixer;
import edu.illinois.reassert.reflect.Factory;
import edu.illinois.reassert.test.reflect.SimpleSpoonLoaderTest;
import edu.illinois.reassert.testutil.IgnoredInner;
import edu.illinois.reassert.testutil.PackageAccessTest;

public class TestFixerTest {

	private static final String TEST_DIR = "test";
	
	private TestFixer fixer;

	@Before
	public void initFixer() throws IOException {
		fixer = new TestFixer();
		fixer.addSourcePath(TEST_DIR);		
	}

	@Test
	public void testNoClass() throws UnfixableException, ClassNotFoundException {
		try {
			fixer.fix("FakeClass#fakeMethod");
			fail();
		}
		catch (UnfixableException e) {
			assertEquals("No class FakeClass.", e.getMessage());
		}
	}
	
	@Test
	public void testNoTests() {
		try {
			fixer.fix(ToFixNoTests.class, "fakeMethod");
			fail();
		}
		catch (UnfixableException e) {
			assertEquals(
					"No method edu.illinois.reassert.test.TestFixerTest$ToFixNoTests#fakeMethod.", 
					e.getMessage());
		}
	}
	public static class ToFixNoTests {}
	
	@Test
	public void testCannotRun() {
		try {
			fixer.fix(ToFixCannotRun.class, "test");
			fail();
		}
		catch (UnfixableException e) {
			assertEquals(
					"No runnable methods.", 
					e.getMessage());
		}
	}
	public static class ToFixCannotRun {
		/*omitted @Test*/public void test() {
			fail(); // should not reach this
		}
	}
		
	@Test
	public void testFixSucceed() throws UnfixableException {
		assertNull(fixer.fix(ToFixSucceed.class, "succeed"));
	}
	@RunWith(IgnoredInner.class)
	public static class ToFixSucceed {
		@Test public void succeed() {
			// succeeds
		}
	}
	
	@Test
	public void testThrownException() {
		try {
			fixer.fix(ToFixThrownException.class, "test");		
			fail();
		}
		catch (UnfixableException e) {
			assertEquals(
					"No applicable fix strategies.", 
					e.getMessage());
		}
	}
	@RunWith(IgnoredInner.class)
	public static class ToFixThrownException {
		@Test public void test() {
			throw new RuntimeException();
		}
	}
	
	@Test
	public void testNoApplicableFixers() {
		try {
			fixer.fix(ToFixNoApplicableFixers.class, "test");	
			fail();
		}
		catch (UnfixableException e) {
			assertEquals(
					"No applicable fix strategies.", 
					e.getMessage());
		}
	}
	@RunWith(IgnoredInner.class)
	public static class ToFixNoApplicableFixers {
		@Test public void test() {
			fail();
		}
	}
	
	@Test
	public void testFixOne() throws UnfixableException {
		CodeFixResult result = (CodeFixResult) fixer.fix(ToFixOne.class, "test");		
		assertEquals(
				ToFixOne.testFixed, 
				result.getFixedElement().getParent(CtMethod.class).toString());
		assertEquals(
				"org.junit.Assert.assertTrue(true)", 
				result.getFixedElement().toString());
	}
	@RunWith(IgnoredInner.class)
	public static class ToFixOne {
		@Test public void test() {
			assertFalse(true);
		}
		public static final String testFixed = 
			"@org.junit.Test\n" +
			"public void test() {\n" +
			"	org.junit.Assert.assertTrue(true);\n" +
			"}";
	}
	
	@Test
	public void testFixMany() throws UnfixableException, ClassNotFoundException {		
		CodeFixResult result; 
		result = (CodeFixResult) fixer.fix(ToFixMany.class.getName() + "#test");
		result = (CodeFixResult) fixer.fix(ToFixMany.class.getName() + "#test");		
		result = (CodeFixResult) fixer.fix(ToFixMany.class.getName() + "#test");		
		assertEquals(
				ToFixMany.testFixed, 
				result.getFixedElement().getParent(CtMethod.class).toString());
	}
	@RunWith(IgnoredInner.class)
	public static class ToFixMany {
		@Test public void test() {
			Assert.assertFalse(true);
			Assert.assertFalse(true);
			Assert.assertFalse(true);
		}
		public static final String testFixed = 
			"@org.junit.Test\n" +
			"public void test() {\n" +
			"	org.junit.Assert.assertTrue(true);\n" +
			"	org.junit.Assert.assertTrue(true);\n" +
			"	org.junit.Assert.assertTrue(true);\n" +
			"}";
	}
	
	@Test
	public void testCustomAssert() throws UnfixableException, NoSuchMethodException {
		CustomAssertFixer assertFixer = new CustomAssertFixer(fixer.getFactory()); 
		
		fixer.prependFixStrategy(assertFixer);
		CodeFixResult result = (CodeFixResult) fixer.fix(ToFixCustomAssert.class, "test");
				
		assertSame(assertFixer, result.getAppliedFixer());
		assertEquals("myAssert(true)", result.getFixedElement().toString());
	}
	@RunWith(IgnoredInner.class)
	public static class ToFixCustomAssert {
		@Test
		public void test() {
			myAssert(false);
		}
		public void myAssert(boolean b) {
			assertTrue(b);
		}
	}
	
	/**
	 * Verifies that adding a new fix strategy does not clobber the default list
	 * of strategies. 
	 */
	@Test
	public void testUnusedCustomFixer() throws UnfixableException {
		CustomAssertFixer assertFixer = new CustomAssertFixer(fixer.getFactory()); 
		
		fixer.addFixStrategy(assertFixer);
		CodeFixResult result = (CodeFixResult) fixer.fix(ToFixUnusedCustomFixer.class, "test");
		
		assertEquals(InvertBooleanAssertFixer.class, result.getAppliedFixer().getClass());
		assertEquals("org.junit.Assert.assertFalse(false)", result.getFixedElement().toString());
	}
	@RunWith(IgnoredInner.class)
	public static class ToFixUnusedCustomFixer {
		@Test
		public void test() {
			assertTrue(false);
		}
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
			if ("myAssert".equals(assertion.getExecutable().getSimpleName())) {
				assertTrue(failureException instanceof RecordedAssertFailure);
				assertEquals(false, ((RecordedAssertFailure) failureException).getArgs()[0]);
				assertion.getArguments().set(0, 
						assertion.getFactory().Code().createLiteral(true));
				return new CodeFixResult(assertion,null);
			}
			return null;
		}
		
		@Override
		public Collection<String> getMethodsToInstrument() {
			return Arrays.asList(
					ToFixCustomAssert.class.getName() + "#myAssert");
		}
	}
	
	/**
	 * See {@link SimpleSpoonLoaderTest#testPackageAccess()}
	 */
	@Test()
	public void testPackageAccess() throws UnfixableException {
		FixResult result = fixer.fix(
				PackageAccessTest.class.getName() + "#testFail");
		assertNotNull(result);
	}
	
}
