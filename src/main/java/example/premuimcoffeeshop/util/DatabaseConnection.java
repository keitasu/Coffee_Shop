package example.premuimcoffeeshop.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class for managing database connections
 */
public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/coffeeshop";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    
    private static Connection connection;
    
    /**
     * Gets a connection to the database, creating a new one if necessary
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }
    
    /**
     * Closes the database connection if open
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
} 