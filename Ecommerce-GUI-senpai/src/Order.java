import java.io.Serializable;
import java.util.ArrayList;

/**
 * Abstraction of a determined Order. Every order has an id, an email which
 * relates the order to the {@code User} who placed it, a status (true if the
 * order has been shipped, false otherwise) and the list of {@code Product} that
 * the {@code User} has ordered.
 * 
 * @see Product
 * @see User
 */
public class Order implements Serializable{

	private static final long serialVersionUID = -2138142735104663987L;
	private ArrayList<Product> items = new ArrayList<Product>();
	private int id;
	private String customer;
	private Boolean shipped;

	/**
	 * {@code Order} class constructor.
	 */
	public Order() {
		this.shipped = false;
	}

	/**
	 * {@code Order} class constructor.
	 * 
	 * @param id       of the {@code Order}. [Int]
	 * @param ship     {@code true} if the order has been shipped, otherwise
	 *                 {@code false}. [Boolean]
	 * @param customer email of the {@code User} who placed the {@code Order}.
	 *                 [String]
	 * @param products    the products the {@code User} wants to buy. [ArrayList of Product]
	 * @see Product
	 * @see User
	 */
	public Order(final int id, final Boolean ship, final String customer, final ArrayList<Product> products) {
		this.id = id;
		this.shipped = ship;
		this.customer = customer;
		this.items = products;
	}

	/**
	 * Gets the email of the customer of the selected {@code Order}.
	 * 
	 * @return customer's email. [String]
	 * @see User
	 */
	public String getCustomer() {
		return this.customer;
	}

	/**
	 * Gets the id of the selected {@code Order}.
	 * 
	 * @return the id of the {@code Order}. [int]
	 */
	public int getId() {
		return this.id;
	}

	/**
	 * Gets the status of the selected {@code Order}.
	 * 
	 * @return {@code true} if the order has been shipped, else {@code false}.
	 *         [Boolean]
	 */
	public Boolean getStatus() {
		return this.shipped;
	}

	/**
	 * Gets the products from the selected {@code Order}.
	 * 
	 * @return the products of the {@code Order}. [ArrayList of Product]
	 * @see Product
	 */
	public ArrayList<Product> getProducts() {
		return this.items;
	}
}