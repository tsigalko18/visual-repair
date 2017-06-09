package edu.illinois.reassert.test.assertfixer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.illinois.reassert.RecordedAssertFailure;
import edu.illinois.reassert.testutil.Fix;
import edu.illinois.reassert.testutil.FixChecker;
import edu.illinois.reassert.testutil.Unfixable;


@RunWith(FixChecker.class)
public class SurroundWithTryCatchFixerTest {

	@Test
	@Unfixable
	public void testThrownException() {
		throw new RuntimeException();
	}
	
	@Test
	@Unfixable
	public void testIgnoreAssertFailure() {
		fail();
	}
	
	/**
	 * Want to ignore recorded failures because 
	 * another fixer instrumented the method
	 */
	@Test
	@Unfixable
	public void testIgnoreRecordedFailure() {
		recordedFailingCall();
	}
	private void recordedFailingCall() {
		throw new RecordedAssertFailure(null);
	}
	
	/**
	 * Want to ignore {@link java.lang.Error}s, since
	 * they usually indicate deeper problems. 
	 */
	@Test
	@Unfixable
	public void testIgnoreErrors() {
		error();
	}
	private void error() {
		throw new Error();
	}
	
	@Test
	public void testFailingCall() {
		failingCall();
	}
	@Fix("testFailingCall")
	public void fixFailingCall() {
		try {
			failingCall();
			fail();
		} 
		catch (RuntimeException e) {
			assertNull(e.getCause());
			assertEquals("java.lang.RuntimeException: message" ,e.toString());
			assertEquals("message" ,e.getMessage());
			assertEquals("message" ,e.getLocalizedMessage());
		}
	}

	@Test
	public void testMultipleStatements() {
		System.currentTimeMillis();
		System.currentTimeMillis();
		failingCall();
		System.currentTimeMillis();
		System.currentTimeMillis();
	}
	@Fix("testMultipleStatements")
	public void fixMultipleStatements() {
		System.currentTimeMillis();
		System.currentTimeMillis();
		try {
			failingCall();
			fail();
		} catch (java.lang.RuntimeException e) {
			assertNull(e.getCause());
			assertEquals("java.lang.RuntimeException: message" ,e.toString());
			assertEquals("message" ,e.getMessage());
			assertEquals("message" ,e.getLocalizedMessage());
		}
		System.currentTimeMillis();
		System.currentTimeMillis();
	}
	
	@Test
	public void testNestedBlocks() {
		if (true) {
			do {
				{
					failingCall();
				}
			} while (false);
		}
	}
	@Fix("testNestedBlocks")
	public void fixNestedBlocks() {
		if (true) {
			do {
				{
					try {
						failingCall();
						fail();
					} catch (java.lang.RuntimeException e) {
						assertNull(e.getCause());
						assertEquals("java.lang.RuntimeException: message" ,e.toString());
						assertEquals("message" ,e.getMessage());
						assertEquals("message" ,e.getLocalizedMessage());
					}
				}
			} while (false);
		} 
	}
	
	@Test
	public void testModifyOnlyTestMethod() {
		indirectFailingCall();
	}
	@Fix("testModifyOnlyTestMethod") 
	public void fixModifyOnlyTestMethod() {
		try {
			indirectFailingCall();
			fail();
		} catch (java.lang.RuntimeException e) {
			assertNull(e.getCause());
			assertEquals("java.lang.RuntimeException: message" ,e.toString());
			assertEquals("message" ,e.getMessage());
			assertEquals("message" ,e.getLocalizedMessage());
		}
	}
	
	@Test
	public void testExceptionWithCause() {
		failWithCause();
	}
	@Fix("testExceptionWithCause")
	public void fixExceptionWithCause() {
		try {
			failWithCause();
			fail();
		} catch (java.lang.RuntimeException e) {
			{
				RuntimeException runtimeexception0 = ((RuntimeException)(e.getCause()));
				assertNull(runtimeexception0.getCause());
				assertEquals("java.lang.RuntimeException: cause" ,runtimeexception0.toString());
				assertEquals("cause" ,runtimeexception0.getMessage());
				assertEquals("cause" ,runtimeexception0.getLocalizedMessage());
			}
			assertEquals("java.lang.RuntimeException: message" ,e.toString());
			assertEquals("message" ,e.getMessage());
			assertEquals("message" ,e.getLocalizedMessage());
		}
	}
	
	@Test
	public void testNestedExpressions() {
		thunk(thunk(thunk(failWithReturn())));
	}
	@Fix("testNestedExpressions")
	public void fixNestedExpressions() {
		try {
			thunk(thunk(thunk(failWithReturn())));
			fail();
		} 
		catch (RuntimeException e) {
			assertNull(e.getCause());
			assertEquals("java.lang.RuntimeException", e.toString());
			assertNull(e.getMessage());
			assertNull(e.getLocalizedMessage());
		}
	}
	
	/**
	 * Unfixable for now due to bugs in Spoon, but possible.
	 */
	@Test
	@Unfixable 
	public void testLocalVariable() {		
		final int local = failWithReturn();
		assertEquals(1, local);
	}
//	@Fix("testLocalVariable")
//	public void fixLocalVariable() {
//		int local = 0;
//		try {
//			local = failWithReturn();
//			fail();
//		} 
//		catch (RuntimeException e) {
//			assertNull(e.getCause());
//			assertEquals("java.lang.RuntimeException", e.toString());
//			assertNull(e.getMessage());
//			assertNull(e.getLocalizedMessage());
//		}
//		assertEquals(1, local);
//	}

	@Test
	public void testAssignment() {
		int local = -1;
		local = failWithReturn();
		assertEquals(1, local);
	}
	@Fix("testAssignment")
	public void fixAssignment() {
		int local = -1;
		try {
			local = failWithReturn();
			fail();
		} catch (RuntimeException e) {
			assertNull(e.getCause());
			assertEquals("java.lang.RuntimeException", e.toString());
			assertNull(e.getMessage());
			assertNull(e.getLocalizedMessage());
		}
		assertEquals(1, local);
	}

	@Test
	public void testUninitializedAssignment_Number() {
		int local;
		local = failWithReturn();
		assertEquals(1, local);
	}
	@Fix("testUninitializedAssignment_Number")
	public void fixUninitializedAssignment_Number() {
		int local = 0;
		try {
			local = failWithReturn();
			fail();
		} 
		catch (RuntimeException e) {
			assertNull(e.getCause());
			assertEquals("java.lang.RuntimeException", e.toString());
			assertNull(e.getMessage());
			assertNull(e.getLocalizedMessage());
		}
		assertEquals(1, local);
	}
	
	@Test
	public void testUninitializedAssignment_WithFinal() {
		final int local;
		local = failWithReturn();
		assertEquals(1, local);
	}
	@Fix("testUninitializedAssignment_WithFinal")
	public void fixUninitializedAssignment_WithFinal() {
		int local = 0; // needs to remove final
		try {
			local = failWithReturn();
			fail();
		} catch (RuntimeException e) {
			assertNull(e.getCause());
			assertEquals("java.lang.RuntimeException", e.toString());
			assertNull(e.getMessage());
			assertNull(e.getLocalizedMessage());
		}
		assertEquals(1, local);
	}

	@Test
	public void testUninitializedAssignment_BoxedNumber() {
		Integer local;
		local = failWithReturn();
		assertEquals(new Integer(1), local);
	}
	@Fix("testUninitializedAssignment_BoxedNumber")
	public void fixUninitializedAssignment_BoxedNumber() {
		Integer local = 0;
		try {
			local = failWithReturn();
			fail();
		} catch (RuntimeException e) {
			assertNull(e.getCause());
			assertEquals("java.lang.RuntimeException", e.toString());
			assertNull(e.getMessage());
			assertNull(e.getLocalizedMessage());
		}
		assertEquals(new Integer(1), local);
	}
	
	@Test
	public void testUninitializedAssignment_ReferenceType() {
		Object local;
		local = failWithReturn();
		assertEquals(1, local);
	}
	@Fix("testUninitializedAssignment_ReferenceType")
	public void fixUninitializedAssignment_ReferenceType() {
		Object local = null;
		try {
			local = failWithReturn();
			fail();
		} catch (RuntimeException e) {
			assertNull(e.getCause());
			assertEquals("java.lang.RuntimeException", e.toString());
			assertNull(e.getMessage());
			assertNull(e.getLocalizedMessage());
		}
		assertEquals(1, local);
	}
	
	@Test
	public void testArrayAssignment() {
		int[] local = { 1, 2 };
		local[0] = failWithReturn();
		assertEquals(1, local[0]);
	}
	@Fix("testArrayAssignment")
	public void fixArrayAssignment() {
		int[] local = new int[]{ 1, 2 };
		try {
			local[0] = failWithReturn();
			fail();
		} catch (RuntimeException e) {
			assertNull(e.getCause());
			assertEquals("java.lang.RuntimeException", e.toString());
			assertNull(e.getMessage());
			assertNull(e.getLocalizedMessage());
		}
		assertEquals(1, local[0]);
	}
	
	@Test
	public void testUninitializedArrayAssignment() {
		int[] local;
		local = new int[] { failWithReturn() };
		assertEquals(1, local[0]);
	}
	@Fix("testUninitializedArrayAssignment")
	public void fixUninitializedArrayAssignment() {
		int[] local = null;
		try {
			local = new int[]{ failWithReturn() };
			fail();
		} catch (RuntimeException e) {
			assertNull(e.getCause());
			assertEquals("java.lang.RuntimeException", e.toString());
			assertNull(e.getMessage());
			assertNull(e.getLocalizedMessage());
		}
		assertEquals(1, local[0]);
	}

	int field;
	@Test
	public void testFieldAssignment() {
		field = failWithReturn();
		assertEquals(1, field);
	}
	@Fix("testFieldAssignment") 
	public void fixFieldAssignment() {
		try {
			field = failWithReturn();
			fail();
		} catch (RuntimeException e) {
			assertNull(e.getCause());
			assertEquals("java.lang.RuntimeException", e.toString());
			assertNull(e.getMessage());
			assertNull(e.getLocalizedMessage());
		}
		assertEquals(1, field);
	}
	
	@Test
	public void testConditional() {
		if (failWithReturn() == 1) {
			thunk(1);
		}
	}
	@Fix("testConditional")
	public void fixConditional() {
		try {			
			if (failWithReturn() == 1) {
				thunk(1);
			}
			fail();
		}
		catch (RuntimeException e) {
			assertNull(e.getCause());
			assertEquals("java.lang.RuntimeException", e.toString());
			assertNull(e.getMessage());
			assertNull(e.getLocalizedMessage());
		}
	}
	
	private int thunk(int arg) {
		return arg;
	}

	private int failWithReturn() {
		throw new RuntimeException();
	}

	private void failWithCause() {
		throw new RuntimeException("message", new RuntimeException("cause"));
	}
	
	private void indirectFailingCall() {
		failingCall();
	}
	
	private void failingCall() {
		throw new RuntimeException("message");
	}

}
