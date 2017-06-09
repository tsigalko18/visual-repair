package edu.uiuc.coupons;

import java.util.Set;

import edu.uiuc.cart.Cart;
import edu.uiuc.products.BlackPen;

/**
 * This class represents a {@link Coupon} for {@link BlackPen}s using which two
 * {@link BlackPen}s can be bought for the price of one.
 * 
 * @author <a href="http://www.vilasjagannath.com">Vilas Jagannath</a>
 */
public class AnniversaryCoupon implements Coupon {

	public String getDescription() {
		return "Anniversary coupon: Buy one get one free for black pens only!";
	}

	/**
	 * Checks that no other coupons are being used. If so, for every two
	 * {@link BlackPen}s a discount value equal to the price of one
	 * {@link BlackPen} is added.
	 * 
	 * @see Coupon#getValue(Cart)
	 */
	public double getValue(Cart cart) {

		Set<Coupon> coupons = cart.getCoupons();
		if (coupons.size() > 1 || !coupons.contains(this)) {
			return 0;
		}

		BlackPen blackPen = new BlackPen();
		Integer blackPensQuantity = cart.getProductQuantities().get(blackPen);
		if (blackPensQuantity == null) {
			blackPensQuantity = 0;
		}
		return (blackPensQuantity / 2) * blackPen.getPrice();
	}

}
