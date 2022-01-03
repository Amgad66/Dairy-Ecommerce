import java.io.Serializable;

/**
 * Abstraction of a determined Product. Every product has a name, a producer, the year
 * of production, some notes that may be useful for the customers, a string
 * containing all the grapes used in each product, the quantity in stock of each
 * product.
 */
public class Product implements Serializable {

	private static final long serialVersionUID = 1727284212719259730L;
	private String name;
	private int productId;
	private String producer;
	private int year;
	private String notes;
	private int quantity;

	/**
	 * {@code Product} class constructor.
	 */
	public Product() {
		this.name = "";
		this.producer = "";
		this.productId = -1;
		this.year = -1;
		this.notes = "";
		this.quantity = -1;
	}

	/**
	 * {@code Product} class constructor.
	 * 
	 * @param id       product id of the {@code Product}.[int]
	 * @param name     name of the {@code Product}. [String]
	 * @param producer producer of the {@code Product}. [String]
	 * @param year     year of production of the {@code Product}. [int]
	 * @param notes    notes for the {@code Product}. [String]
	 * @param quantity quantity of the {@code Product}. [int]
	 */
	public Product(final int id, final String name, final String producer, final int year, final String notes,
				   final int quantity) {

		this.name = name;
		this.producer = producer;
		this.year = year;
		this.notes = notes;
		this.productId = id;
		this.quantity = quantity;
	}

	/**
	 * Gets the name of the {@code Product}.
	 * 
	 * @return the name of the {@code Product}. [String]
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Gets the producer of the {@code Product}.
	 * 
	 * @return the producer of the {@code Product}. [String]
	 */
	public String getProducer() {
		return this.producer;
	}

	/**
	 * Gets the name of the {@code Product}.
	 * 
	 * @return the name of the {@code Product}. [String]
	 */
	public int getYear() {
		return this.year;
	}

	/**
	 * Gets the notes of the {@code Product}.
	 * 
	 * @return the notes of the {@code Product}. [String]
	 */
	public String getNotes() {
		return this.notes;
	}

	/**
	 * Gets the quantity of the {@code Product}.
	 * 
	 * @return the quantity of the {@code Product}. [int]
	 */
	public int getQuantity() {
		return this.quantity;
	}


	/**
	 * Gets the product id of the {@code Product}.
	 * 
	 * @return the product id of the {@code Product}. [int]
	 */
	public int getProductId() {
		return this.productId;
	}
}