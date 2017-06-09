/*
 * Adapted for ReAssert from {@link junit.framework.Assert} bundled with JUnit 4.4.  
 * Assert methods are instrumented manually with RecordedAssertFailure.  
 * 
 * Necessary because dynamic instrumentation would need to dynamically load 
 * {@link junit.framework.TestCase}, which would break the JUnit runner's instanceof 
 * check.
 * 
 * To enable this instrumentation, this class must come before the default JUnit 
 * on the CLASSPATH.
 * 
 * @author <a href="http://www.cs.uiuc.edu/homes/bdaniel3/">Brett Daniel</a>
 */
package junit.framework;

import edu.illinois.reassert.RecordedAssertFailure;

/**
 * A set of assert methods. Messages are only displayed when an assert fails.
 */
public class Assert {

	/*
	 * Added for ReAssert. Allows ReAssert to turn off instrumentation for its
	 * own tests.
	 */
	public static boolean ENABLE_INSTRUMENTATION = false;

	/**
	 * Protect constructor since it is a static only class
	 */
	protected Assert() {
	}

	/**
	 * Asserts that a condition is true. If it isn't it throws an
	 * AssertionFailedError with the given message.
	 */
	static public void assertTrue(String message, boolean condition) {
		try {
			if (!condition)
				fail(message);
		}
		catch (Error e) {
			if (ENABLE_INSTRUMENTATION) {
				throw new RecordedAssertFailure(e, message, condition);
			}
			throw e;
		}
	}

	/**
	 * Asserts that a condition is true. If it isn't it throws an
	 * AssertionFailedError.
	 */
	static public void assertTrue(boolean condition) {
		try {
			assertTrue(null, condition);
		}
		catch (Error e) {
			if (ENABLE_INSTRUMENTATION) {
				throw new RecordedAssertFailure(e, condition);
			}
			throw e;
		}
	}

	/**
	 * Asserts that a condition is false. If it isn't it throws an
	 * AssertionFailedError with the given message.
	 */
	static public void assertFalse(String message, boolean condition) {
		try {
			assertTrue(message, !condition);
		}
		catch (Error e) {
			if (ENABLE_INSTRUMENTATION) {
				throw new RecordedAssertFailure(e, message, condition);
			}
			throw e;
		}
	}

	/**
	 * Asserts that a condition is false. If it isn't it throws an
	 * AssertionFailedError.
	 */
	static public void assertFalse(boolean condition) {
		try {
			assertFalse(null, condition);
		}
		catch (Error e) {
			if (ENABLE_INSTRUMENTATION) {
				throw new RecordedAssertFailure(e, condition);
			}
			throw e;
		}
	}

	/**
	 * Fails a test with the given message.
	 */
	static public void fail(String message) {
		throw new AssertionFailedError(message);
	}

	/**
	 * Fails a test with no message.
	 */
	static public void fail() {
		fail(null);
	}

	/**
	 * Asserts that two objects are equal. If they are not an
	 * AssertionFailedError is thrown with the given message.
	 */
	static public void assertEquals(String message, Object expected,
			Object actual) {
		try {
			if (expected == null && actual == null)
				return;
			if (expected != null && expected.equals(actual))
				return;
			failNotEquals(message, expected, actual);
		}
		catch (Error e) {
			if (ENABLE_INSTRUMENTATION) {
				throw new RecordedAssertFailure(e, message, expected, actual);
			}
			throw e;
		}
	}

	/**
	 * Asserts that two objects are equal. If they are not an
	 * AssertionFailedError is thrown.
	 */
	static public void assertEquals(Object expected, Object actual) {
		try {
			assertEquals(null, expected, actual);
		}
		catch (Error e) {
			if (ENABLE_INSTRUMENTATION) {
				throw new RecordedAssertFailure(e, expected, actual);
			}
			throw e;
		}
	}

	/**
	 * Asserts that two Strings are equal.
	 */
	static public void assertEquals(String message, String expected,
			String actual) {
		try {
			if (expected == null && actual == null)
				return;
			if (expected != null && expected.equals(actual))
				return;
			throw new ComparisonFailure(message, expected, actual);
		}
		catch (Error e) {
			if (ENABLE_INSTRUMENTATION) {
				throw new RecordedAssertFailure(e, message, expected, actual);
			}
			throw e;
		}
	}

	/**
	 * Asserts that two Strings are equal.
	 */
	static public void assertEquals(String expected, String actual) {
		try {
			assertEquals(null, expected, actual);
		}
		catch (Error e) {
			if (ENABLE_INSTRUMENTATION) {
				throw new RecordedAssertFailure(e, expected, actual);
			}
			throw e;
		}
	}

	/**
	 * Asserts that two doubles are equal concerning a delta. If they are not an
	 * AssertionFailedError is thrown with the given message. If the expected
	 * value is infinity then the delta value is ignored.
	 */
	static public void assertEquals(String message, double expected,
			double actual, double delta) {
		try {
			if (Double.compare(expected, actual) == 0)
				return;
			if (!(Math.abs(expected - actual) <= delta))
				failNotEquals(message, new Double(expected), new Double(actual));
		}
		catch (Error e) {
			if (ENABLE_INSTRUMENTATION) {
				throw new RecordedAssertFailure(e, message, expected, actual,
						delta);
			}
			throw e;
		}
	}

	/**
	 * Asserts that two doubles are equal concerning a delta. If the expected
	 * value is infinity then the delta value is ignored.
	 */
	static public void assertEquals(double expected, double actual, double delta) {
		try {
			assertEquals(null, expected, actual, delta);
		}
		catch (Error e) {
			if (ENABLE_INSTRUMENTATION) {
				throw new RecordedAssertFailure(e, expected, actual, delta);
			}
			throw e;
		}
	}

	/**
	 * Asserts that two floats are equal concerning a positive delta. If they
	 * are not an AssertionFailedError is thrown with the given message. If the
	 * expected value is infinity then the delta value is ignored.
	 */
	static public void assertEquals(String message, float expected,
			float actual, float delta) {
		try {
			if (Float.compare(expected, actual) == 0)
				return;
			if (!(Math.abs(expected - actual) <= delta))
				failNotEquals(message, new Float(expected), new Float(actual));
		}
		catch (Error e) {
			if (ENABLE_INSTRUMENTATION) {
				throw new RecordedAssertFailure(e, message, expected, actual,
						delta);
			}
			throw e;
		}
	}

	/**
	 * Asserts that two floats are equal concerning a delta. If the expected
	 * value is infinity then the delta value is ignored.
	 */
	static public void assertEquals(float expected, float actual, float delta) {
		try {
			assertEquals(null, expected, actual, delta);
		}
		catch (Error e) {
			if (ENABLE_INSTRUMENTATION) {
				throw new RecordedAssertFailure(e, expected, actual, delta);
			}
			throw e;
		}
	}

	/**
	 * Asserts that two longs are equal. If they are not an AssertionFailedError
	 * is thrown with the given message.
	 */
	static public void assertEquals(String message, long expected, long actual) {
		try {
			assertEquals(message, new Long(expected), new Long(actual));
		}
		catch (Error e) {
			if (ENABLE_INSTRUMENTATION) {
				throw new RecordedAssertFailure(e, message, expected, actual);
			}
			throw e;
		}
	}

	/**
	 * Asserts that two longs are equal.
	 */
	static public void assertEquals(long expected, long actual) {
		try {
			assertEquals(null, expected, actual);
		}
		catch (Error e) {
			if (ENABLE_INSTRUMENTATION) {
				throw new RecordedAssertFailure(e, expected, actual);
			}
			throw e;
		}
	}

	/**
	 * Asserts that two booleans are equal. If they are not an
	 * AssertionFailedError is thrown with the given message.
	 */
	static public void assertEquals(String message, boolean expected,
			boolean actual) {
		try {
			assertEquals(message, Boolean.valueOf(expected), Boolean
					.valueOf(actual));
		}
		catch (Error e) {
			if (ENABLE_INSTRUMENTATION) {
				throw new RecordedAssertFailure(e, message, expected, actual);
			}
			throw e;
		}
	}

	/**
	 * Asserts that two booleans are equal.
	 */
	static public void assertEquals(boolean expected, boolean actual) {
		try {
			assertEquals(null, expected, actual);
		}
		catch (Error e) {
			if (ENABLE_INSTRUMENTATION) {
				throw new RecordedAssertFailure(e, expected, actual);
			}
			throw e;
		}
	}

	/**
	 * Asserts that two bytes are equal. If they are not an AssertionFailedError
	 * is thrown with the given message.
	 */
	static public void assertEquals(String message, byte expected, byte actual) {
		try {
			assertEquals(message, new Byte(expected), new Byte(actual));
		}
		catch (Error e) {
			if (ENABLE_INSTRUMENTATION) {
				throw new RecordedAssertFailure(e, message, expected, actual);
			}
			throw e;
		}
	}

	/**
	 * Asserts that two bytes are equal.
	 */
	static public void assertEquals(byte expected, byte actual) {
		try {
			assertEquals(null, expected, actual);
		}
		catch (Error e) {
			if (ENABLE_INSTRUMENTATION) {
				throw new RecordedAssertFailure(e, expected, actual);
			}
			throw e;
		}
	}

	/**
	 * Asserts that two chars are equal. If they are not an AssertionFailedError
	 * is thrown with the given message.
	 */
	static public void assertEquals(String message, char expected, char actual) {
		try {
			assertEquals(message, new Character(expected),
					new Character(actual));
		}
		catch (Error e) {
			if (ENABLE_INSTRUMENTATION) {
				throw new RecordedAssertFailure(e, message, expected, actual);
			}
			throw e;
		}
	}

	/**
	 * Asserts that two chars are equal.
	 */
	static public void assertEquals(char expected, char actual) {
		try {
			assertEquals(null, expected, actual);
		}
		catch (Error e) {
			if (ENABLE_INSTRUMENTATION) {
				throw new RecordedAssertFailure(e, expected, actual);
			}
			throw e;
		}
	}

	/**
	 * Asserts that two shorts are equal. If they are not an
	 * AssertionFailedError is thrown with the given message.
	 */
	static public void assertEquals(String message, short expected, short actual) {
		try {
			assertEquals(message, new Short(expected), new Short(actual));
		}
		catch (Error e) {
			if (ENABLE_INSTRUMENTATION) {
				throw new RecordedAssertFailure(e, message, expected, actual);
			}
			throw e;
		}
	}

	/**
	 * Asserts that two shorts are equal.
	 */
	static public void assertEquals(short expected, short actual) {
		try {
			assertEquals(null, expected, actual);
		}
		catch (Error e) {
			if (ENABLE_INSTRUMENTATION) {
				throw new RecordedAssertFailure(e, expected, actual);
			}
			throw e;
		}
	}

	/**
	 * Asserts that two ints are equal. If they are not an AssertionFailedError
	 * is thrown with the given message.
	 */
	static public void assertEquals(String message, int expected, int actual) {
		try {
			assertEquals(message, new Integer(expected), new Integer(actual));
		}
		catch (Error e) {
			if (ENABLE_INSTRUMENTATION) {
				throw new RecordedAssertFailure(e, message, expected, actual);
			}
			throw e;
		}
	}

	/**
	 * Asserts that two ints are equal.
	 */
	static public void assertEquals(int expected, int actual) {
		try {
			assertEquals(null, expected, actual);
		}
		catch (Error e) {
			if (ENABLE_INSTRUMENTATION) {
				throw new RecordedAssertFailure(e, expected, actual);
			}
			throw e;
		}
	}

	/**
	 * Asserts that an object isn't null.
	 */
	static public void assertNotNull(Object object) {
		try {
			assertNotNull(null, object);
		}
		catch (Error e) {
			if (ENABLE_INSTRUMENTATION) {
				throw new RecordedAssertFailure(e, object);
			}
			throw e;
		}
	}

	/**
	 * Asserts that an object isn't null. If it is an AssertionFailedError is
	 * thrown with the given message.
	 */
	static public void assertNotNull(String message, Object object) {
		try {
			assertTrue(message, object != null);
		}
		catch (Error e) {
			if (ENABLE_INSTRUMENTATION) {
				throw new RecordedAssertFailure(e, message, object);
			}
			throw e;
		}
	}

	/**
	 * Asserts that an object is null.
	 */
	static public void assertNull(Object object) {
		try {
			assertNull(null, object);
		}
		catch (Error e) {
			if (ENABLE_INSTRUMENTATION) {
				throw new RecordedAssertFailure(e, object);
			}
			throw e;
		}
	}

	/**
	 * Asserts that an object is null. If it is not an AssertionFailedError is
	 * thrown with the given message.
	 */
	static public void assertNull(String message, Object object) {
		try {
			assertTrue(message, object == null);
		}
		catch (Error e) {
			if (ENABLE_INSTRUMENTATION) {
				throw new RecordedAssertFailure(e, message, object);
			}
			throw e;
		}
	}

	/**
	 * Asserts that two objects refer to the same object. If they are not an
	 * AssertionFailedError is thrown with the given message.
	 */
	static public void assertSame(String message, Object expected, Object actual) {
		try {
			if (expected == actual)
				return;
			failNotSame(message, expected, actual);
		}
		catch (Error e) {
			if (ENABLE_INSTRUMENTATION) {
				throw new RecordedAssertFailure(e, message, expected, actual);
			}
			throw e;
		}
	}

	/**
	 * Asserts that two objects refer to the same object. If they are not the
	 * same an AssertionFailedError is thrown.
	 */
	static public void assertSame(Object expected, Object actual) {
		try {
			assertSame(null, expected, actual);
		}
		catch (Error e) {
			if (ENABLE_INSTRUMENTATION) {
				throw new RecordedAssertFailure(e, expected, actual);
			}
			throw e;
		}
	}

	/**
	 * Asserts that two objects do not refer to the same object. If they do
	 * refer to the same object an AssertionFailedError is thrown with the given
	 * message.
	 */
	static public void assertNotSame(String message, Object expected,
			Object actual) {
		try {
			if (expected == actual)
				failSame(message);
		}
		catch (Error e) {
			if (ENABLE_INSTRUMENTATION) {
				throw new RecordedAssertFailure(e, message, expected, actual);
			}
			throw e;
		}
	}

	/**
	 * Asserts that two objects do not refer to the same object. If they do
	 * refer to the same object an AssertionFailedError is thrown.
	 */
	static public void assertNotSame(Object expected, Object actual) {
		try {
			assertNotSame(null, expected, actual);
		}
		catch (Error e) {
			if (ENABLE_INSTRUMENTATION) {
				throw new RecordedAssertFailure(e, expected, actual);
			}
			throw e;
		}
	}

	static public void failSame(String message) {
		String formatted = "";
		if (message != null)
			formatted = message + " ";
		fail(formatted + "expected not same");
	}

	static public void failNotSame(String message, Object expected,
			Object actual) {
		String formatted = "";
		if (message != null)
			formatted = message + " ";
		fail(formatted + "expected same:<" + expected + "> was not:<" + actual
				+ ">");
	}

	static public void failNotEquals(String message, Object expected,
			Object actual) {
		fail(format(message, expected, actual));
	}

	public static String format(String message, Object expected, Object actual) {
		String formatted = "";
		if (message != null)
			formatted = message + " ";
		return formatted + "expected:<" + expected + "> but was:<" + actual
				+ ">";
	}
}
