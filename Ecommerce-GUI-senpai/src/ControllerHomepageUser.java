import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;

/**
 * Controller for the User Homepage.
 */
public class ControllerHomepageUser implements Controller {

	private User currentUser;

	@FXML
	private AnchorPane rootPane;

	@FXML
	private TextField searchboxName;

	@FXML
	private TextField searchboxYear;

	@FXML
	private TreeView<String> treeView;

	@FXML
	private TableView<Product> tableView;

	@FXML
	private TableColumn<Product, String> nameColumn;

	@FXML
	private TableColumn<Product, Integer> yearColumn;

	@FXML
	private TableColumn<Product, String> producerColumn;

	@FXML
	private TableColumn<Product, String> grapesColumn;

	@FXML
	private TableColumn<Product, String> notesColumn;

	@FXML
	private TextField quantity;

	/**
	 * Initialize {@code this.currentUser} with the passed value. This method is
	 * made to be called from another controller, using the {@code load} method in
	 * {@code Loader} class. It also adds all the products to display to the TableView
	 * and handles the process to display the notifications.
	 * 
	 * @param user the {@code User} we want to pass. [User]
	 * @see Loader
	 * @see Product
	 */
	@SuppressWarnings("unchecked")
	public void initData(User user) {
		this.currentUser = user;

		try {
			// Fill the frontpage with products.
			Socket socket = new Socket("localhost", 4316);

			// client -> server
			OutputStream outputStream = socket.getOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(outputStream);
			String[] toBeSent = { "get_products" };
			out.writeObject(toBeSent);

			// server ->client
			InputStream inputStream = socket.getInputStream();
			ObjectInputStream in = new ObjectInputStream(inputStream);
			ArrayList<Product> products = (ArrayList<Product>) in.readObject();

			// adds the product of the shop to the TableView to be displayed
			addToTable(products);
			socket.close();

			// Checks notifications.
			Socket socket2 = new Socket("localhost", 4316);

			// client -> server
			OutputStream outputStream2 = socket2.getOutputStream();
			ObjectOutputStream out2 = new ObjectOutputStream(outputStream2);
			String[] toBeSentNotifications = { "get_notifications", this.currentUser.getEmail() };
			out2.writeObject(toBeSentNotifications);

			// server -> client
			InputStream inputStream2 = socket2.getInputStream();
			ObjectInputStream in2 = new ObjectInputStream(inputStream2);
			ArrayList<Product> notification = (ArrayList<Product>) in2.readObject();

			// displays the correct notification to the User
			displayNotifications(notification);
			socket2.close();

			// displays the orders made by the User
			displayOrders();

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
		nameColumn.setCellValueFactory(new PropertyValueFactory<Product, String>("Name"));
		yearColumn.setCellValueFactory(new PropertyValueFactory<Product, Integer>("Year"));
		producerColumn.setCellValueFactory(new PropertyValueFactory<Product, String>("Producer"));
		notesColumn.setCellValueFactory(new PropertyValueFactory<Product, String>("Notes"));
		ObservableList<Product> oListProducts = FXCollections.observableArrayList(products);
		// load data
		tableView.setItems(oListProducts);
	}

	/**
	 * Displays an alert when a product is restocked, from an ArrayList of Products. This
	 * method is automatically called whenever the homepage of the user is loaded.
	 * 
	 * @param products the content that needs to be displayed on the alert. [Arraylist
	 *              of Product]
	 * @see Product
	 */
	public void displayNotifications(ArrayList<Product> products) {
		if (products.size() > 0) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Some products have been restocked");
			alert.setHeaderText("These products have been restocked:");
			StringBuilder productsSb = new StringBuilder();

			for (Product product : products) {
				productsSb.append(String.format("%s (%d)\n", product.getName(), product.getYear()));
			}
			String productsString = productsSb.toString();
			alert.setContentText(productsString);
			alert.showAndWait();
		}
	}

	/**
	 * Allows the {@code User} to add the products to his cart.
	 * 
	 * @param event GUI event. [ActionEvent]
	 * @throws UnknownHostException if the IP address of the host could not be
	 *                              determined.
	 * @throws IOException          if an I/O error occurs when creating the socket.
	 * @see User
	 */
	@FXML
	@SuppressWarnings("unused")
	public void addToCart(ActionEvent event) throws UnknownHostException, IOException {
		// permission check, guests can't add to cart
		if (this.currentUser.getPermission() > 0) {
			Socket socket = new Socket("localhost", 4316);

			try {
				// gets the quantity given by the User
				int quantity = Integer.parseInt(this.quantity.getText());

				// quantity check
				if (quantity > 0) {

					// getting selection of the tableview
					Product product = tableView.getSelectionModel().getSelectedItem();

					// client -> server
					OutputStream outputStream = socket.getOutputStream();
					ObjectOutputStream out = new ObjectOutputStream(outputStream);
					String[] toBeSent = { "add_to_cart", this.currentUser.getEmail(),
							String.valueOf(product.getProductId()), this.quantity.getText() };
					out.writeObject(toBeSent);

					// server -> client
					InputStream inputStream = socket.getInputStream();
					ObjectInputStream in = new ObjectInputStream(inputStream);
					Boolean addResult = (Boolean) in.readObject();

					if (addResult) {
						// operation addToCart was successful
						Alert alert = new Alert(AlertType.INFORMATION);
						alert.setTitle(String.format("Added to cart"));
						alert.setHeaderText(String.format("Added %s to cart.", product.getName()));
						alert.showAndWait();
					} else {
						// operation addToCart was not successful
						Alert alert = new Alert(AlertType.WARNING);
						alert.setTitle(String.format("Select a product"));
						alert.setHeaderText("You have to click on a Product, enter the quantity and then Add.");
						alert.showAndWait();
					}
				} else {
					Alert alert = new Alert(AlertType.WARNING);
					alert.setTitle(String.format("Quantity not valid"));
					alert.setHeaderText("Please insert a valid quantity.");
					alert.showAndWait();
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (NumberFormatException e) {
				// the quantity was non inserted correctly
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle(String.format("Insert quantity"));
				alert.setHeaderText("Please insert the quantity.");
				alert.showAndWait();
			}
			socket.close();
		} else {
			// the User is a guest, so he needs to login first
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Please login");
			alert.setHeaderText("You need to login to perform this action.");
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
	public void search(ActionEvent event) throws IOException, ClassNotFoundException {
		Socket socket = new Socket("localhost", 4316);

		// client -> server
		OutputStream outputStream = socket.getOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(outputStream);
		String[] toBeSent = { "search", searchboxName.getText(), searchboxYear.getText() };
		out.writeObject(toBeSent);

		// server -> client
		InputStream inputStream = socket.getInputStream();
		ObjectInputStream in = new ObjectInputStream(inputStream);
		ArrayList<Product> searchResult = (ArrayList<Product>) in.readObject();

		// displays the result in the tableview
		addToTable(searchResult);
		socket.close();
	}

	/**
	 * Goes to the cart page. It also checks the permission of the {@code User},
	 * onlu users with permission > 0 can access to the cart page (users, employees,
	 * administrators but not guests).
	 * 
	 * @param event GUI event. [ActionEvent]
	 * @throws IOException if the file can't be accessed.
	 */
	@FXML
	public void showCart(ActionEvent event) throws IOException {
		if (this.currentUser.getPermission() > 0) {
			Loader loader = new Loader(this.currentUser, this.rootPane);
			loader.load("cart");
		} else {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Please login");
			alert.setHeaderText("You need to login to perform this action.");
			alert.showAndWait();
		}
	}

	/**
	 * Displays all the orders made by the {@code User} in the TreeView.
	 * 
	 * @throws UnknownHostException if the IP address of the host could not be
	 *                              determined.
	 * @throws IOException          if an I/O error occurs when creating the socket.
	 * @see Order
	 * @see User
	 */
	@SuppressWarnings("unchecked")
	public void displayOrders() throws IOException {
		// user is authorized to perform the action
		Socket socket = new Socket("localhost", 4316);

		// client -> server
		OutputStream outputStream = socket.getOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(outputStream);
		String[] toBeSent = { "get_orders_user", this.currentUser.getEmail() };
		out.writeObject(toBeSent);

		// server ->client
		InputStream inputStream = socket.getInputStream();
		ObjectInputStream in = new ObjectInputStream(inputStream);

		try {
			// receives the ArrayList of orders from the server
			ArrayList<Order> orders = (ArrayList<Order>) in.readObject();
			// creates the TreeView's root
			TreeItem<String> rootItem = new TreeItem<String>("Orders");

			for (Order order : orders) {
				// fills the TreeView with the orders
				TreeItem<String> rootOrder = new TreeItem<String>(Integer.toString(order.getId()));
				TreeItem<String> id = new TreeItem<String>("Order ID: " + order.getId());
				TreeItem<String> status = new TreeItem<String>("Status: " + order.getStatus());
				TreeItem<String> customer = new TreeItem<String>("Customer: " + order.getCustomer());
				rootOrder.getChildren().addAll(id, status, customer);

				// for each order it displays each product of the order
				for (Product product : order.getProducts()) {
					TreeItem<String> rootProduct = new TreeItem<String>(
							String.format("%d - %s %s", product.getProductId(), product.getName(), product.getYear()));
					TreeItem<String> quantity = new TreeItem<String>("Quantity: " + product.getQuantity());
					rootProduct.getChildren().add(quantity);
					rootOrder.getChildren().add(rootProduct);
				}
				rootItem.getChildren().add(rootOrder);
			}
			treeView.setRoot(rootItem);
			treeView.setShowRoot(false);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		socket.close();
	}

	/**
	 * Goes back to the login page.
	 * 
	 * @param event GUI event. [ActionEvent]
	 * @throws IOException if the filename cannot be read.
	 */
	@FXML
	public void logout(ActionEvent event) throws IOException {
		AnchorPane pane = FXMLLoader.load(getClass().getResource("./login.fxml"));
		rootPane.getChildren().setAll(pane);
	}
}