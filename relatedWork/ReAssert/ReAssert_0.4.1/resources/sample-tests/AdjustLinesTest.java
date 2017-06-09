import static org.junit.Assert.assertEquals;

import java.util.Currency;
import java.util.Locale;

import org.junit.Test;

public class AdjustLinesTest {

	//TODO: to prevent name qualification
	Currency c = null;
	Locale l = null;

	@Test
	public void test() {
		assertEquals("expected", "actual");
		assertEquals(
				Currency.getInstance(Locale.US), 
				Currency.getInstance(Locale.CANADA));
		assertEquals("expected", "actual");
		assertEquals(
				"expected", 
				"actual");
		assertEquals("expected", "actual");
	}
	
	@Test	
	public void test2() {
		assertEquals("expected", "actual");
		assertEquals(
				"expected", 
				"actual");
		assertEquals("expected", "actual");
		assertEquals(
				Currency.getInstance(Locale.US), 
				Currency.getInstance(Locale.CANADA));
		assertEquals("expected", "actual");

	}
	
}
