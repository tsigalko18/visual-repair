package sample.counter;
 
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class CounterTest {
	
	private Counter actual;
	
	@Before public void initCounter() {
		actual = new Counter(3);
		assertEquals(3, actual.getValue());
	}
	
	@Test public void testIncrement() {
		actual.increment();
		assertEquals(4, actual.getValue());
		actual.increment();
		assertEquals(5, actual.getValue());
	}
	
	@Test public void testReset() {
		actual.increment();
		assertEquals(4, actual.getValue());
		actual.reset();
		assertEquals(3, actual.getValue());
	}
	
	@Test public void testEquals() {
		actual.increment();
		assertEquals(new Counter(4), actual);
	}	
}
