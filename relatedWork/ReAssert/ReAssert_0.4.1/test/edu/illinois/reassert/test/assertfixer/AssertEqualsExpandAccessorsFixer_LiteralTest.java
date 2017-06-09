package edu.illinois.reassert.test.assertfixer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.illinois.reassert.assertfixer.AssertEqualsExpandAccessorsFixer;
import edu.illinois.reassert.testutil.Fix;
import edu.illinois.reassert.testutil.FixChecker;
import edu.illinois.reassert.testutil.Fixers;


@RunWith(FixChecker.class)
@Fixers(AssertEqualsExpandAccessorsFixer.class)
public class AssertEqualsExpandAccessorsFixer_LiteralTest {
	
	@Test
	public void testSucceed() {
		assertEquals(1, 1);
	}

	@Test
	public void testByte() {
		byte expected = 1;
		byte actual = 2;
		assertEquals(expected, actual);
	}
	@Fix("testByte")
	public void fixByte() {
		byte expected = 1;
		byte actual = 2;
		assertEquals(2, actual);
	}

	@Test
	public void testShort() {
		short expected = 1;
		short actual = 2;
		assertEquals(expected, actual);
	}
	@Fix("testShort")
	public void fixShort() {
		short expected = 1;
		short actual = 2;
		assertEquals(2, actual);
	}

	@Test
	public void testInt() {
		assertEquals(1, 2);
	}
	@Fix("testInt")
	public void fixInt() {
		assertEquals(2, 2);
	}
	
	@Test
	public void testLong() {
		assertEquals(1L, 2L);
	}
	@Fix("testLong")
	public void fixLong() {
		assertEquals(2L, 2L);
	}
	
	@Test
	public void testFloat() {
		assertEquals(1F, 2F);
	}
	@Fix("testFloat")
	public void fixFloat() {
		assertEquals(2F, 2F);
	}
	
	@Test
	public void testFloat_WithTolerance() {
		assertEquals(1F, 2F, 0.1F);
	}
	@Fix("testFloat_WithTolerance")
	public void fixFloat_WithTolerance() {
		assertEquals(2F, 2F, 0.1F);
	}
	
	@Test
	public void testNumber() {
		Number actual = 2L;
		assertEquals(1, actual);
	}
	@Fix("testNumber")
	public void fixNumber() {
		Number actual = 2L;
		assertEquals(2L, actual);
	}
	
	@Test
	public void testAssertEqualsMessage() {
		assertEquals("message", 1, 2);
	}
	@Fix("testAssertEqualsMessage")
	public void fixAssertEqualsMessage() {
		assertEquals(2, 2);
	}

	@Test
	public void testAssertNullMessage() {
		assertEquals("message", 1, null);
	}
	@Fix("testAssertNullMessage")
	public void fixAssertNullMessage() {
		assertNull(null);
	}
	
	@Test
	public void testString() {
		assertEquals("a", "b");
	}
	@Fix("testString")
	public void fixString() {
		assertEquals("b", "b");
	}
	
	@Test
	public void testClass() {
		assertEquals(Object.class, String.class);
	}
	@Fix("testClass")
	public void fixClass() {
		assertEquals(String.class, String.class);
	}
	
	@Test
	public void testNullExpected() {
		assertEquals(null, 5);
	}
	@Fix("testNullExpected")
	public void fixNullExpected() {
		assertEquals(5, 5);
	}
	
	@Test
	public void testNullActual() {
		Object n = null;
		assertEquals(5, n);
	}
	@Fix("testNullActual")
	public void fixNullActual() {
		Object n = null;
		assertNull(n);
	}
	
	@Test
	public void testDoubles() {
		assertEquals(1.1, 2.2);
	}
	@Fix("testDoubles")
	public void fixDoubles() {
		assertEquals(2.2, 2.2);
	}

	@Test
	public void testDoubles_WithMessage() {
		assertEquals("message", 1.1, 2.2);
	}
	@Fix("testDoubles_WithMessage")
	public void fixDoubles_WithMessage() {
		assertEquals(2.2, 2.2);
	}
	
	@Test
	public void testDoubles_WithTolerance() {
		assertEquals(1.1, 2.2, 0.1);
	}
	@Fix("testDoubles_WithTolerance")
	public void fixDoubles_WithTolerance() {
		assertEquals(2.2, 2.2, 0.1);
	}
	
	@Test
	public void testDoubles_WithTolerance_WithMessage() {
		assertEquals("message", 1.1, 2.2, 0.1);
	}
	@Fix("testDoubles_WithTolerance_WithMessage")
	public void fixDoubles_WithTolerance_WithMessage() {
		assertEquals(2.2, 2.2, 0.1);
	}
	
	@Test
	public void testBooleanTrue() {
		boolean expected = false;
		boolean actual = true;
		assertEquals(expected, actual);
	}
	@Fix("testBooleanTrue")
	public void fixBooleanTrue() {
		boolean expected = false;
		boolean actual = true;
		assertTrue(actual);
	}
	
	@Test
	public void testBooleanFalse() {
		boolean expected = true;
		boolean actual = false;
		assertEquals(expected, actual);
	}
	@Fix("testBooleanFalse")
	public void fixBooleanFalse() {
		boolean expected = true;
		boolean actual = false;
		assertFalse(actual);
	}
	
	@Test
	public void testBooleanExpression() {
		assertEquals(null, true && false || true);
	}
	@Fix("testBooleanExpression")
	public void fixBooleanExpression() {
		assertTrue(true && false || true);
	}
	
	@Test
	public void testSwapLiteralToExpected() {
		String actual = "actual";
		assertEquals(actual, "expected");
	}
	@Fix("testSwapLiteralToExpected")
	public void fixSwapLiteralToExpected() {
		String actual = "actual";
		assertEquals("expected", actual);
	}
}
