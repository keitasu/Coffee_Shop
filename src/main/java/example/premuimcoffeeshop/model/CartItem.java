package example.premuimcoffeeshop.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CartItem {
    private final StringProperty drinkName;
    private final StringProperty type;
    private final IntegerProperty quantity;
    private final DoubleProperty price;
    private final DoubleProperty total;

    public CartItem(String drinkName, String type, int quantity, double price) {
        this.drinkName = new SimpleStringProperty(drinkName);
        this.type = new SimpleStringProperty(type);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.price = new SimpleDoubleProperty(price);
        this.total = new SimpleDoubleProperty(quantity * price);
    }

    // Property getters
    public StringProperty drinkNameProperty() { return drinkName; }
    public StringProperty typeProperty() { return type; }
    public IntegerProperty quantityProperty() { return quantity; }
    public DoubleProperty priceProperty() { return price; }
    public DoubleProperty totalProperty() { return total; }

    // Standard getters
    public String getDrinkName() { return drinkName.get(); }
    public String getType() { return type.get(); }
    public int getQuantity() { return quantity.get(); }
    public double getPrice() { return price.get(); }
    public double getTotal() { return total.get(); }

    // Standard setters
    public void setDrinkName(String name) { this.drinkName.set(name); }
    public void setType(String type) { this.type.set(type); }
    public void setQuantity(int quantity) { 
        this.quantity.set(quantity);
        updateTotal();
    }
    public void setPrice(double price) { 
        this.price.set(price);
        updateTotal();
    }

    private void updateTotal() {
        total.set(getQuantity() * getPrice());
    }
} 