package edu.illinois.reassert.test.assertfixer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.illinois.reassert.assertfixer.AssertEqualsReplaceLiteralFixer;
import edu.illinois.reassert.testutil.Box;
import edu.illinois.reassert.testutil.Fix;
import edu.illinois.reassert.testutil.FixChecker;
import edu.illinois.reassert.testutil.Fixers;
import edu.illinois.reassert.testutil.Unfixable;


/**
 * See {@link AssertEqualsExpandAccessorsFixer_LiteralTest} for more extensive tests 
 * 
 * @author <a href="http://www.cs.uiuc.edu/homes/bdaniel3/">Brett Daniel</a>
 */
@RunWith(FixChecker.class)
@Fixers(AssertEqualsReplaceLiteralFixer.class)
public class AssertEqualsReplaceLiteralFixerTest {

	@Test
	public void testLiterable() {
		assertEquals("expected", "actual");
	}
	@Fix("testLiterable")
	public void fixLiterable() {
		assertEquals("actual", "actual");
	}
	
	@Test
	@Unfixable
	public void testNotLiterable() {
		Box actual = new Box(5);
		assertEquals(null, actual);
	}
}
