package example.premuimcoffeeshop.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import example.premuimcoffeeshop.model.User;
import example.premuimcoffeeshop.util.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password");
            return;
        }

        try {
            User user = authenticateUser(username, password);
            if (user != null) {
                openAppropriateView(user);
            } else {
                showError("Invalid username or password");
            }
        } catch (Exception e) {
            showError("Login error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private User authenticateUser(String username, String password) throws Exception {
        String sql = "SELECT * FROM admin_account WHERE admin_username = ? AND admin_password = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getString("admin_username"),
                        rs.getString("admin_password"),
                        rs.getString("role"),
                        rs.getString("status")
                    );
                }
            }
        }
        return null;
    }

    private void openAppropriateView(User user) throws Exception {
        String fxmlFile;
        String title;

        // Convert role to lowercase for case-insensitive comparison
        String role = user.getRole().toLowerCase();
        
        if (role.equals("admin")) {
            fxmlFile = "/example/premuimcoffeeshop/admin-dashboard.fxml";
            title = "Admin Dashboard";
        } else if (role.equals("cashier")) {
            fxmlFile = "/example/premuimcoffeeshop/user-dashboard.fxml";
            title = "Cashier Dashboard";
        } else {
            throw new IllegalStateException("Unknown role: " + user.getRole());
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();
        
        // Get current stage
        Stage stage = (Stage) usernameField.getScene().getWindow();
        
        // Set new scene
        Scene scene = new Scene(root);
        stage.setTitle(title);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }
} 