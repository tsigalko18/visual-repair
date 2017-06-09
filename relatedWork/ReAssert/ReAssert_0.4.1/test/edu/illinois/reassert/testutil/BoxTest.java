package edu.illinois.reassert.testutil;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class BoxTest {

	@Test
	public void testToString() {
		assertEquals("Box(4)", new Box(4).toString());
		assertEquals("Box(Box(5))", new Box(new Box(5)).toString());
		assertEquals("Box(Box(Box(...)))", new Box(new Box(new Box(new Box(new Box())))).toString());
		
		// circular boxes
		Box outer = new Box();
		Box inner = new Box(outer);
		outer.setValue(inner);
		assertEquals("Box(Box(Box(...)))", outer.toString());
		assertEquals("Box(Box(Box(...)))", inner.toString());
	}
}
