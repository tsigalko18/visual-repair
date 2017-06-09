import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

public class JUnit4TestInput {
	
	@Test
	public void succeed() {}
	
	@Test
	public void unfixable() {
		fail();
	}
	
	@Test
	public void fixOne() {
		assertEquals("expected", "actual");
	}
	
	@Test
	public void fixMultiple() {
		assertEquals("expected", "actual");
		assertEquals("expected", "actual");
		assertEquals("expected", "actual");
	}
	
	@Test
	public void multipleStepFix() {
		List<String> coll = Arrays.asList("a", "b", "c");
		assertTrue(coll.isEmpty());
	}

	@Test
	public void manyLineFix() {
		Currency us = Currency.getInstance(Locale.US);
		Currency uk = Currency.getInstance(Locale.UK);
		assertEquals(us, uk);
	}
	
	@Test
	public void semiFixable() {
		assertEquals("expected", "actual");
		assertEquals("expected", "actual");
		assertEquals("expected", "actual");
		fail();
	}
	
	@Test
	public void throwsException() {
		throw new RuntimeException();
	}

	@Test
	public void callThrowsException() {
		throwsException();
	}
	
	@Test
	public void callsMethods() {
		helper1();
		helper2();
	}
	public void helper1() {
		assertEquals("expected", "actual");
	}
	public void helper2() {
		assertEquals("expected", "actual");
	}

	@Test
	public void usesOtherFixer() {
		assertTrue(false);
	}

	@Test
	public void overFixLimit() {
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

