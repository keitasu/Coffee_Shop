module example.premuimcoffeeshop {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    // Open packages for JavaFX
    opens example.premuimcoffeeshop to javafx.fxml;
    opens example.premuimcoffeeshop.controller to javafx.fxml;
    opens example.premuimcoffeeshop.model to javafx.base;
    
    exports example.premuimcoffeeshop;
    exports example.premuimcoffeeshop.controller;
    exports example.premuimcoffeeshop.model;
}