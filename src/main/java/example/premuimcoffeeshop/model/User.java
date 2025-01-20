package example.premuimcoffeeshop.model;

import java.time.LocalDateTime;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class User {
    private final StringProperty adminUsername;
    private final StringProperty adminPassword;
    private final StringProperty role;
    private final StringProperty status;
    private final ObjectProperty<LocalDateTime> createdAt;
    
    // Original constructor for login compatibility
    public User(String username, String password, String role, String status) {
        this(username, password, role, status, LocalDateTime.now());
    }
    
    // New constructor with createdAt
    public User(String username, String password, String role, String status, LocalDateTime createdAt) {
        this.adminUsername = new SimpleStringProperty(username);
        this.adminPassword = new SimpleStringProperty(password);
        this.role = new SimpleStringProperty(role);
        this.status = new SimpleStringProperty(status);
        this.createdAt = new SimpleObjectProperty<>(createdAt);
    }
    
    // Property getters
    public StringProperty adminUsernameProperty() { return adminUsername; }
    public StringProperty adminPasswordProperty() { return adminPassword; }
    public StringProperty roleProperty() { return role; }
    public StringProperty statusProperty() { return status; }
    
    // New createdAt property getter
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }
    
    // Standard getters
    public String getAdminUsername() { return adminUsername.get(); }
    public String getAdminPassword() { return adminPassword.get(); }
    public String getRole() { return role.get(); }
    public String getStatus() { return status.get(); }
    
    // New createdAt getter
    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    
    // Standard setters
    public void setAdminUsername(String username) { this.adminUsername.set(username); }
    public void setAdminPassword(String password) { this.adminPassword.set(password); }
    public void setRole(String role) { this.role.set(role); }
    public void setStatus(String status) { this.status.set(status); }
    
    // New createdAt setter
    public void setCreatedAt(LocalDateTime date) { this.createdAt.set(date); }
} 