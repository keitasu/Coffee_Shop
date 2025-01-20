package example.premuimcoffeeshop.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import example.premuimcoffeeshop.model.Drink;
import example.premuimcoffeeshop.model.Order;
import example.premuimcoffeeshop.model.User;
import example.premuimcoffeeshop.util.DatabaseConnection;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controller for the admin dashboard view
 * Handles drink management and user administration
 */
public class AdminDashboardController {
    @FXML private TableView<Drink> drinksTable;
    @FXML private TableColumn<Drink, Integer> drinkIdColumn;
    @FXML private TableColumn<Drink, String> drinkNameColumn;
    @FXML private TableColumn<Drink, Number> hotPriceColumn;
    @FXML private TableColumn<Drink, Number> icedPriceColumn;
    @FXML private TableColumn<Drink, Number> frappeePriceColumn;
    @FXML private TableColumn<Drink, String> imagePathColumn;
    @FXML private TextField searchField;
    
    @FXML private VBox drinksSection;
    @FXML private VBox usersSection;
    @FXML private VBox ordersSection;
    @FXML private VBox reportsSection;
    @FXML private VBox settingsSection;
    @FXML private ToggleButton darkModeToggle;

    private ObservableList<Drink> drinksList = FXCollections.observableArrayList();
    private FilteredList<Drink> filteredDrinks;

    // Users Section
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> adminUsernameColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, String> userStatusColumn;
    @FXML private TableColumn<User, LocalDateTime> createdAtColumn;

    // Orders Section
    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Integer> orderNoColumn;
    @FXML private TableColumn<Order, Integer> customerIdColumn;
    @FXML private TableColumn<Order, String> drinkDetailsColumn;
    @FXML private TableColumn<Order, LocalDateTime> orderDateTimeColumn;
    @FXML private TableColumn<Order, String> statusColumn;
    @FXML private TableColumn<Order, Double> totalPriceColumn;
    @FXML private TableColumn<Order, String> confirmedByColumn;
    @FXML private TableColumn<Order, String> acceptedByColumn;
    
    @FXML private ComboBox<String> statusFilter;
    @FXML private DatePicker dateFilter;

    private String currentUsername;

    @FXML
    private void initialize() {
        // Show orders section by default
        showOrders();
        
        // Initialize status filter
        statusFilter.getItems().addAll("All", "Ordered", "Confirmed", "Completed", "Canceled");
        statusFilter.setValue("All");
        
        // Initialize Orders Table
        orderNoColumn.setCellValueFactory(cellData -> cellData.getValue().noProperty().asObject());
        customerIdColumn.setCellValueFactory(cellData -> cellData.getValue().customerIdProperty().asObject());
        drinkDetailsColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getQuantity() + "x " +
                                   cellData.getValue().getDrinkName() + " (" +
                                   cellData.getValue().getDrinkType() + ")"));
        orderDateTimeColumn.setCellValueFactory(cellData -> cellData.getValue().orderDateTimeProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        totalPriceColumn.setCellValueFactory(cellData -> cellData.getValue().totalPriceProperty().asObject());
        confirmedByColumn.setCellValueFactory(cellData -> cellData.getValue().confirmedByProperty());
        acceptedByColumn.setCellValueFactory(cellData -> cellData.getValue().acceptedByProperty());

        // Initialize Users Table
        initializeUsersTable();
        
        // Load initial data
        loadOrders();
        loadUsers();

        // Initialize drinks table
        initializeDrinksTable();
        loadDrinks();

        // Initialize dark mode toggle
        darkModeToggle.setSelected(false);
        darkModeToggle.textProperty().bind(
            Bindings.when(darkModeToggle.selectedProperty())
                .then("On")
                .otherwise("Off")
        );
    }

    private void initializeDrinksTable() {
        drinkIdColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        drinkNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        hotPriceColumn.setCellValueFactory(cellData -> cellData.getValue().hotPriceProperty());
        icedPriceColumn.setCellValueFactory(cellData -> cellData.getValue().icedPriceProperty());
        frappeePriceColumn.setCellValueFactory(cellData -> cellData.getValue().frappeePriceProperty());
        imagePathColumn.setCellValueFactory(cellData -> cellData.getValue().imagePathProperty());
    }

    private void loadDrinks() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM drink_list";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            ObservableList<Drink> drinks = FXCollections.observableArrayList();
            while (rs.next()) {
                drinks.add(new Drink(
                    rs.getInt("drink_id"),
                    rs.getString("drink_name"),
                    rs.getDouble("drink_hot_price"),
                    rs.getDouble("drink_iced_price"),
                    rs.getDouble("drink_frappee_price"),
                    rs.getString("drink_img")
                ));
            }
            drinksTable.setItems(drinks);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load drinks: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddDrink() {
        Dialog<Drink> dialog = createDrinkDialog("Add New Drink", null);
        dialog.showAndWait().ifPresent(drink -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "INSERT INTO drink_list (drink_name, drink_hot_price, drink_iced_price, drink_frappee_price, drink_img) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, drink.getName());
                pstmt.setDouble(2, drink.getHotPrice());
                pstmt.setDouble(3, drink.getIcedPrice());
                pstmt.setDouble(4, drink.getFrappeePrice());
                pstmt.setString(5, drink.getImagePath());
                pstmt.executeUpdate();
                
                loadDrinks();
                showAlert("Success", "Drink added successfully!");
            } catch (SQLException e) {
                showAlert("Error", "Failed to add drink: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void handleEditDrink() {
        Drink selectedDrink = drinksTable.getSelectionModel().getSelectedItem();
        if (selectedDrink == null) {
            showAlert("Error", "Please select a drink to edit");
            return;
        }

        Dialog<Drink> dialog = createDrinkDialog("Edit Drink", selectedDrink);
        dialog.showAndWait().ifPresent(drink -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "UPDATE drink_list SET drink_name = ?, drink_hot_price = ?, drink_iced_price = ?, drink_frappee_price = ?, drink_img = ? WHERE drink_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, drink.getName());
                pstmt.setDouble(2, drink.getHotPrice());
                pstmt.setDouble(3, drink.getIcedPrice());
                pstmt.setDouble(4, drink.getFrappeePrice());
                pstmt.setString(5, drink.getImagePath());
                pstmt.setInt(6, drink.getId());
                pstmt.executeUpdate();
                
                loadDrinks();
                showAlert("Success", "Drink updated successfully!");
            } catch (SQLException e) {
                showAlert("Error", "Failed to update drink: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void handleDeleteDrink() {
        Drink selectedDrink = drinksTable.getSelectionModel().getSelectedItem();
        if (selectedDrink == null) {
            showAlert("Error", "Please select a drink to delete");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setContentText("Are you sure you want to delete drink: " + selectedDrink.getName() + "?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "DELETE FROM drink_list WHERE drink_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, selectedDrink.getId());
                pstmt.executeUpdate();
                
                loadDrinks();
                showAlert("Success", "Drink deleted successfully!");
            } catch (SQLException e) {
                showAlert("Error", "Failed to delete drink: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void showDrinks() {
        ordersSection.setVisible(false);
        usersSection.setVisible(false);
        drinksSection.setVisible(true);
        loadDrinks();
    }

    private Dialog<Drink> createDrinkDialog(String title, Drink drink) {
        Dialog<Drink> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        TextField hotPriceField = new TextField();
        TextField icedPriceField = new TextField();
        TextField frappeePriceField = new TextField();
        TextField imagePathField = new TextField();

        if (drink != null) {
            nameField.setText(drink.getName());
            hotPriceField.setText(String.valueOf(drink.getHotPrice()));
            icedPriceField.setText(String.valueOf(drink.getIcedPrice()));
            frappeePriceField.setText(String.valueOf(drink.getFrappeePrice()));
            imagePathField.setText(drink.getImagePath());
        }

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Hot Price:"), 0, 1);
        grid.add(hotPriceField, 1, 1);
        grid.add(new Label("Iced Price:"), 0, 2);
        grid.add(icedPriceField, 1, 2);
        grid.add(new Label("Frappee Price:"), 0, 3);
        grid.add(frappeePriceField, 1, 3);
        grid.add(new Label("Image Path:"), 0, 4);
        grid.add(imagePathField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new Drink(
                    drink != null ? drink.getId() : 0,
                    nameField.getText(),
                    Double.parseDouble(hotPriceField.getText()),
                    Double.parseDouble(icedPriceField.getText()),
                    Double.parseDouble(frappeePriceField.getText()),
                    imagePathField.getText().isEmpty() ? "resources/img/temp_icon.png" : imagePathField.getText()
                );
            }
            return null;
        });

        return dialog;
    }

    @FXML
    private void showOrders() {
        ordersSection.setVisible(true);
        usersSection.setVisible(false);
        drinksSection.setVisible(false);
    }

    @FXML
    private void showUsers() {
        ordersSection.setVisible(false);
        usersSection.setVisible(true);
        drinksSection.setVisible(false);
        loadUsers();
    }

    public void setCurrentUsername(String username) {
        this.currentUsername = username;
    }

    private String getCurrentUsername() {
        return currentUsername != null ? currentUsername : "Admin";
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

    private void initializeUsersTable() {
        adminUsernameColumn.setCellValueFactory(cellData -> cellData.getValue().adminUsernameProperty());
        roleColumn.setCellValueFactory(cellData -> cellData.getValue().roleProperty());
        userStatusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        createdAtColumn.setCellValueFactory(cellData -> cellData.getValue().createdAtProperty());
        
        // Format the date column
        createdAtColumn.setCellFactory(column -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            
            @Override
            protected void updateItem(LocalDateTime date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(formatter.format(date));
                }
            }
        });
    }

    private void loadUsers() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM admin_account";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            ObservableList<User> users = FXCollections.observableArrayList();
            while (rs.next()) {
                users.add(new User(
                    rs.getString("admin_username"),
                    rs.getString("admin_password"),
                    rs.getString("role"),
                    rs.getString("status")
                ));
            }
            usersTable.setItems(users);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load users: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void refreshOrders() {
        loadOrders();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/example/premuimcoffeeshop/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ordersSection.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.setMaximized(false);
            stage.setWidth(800);
            stage.setHeight(600);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to logout: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddUser() {
        Dialog<User> dialog = createUserDialog("Add New User", null);
        dialog.showAndWait().ifPresent(user -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "INSERT INTO admin_account (admin_username, admin_password, role, status) " +
                            "VALUES (?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, user.getAdminUsername());
                pstmt.setString(2, user.getAdminPassword());
                pstmt.setString(3, user.getRole());
                pstmt.setString(4, "Active");
                pstmt.executeUpdate();
                
                loadUsers();
                showAlert("Success", "User added successfully!");
            } catch (SQLException e) {
                showAlert("Error", "Failed to add user: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void handleEditUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("Error", "Please select a user to edit");
            return;
        }

        Dialog<User> dialog = createUserDialog("Edit User", selectedUser);
        dialog.showAndWait().ifPresent(user -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "UPDATE admin_account SET admin_password = ?, role = ? WHERE admin_username = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, user.getAdminPassword());
                pstmt.setString(2, user.getRole());
                pstmt.setString(3, user.getAdminUsername());
                pstmt.executeUpdate();
                
                loadUsers();
                showAlert("Success", "User updated successfully!");
            } catch (SQLException e) {
                showAlert("Error", "Failed to update user: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void handleDeleteUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("Error", "Please select a user to delete");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setContentText("Are you sure you want to delete user: " + selectedUser.getAdminUsername() + "?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "DELETE FROM admin_account WHERE admin_username = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, selectedUser.getAdminUsername());
                pstmt.executeUpdate();
                
                loadUsers();
                showAlert("Success", "User deleted successfully!");
            } catch (SQLException e) {
                showAlert("Error", "Failed to delete user: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private Dialog<User> createUserDialog(String title, User user) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        ComboBox<String> roleComboBox = new ComboBox<>();
        
        usernameField.setPromptText("Username");
        passwordField.setPromptText("Password");
        roleComboBox.getItems().addAll("Admin", "Cashier");
        roleComboBox.setPromptText("Select Role");

        if (user != null) {
            usernameField.setText(user.getAdminUsername());
            usernameField.setEditable(false);
            passwordField.setPromptText("Enter new password (leave empty to keep current)");
            roleComboBox.setValue(user.getRole());
        }

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("Role:"), 0, 2);
        grid.add(roleComboBox, 1, 2);

        dialog.getDialogPane().setContent(grid);

        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        BooleanBinding validInput = Bindings.createBooleanBinding(() -> {
            boolean validUsername = !usernameField.getText().trim().isEmpty();
            boolean validPassword = user != null ? true : !passwordField.getText().trim().isEmpty();
            boolean validRole = roleComboBox.getValue() != null;
            return validUsername && validPassword && validRole;
        }, usernameField.textProperty(), passwordField.textProperty(), roleComboBox.valueProperty());

        saveButton.disableProperty().bind(validInput.not());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String password = passwordField.getText().trim().isEmpty() && user != null 
                                ? user.getAdminPassword() 
                                : passwordField.getText();
                return new User(
                    usernameField.getText(),
                    password,
                    roleComboBox.getValue(),
                    "Active"
                );
            }
            return null;
        });

        return dialog;
    }

    @FXML
    private void showSettings() {
        ordersSection.setVisible(false);
        usersSection.setVisible(false);
        drinksSection.setVisible(false);
        settingsSection.setVisible(true);
    }

    @FXML
    private void toggleDarkMode() {
        Scene scene = settingsSection.getScene();
        if (darkModeToggle.isSelected()) {
            scene.getStylesheets().remove("/example/premuimcoffeeshop/styles.css");
            scene.getStylesheets().add("/example/premuimcoffeeshop/dark-styles.css");
        } else {
            scene.getStylesheets().remove("/example/premuimcoffeeshop/dark-styles.css");
            scene.getStylesheets().add("/example/premuimcoffeeshop/styles.css");
        }
    }

    // ... rest of your existing methods ...
} 