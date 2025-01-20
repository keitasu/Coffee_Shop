package example.premuimcoffeeshop.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Model class representing a drink in the coffee shop
 */
public class Drink {
    private final IntegerProperty id;
    private final StringProperty name;
    private final DoubleProperty hotPrice;
    private final DoubleProperty icedPrice;
    private final DoubleProperty frappeePrice;
    private final StringProperty imagePath;



    public Drink(int id, String name, double hotPrice, double icedPrice, 
                 double frappeePrice, String imagePath) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.hotPrice = new SimpleDoubleProperty(hotPrice);
        this.icedPrice = new SimpleDoubleProperty(icedPrice);
        this.frappeePrice = new SimpleDoubleProperty(frappeePrice);
        this.imagePath = new SimpleStringProperty(imagePath);
    }

    // Property getters
    public IntegerProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public DoubleProperty hotPriceProperty() { return hotPrice; }
    public DoubleProperty icedPriceProperty() { return icedPrice; }
    public DoubleProperty frappeePriceProperty() { return frappeePrice; }
    public StringProperty imagePathProperty() { return imagePath; }

    // Standard getters
    public int getId() { return id.get(); }
    public String getName() { return name.get(); }
    public double getHotPrice() { return hotPrice.get(); }
    public double getIcedPrice() { return icedPrice.get(); }
    public double getFrappeePrice() { return frappeePrice.get(); }
    public String getImagePath() { return imagePath.get(); }

    // Standard setters
    public void setId(int id) { this.id.set(id); }
    public void setName(String name) { this.name.set(name); }
    public void setHotPrice(double price) { this.hotPrice.set(price); }
    public void setIcedPrice(double price) { this.icedPrice.set(price); }
    public void setFrappeePrice(double price) { this.frappeePrice.set(price); }
    public void setImagePath(String path) { this.imagePath.set(path); }
} 