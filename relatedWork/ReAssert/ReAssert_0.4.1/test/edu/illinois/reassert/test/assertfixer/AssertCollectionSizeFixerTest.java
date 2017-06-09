package edu.illinois.reassert.test.assertfixer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.illinois.reassert.assertfixer.AssertCollectionSizeFixer;
import edu.illinois.reassert.assertfixer.AssertEqualsExpandAccessorsFixer;
import edu.illinois.reassert.assertfixer.InvertBooleanAssertFixer;
import edu.illinois.reassert.testutil.Fix;
import edu.illinois.reassert.testutil.FixChecker;
import edu.illinois.reassert.testutil.Fixers;


@RunWith(FixChecker.class)
@Fixers({
	AssertCollectionSizeFixer.class, 
	AssertEqualsExpandAccessorsFixer.class, 
	InvertBooleanAssertFixer.class})
public class AssertCollectionSizeFixerTest {

	private Collection<String> full = Arrays.asList("a", "b", "c");
	private Collection<String> empty = Arrays.asList(new String[0]);

	@Test
	public void testIsEmpty() {
		assertTrue(full.isEmpty());
	}
	@Fix("testIsEmpty")
	public void fixIsEmpty() {
		assertEquals(-1, full.size());
	}
	
	@Test
	public void testSizeFull() {
		assertEquals(-1, full.size());
	}
	@Fix("testSizeFull")
	public void fixSizeFull() {
		assertEquals(3, full.size()); // fixed by AssertEqualsExpandAccessorsFixer
	}
	
	@Test
	public void testNotIsEmpty() {
		assertFalse(empty.isEmpty());
	}
	@Fix("testNotIsEmpty")
	public void fixNotIsEmpty() {
		assertTrue(empty.isEmpty()); // fixed by AssertBooleanFixer
	}
	
	@Test
	public void testSizeEmpty() {
		assertEquals(-1, empty.size());
	}
	@Fix("testSizeEmpty")
	public void fixSizeEmpty() {
		assertTrue(empty.isEmpty());
	}
	
	@Test
	public void testMap_IsEmpty() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("key", "value");
		assertTrue(map.isEmpty());
	}
	@Fix("testMap_IsEmpty")
	public void fixMap_IsEmpty() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("key", "value");
		assertEquals(-1, map.size());
	}

	@Test
	public void testMap_SizeEmpty() {
		Map<String, String> map = new HashMap<String, String>();
		assertEquals(-1, map.size());
	}
	@Fix("testMap_SizeEmpty")
	public void fixMap_SizeEmpty() {
		Map<String, String> map = new HashMap<String, String>();
		assertTrue(map.isEmpty());		
	}
	
	
	@Test
	public void testNotCollection_IsEmpty() {
		assertTrue(new NotCollection(4).isEmpty());
	}
	@Fix("testNotCollection_IsEmpty") 
	public void fixNotCollection_IsEmpty() {
		assertEquals(-1, new NotCollection(4).size());
	}
	
	@Test
	public void testNotCollection_SizeEmpty() {
		assertEquals(-1, new NotCollection(0).size());
	}
	@Fix("testNotCollection_SizeEmpty") 
	public void fixNotCollection_SizeEmpty() {
		assertTrue(new NotCollection(0).isEmpty());
	}
	
	public static class NotCollection {
		private int size;
		public NotCollection(int size) {
			this.size = size;
		}
		public boolean isEmpty() {
			return size == 0;
		}
		public int size() {
			return size;
		}
	}

	
	@Test
	public void testMissingSize_IsEmpty() {
		assertTrue(new MissingSize(4).isEmpty());
	}
	@Fix("testMissingSize_IsEmpty") 
	public void fixMissingSize_IsEmpty() {
		assertFalse(new MissingSize(4).isEmpty()); // doesn't change to assertEquals(...size())
	}
	public static class MissingSize {
		private int size;
		public MissingSize(int size) {
			this.size = size;
		}
		public boolean isEmpty() {
			return size == 0;
		}
	}
	
	
	@Test
	public void testMissingIsEmpty_SizeEmpty() {
		assertEquals(-1, new MissingIsEmpty(0).size());
	}
	@Fix("testMissingIsEmpty_SizeEmpty") 
	public void fixMissingIsEmpty_SizeEmpty() {
		assertEquals(0, new MissingIsEmpty(0).size()); // doesn't change to assertTrue(...isEmpty())
	}
	public static class MissingIsEmpty {
		private int size;
		public MissingIsEmpty(int size) {
			this.size = size;
		}
		public int size() {
			return size;
		}
	}

	
	@Test
	public void testStaticInvocation() {
		assertTrue(StaticClass.isEmpty());
	}
	@Fix("testStaticInvocation")
	public void fixStaticInvocation() {
		assertFalse(StaticClass.isEmpty());
	}
	public static class StaticClass {
		public static boolean isEmpty() {
			return false;
		}
	}

	
	@Test
	public void testAssertTrueWithMessage() {
		assertTrue("message", full.isEmpty());
	}
	@Fix("testAssertTrueWithMessage")
	public void fixAssertTrueWithMessage() {
		assertEquals(-1, full.size());
	}
	
	@Test
	public void testAssertEqualsWithMessage() {
		assertEquals("message", -1, empty.size());
	}
	@Fix("testAssertEqualsWithMessage")
	public void fixAssertEqualsWithMessage() {
		assertTrue(empty.isEmpty());
	}
	
	/**
	 * Verifies that the fixer can find the size and isEmpty methods 
	 * even if the declared type of the target is an interface that 
	 * inherits the method from a superinterface.  
	 */
	@Test
	public void testIndirectInterface_SizeEmpty() {
		IndirectInterface2 empty = new ConcreteIndirectInterface(0); 
		assertEquals(-1, empty.size());
	}
	@Fix("testIndirectInterface_SizeEmpty")
	public void fixIndirectInterface_SizeEmpty() {
		IndirectInterface2 empty = new ConcreteIndirectInterface(0); 
		assertTrue(empty.isEmpty());
	}
	
	/**
	 * Verifies that the fixer can find the size and isEmpty methods 
	 * even if the declared type of the target is an interface that 
	 * inherits the method from a superinterface.  
	 */
	@Test
	public void testIndirectInterface_IsEmpty() {
		IndirectInterface2 full = new ConcreteIndirectInterface(5); 
		assertTrue(full.isEmpty());
	}
	@Fix("testIndirectInterface_IsEmpty")
	public void fixIndirectInterface_IsEmpty() {
		IndirectInterface2 full = new ConcreteIndirectInterface(5); 
		assertEquals(-1, full.size());
	}
	
	public static interface IndirectInterface {
		public abstract boolean isEmpty();
		public abstract int size();
	}
	public static interface IndirectInterface2 extends IndirectInterface {
		// inherits the "collection" methods from superinterface
	}
	public static class ConcreteIndirectInterface implements IndirectInterface2 {
		private int size;
		public ConcreteIndirectInterface(int size) {
			this.size = size;
		}
		@Override
		public boolean isEmpty() {
			return size == 0;
		}

		@Override
		public int size() {
			return size;
		}
	}
}
