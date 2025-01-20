package example.premuimcoffeeshop.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import example.premuimcoffeeshop.model.CartItem;
import example.premuimcoffeeshop.model.Drink;
import example.premuimcoffeeshop.model.Order;
import example.premuimcoffeeshop.util.DatabaseConnection;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class UserDashboardController {
    @FXML private GridPane drinksGrid;
    @FXML private VBox menuSection;
    @FXML private VBox cartSection;
    @FXML private VBox ordersSection;
    @FXML private TableView<CartItem> cartTable;
    @FXML private TableView<Order> ordersTable;
    @FXML private Label totalAmountLabel;
    @FXML private TextField searchField;

    // Table columns
    @FXML private TableColumn<CartItem, String> drinkNameColumn;
    @FXML private TableColumn<CartItem, String> typeColumn;
    @FXML private TableColumn<CartItem, Integer> quantityColumn;
    @FXML private TableColumn<CartItem, Double> priceColumn;
    @FXML private TableColumn<CartItem, Double> totalColumn;
    
    @FXML
    private TableColumn<Order, Integer> orderIdColumn;
    @FXML
    private TableColumn<Order, LocalDateTime> orderDateColumn;
    @FXML
    private TableColumn<Order, String> orderItemsColumn;
    @FXML
    private TableColumn<Order, String> orderStatusColumn;
    @FXML
    private TableColumn<Order, Double> orderTotalColumn;

    private ObservableList<CartItem> cartItems = FXCollections.observableArrayList();
    private ObservableList<Order> orderHistory = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Remove window mode settings, always maximize
        Platform.runLater(() -> {
            Stage stage = (Stage) menuSection.getScene().getWindow();
            if (stage != null) {
                stage.setMaximized(true);
            }
        });

        loadDrinks();
        setupSearch();
        setupTables();

        // Initialize cart table columns
        drinkNameColumn.setCellValueFactory(cellData -> cellData.getValue().drinkNameProperty());
        typeColumn.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        quantityColumn.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());
        priceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());
        totalColumn.setCellValueFactory(cellData -> cellData.getValue().totalProperty().asObject());
        
        // Set the items to the table
        cartTable.setItems(cartItems);

        // Initialize orders table columns
        orderIdColumn.setCellValueFactory(cellData -> cellData.getValue().customerIdProperty().asObject());
        orderDateColumn.setCellValueFactory(cellData -> cellData.getValue().orderDateTimeProperty());
        orderItemsColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getQuantity() + "x " +
                                   cellData.getValue().getDrinkName() + " (" +
                                   cellData.getValue().getDrinkType() + ")"));
        orderStatusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        orderTotalColumn.setCellValueFactory(cellData -> cellData.getValue().totalPriceProperty().asObject());

        // Format date column
        orderDateColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                }
            }
        });

        // Load orders when switching to orders tab
        showOrders();
    }

    private void loadDrinks() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM drink_list")) {

            drinksGrid.getChildren().clear();
            drinksGrid.setHgap(30);  // Increased horizontal gap
            drinksGrid.setVgap(30);  // Increased vertical gap
            drinksGrid.setPadding(new Insets(25));  // Increased padding

            int column = 0;
            int row = 0;
            int maxColumns = 3; // Fixed number of columns for better layout

            while (rs.next()) {
                Drink drink = new Drink(
                    rs.getInt("drink_id"),
                    rs.getString("drink_name"),
                    rs.getDouble("drink_hot_price"),
                    rs.getDouble("drink_iced_price"),
                    rs.getDouble("drink_frappee_price"),
                    rs.getString("drink_img")
                );

                VBox drinkCard = createDrinkCard(drink);
                drinksGrid.add(drinkCard, column, row);

                column++;
                if (column >= maxColumns) {
                    column = 0;
                    row++;
                }
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to load drinks: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private VBox createDrinkCard(Drink drink) {
        VBox card = new VBox(10);
        card.getStyleClass().add("drink-card");
        card.setPadding(new Insets(10));
        
        // Safely load the image
        ImageView imageView = new ImageView();
        imageView.setFitHeight(150);
        imageView.setFitWidth(150);
        
        try {
            String imagePath = drink.getImagePath();
            if (imagePath != null && !imagePath.isEmpty()) {
                // Try to load from resources
                String resourcePath = "/example/premuimcoffeeshop/images/" + 
                    imagePath.substring(imagePath.lastIndexOf("/") + 1);
                Image image = new Image(getClass().getResourceAsStream(resourcePath));
                if (image.isError()) {
                    // If resource loading fails, try loading from file system
                    image = new Image("file:" + imagePath);
                }
                imageView.setImage(image);
            } else {
                // Load default image
                Image defaultImage = new Image(getClass().getResourceAsStream(
                    "/example/premuimcoffeeshop/images/default-drink.png"));
                imageView.setImage(defaultImage);
            }
        } catch (Exception e) {
            System.err.println("Failed to load image for drink: " + drink.getName());
            e.printStackTrace();
            // Load default image
            try {
                Image defaultImage = new Image(getClass().getResourceAsStream(
                    "/example/premuimcoffeeshop/images/default-drink.png"));
                imageView.setImage(defaultImage);
            } catch (Exception ex) {
                System.err.println("Failed to load default image");
            }
        }
        
        Label nameLabel = new Label(drink.getName());
        nameLabel.getStyleClass().add("drink-name");
        
        VBox pricesBox = new VBox(5);
        pricesBox.getChildren().addAll(
            new Label(String.format("Hot: $%.2f", drink.getHotPrice())),
            new Label(String.format("Iced: $%.2f", drink.getIcedPrice())),
            new Label(String.format("Frappee: $%.2f", drink.getFrappeePrice()))
        );
        
        Button addHotBtn = new Button("Add Hot");
        addHotBtn.setOnAction(e -> addToCart(drink, "hot"));
        addHotBtn.getStyleClass().add("add-button");
        
        Button addIcedBtn = new Button("Add Iced");
        addIcedBtn.setOnAction(e -> addToCart(drink, "iced"));
        addIcedBtn.getStyleClass().add("add-button");
        
        Button addFrappeeBtn = new Button("Add Frappee");
        addFrappeeBtn.setOnAction(e -> addToCart(drink, "frappee"));
        addFrappeeBtn.getStyleClass().add("add-button");
        
        card.getChildren().addAll(imageView, nameLabel, pricesBox, addHotBtn, addIcedBtn, addFrappeeBtn);
        return card;
    }

    @FXML
    private void showMenu() {
        menuSection.setVisible(true);
        cartSection.setVisible(false);
        ordersSection.setVisible(false);
    }

    @FXML
    private void showCart() {
        menuSection.setVisible(false);
        cartSection.setVisible(true);
        ordersSection.setVisible(false);
    }

    @FXML
    private void showOrders() {
        menuSection.setVisible(false);
        cartSection.setVisible(false);
        ordersSection.setVisible(true);
        
        // Load orders from database
        loadOrders();
    }

    @FXML
    private void handleCheckout() {
        if (cartItems.isEmpty()) {
            showAlert("Error", "Your cart is empty!");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Get the next customer ID
            int customerId = getNextCustomerId(conn);
            
            // Start transaction
            conn.setAutoCommit(false);
            
            try {
                // Remove Nº from the INSERT statement since it's auto-increment
                String insertSQL = "INSERT INTO history (customer_id, drink_name, drink_type, " +
                                 "drink_price, drink_quantity, total_price, date, time, status) " +
                                 "VALUES (?, ?, ?, ?, ?, ?, CURDATE(), CURTIME(), 'Ordered')";
                
                PreparedStatement pstmt = conn.prepareStatement(insertSQL);
                
                // Insert each cart item
                for (CartItem item : cartItems) {
                    pstmt.setInt(1, customerId);
                    pstmt.setString(2, item.getDrinkName());
                    pstmt.setString(3, item.getType());
                    pstmt.setDouble(4, item.getPrice());
                    pstmt.setInt(5, item.getQuantity());
                    pstmt.setDouble(6, item.getTotal());
                    pstmt.executeUpdate();
                }
                
                // Commit the transaction
                conn.commit();
                
                // Clear the cart and update UI
                cartItems.clear();
                updateCartTotal();
                
                // Show success message
                showAlert("Success", "Order placed successfully!\nYour order ID is: " + customerId);
                
            } catch (SQLException e) {
                // If there's an error, rollback the transaction
                conn.rollback();
                throw e;
            } finally {
                // Reset auto-commit to default
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            showAlert("Error", "Failed to place order: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private int getNextCustomerId(Connection conn) throws SQLException {
        // Get the maximum customer_id from history table
        String sql = "SELECT MAX(customer_id) FROM history";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1) + 1;
            }
            return 1; // If no existing orders
        }
    }
    
    // Helper method to load orders for a specific customer
    public void loadCustomerOrders(int customerId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                "SELECT * FROM history WHERE customer_id = ? ORDER BY date DESC, time DESC")) {
            
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            
            ObservableList<Order> orders = FXCollections.observableArrayList();
            while (rs.next()) {
                LocalDateTime orderDateTime = LocalDateTime.of(
                    rs.getDate("date").toLocalDate(),
                    rs.getTime("time").toLocalTime()
                );

                orders.add(new Order(
                    rs.getInt("Nº"),
                    rs.getInt("customer_id"),
                    rs.getString("drink_name"),
                    rs.getString("drink_type"),
                    rs.getDouble("drink_price"),
                    rs.getInt("drink_quantity"),
                    rs.getDouble("total_price"),
                    orderDateTime,
                    rs.getString("status"),
                    rs.getString("confirmed_by"),
                    rs.getString("accepted_by")
                ));
            }
            
            ordersTable.setItems(orders);
            
        } catch (SQLException e) {
            showAlert("Error", "Failed to load orders: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            // Load the login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/example/premuimcoffeeshop/login.fxml"));
            Parent root = loader.load();
            
            // Get the current stage
            Stage stage = (Stage) menuSection.getScene().getWindow();
            
            // Create and set the new scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Login");
            
            // Set size for login page
            stage.setMaximized(false);
            stage.setWidth(800);
            stage.setHeight(600);
            stage.centerOnScreen();
            
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to logout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAddToCartDialog(Drink drink) {
        // TODO: Implement add to cart dialog
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            // TODO: Implement search functionality
        });
    }

    private void setupTables() {
        // TODO: Initialize table columns and data
    }

    private void updateTotal() {
        double total = cartItems.stream()
            .mapToDouble(item -> item.getQuantity() * item.getPrice())
            .sum();
        totalAmountLabel.setText(String.format("%.2f", total));
    }

    public void addToCart(Drink drink, String type) {
        // Get the appropriate price based on drink type
        double price = switch (type.toLowerCase()) {
            case "hot" -> drink.getHotPrice();
            case "iced" -> drink.getIcedPrice();
            case "frappee" -> drink.getFrappeePrice();
            default -> drink.getHotPrice(); // default to hot price
        };

        // Create new cart item with default quantity of 1
        CartItem newItem = new CartItem(drink.getName(), type, 1, price);
        cartItems.add(newItem);
        
        // Update total
        updateCartTotal();
        
        // Show confirmation
        showAddToCartConfirmation(drink.getName(), type);
    }

    private void updateCartTotal() {
        double total = cartItems.stream()
                              .mapToDouble(CartItem::getTotal)
                              .sum();
        totalAmountLabel.setText(String.format("$%.2f", total));
    }

    private void showAddToCartConfirmation(String drinkName, String type) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Added to Cart");
        alert.setHeaderText(null);
        alert.setContentText(String.format("%s (%s) has been added to your cart.", drinkName, type));
        alert.show();
    }

    private void loadOrders() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM history ORDER BY date DESC, time DESC";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            ObservableList<Order> orders = FXCollections.observableArrayList();
            while (rs.next()) {
                LocalDateTime orderDateTime = LocalDateTime.of(
                    rs.getDate("date").toLocalDate(),
                    rs.getTime("time").toLocalTime()
                );

                orders.add(new Order(
                    rs.getInt("Nº"),
                    rs.getInt("customer_id"),
                    rs.getString("drink_name"),
                    rs.getString("drink_type"),
                    rs.getDouble("drink_price"),
                    rs.getInt("drink_quantity"),
                    rs.getDouble("total_price"),
                    orderDateTime,
                    rs.getString("status"),
                    rs.getString("confirmed_by"),
                    rs.getString("accepted_by")
                ));
            }

            ordersTable.setItems(orders);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load orders: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Helper method to format currency
    private String formatPrice(double price) {
        return String.format("$%.2f", price);
    }
} 