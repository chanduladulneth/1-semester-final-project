package com.dcroyal.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection.java
 * Simple database connection class.
 * Just call DBConnection.getConnection() anywhere you need to use the database.
 */
public class DBConnection {

    // Database connection details
    private static final String URL      = "jdbc:mysql://localhost:3306/dc_royal_printing";
    private static final String USER     = "root";
    private static final String PASSWORD = "ijse@1234";

    // Keep one connection object (reuse it)
    private static Connection connection = null;

    /**
     * Returns a connection to the MySQL database.
     * If connection is not yet made, it creates one.
     */
    public static Connection getConnection() {
        try {
            // If connection is null or closed, make a new one
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Database connected successfully!");
            }
        } catch (SQLException e) {
            // Print error if connection fails
            System.out.println("Database connection error: " + e.getMessage());
        }
        return connection;
    }
}
