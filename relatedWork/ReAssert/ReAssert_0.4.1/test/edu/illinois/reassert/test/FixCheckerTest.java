package edu.illinois.reassert.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.ComparisonFailure;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

import edu.illinois.reassert.assertfixer.InvertBooleanAssertFixer;
import edu.illinois.reassert.testutil.Fix;
import edu.illinois.reassert.testutil.FixChecker;
import edu.illinois.reassert.testutil.Fixers;
import edu.illinois.reassert.testutil.IgnoredInner;
import edu.illinois.reassert.testutil.Unfixable;


public class FixCheckerTest {

	private RunNotifier notifier;
	private RecordingListener listener;
	
	@Before
	public void initListeners() {
		notifier = new RunNotifier();
		listener = new RecordingListener();
		notifier.addListener(listener);
	}
	public static class RecordingListener extends RunListener {
		private boolean isFinished = false;
		private Failure failure = null;

		@Override
		public void testFailure(Failure failure) throws Exception {
			this.failure = failure;
		}
		
		@Override
		public void testFinished(Description description) throws Exception {
			this.isFinished = true;
		}

		public boolean failed() {
			return failure != null;
		}
		
		public Failure getFailure() {
			return failure;
		}
		
		public boolean completed() {
			return isFinished;
		}
	}
	
	@Test
	public void testSucceed() throws InitializationError {
		FixChecker checker = new FixChecker(SuccessClass.class);
		checker.run(notifier);
		assertFalse(listener.failed());
		assertTrue(listener.completed());
		
	}
	@RunWith(IgnoredInner.class)
	public static class SuccessClass {
		@Test 
		public void test() {
			// do nothing
		}
	}
	
	@Test
	public void testSucceedWithFix() throws InitializationError {
		FixChecker checker = new FixChecker(SuccessWithFixClass.class);
		checker.run(notifier);
		assertTrue(listener.failed());
		assertEquals(
				"test succeeded but has @Fix method", 
				listener.getFailure().getMessage()); 
		assertTrue(listener.completed());
		
	}
	@RunWith(IgnoredInner.class)
	public static class SuccessWithFixClass {
		@Test 
		public void test() {
			// do nothing
		}
		@Fix("test")
		public void fix() {
			// do nothing
		}
	}
	
	@Test
	public void testFailWithoutFix() throws InitializationError {
		FixChecker checker = new FixChecker(FailWithoutFixClass.class);
		checker.run(notifier);
		assertTrue(listener.failed());
		assertEquals(
				"No method annotated with @Fix(\"test\") was found.", 
				listener.getFailure().getMessage()); 
		assertTrue(listener.completed());
	}
	@RunWith(IgnoredInner.class)
	public static class FailWithoutFixClass {
		@Test
		public void test() {
			assertEquals(1,2);
		}
	}
	
	@Test
	public void testFailWithFix() throws InitializationError {
		FixChecker checker = new FixChecker(FailWithFixClass.class);
		checker.run(notifier);
		assertFalse(listener.failed());
		assertTrue(listener.completed());
		
	}
	@RunWith(IgnoredInner.class)
	public static class FailWithFixClass {
		@Test
		public void test() {
			assertEquals(1,2);
		}
		@Fix("test")
		public void fix() {
			assertEquals(2,2);
		}
	}
	
	@Test
	public void testFixDoesNotMatch() throws InitializationError {
		FixChecker checker = new FixChecker(FixDoesNotMatchClass.class);
		checker.run(notifier);
		assertTrue(listener.failed());
		Failure failure = listener.getFailure();
		assertTrue(failure.getException() instanceof ComparisonFailure);
		String message = failure.getMessage();
		assertTrue(message.startsWith(
				"Fixed method test does not match @Fix(\"test\") method")); 
		assertTrue(listener.completed());
	}
	@RunWith(IgnoredInner.class)
	public static class FixDoesNotMatchClass {
		@Test
		public void test() {
			assertEquals(1,2);
		}
		@Fix("test")
		public void fix() {
			assertEquals(4,4);
		}
	}
	
	@Test
	public void testExpectedException() throws InitializationError {
		FixChecker checker = new FixChecker(ExpectedExceptionClass.class);
		checker.run(notifier);
		assertFalse(listener.failed());
		assertNull(listener.getFailure());
		assertTrue(listener.completed());
	}
	@RunWith(IgnoredInner.class)
	public static class ExpectedExceptionClass {
		@Test(expected=RuntimeException.class)
		public void test() {
			throw new RuntimeException();
		}
	}
	
	@Test
	public void testMarkedUnfixable() throws InitializationError {
		FixChecker checker = new FixChecker(MarkedUnfixableClass.class);
		checker.run(notifier);
		assertFalse(listener.failed());
		assertNull(listener.getFailure());
		assertTrue(listener.completed());
	}
	@RunWith(IgnoredInner.class)
	public static class MarkedUnfixableClass {
		@Test
		@Unfixable
		public void test() {
			fail();
		}
	}
	
	@Test
	public void testUnmarkedUnfixable() throws InitializationError {
		FixChecker checker = new FixChecker(UnmarkedUnfixableClass.class);
		checker.run(notifier);
		assertTrue(listener.failed());
		String failureMessage = listener.getFailure().getMessage();
		assertEquals(
				"Unable to fix test. No applicable fix strategies.\n" +
				"Mark with @Unfixable if this was expected", 
				failureMessage);
		assertTrue(failureMessage.endsWith(
				"Mark with @Unfixable if this was expected")); 
		assertTrue(listener.completed());
	}
	@RunWith(IgnoredInner.class)
	public static class UnmarkedUnfixableClass {
		@Test
		public void test() {
			fail();
		}
	}
	
	@Test
	public void testFixableMarkedUnfixable() throws InitializationError {
		FixChecker checker = new FixChecker(FixableMarkedUnfixable.class);
		checker.run(notifier);
		assertTrue(listener.failed());
		assertEquals(
				"Fix was applied but test is marked @Unfixable.", 
				listener.getFailure().getMessage()); 
		assertTrue(listener.completed());
	}
	@RunWith(IgnoredInner.class)
	public static class FixableMarkedUnfixable {
		@Test
		@Unfixable
		public void test() {
			assertEquals(1, 2);
		}
	}
	
	@Test
	public void testFixersAnnotation() throws InitializationError {
		FixChecker checker = new FixChecker(NoFixersClass.class);
		checker.run(notifier);
		assertTrue(listener.failed()); // boolean fixer can't fix assertEquals
		String failureMessage = listener.getFailure().getMessage();
		assertEquals(
				"Unable to fix test. No applicable fix strategies.\n" +
				"Mark with @Unfixable if this was expected", 
				failureMessage);
		assertTrue(listener.completed());		
	}
	@RunWith(IgnoredInner.class)
	@Fixers(InvertBooleanAssertFixer.class)
	public static class NoFixersClass {
		@Test
		public void test() {
			assertEquals(1,2);
		}
		@Fix("test")
		public void fix() {
			assertEquals(2,2);
		}
	}
}