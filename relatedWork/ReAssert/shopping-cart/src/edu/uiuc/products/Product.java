package edu.uiuc.products;

import edu.uiuc.cart.Cart;

/**
 * This class represents a {@link Product} that can be added to a {@link Cart}.
 * 
 * @author <a href="http://www.vilasjagannath.com">Vilas Jagannath</a>
 */
public class Product {

	private String description;
	private double price;

	/**
	 * Creates a new {@link Product} with the given description and price.
	 * 
	 * @param description
	 *            the description for this product
	 * @param price
	 *            the price for this product. Should be positive.
	 */
	public Product(String description, double price) {
		if (price <= 0) {
			throw new IllegalArgumentException("Price should be positive.");
		}
		this.price = price;
		this.description = description;
	}

	/**
	 * Returns the price of this {@link Product}.
	 * 
	 * @return the price of this {@link Product}.
	 */
	public double getPrice() {
		return price;
	}

	/**
	 * Returns the description of this {@link Product}.
	 * 
	 * @return the description of this {@link Product}.
	 */
	public String getDescription() {
		return description;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		long temp;
		temp = Double.doubleToLongBits(price);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Product other = (Product) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (Double.doubleToLongBits(price) != Double
				.doubleToLongBits(other.price))
			return false;
		return true;
	}

}
