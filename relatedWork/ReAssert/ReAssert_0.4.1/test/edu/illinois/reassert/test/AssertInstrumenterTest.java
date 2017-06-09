package edu.illinois.reassert.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;

import edu.illinois.reassert.AssertInstrumenter;
import edu.illinois.reassert.RecordedAssertFailure;


public class AssertInstrumenterTest {	
	
	private AssertInstrumenter loader;
	
	@Before
	public void initLoader() {
		loader = new AssertInstrumenter();		
	}
	
	@Test
	public void testCustomAssert() throws Throwable {
		loader.instrument(TestCustomAssert.class, "myAssert");
		Class<?> instrumented = loader.loadClass(TestCustomAssert.class.getName());
		RecordedAssertFailure record = invoke(instrumented, "test");
		assertTrue(record.getCause() instanceof RuntimeException);
		assertEquals("DUMMY FAILURE", record.getCause().getMessage());
		assertEquals(true, record.getArgs()[0]);
		assertEquals((byte) 1, record.getArgs()[1]);
		assertEquals((short) 2, record.getArgs()[2]);
		assertEquals('a', record.getArgs()[3]);
		assertEquals(3, record.getArgs()[4]);
		assertEquals(4L, record.getArgs()[5]);
		assertEquals(5.0F, record.getArgs()[6]);
		assertEquals(6.0, record.getArgs()[7]);
		assertEquals("foo", record.getArgs()[8]);
	}
	public static class TestCustomAssert {
		public void test() {
			myAssert(true, (byte) 1, (short) 2, 'a', 3, 4L, 5.0F, 6.0, "foo");
		}
		private void myAssert(boolean b, byte c, short s, char d, int i,
				long l, float f, double e, Object o) {
			throw new RuntimeException("DUMMY FAILURE");
		}
	}
	
	private RecordedAssertFailure invoke(Class<?> instrumented, String methodName)
			throws Throwable {
		Method instrumentedMethod = instrumented.getMethod(methodName);
		try {
			instrumentedMethod.invoke(instrumented.newInstance());
			throw new RuntimeException("Invocation should throw exception");
		}
		catch (InvocationTargetException e) {
			Throwable targetException = e.getTargetException();
			if (targetException instanceof RecordedAssertFailure) {
				return (RecordedAssertFailure) targetException;
			}
			throw new RuntimeException("Method was not instrumented to throw recorded exception", targetException);
		}
	}

}
