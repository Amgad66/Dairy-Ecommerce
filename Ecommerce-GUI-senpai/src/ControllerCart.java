import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Controller for Cart, page accessible by {@code User} with permission > 0 (aka
 * everyone)
 */
public class ControllerCart implements Controller {

	private User currentUser;

	@FXML
	private AnchorPane rootPane;

	@FXML
	private TableView<Product> tableView;

	@FXML
	private TableColumn<Product, String> nameColumn;

	@FXML
	private TableColumn<Product, Integer> yearColumn;

	@FXML
	private TableColumn<Product, String> producerColumn;

	@FXML
	private TableColumn<Product, Integer> quantityColumn;

	/**
	 * Initialize {@code this.currentUser} with the passed value. This method is
	 * made to be called from another controller, using the {@code load} method in
	 * {@code Loader} class.
	 * 
	 * @param user the {@code User} we want to pass. [User]
	 * @see Loader
	 */
	@SuppressWarnings("unchecked")
	public void initData(User user) {
		this.currentUser = user;

		try {
			Socket socket = new Socket("localhost", 4316);

			// client -> server
			OutputStream outputStream = socket.getOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(outputStream);
			String[] toBeSent = { "display_cart", this.currentUser.getEmail() };
			out.writeObject(toBeSent);

			// server -> client
			InputStream inputStream = socket.getInputStream();
			ObjectInputStream in = new ObjectInputStream(inputStream);

			ArrayList<Product> cartResult = (ArrayList<Product>) in.readObject();
			addToTable(cartResult);
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Loads the specified ArrayList of Products in the table view. This method will
	 * override the previous content of the table.
	 * 
	 * @param products the content that needs to be displayed on the table. [Arraylist
	 *              of Product]
	 * @see Product
	 */
	public void addToTable(ArrayList<Product> products) {
		// set up the columns in the table
		this.nameColumn.setCellValueFactory(new PropertyValueFactory<Product, String>("Name"));
		this.yearColumn.setCellValueFactory(new PropertyValueFactory<Product, Integer>("Year"));
		this.producerColumn.setCellValueFactory(new PropertyValueFactory<Product, String>("Producer"));
		this.quantityColumn.setCellValueFactory(new PropertyValueFactory<Product, Integer>("Quantity"));
		ObservableList<Product> oListProducts = FXCollections.observableArrayList(products);
		// load data
		tableView.setItems(oListProducts);
	}

	/**
	 * Goes back to the employee homepage.
	 * 
	 * @param event GUI event. [ActionEvent]
	 * @throws IOException if the file can't be accessed.
	 */
	@FXML
	public void back(ActionEvent event) throws IOException {
		Loader loader = new Loader(this.currentUser, this.rootPane);
		loader.load("homepage_user");
	}

	/**
	 * Allows the {@code User} to buy the items in his cart.
	 * 
	 * @param event GUI event. [ActionEvent]
	 * @throws UnknownHostException if the IP address of the host could not be
	 *                              determined.
	 * @throws IOException          if an I/O error occurs when creating the socket.
	 * @see User
	 */
	@FXML
	public void buy(ActionEvent event) throws IOException, UnknownHostException {
		if (this.currentUser.getPermission() > 0) {
			// user is authorized to perform the action
			Socket socket = new Socket("localhost", 4316);

			// client -> server
			OutputStream outputStream = socket.getOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(outputStream);
			String[] toBeSent = { "new_order", this.currentUser.getEmail() };
			out.writeObject(toBeSent);

			// server -> client
			InputStream inputStream = socket.getInputStream();
			ObjectInputStream in = new ObjectInputStream(inputStream);

			try {
				Order newOrder = (Order) in.readObject();
				initData(this.currentUser);
				if (newOrder.getId() == 0) {
					Alert alert = new Alert(AlertType.WARNING);
					alert.setTitle("Order failed!");
					alert.setHeaderText(String.format(
							"Your order has been not been placed!\nWe do not enough in stock. We will send a notification once we restock"));
					alert.showAndWait();
				} else {
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Order submitted!");
					alert.setHeaderText(String.format("Your order has been placed!\nOrder ID: %d", newOrder.getId()));
					alert.setContentText(
							"(If some products are left in your cart we either are out of stock or they are not available in the desired quantity, you will receive a notification once we restock!)");
					alert.showAndWait();
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			socket.close();
		} else {
			// user is not authorized to perform the action
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Not authorized");
			alert.setHeaderText("You are not allowed to perform this action.");
			alert.showAndWait();
		}
	}

	/**
	 * Allows anyone to search for products.
	 * 
	 * @param event GUI event. [ActionEvent]
	 * @throws UnknownHostException if the IP address of the host could not be
	 *                              determined.
	 * @throws IOException          if an I/O error occurs when creating the socket.
	 */
	@FXML
	@SuppressWarnings("unchecked")
	public void displayCart(ActionEvent event) throws IOException {
		Socket socket = new Socket("localhost", 4316);

		// client -> server
		OutputStream outputStream = socket.getOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(outputStream);
		String[] toBeSent = { "display_cart", this.currentUser.getEmail() };
		out.writeObject(toBeSent);

		// server -> client
		InputStream inputStream = socket.getInputStream();
		ObjectInputStream in = new ObjectInputStream(inputStream);

		try {
			ArrayList<Product> cartResult = (ArrayList<Product>) in.readObject();
			addToTable(cartResult);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		socket.close();
	}

	/**
	 * Allows the {@code User} to remove products from his cart.
	 * 
	 * @param event GUI event. [ActionEvent]
	 * @throws UnknownHostException if the IP address of the host could not be
	 *                              determined.
	 * @throws IOException          if an I/O error occurs when creating the socket.
	 * @see User
	 */
	@FXML
	public void removeFromCart(ActionEvent event) throws UnknownHostException, IOException {
		Socket socket = new Socket("localhost", 4316);
		try {
			// getting selection of the tableview
			Product product = tableView.getSelectionModel().getSelectedItem();

			// client -> server
			OutputStream outputStream = socket.getOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(outputStream);
			String[] toBeSent = { "remove_from_cart", this.currentUser.getEmail(),
					String.valueOf(product.getProductId()) };
			out.writeObject(toBeSent);

			// server -> client
			InputStream inputStream = socket.getInputStream();
			ObjectInputStream in = new ObjectInputStream(inputStream);
			Boolean removeResult = (Boolean) in.readObject();

			if (removeResult) {
				// if result is true, the product has been correctly removed from cart
				initData(this.currentUser);
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle(String.format("Removed from cart"));
				alert.setHeaderText(String.format("Removed %s from cart.", product.getName()));
				alert.showAndWait();
			} else {
				// else, the product has not been correctly removed from cart
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle(String.format("Select a product"));
				alert.setHeaderText("You have to click on a Product and then Remove.");
				alert.showAndWait();
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		socket.close();
	}
}