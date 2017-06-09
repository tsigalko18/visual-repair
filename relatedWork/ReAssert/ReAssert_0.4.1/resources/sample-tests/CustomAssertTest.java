import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class CustomAssertTest {
	
	@Test
	public void testWithCustomAssert() {
		myAssertTrue(false);
	}
	
	@Test
	public void testWithoutCustomAssert() {
		assertTrue(false);
	}
	
	public void myAssertTrue(boolean value) {
		assertTrue(value);
	}

	public void myAssertFalse(boolean value) {
		assertFalse(value);
	}
	
}
