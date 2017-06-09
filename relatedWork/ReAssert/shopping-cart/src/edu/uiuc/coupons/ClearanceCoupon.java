package edu.uiuc.coupons;

import edu.uiuc.cart.Cart;
import edu.uiuc.products.RedPen;

/**
 * This class represents an {@link Coupon} for {@link RedPen} objects using
 * which three {@link RedPen} objects can be bought for the price of one.
 * 
 * @author <a href="http://www.vilasjagannath.com">Vilas Jagannath</a>
 */
public class ClearanceCoupon implements Coupon {

	public String getDescription() {
		return "Clearance coupon: Buy one red pen, get two red pens free!";
	}

	/**
	 * For every three {@link RedPen}s, a discount value equal to the price of
	 * two {@link RedPen}s is added. This {@link ClearanceCoupon} can be applied
	 * along with any other {@link Coupon}.
	 * 
	 * @see Coupon#getValue(Cart)
	 */
	public double getValue(Cart cart) {

		RedPen redPen = new RedPen();
		Integer redPensQuantity = cart.getProductQuantities().get(redPen);
		if (redPensQuantity == null) {
			redPensQuantity = 0;
		}

		return (redPensQuantity / 3) * 2 * redPen.getPrice();
	}

}
