package edu.illinois.reassert.test.assertfixer;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Test;

import edu.illinois.reassert.assertfixer.AccessorTree;
import edu.illinois.reassert.assertfixer.AccessorTree.AccessorTreeNode;
import edu.illinois.reassert.testutil.Box;
import edu.illinois.reassert.testutil.UnboundedBox;


/**
 * See also {@link AssertEqualsExpandAccessorsFixer_ReferenceTypeTest} for tests verifying
 * how an accessor tree is turned into an assertion tree  
 */
public class AccessorTreeTest {

	@Test
	public void testBuildSimpleTree() {
		Box expected = new Box(5);
		Box actual = new Box(6);
		AccessorTree tree = AccessorTree.build(expected, actual);
		Iterator<AccessorTreeNode> nodeIter = tree.preOrder().iterator();
		
		AccessorTreeNode node = nodeIter.next();
		assertFalse(node.getExpected().hasMethod());
		assertEquals(expected, node.getExpected().getValue());
		assertFalse(node.getActual().hasMethod());
		assertEquals(actual, node.getActual().getValue());
		assertFalse(node.valuesAreEqual());

		node = nodeIter.next();
		assertEquals("getValue", node.getExpected().getMethod().getName());
		assertEquals(5, node.getExpected().getValue());
		assertEquals("getValue", node.getActual().getMethod().getName());
		assertEquals(6, node.getActual().getValue());
		assertFalse(node.valuesAreEqual());
		
		node = nodeIter.next();
		assertEquals("toString", node.getExpected().getMethod().getName());
		assertEquals("Box(5)", node.getExpected().getValue());
		assertEquals("toString", node.getActual().getMethod().getName());
		assertEquals("Box(6)", node.getActual().getValue());
		assertFalse(node.valuesAreEqual());

		assertFalse(nodeIter.hasNext());
	}
	
	@Test
	public void testLiterals() {
		Object expected = 5;
		Object actual = 6;
		AccessorTree tree = AccessorTree.build(expected, actual);
		
		AccessorTreeNode root = tree.getRoot();
		assertFalse(root.getExpected().hasMethod());
		assertEquals(expected, root.getExpected().getValue());
		assertFalse(root.getActual().hasMethod());
		assertEquals(actual, root.getActual().getValue());
		assertFalse(root.valuesAreEqual());
		assertFalse(root.hasChildren());
	}
	
	@Test
	public void testBuildOnlyActual() {
		Box expected = null;
		Box actual = new Box(new Box(5));
		AccessorTree tree = AccessorTree.build(expected, actual);
		for (AccessorTreeNode node : tree.preOrder()) {
			assertFalse(node.getExpected().hasMethod());
			assertTrue(node.getActual() != AccessorTree.NO_VALUE);
		}		
	}
	
	@Test
	public void testBuildOnlyExpected() {
		Box expected = new Box(new Box(5));;
		Box actual = null;
		AccessorTree tree = AccessorTree.build(expected, actual);
		for (AccessorTreeNode node : tree.preOrder()) {
			assertFalse(node.getActual().hasMethod());
			assertTrue(node.getExpected() != AccessorTree.NO_VALUE);
		}		
	}
	
	@Test
	public void testStopAtEquals() {
		Box expected = new Box(5);
		Box actual = new Box(5);
		AccessorTree tree = AccessorTree.build(expected, actual);
		
		AccessorTreeNode root = tree.getRoot();
		assertFalse(root.getExpected().hasMethod());
		assertEquals(expected, root.getExpected().getValue());
		assertFalse(root.getActual().hasMethod());
		assertEquals(actual, root.getActual().getValue());
		assertTrue(root.valuesAreEqual());
		assertFalse(root.hasChildren());
	}
	
	@Test
	public void testNulls() {
		AccessorTree tree = AccessorTree.build(null, null);
		
		AccessorTreeNode root = tree.getRoot();
		assertFalse(root.getExpected().hasMethod());
		assertEquals(null, root.getExpected().getValue());
		assertFalse(root.getActual().hasMethod());
		assertEquals(null, root.getActual().getValue());
		assertTrue(root.valuesAreEqual());
		assertFalse(root.hasChildren());
	}
	
	/**
	 * Verfies that the traversal stops when it hits a known value in a branch
	 */
	@Test
	public void testCircular() {
		Box expected = null;
		Box actual = new Box();
		Box inner = new Box(actual);
		actual.setValue(inner);

		AccessorTree tree = AccessorTree.build(expected, actual);
		
		Iterator<AccessorTreeNode> nodeIter = tree.preOrder().iterator();
		
		AccessorTreeNode root = nodeIter.next();
		assertFalse(root.getActual().hasMethod());
		assertEquals(actual, root.getActual().getValue());
		
		AccessorTreeNode node = nodeIter.next();
		assertEquals("getValue", node.getActual().getMethod().getName());
		assertEquals(inner, node.getActual().getValue());

		node = nodeIter.next();
		assertEquals("getValue", node.getActual().getMethod().getName());
		assertEquals(actual, node.getActual().getValue());	
		
		node = nodeIter.next();
		assertEquals("toString", node.getActual().getMethod().getName());
		assertEquals("Box(Box(Box(...)))", node.getActual().getValue());

		node = nodeIter.next();
		assertEquals("toString", node.getActual().getMethod().getName());
		assertEquals("Box(Box(Box(...)))", node.getActual().getValue());

		assertFalse(nodeIter.hasNext());
		
	}

	/**
	 * Verifies that the traversal stops at the maximum depth
	 */
	@Test
	public void testMaxDepth() {
		final int expectedDepth = 10;
		AccessorTree tree = AccessorTree.build(null, new UnboundedBox(5), expectedDepth);
		int actualDepth = 0;
		for (AccessorTreeNode node : tree.preOrder()) {
			if ("getValue".equals(node.getActual().getMethod().getName())) {
				actualDepth++;
				//System.out.println(node);
			}
		}
		assertEquals(expectedDepth, actualDepth);
	}
	
}
