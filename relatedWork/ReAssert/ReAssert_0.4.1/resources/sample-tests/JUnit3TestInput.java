import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import junit.framework.TestCase;

public class JUnit3TestInput extends TestCase {
	
	public void testSucceed() {}
	
	public void testUnfixable() {
		fail();
	}
	
	public void testFixOne() {
		assertEquals("expected", "actual");
	}
	
	public void testFixMultiple() {
		assertEquals("expected", "actual");
		assertEquals("expected", "actual");
		assertEquals("expected", "actual");
	}
	
	public void testMultipleStepFix() {
		List<String> coll = Arrays.asList("a", "b", "c");
		assertTrue(coll.isEmpty());
	}

	public void testManyLineFix() {
		Currency us = Currency.getInstance(Locale.US);
		Currency uk = Currency.getInstance(Locale.UK);
		assertEquals(us, uk);
	}
	
	public void testSemiFixable() {
		assertEquals("expected", "actual");
		assertEquals("expected", "actual");
		assertEquals("expected", "actual");
		fail();
	}
	
	public void testThrowsException() {
		throw new RuntimeException();
	}

	public void testCallThrowsException() {
		testThrowsException();
	}
	
	public void testCallsMethods() {
		helper1();
		helper2();
	}
	public void helper1() {
		assertEquals("expected", "actual");
	}
	public void helper2() {
		assertEquals("expected", "actual");
	}

	public void testOverFixLimit() {
		assertEquals("expected", "actual");
		assertEquals("expected", "actual");
		assertEquals("expected", "actual");
		assertEquals("expected", "actual");
		assertEquals("expected", "actual");
		assertEquals("expected", "actual");
		assertEquals("expected", "actual");
		assertEquals("expected", "actual");
		assertEquals("expected", "actual");
	}
	
}

