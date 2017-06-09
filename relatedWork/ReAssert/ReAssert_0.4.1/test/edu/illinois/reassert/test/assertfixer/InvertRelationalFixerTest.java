package edu.illinois.reassert.test.assertfixer;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.illinois.reassert.testutil.Fix;
import edu.illinois.reassert.testutil.FixChecker;


@RunWith(FixChecker.class)
public class InvertRelationalFixerTest {

	@Test
	public void testInvertLessThan() {
		assertTrue(1 < 0);
	}
	@Fix("testInvertLessThan")
	public void fixInvertLessThan() {
		assertTrue(1 >= 0);
	}
	
	@Test
	public void testInvertLessEquals() {
		assertTrue(1 <= 0);
	}
	@Fix("testInvertLessEquals")
	public void fixInvertLessEquals() {
		assertTrue(1 > 0);
	}
	
	@Test
	public void testInvertGreaterThan() {
		assertTrue(0 > 1);
	}
	@Fix("testInvertGreaterThan")
	public void fixInvertGreaterThan() {
		assertTrue(0 <= 1);
	}
	
	@Test
	public void testInvertGreaterEquals() {
		assertTrue(0 >= 1);
	}
	@Fix("testInvertGreaterEquals")
	public void fixInvertGreaterEquals() {
		assertTrue(0 < 1);
	}
	
}
