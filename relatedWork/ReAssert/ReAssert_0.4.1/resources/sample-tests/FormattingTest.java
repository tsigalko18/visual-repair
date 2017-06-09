/*
 * header comment
 */


// comment

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

import edu.illinois.reassert.testutil.Box;

/**
 * JavaDoc
 */
public class FormattingTest {

	Object field;
Object field2;
                 Object field3, field4;
	
	@Test
	public void test() {
		org.junit.Assert.assertEquals("expected","actual");
		Assert.assertEquals("expected", "actual");
		assertEquals("expected"   ,		"actual");
		
		Box expected = new Box(5);
		Box actual = new Box(6);
		assertEquals(expected ,actual);
	}

	@Test public void otherTest() {}
	
	/**
	 * JavaDoc
	 */
	public void otherMethod(Object o,Object o2 ,	Object o3) 
	{
		// comment

		otherMethod(null,null  ,	null)
		;
		while (    true     ) {
			
		}
		
	}
	
	
	
	
}




