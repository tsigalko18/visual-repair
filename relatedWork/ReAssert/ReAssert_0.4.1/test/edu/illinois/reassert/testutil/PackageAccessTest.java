package edu.illinois.reassert.testutil;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test input used to verify class loading 
 */
@RunWith(IgnoredInner.class)
public class PackageAccessTest {

	/**
	 * Will fail. Never meant to be executed directly. 
	 */
	@Test
	public void testFail() {
		assertEquals(-1, PackageAccess.packageStaticField);
	}
	
	@Test
	public void testPass() {
		assertEquals(0, PackageAccess.packageStaticField);
	}
}
