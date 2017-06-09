package edu.illinois.reassert.test.assertfixer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.illinois.reassert.assertfixer.AssertEqualsExpandAccessorsFixer;
import edu.illinois.reassert.testutil.Box;
import edu.illinois.reassert.testutil.Fix;
import edu.illinois.reassert.testutil.FixChecker;
import edu.illinois.reassert.testutil.Fixers;


@RunWith(FixChecker.class)
@Fixers(AssertEqualsExpandAccessorsFixer.class)
public class AssertEqualsExpandAccesorsFixer_ArrayTest {

	@Test
	public void testLiteralArray() {
		Object expected = null; // don't care
		Object actual = new int[] { 2, 3, 4 };
		assertEquals(expected, actual);
	}
	@Fix("testLiteralArray")
	public void fixLiteralArrays() {
		Object expected = null;
		Object actual = new int[]{ 2 , 3 , 4 };
		assertEquals(new int[]{ 2 , 3 , 4 } ,actual);
	}
	
	@Test
	public void testAssertArrayEquals() {
		int[] expected = null; // don't care
		int[] actual = new int[] { 2, 3, 4 };
		assertArrayEquals(expected, actual);
	}
	@Fix("testAssertArrayEquals")
	public void fixAssertArrayEquals() {
		int[] expected = null;
		int[] actual = new int[]{ 2 , 3 , 4 };
		assertEquals(new int[]{ 2 , 3 , 4 } ,actual);
	}
	
	@Test
	public void testMessage() {
		Object expected = null; // don't care
		Object actual = new int[] { 2, 3, 4 };
		assertEquals("message", expected, actual);
	}
	@Fix("testMessage")
	public void fixMessage() {
		Object expected = null;
		Object actual = new int[]{ 2 , 3 , 4 };
		assertEquals(new int[]{ 2 , 3 , 4 } ,actual);
	}
	
	@Test
	public void testMultidimensionalLiteralArray() {
		Object expected = null; // don't care
		Object actual = new int[][]{ new int[] { 1, 2 }, new int[] { 3, 4 } };
		assertEquals(expected, actual);
	}
	@Fix("testMultidimensionalLiteralArray")
	public void fixMultidimensionalLiteralArrays() {
		Object expected = null;
		Object actual = new int[][]{ new int[]{ 1 , 2 } , new int[]{ 3 , 4 } };
		assertEquals(new int[][]{ new int[]{ 1 , 2 } , new int[]{ 3 , 4 } } , actual);
	}
	
	@Test
	public void testDifferentDimensions() {
		assertEquals(
				new int[] { 5 }, 
				new int[][] { new int[] {6} });
	}
	@SuppressWarnings("deprecation")
	@Fix("testDifferentDimensions")
	public void fixDifferentDimensions() {
		assertEquals(
				new int[][] { new int[] {6} }, 
				new int[][] { new int[] {6} });
	}
	
	@Test
	@Ignore("Under development")
	public void testBoxArray() {
		Box expected = null; // don't care
		Box actual = new Box(new Box[] { new Box(2), new Box(3) });
		assertEquals(expected, actual);
	}
	@Fix("testBoxArray")
	public void fixBoxArray() {

	}

}
