package edu.uiuc.cart;

import java.util.Map;

import junit.framework.TestCase;
import edu.uiuc.coupons.AnniversaryCoupon;
import edu.uiuc.products.BlackPen;
import edu.uiuc.products.Product;
import edu.uiuc.products.RedPen;

/**
 * Tests for operations on {@link Cart}, including adding/removing products and
 * computing coupon values.
 * 
 * @author <a href="http://www.vilasjagannath.com">Vilas Jagannath</a>
 */
public class CartTest extends TestCase {
	
	private Cart cart;
	private Product blackPen;
	private Product redPen;
	private AnniversaryCoupon anniversaryCoupon;

	public void setUp() {
		cart = new Cart();
		blackPen = new BlackPen();
		redPen = new RedPen();
		anniversaryCoupon = new AnniversaryCoupon();
	}

	public void testEmptyCart() {
		assertEquals(0.0, cart.getTotalPrice());
		assertEquals("Total: $0.00", cart.getBill());
	}

	public void testAddOneProduct() {
		cart.addProduct(blackPen, 2);

		assertEquals(5.0, cart.getTotalPrice());
		assertEquals("A black pen, Price: $2.50, Quantity: 2, Sub-total: $5.00\n" +
			"Total: $5.00", cart.getBill());
	}

	public void testAddSameProductTwice() {
		cart.addProduct(blackPen, 2);
		cart.addProduct(blackPen, 1);

		assertEquals(7.5, cart.getTotalPrice());
		assertEquals("A black pen, Price: $2.50, Quantity: 3, Sub-total: $7.50\n" +
			"Total: $7.50", cart.getBill());
	}

	public void testAddTwoDifferentProducts() {
		cart.addProduct(blackPen, 2);
		cart.addProduct(redPen, 3);

		assertEquals(14.0, cart.getTotalPrice());
		assertEquals("A black pen, Price: $2.50, Quantity: 2, Sub-total: $5.00\n" +
			"A red pen, Price: $3.00, Quantity: 3, Sub-total: $9.00\n" +
			"Total: $14.00", cart.getBill());
	}

	public void testRemoveNonExistantProduct() {
		assertFalse(cart.removeProduct(blackPen, 1));
	}

	public void testRemoveMoreThanAdded() {
		cart.addProduct(blackPen, 1);
		Map<Product, Integer> expectedProductQuantitites = cart
				.getProductQuantities();
		expectedProductQuantitites.put(blackPen, 1);

		assertEquals(expectedProductQuantitites, cart.getProductQuantities());
		assertFalse(cart.removeProduct(blackPen, 2));
		assertEquals(expectedProductQuantitites, cart.getProductQuantities());
	}

	public void testRemoveLessThanAdded() {
		cart.addProduct(blackPen, 2);

		assertEquals(5.0, cart.getTotalPrice());
		assertEquals("A black pen, Price: $2.50, Quantity: 2, Sub-total: $5.00\n" +
			"Total: $5.00", cart.getBill());

		assertTrue(cart.removeProduct(blackPen, 1));

		assertEquals(2.5, cart.getTotalPrice());
		assertEquals("A black pen, Price: $2.50, Quantity: 1, Sub-total: $2.50\n" +
			"Total: $2.50", cart.getBill());
	}

	public void testRemoveAsMuchAsAdded() {
		cart.addProduct(blackPen, 2);

		assertEquals(5.00, cart.getTotalPrice());
		assertEquals(
				"A black pen, Price: $2.50, Quantity: 2, Sub-total: $5.00\nTotal: $5.00",
				cart.getBill());

		assertTrue(cart.removeProduct(blackPen, 2));

		assertEquals(0.0, cart.getTotalPrice());
		assertEquals("Total: $0.00", cart.getBill());
	}

	public void testAddRemoveSequences() {
		cart.addProduct(blackPen, 2);
		cart.addProduct(blackPen, 1);

		assertEquals(7.50, cart.getTotalPrice());
		assertEquals(
				"A black pen, Price: $2.50, Quantity: 3, Sub-total: $7.50\nTotal: $7.50",
				cart.getBill());

		assertTrue(cart.removeProduct(blackPen, 1));
		cart.addProduct(redPen, 3);

		assertEquals(14.0, cart.getTotalPrice());
		assertEquals("A black pen, Price: $2.50, Quantity: 2, Sub-total: $5.00\nA red pen, Price: $3.00, Quantity: 3, Sub-total: $9.00\nTotal: $14.00", cart.getBill());

		assertFalse(cart.removeProduct(blackPen, 3));
		assertTrue(cart.removeProduct(redPen, 3));

		assertEquals(5.00, cart.getTotalPrice());
		assertEquals(
				"A black pen, Price: $2.50, Quantity: 2, Sub-total: $5.00\nTotal: $5.00",
				cart.getBill());

		assertFalse(cart.removeProduct(blackPen, 3));
		assertTrue(cart.removeProduct(blackPen, 2));

		assertEquals(0.0, cart.getTotalPrice());
		assertEquals("Total: $0.00", cart.getBill());
	}

	public void testInapplicableCoupon() {
		cart.addProduct(redPen, 3);

		assertEquals(9.0, cart.getTotalPrice());
		assertEquals("A red pen, Price: $3.00, Quantity: 3, Sub-total: $9.00\nTotal: $9.00", cart.getBill());

		cart.addCoupon(anniversaryCoupon);

		assertEquals(9.0, cart.getTotalPrice());
		assertEquals("A red pen, Price: $3.00, Quantity: 3, Sub-total: $9.00\nAnniversary coupon: Buy one get one free for black pens only!, Sub-total: -$0.00\nTotal: $9.00", cart.getBill());

		cart.removeCoupon(anniversaryCoupon);

		assertEquals(9.0, cart.getTotalPrice());
		assertEquals("A red pen, Price: $3.00, Quantity: 3, Sub-total: $9.00\nTotal: $9.00", cart.getBill());
	}

	public void testApplicableCoupon() {
		cart.addProduct(blackPen, 3);

		assertEquals(7.5, cart.getTotalPrice());
		assertEquals(
				"A black pen, Price: $2.50, Quantity: 3, Sub-total: $7.50\nTotal: $7.50",
				cart.getBill());

		cart.addCoupon(anniversaryCoupon);

		assertEquals(5.00, cart.getTotalPrice());
		assertEquals(
				"A black pen, Price: $2.50, Quantity: 3, Sub-total: $7.50\nAnniversary coupon: Buy one get one free for black pens only!, Sub-total: -$2.50\nTotal: $5.00",
				cart.getBill());

		cart.removeCoupon(anniversaryCoupon);

		assertEquals(7.5, cart.getTotalPrice());
		assertEquals(
				"A black pen, Price: $2.50, Quantity: 3, Sub-total: $7.50\nTotal: $7.50",
				cart.getBill());
	}

	public void testCouponWithQuantity() {
		cart.addProduct(blackPen, 1);
		cart.addCoupon(anniversaryCoupon);

		assertEquals(2.50, cart.getTotalPrice());
		assertEquals("A black pen, Price: $2.50, Quantity: 1, Sub-total: $2.50\nAnniversary coupon: Buy one get one free for black pens only!, Sub-total: -$0.00\nTotal: $2.50", cart.getBill());

		cart.addProduct(blackPen, 1);

		assertEquals(2.5, cart.getTotalPrice());
		assertEquals("A black pen, Price: $2.50, Quantity: 2, Sub-total: $5.00\nAnniversary coupon: Buy one get one free for black pens only!, Sub-total: -$2.50\nTotal: $2.50", cart.getBill());

		cart.removeCoupon(anniversaryCoupon);

		assertEquals(5.0, cart.getTotalPrice());
		assertEquals(
				"A black pen, Price: $2.50, Quantity: 2, Sub-total: $5.00\nTotal: $5.00",
				cart.getBill());

		cart.addCoupon(anniversaryCoupon);
		cart.removeProduct(blackPen, 1);

		assertEquals(2.50, cart.getTotalPrice());
		assertEquals("A black pen, Price: $2.50, Quantity: 1, Sub-total: $2.50\nAnniversary coupon: Buy one get one free for black pens only!, Sub-total: -$0.00\nTotal: $2.50", cart.getBill());
	}

	public void testTwiceApplicableCoupon() {
		cart.addProduct(blackPen, 2);
		cart.addCoupon(anniversaryCoupon);

		assertEquals(2.5, cart.getTotalPrice());
		assertEquals("A black pen, Price: $2.50, Quantity: 2, Sub-total: $5.00\nAnniversary coupon: Buy one get one free for black pens only!, Sub-total: -$2.50\nTotal: $2.50", cart.getBill());

		cart.addProduct(blackPen, 2);

		assertEquals(5.0, cart.getTotalPrice());
		assertEquals("A black pen, Price: $2.50, Quantity: 4, Sub-total: $10.00\nAnniversary coupon: Buy one get one free for black pens only!, Sub-total: -$5.00\nTotal: $5.00", cart.getBill());

		cart.removeCoupon(anniversaryCoupon);

		assertEquals(10.0, cart.getTotalPrice());
		assertEquals(
				"A black pen, Price: $2.50, Quantity: 4, Sub-total: $10.00\nTotal: $10.00",
				cart.getBill());

		cart.addCoupon(anniversaryCoupon);

		assertEquals(5.0, cart.getTotalPrice());
		assertEquals("A black pen, Price: $2.50, Quantity: 4, Sub-total: $10.00\nAnniversary coupon: Buy one get one free for black pens only!, Sub-total: -$5.00\nTotal: $5.00", cart.getBill());
	}

	public void testApplicableAndInapplicableCoupon() {
		cart.addProduct(redPen, 3);
		cart.addProduct(blackPen, 3);
		cart.addCoupon(anniversaryCoupon);

		assertEquals(14.0, cart.getTotalPrice());
		assertEquals("A black pen, Price: $2.50, Quantity: 3, Sub-total: $7.50\nA red pen, Price: $3.00, Quantity: 3, Sub-total: $9.00\nAnniversary coupon: Buy one get one free for black pens only!, Sub-total: -$2.50\nTotal: $14.00", cart.getBill());

		cart.removeCoupon(anniversaryCoupon);

		assertEquals(16.5, cart.getTotalPrice());
		assertEquals("A black pen, Price: $2.50, Quantity: 3, Sub-total: $7.50\nA red pen, Price: $3.00, Quantity: 3, Sub-total: $9.00\nTotal: $16.50", cart.getBill());

		cart.addCoupon(anniversaryCoupon);

		assertEquals(14.0, cart.getTotalPrice());
		assertEquals("A black pen, Price: $2.50, Quantity: 3, Sub-total: $7.50\nA red pen, Price: $3.00, Quantity: 3, Sub-total: $9.00\nAnniversary coupon: Buy one get one free for black pens only!, Sub-total: -$2.50\nTotal: $14.00", cart.getBill());
	}

	public void testAddRemoveSequenceAndCoupon() {
		cart.addProduct(blackPen, 2);
		cart.addCoupon(anniversaryCoupon);
		cart.addProduct(blackPen, 1);

		assertEquals(5.0, cart.getTotalPrice());
		assertEquals("A black pen, Price: $2.50, Quantity: 3, Sub-total: $7.50\nAnniversary coupon: Buy one get one free for black pens only!, Sub-total: -$2.50\nTotal: $5.00", cart.getBill());

		cart.removeCoupon(anniversaryCoupon);

		assertEquals(7.50, cart.getTotalPrice());
		assertEquals(
				"A black pen, Price: $2.50, Quantity: 3, Sub-total: $7.50\nTotal: $7.50",
				cart.getBill());

		cart.addCoupon(anniversaryCoupon);
		assertTrue(cart.removeProduct(blackPen, 1));
		cart.addProduct(redPen, 3);

		assertEquals(11.5, cart.getTotalPrice());
		assertEquals("A black pen, Price: $2.50, Quantity: 2, Sub-total: $5.00\nA red pen, Price: $3.00, Quantity: 3, Sub-total: $9.00\nAnniversary coupon: Buy one get one free for black pens only!, Sub-total: -$2.50\nTotal: $11.50", cart.getBill());

		assertFalse(cart.removeProduct(blackPen, 3));
		assertTrue(cart.removeProduct(redPen, 3));

		assertEquals(2.5, cart.getTotalPrice());
		assertEquals("A black pen, Price: $2.50, Quantity: 2, Sub-total: $5.00\nAnniversary coupon: Buy one get one free for black pens only!, Sub-total: -$2.50\nTotal: $2.50", cart.getBill());

		assertFalse(cart.removeProduct(blackPen, 3));
		assertTrue(cart.removeProduct(blackPen, 2));

		assertEquals(0.0, cart.getTotalPrice());
		assertEquals("Anniversary coupon: Buy one get one free for black pens only!, Sub-total: -$0.00\nTotal: $0.00", cart.getBill());
	}

}
