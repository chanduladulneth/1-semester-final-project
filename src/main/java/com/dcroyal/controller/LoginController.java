package com.dcroyal.controller;

import com.dcroyal.db.DBConnection;
import com.dcroyal.model.User;
import com.dcroyal.util.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * LoginController.java
 * Handles all the logic for the Login screen.
 * Validates credentials against MySQL and opens the specific dashboard for that user's role.
 */
public class LoginController {

    // Connect FXML elements to Java variables
    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button        loginButton;
    @FXML private Label         errorLabel;

    /**
     * This method runs when the Login button is clicked.
     * It checks the email and password against the database.
     */
    @FXML
    private void handleLogin() {

        // Get what the user typed
        String email    = emailField.getText().trim();
        String password = passwordField.getText().trim();

        // Simple check - don't allow empty fields
        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter your email and password.");
            return;
        }

        // Try to find the user in the database
        try {
            Connection con = DBConnection.getConnection();

            // SQL: find user with matching email, password, and active status
            // Also join with roles table to get the role name and code
            String sql = "SELECT u.user_id, u.full_name, u.email, r.role_code, r.role_name " +
                         "FROM users u " +
                         "JOIN roles r ON u.role_id = r.role_id " +
                         "WHERE u.email = ? AND u.password_hash = ? AND u.is_active = 1";

            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, email);
            pst.setString(2, password);

            ResultSet rs = pst.executeQuery();

            // If a matching user was found
            if (rs.next()) {

                // Create a User object with the data from the database
                User loggedInUser = new User(
                        rs.getInt("user_id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("role_code"),
                        rs.getString("role_name")
                );

                // Save the user in Session so other screens can access it
                Session.setCurrentUser(loggedInUser);

                // Close result set and statement
                rs.close();
                pst.close();

                // Load the specific Role Dashboard screen
                openDashboard(loggedInUser.getRoleCode());

            } else {
                // No matching user found
                errorLabel.setText("");
                errorLabel.setVisible(false);
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Sign-in failed");
                alert.setHeaderText("Invalid credentials");
                alert.setContentText("The email or password is incorrect. Please try again.");
                alert.showAndWait();
            }

        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
            System.out.println("Login error: " + e.getMessage());
        } catch (Exception e) {
            showError("An error occurred. Please try again.");
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Opens the separate Dashboard screen based on the user's role.
     */
    private void openDashboard(String roleCode) {
        try {
            // Pick the specific FXML file for this role
            String fxmlFile;
            switch (roleCode) {
                case "OWNER":
                    fxmlFile = "/com/dcroyal/view/OwnerDashboard.fxml";
                    break;
                case "MANAGER":
                    fxmlFile = "/com/dcroyal/view/ManagerDashboard.fxml";
                    break;
                case "COUNTER_STAFF":
                    fxmlFile = "/com/dcroyal/view/CounterDashboard.fxml";
                    break;
                case "DESIGNER":
                    fxmlFile = "/com/dcroyal/view/DesignerDashboard.fxml";
                    break;
                case "PRINTER_OPERATOR":
                    fxmlFile = "/com/dcroyal/view/OperatorDashboard.fxml";
                    break;
                case "INVENTORY_STAFF":
                    fxmlFile = "/com/dcroyal/view/InventoryDashboard.fxml";
                    break;
                case "PACKING_STAFF":
                    fxmlFile = "/com/dcroyal/view/PackingDashboard.fxml";
                    break;
                case "DELIVERY_STAFF":
                    fxmlFile = "/com/dcroyal/view/DeliveryDashboard.fxml";
                    break;
                case "NETWORK_ADMIN":
                    fxmlFile = "/com/dcroyal/view/NetworkDashboard.fxml";
                    break;
                default:
                    fxmlFile = "/com/dcroyal/view/OwnerDashboard.fxml";
                    break;
            }

            // Load the chosen dashboard FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));

            // Create a large scene for the dashboard
            Scene scene = new Scene(loader.load(), 1280, 760);

            // Apply CSS stylesheet
            scene.getStylesheets().add(
                    getClass().getResource("/com/dcroyal/css/style.css").toExternalForm()
            );

            // Get the current window (Stage) and set the new scene
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("DC Royal Printing — " + Session.getCurrentUser().getRoleName() + " Dashboard");
            stage.setMinWidth(960);
            stage.setMinHeight(600);
            stage.setResizable(true);
            stage.setMaximized(true);

        } catch (Exception e) {
            showError("Could not load dashboard: " + e.getMessage());
            System.out.println("Dashboard load error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Shows a red error message below the login form.
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
