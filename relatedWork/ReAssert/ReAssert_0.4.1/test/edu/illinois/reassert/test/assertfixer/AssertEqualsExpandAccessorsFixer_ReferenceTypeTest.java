package edu.illinois.reassert.test.assertfixer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.illinois.reassert.assertfixer.AssertEqualsExpandAccessorsFixer;
import edu.illinois.reassert.testutil.Box;
import edu.illinois.reassert.testutil.ExtendedBox;
import edu.illinois.reassert.testutil.Fix;
import edu.illinois.reassert.testutil.FixChecker;
import edu.illinois.reassert.testutil.Fixers;
import edu.illinois.reassert.testutil.UnboundedBox;


@RunWith(FixChecker.class)
@Fixers(AssertEqualsExpandAccessorsFixer.class)
public class AssertEqualsExpandAccessorsFixer_ReferenceTypeTest {

	@Test
	public void testObject() {
		Object expected = new Object();
		Object actual = new Object();
		assertEquals(expected, actual);
	}
	@Fix("testObject")
	public void fixObject() {
		Object expected = new Object();
		Object actual = new Object();
		assertNotNull(actual);
	}
	
	@Test
	public void testBox() {
		Box expected = new Box(4);
		Box actual = new Box(5);
		assertEquals(expected, actual);
	}
	@Fix("testBox")
	public void fixTestBox() {
		Box expected = new Box(4);		
		Box actual = new Box(5);
		{
			assertEquals("Box(5)", actual.toString());
			assertEquals(5, actual.getValue());
		}
	}

	@Test
	public void testNestedBoxes() {
		Box expected = new Box(new Box(4));
		Box actual = new Box(new Box(5));
		assertEquals(expected, actual);
	}
	@Fix("testNestedBoxes")
	public void fixNestedBoxes() {
		Box expected = new Box(new Box(4));
		Box actual = new Box(new Box(5));
		{
			assertEquals("Box(Box(5))", actual.toString());
			{
				Box box0 = (Box) expected.getValue();
				Box box1 = (Box) actual.getValue();
				assertEquals("Box(5)", box1.toString());
				assertEquals(5, box1.getValue());
			}
		}
	}
	
	@Test
	public void testCircularBoxes() {
		Box expected = null;
		Box actual = new Box();
		Box inner = new Box(actual);
		actual.setValue(inner);
		assertEquals(expected, actual);
	}
	@Fix("testCircularBoxes")
	public void fixCircularBoxes() {
		Box expected = null;
		Box actual = new Box();
		Box inner = new Box(actual);
		actual.setValue(inner);
		{
			assertEquals("Box(Box(Box(...)))" ,actual.toString());
			{
				Box box0 = ((Box)(actual.getValue()));
				assertEquals("Box(Box(Box(...)))" ,box0.toString());
				assertEquals(actual ,box0.getValue());
			}
		}
	}
	
	@Test
	public void testUnbalancedActual() {
		Box expected = new Box(5);
		Box actual = new Box(new Box(new Box(6)));
		assertEquals(expected, actual);
	}
	@Fix("testUnbalancedActual")
	public void fixUnbalancedActual() {
		Box expected = new Box(5);
		Box actual = new Box(new Box(new Box(6)));
		{
			assertEquals("Box(Box(Box(6)))" ,actual.toString());
			{
				Integer integer0 = ((Integer)(expected.getValue()));
				Box box1 = ((Box)(actual.getValue()));
				assertEquals("Box(Box(6))" ,box1.toString());
				{
					Box box2 = ((Box)(box1.getValue()));
					assertEquals("Box(6)" ,box2.toString());
					assertEquals(6 ,box2.getValue());
				}
			}
		}
	}

	@Test
	public void testUnbalancedExpected() {
		Box expected = new Box(new Box(new Box(6)));
		Box actual = new Box(5);
		assertEquals(expected, actual);
	}
	@Fix("testUnbalancedExpected")
	public void fixUnbalancedExpected() {
		Box expected = new Box(new Box(new Box(6)));
		Box actual = new Box(5);
		{
			assertEquals("Box(5)" ,actual.toString());
			assertEquals(5 ,actual.getValue());
		}
	}
	
	@Test
	public void testExtractDummyVariables() {
		assertEquals(new Box(4), new Box(5));
	}
	@Fix("testExtractDummyVariables")
	public void fixExtractDummyVariables() {
		{
			Box box0 = new Box(4);		
			Box box1 = new Box(5);
			assertEquals("Box(5)", box1.toString());
			assertEquals(5, box1.getValue());
		}
	}
	
	@Test
	public void testCommonFields() {
		ExtendedBox expected = new ExtendedBox(5);
		expected.setExtendedValue(7);
		ExtendedBox actual = new ExtendedBox(6);
		actual.setExtendedValue(7);
		assertEquals(expected, actual);
	}
	@Fix("testCommonFields")
	public void fixCommonField() {
		ExtendedBox expected = new ExtendedBox(5);
		expected.setExtendedValue(7);
		ExtendedBox actual = new ExtendedBox(6);
		actual.setExtendedValue(7);
		{
			assertEquals(expected.getExtendedValue(), actual.getExtendedValue());
			assertEquals("ExtendedBox(6)", actual.toString());
			assertEquals(6, actual.getValue());
		}
	}
	
	@Test
	public void testActualWithExtraAccessor() {
		Box expected = new Box(5);
		ExtendedBox actual = new ExtendedBox(6);
		actual.setExtendedValue(new Box(7));
		assertEquals(expected, actual);
	}
	@Fix("testActualWithExtraAccessor")
	public void fixActualWithExtraAccessor() {
		Box expected = new Box(5);
		ExtendedBox actual = new ExtendedBox(6);
		actual.setExtendedValue(new Box(7));
		{
			assertEquals("ExtendedBox(6)" ,actual.toString());
			assertEquals(6 ,actual.getValue());
			{
				Box box0 = ((Box)(actual.getExtendedValue()));
				assertEquals("Box(7)" ,box0.toString());
				assertEquals(7 ,box0.getValue());
			}
		}
	}
	
	@Test
	public void testExpectedWithExtraAccessor() {
		ExtendedBox expected = new ExtendedBox(5);
		expected.setExtendedValue(new Box(7));
		Box actual = new Box(6);
		assertEquals(expected, actual);
	}
	@Fix("testExpectedWithExtraAccessor")
	public void fixExpectedWithExtraAccessor() {
		ExtendedBox expected = new ExtendedBox(5);
		expected.setExtendedValue(new Box(7));
		Box actual = new Box(6);
		{
			assertEquals("Box(6)" ,actual.toString());
			assertEquals(6 ,actual.getValue());
		}	
	}
	
	@Test
	public void testNestedNullExpected() {
		Box expected = new Box(new Box(null));
		Box actual = new Box(new Box(5));
		assertEquals(expected, actual);
	}
	@Fix("testNestedNullExpected")
	public void fixNestedNullExpected() {
		Box expected = new Box(new Box(null));
		Box actual = new Box(new Box(5));
		{
			assertEquals("Box(Box(5))" ,actual.toString());
			{
				Box box0 = ((Box)(expected.getValue()));
				Box box1 = ((Box)(actual.getValue()));
				assertEquals("Box(5)" ,box1.toString());
				assertEquals(5 ,box1.getValue());
			}
		}
	}

	@Test
	public void testNestedNullActual() {
		Box expected = new Box(new Box(5));
		Box actual = new Box(new Box(null));
		assertEquals(expected, actual);
	}
	@Fix("testNestedNullActual")
	public void fixNestedNullActual() {
		Box expected = new Box(new Box(5));
		Box actual = new Box(new Box(null));
		{
			assertEquals("Box(Box(null))" ,actual.toString());
			{
				Box box0 = ((Box)(expected.getValue()));
				Box box1 = ((Box)(actual.getValue()));
				assertEquals("Box(null)" ,box1.toString());
				assertNull(box1.getValue());
			}
		}
	}
	
	@Test
	public void testCrossEdge() {
		Box expected = null; // don't care
		Box common = new Box(5);
		Box actual = new ExtendedBox(common, new Box(common));
		assertEquals(expected, actual);
	}
	@Fix("testCrossEdge")
	public void fixCrossEdge() {
		Box expected = null;
		Box common = new Box(5);
		Box actual = new ExtendedBox(common , new Box(common));
		{
			ExtendedBox extendedbox0 = ((ExtendedBox)(actual));
			{
				Box box1 = ((Box)(extendedbox0.getExtendedValue()));
				assertEquals("Box(Box(5))" ,box1.toString());
				{
					Box box2 = ((Box)(box1.getValue()));
					assertEquals("Box(5)" ,box2.toString());
					assertEquals(5 ,box2.getValue());
				}
			}
			assertEquals("ExtendedBox(ExtendedBox(5))" ,extendedbox0.toString());
			//assertEquals(box2, extendedbox0.getValue()); doesn't appear because box2 is out of scope
		}
	}
	
	@Test
	public void testCastToRuntimeType() {
		Box expected = null; // don't care
		Box actual = new ExtendedBox(5, 7);
		assertEquals(expected, actual);
	}
	@Fix("testCastToRuntimeType")
	public void fixCastToRuntimeType() {
		Box expected = null;
		Box actual = new ExtendedBox(5 , 7);
		{
			ExtendedBox extendedbox0 = ((ExtendedBox)(actual));
			assertEquals(7 ,extendedbox0.getExtendedValue());
			assertEquals("ExtendedBox(5)" ,extendedbox0.toString());
			assertEquals(5 ,extendedbox0.getValue());
		}
	}

	@Test
	public void testBoxedPrimitive_BooleanTrue() {
		Box expected = null; // don't care 
		Box actual = new Box(new Boolean(true));
		assertEquals(expected, actual);
	}
	@Fix("testBoxedPrimitive_BooleanTrue")
	public void fixBoxedPrimitive_BooleanTrue() {
		Box expected = null; // don't care 
		Box actual = new Box(new Boolean(true));
		{
			assertEquals("Box(true)" ,actual.toString());
			assertTrue((Boolean) actual.getValue());
		}
	}
	
	@Test
	public void testBoxedPrimitive_BooleanFalse() {
		Box expected = null; // don't care 
		Box actual = new Box(new Boolean(false));
		assertEquals(expected, actual);
	}
	@Fix("testBoxedPrimitive_BooleanFalse")
	public void fixBoxedPrimitive_BooleanFalse() {
		Box expected = null; // don't care 
		Box actual = new Box(new Boolean(false));
		{
			assertEquals("Box(false)" ,actual.toString());
			assertFalse((Boolean) actual.getValue());
		}
	}
	
	@Test
	public void testBooleanAccessor() {
		BooleanBox expected = new BooleanBox(true);
		BooleanBox actual = new BooleanBox(false);
		assertEquals(expected, actual);
	}
	@Fix("testBooleanAccessor")
	public void fixBooleanAccessor() {
		BooleanBox expected = new BooleanBox(true);
		BooleanBox actual = new BooleanBox(false);
		{
			assertEquals(expected.getTrue() ,actual.getTrue());
			assertEquals(expected.getFalse() ,actual.getFalse());
			assertFalse(actual.getValue());
		}
	}
	public static class BooleanBox {
		private boolean value = false;
		public BooleanBox(boolean value) {
			this.value = value;
		}
		public boolean getValue() {
			return value;
		}
		public boolean getTrue() {
			return true;
		}
		public boolean getFalse() {
			return false;
		}
	}
	
	@Test
	public void testMaximumDepth() {
		UnboundedBox actual = new UnboundedBox(5);
		assertEquals(null, actual);
	}
	@Fix("testMaximumDepth")
	public void fixMaximumDepth() {
		UnboundedBox actual = new UnboundedBox(5);
		{
			{
				UnboundedBox unboundedbox0 = ((UnboundedBox)(actual.getValue()));
				{
					UnboundedBox unboundedbox1 = ((UnboundedBox)(unboundedbox0.getValue()));
					{
						UnboundedBox unboundedbox2 = ((UnboundedBox)(unboundedbox1.getValue()));
						assertEquals("UnboundedBox(5)" ,unboundedbox2.toString());
					}
					assertEquals("UnboundedBox(5)" ,unboundedbox1.toString());
				}
				assertEquals("UnboundedBox(5)" ,unboundedbox0.toString());
			}
			assertEquals("UnboundedBox(5)" ,actual.toString());
		}
	}
	
	@Test
	public void testAnonymousExpected() {
		Box expected = new Box(5) {}; // anonymous
		Box actual = new Box(3);		
		assertEquals(expected, actual);		
	}
	@Fix("testAnonymousExpected")
	public void fixAnonymousExpected() {
		Box expected = new Box(5) {}; // anonymous
		Box actual = new Box(3);
		{
			assertEquals("Box(3)", actual.toString());
			assertEquals(3, actual.getValue());
		}
	}
	
	@Test
	public void testAnonymousActual() {
		Box expected = new Box(5); 
		Box actual = new Box(3) {}; // anonymous		
		assertEquals(expected, actual);		
	}
	@Fix("testAnonymousActual") 
	public void fixAnonymousActual() {
		Box expected = new Box(5);
		Box actual = new Box(3) {};
		{
			assertEquals("(3)", actual.toString());
			assertEquals(3, actual.getValue());
		}
	}
	
	@Test
	public void testAnonymousNested() {
		Box actual = new Box(new Box() {});
		assertEquals(null, actual);
	}
	@Fix("testAnonymousNested")
	public void fixAnonymousNested() {
		Box actual = new Box(new Box() {});
		{
			assertEquals("Box(Box(null))", actual.toString());
			{
				Box box0 = (Box)(actual.getValue());
				assertEquals("(null)", box0.toString());
				assertNull(box0.getValue());
			}
		}
	}

	@Test
	public void testInaccessibleType() {
		BoxHoldingInaccessible actual = new BoxHoldingInaccessible();
		assertEquals(null, actual);
	}
	@Fix("testInaccessibleType") 
	public void fixInaccessibleType() {
		BoxHoldingInaccessible actual = new BoxHoldingInaccessible();
		{
			assertNotNull(actual.getValue());
			assertEquals("BoxHoldingInaccessible(null)", actual.toString());
		}
	}
	public static final class BoxHoldingInaccessible extends Box {
		@Override
		public Object getValue() {
			return new InaccessibleInner();
		}
		private static final class InaccessibleInner {
			public Object getOtherValue() {
				return "other";
			}
		}
		@Override
		public boolean equals(Object obj) {
			return false;
		}
	}

	@Test
	public void testInaccessibleTypeAccessibleSupertype() {
		BoxHoldingAccessibleSupertype actual = new BoxHoldingAccessibleSupertype();
		assertEquals(null, actual);
	}
	@Fix("testInaccessibleTypeAccessibleSupertype") 
	public void fixInaccessibleTypeAccessibleSupertype() {
		BoxHoldingAccessibleSupertype actual = new BoxHoldingAccessibleSupertype();
		{
			{
				Box box0 = (Box)(actual.getValue());
				assertEquals("InaccessibleInner(null)", box0.toString());
				assertNull(box0.getValue());
			}
			assertEquals("BoxHoldingAccessibleSupertype(null)", actual.toString());
		}
	}
	public static final class BoxHoldingAccessibleSupertype extends Box {
		public BoxHoldingAccessibleSupertype() {}
		@Override
		public Object getValue() {
			return new InaccessibleInner();
		}
		private static final class InaccessibleInner extends Box {
			public Object getOtherValue() {
				return "other";
			}
		}
	}

}
