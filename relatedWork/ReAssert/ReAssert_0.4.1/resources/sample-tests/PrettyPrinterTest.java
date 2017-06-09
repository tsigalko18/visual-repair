
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Iterator;
import org.junit.Assert;

public class PrettyPrinterTest implements Serializable, Iterable {
	public PrettyPrinterTest(int arg0, int arg1, int arg2) throws IllegalArgumentException, RuntimeException {
	}
	
	public Iterator iterator() {
		return null;
	}
	
	@A(value = { 1, 2, 3 })
	public void commas(boolean b, byte c, short s, char d, int i, long l, float f, double e, Object o) {
		for (int k = 0, j = 0; k < 5; k++, j++) {
		}
		int[] ia = new int[]{ i, i, i };
		new PrettyPrinterTest(i, i, i);
		commas(true, (byte) 1, (short) 2, 'a', 3, 2147483648L, 5.0F, 6.0, "foo");
		try {
			casts();
		}
		catch (RuntimeException e1) {
			casts();
		}
		catch (Exception e2) {
			casts();
		}
		finally {
			casts();
		}
	}
	
	public static void staticMethods() {
		staticMethods();
		System.currentTimeMillis();
		assertTrue(true);
		assertTrue("message", true);
		assertFalse(true);
		java.util.Calendar.getInstance();
	}
	
	public void casts() {
		byte x = (byte) 1;
		byte y = (byte)(1 + 1);
		byte z = (byte)((short)(1 + 1));
		x = (byte) 1;
		y = (byte)(1 + 1);
		z = (byte)((short)(1 + 1));
		boolean b;
		if (b = ((byte) 1) < 1) {
		} 
	}
	
}

enum E implements Serializable, Iterable {
	A, B;
	public Iterator iterator() {
		return null;
	}
	
}

interface I extends Serializable, Iterable {
}

@interface A {
	int[] value() default { 1, 2, 3 };
	
}

