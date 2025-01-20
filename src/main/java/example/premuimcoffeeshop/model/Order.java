package example.premuimcoffeeshop.model;

import java.time.LocalDateTime;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Order {
    private final IntegerProperty no;  // matches 'Nº' in history table
    private final IntegerProperty customerId;
    private final StringProperty drinkName;
    private final StringProperty drinkType;
    private final DoubleProperty drinkPrice;
    private final IntegerProperty quantity;
    private final DoubleProperty totalPrice;
    private final ObjectProperty<LocalDateTime> orderDateTime;
    private final StringProperty status;
    private final StringProperty confirmedBy;
    private final StringProperty acceptedBy;

    public Order(int no, int customerId, String drinkName, String drinkType, 
                double drinkPrice, int quantity, double totalPrice, 
                LocalDateTime orderDateTime, String status, 
                String confirmedBy, String acceptedBy) {
        this.no = new SimpleIntegerProperty(no);
        this.customerId = new SimpleIntegerProperty(customerId);
        this.drinkName = new SimpleStringProperty(drinkName);
        this.drinkType = new SimpleStringProperty(drinkType);
        this.drinkPrice = new SimpleDoubleProperty(drinkPrice);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.totalPrice = new SimpleDoubleProperty(totalPrice);
        this.orderDateTime = new SimpleObjectProperty<>(orderDateTime);
        this.status = new SimpleStringProperty(status);
        this.confirmedBy = new SimpleStringProperty(confirmedBy);
        this.acceptedBy = new SimpleStringProperty(acceptedBy);
    }

    // Property getters
    public IntegerProperty noProperty() { return no; }
    public IntegerProperty customerIdProperty() { return customerId; }
    public StringProperty drinkNameProperty() { return drinkName; }
    public StringProperty drinkTypeProperty() { return drinkType; }
    public DoubleProperty drinkPriceProperty() { return drinkPrice; }
    public IntegerProperty quantityProperty() { return quantity; }
    public DoubleProperty totalPriceProperty() { return totalPrice; }
    public ObjectProperty<LocalDateTime> orderDateTimeProperty() { return orderDateTime; }
    public StringProperty statusProperty() { return status; }
    public StringProperty confirmedByProperty() { return confirmedBy; }
    public StringProperty acceptedByProperty() { return acceptedBy; }

    // Standard getters
    public int getNo() { return no.get(); }
    public int getCustomerId() { return customerId.get(); }
    public String getDrinkName() { return drinkName.get(); }
    public String getDrinkType() { return drinkType.get(); }
    public double getDrinkPrice() { return drinkPrice.get(); }
    public int getQuantity() { return quantity.get(); }
    public double getTotalPrice() { return totalPrice.get(); }
    public LocalDateTime getOrderDateTime() { return orderDateTime.get(); }
    public String getStatus() { return status.get(); }
    public String getConfirmedBy() { return confirmedBy.get(); }
    public String getAcceptedBy() { return acceptedBy.get(); }

    // Standard setters
    public void setNo(int no) { this.no.set(no); }
    public void setCustomerId(int id) { this.customerId.set(id); }
    public void setDrinkName(String name) { this.drinkName.set(name); }
    public void setDrinkType(String type) { this.drinkType.set(type); }
    public void setDrinkPrice(double price) { this.drinkPrice.set(price); }
    public void setQuantity(int quantity) { this.quantity.set(quantity); }
    public void setTotalPrice(double price) { this.totalPrice.set(price); }
    public void setOrderDateTime(LocalDateTime dateTime) { this.orderDateTime.set(dateTime); }
    public void setStatus(String status) { this.status.set(status); }
    public void setConfirmedBy(String user) { this.confirmedBy.set(user); }
    public void setAcceptedBy(String user) { this.acceptedBy.set(user); }
} 