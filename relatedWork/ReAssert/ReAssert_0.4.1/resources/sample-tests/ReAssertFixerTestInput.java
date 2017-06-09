
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.illinois.reassert.testutil.FixChecker;
import edu.illinois.reassert.testutil.Fix;

@RunWith(FixChecker.class)
public class ReAssertFixerTestInput {

	@Test
	public void test() {
		assertEquals("expected", "actual");
	}
	@Fix("test")
	public void fix() {
		//incorrect
	}
	
	@Test
	public void test2() {
		assertEquals("expected", "actual");
	}
	@Fix("test2")
	public void fix2() {
		//incorrect
	}
	
}