package edu.illinois.reassert.test.assertfixer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.illinois.reassert.assertfixer.AssertEqualsExpandAccessorsFixer;
import edu.illinois.reassert.assertfixer.InvertBooleanAssertFixer;
import edu.illinois.reassert.testutil.Box;
import edu.illinois.reassert.testutil.Fix;
import edu.illinois.reassert.testutil.FixChecker;
import edu.illinois.reassert.testutil.Fixers;


@RunWith(FixChecker.class)
@Fixers({InvertBooleanAssertFixer.class, AssertEqualsExpandAccessorsFixer.class})
public class InvertBooleanAssertFixerTest {

	@Test
	public void testInvertAssertTrue() {
		assertTrue(false);
	}
	@Fix("testInvertAssertTrue")
	public void fixInvertAssertTrue() {
		assertFalse(false);
	}
	
	@Test
	public void testInvertAssertTrueWithMessage() {
		assertTrue("message", false);
	}
	@Fix("testInvertAssertTrueWithMessage")
	public void fixInvertAssertTrueWithMessage() {
		assertFalse(false);
	}
	
	@Test
	public void testInvertAssertFalse() {
		assertFalse(true);
	}
	@Fix("testInvertAssertFalse")
	public void fixInvertAssertFalse() {
		assertTrue(true);
	}
	
	@Test
	public void testInvertAssertFalseWithMessage() {
		assertFalse("message", true);
	}
	@Fix("testInvertAssertFalseWithMessage")
	public void fixInvertAssertFalseWithMessage() {
		assertTrue(true);
	}
	
	@Test
	public void testFalseEquals() {
		Box expected = new Box(5);
		Box actual = new Box(5);
		assertFalse(expected.equals(actual));
	}
	@Fix("testFalseEquals")
	public void fixFalseEquals() {
		Box expected = new Box(5);
		Box actual = new Box(5);
		assertEquals(expected, actual);
	}

	@Test
	public void testTrueEquals_Step1() {
		Box expected = new Box(5);
		Box actual = new Box(6);
		assertTrue(expected.equals(actual));
	}
	@Fix("testTrueEquals_Step1")
	public void fixTrueEquals_Step1() {
		Box expected = new Box(5);
		Box actual = new Box(6);
		assertEquals(expected, actual);
	}
	
	@Test
	public void testTrueEquals_Step2() {
		Box expected = new Box(5);
		Box actual = new Box(6);
		assertEquals(expected, actual);
	}
	@Fix("testTrueEquals_Step2")
	public void fixTrueEquals_Step2() {
		Box expected = new Box(5);
		Box actual = new Box(6);
		{
			assertEquals("Box(6)", actual.toString());
			assertEquals(6, actual.getValue());
		}
	}

	@Test
	public void testFalseEqualsLiteral() {
		String actual = "actual";
		assertFalse(actual.equals("actual"));
	}
	@Fix("testFalseEqualsLiteral")
	public void fixFalseEqualsLiteral() {
		String actual = "actual";
		assertEquals("actual", actual);
	}
	
	@Test
	public void testTrueEqualsLiteral_Step1() {
		String actual = "actual";
		assertTrue(actual.equals("expected"));
	}
	@Fix("testTrueEqualsLiteral_Step1")
	public void fixTrueEqualsLiteral_Step1() {
		String actual = "actual";
		assertEquals("expected", actual);
	}
	
	@Test
	public void testTrueEqualsLiteral_Step2() {
		String actual = "actual";
		assertEquals("expected", actual);
	}
	@Fix("testTrueEqualsLiteral_Step2")
	public void fixTrueEqualsLiteral_Step2() {
		String actual = "actual";
		assertEquals("actual", actual);
	}
	
	@Test
	public void testAssertFalseIdentity_LeftActual() {
		int actual = 0;
		assertFalse(actual == 0);
	}
	@Fix("testAssertFalseIdentity_LeftActual")
	public void fixAssertFalseIdentity_LeftActual() {
		int actual = 0;
		assertEquals(0, actual);
	}
	
	@Test
	public void testAssertFalseIdentity_RightActual() {
		int actual = 0;
		assertFalse(0 == actual);
	}
	@Fix("testAssertFalseIdentity_RightActual")
	public void fixAssertFalseIdentity_RightActual() {
		int actual = 0;
		assertEquals(0, actual);
	}
	
	@Test
	public void testAssertTrueIdentity_LeftActual() {
		int actual = 0;
		assertTrue(actual == 1);
	}
	@Fix("testAssertTrueIdentity_LeftActual")
	public void fixAssertTrueIdentity_LeftActual() {
		int actual = 0;
		assertEquals(1, actual);
	}
	
	@Test
	public void testAssertTrueIdentity_RightActual() {
		int actual = 0;
		assertTrue(1 == actual);
	}
	@Fix("testAssertTrueIdentity_RightActual")
	public void fixAssertTrueIdentity_RightActual() {
		int actual = 0;
		assertEquals(1, actual);
	}
}
