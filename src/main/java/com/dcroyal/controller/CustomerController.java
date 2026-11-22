package com.dcroyal.controller;

import com.dcroyal.db.DBConnection;
import com.dcroyal.tm.CustomerTM;
import com.dcroyal.util.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * CustomerController.java
 * Handles full customer management functionality:
 * - Load all customers into TableView
 * - Auto-generate next customer code (CUS-000X)
 * - Add new customer
 * - Update existing customer details
 * - Delete customer (with confirmation)
 * - Live search filter by name, phone, or code
 */
public class CustomerController {

    // ---- FXML Form Inputs ----
    @FXML private TextField customerCodeField;
    @FXML private TextField fullNameField;
    @FXML private TextField phoneField;
    @FXML private TextField usernameField;
    @FXML private TextField cityField;
    @FXML private TextArea  addressField;
    @FXML private TextField searchField;

    // ---- FXML Action Buttons ----
    @FXML private Button saveButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button clearButton;

    // ---- FXML TableView & Columns ----
    @FXML private TableView<CustomerTM> customerTable;
    @FXML private TableColumn<CustomerTM, String> colCode;
    @FXML private TableColumn<CustomerTM, String> colName;
    @FXML private TableColumn<CustomerTM, String> colPhone;
    @FXML private TableColumn<CustomerTM, String> colUsername;
    @FXML private TableColumn<CustomerTM, String> colCity;
    @FXML private TableColumn<CustomerTM, String> colAddress;

    // Master list holding customer data for table
    private ObservableList<CustomerTM> customerList = FXCollections.observableArrayList();

    // ============================================================
    // INITIALIZE
    // ============================================================
    @FXML
    public void initialize() {
        // Link table columns with CustomerTM property getters
        colCode.setCellValueFactory(new PropertyValueFactory<>("customerCode"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colCity.setCellValueFactory(new PropertyValueFactory<>("city"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));

        // Load customers from database
        loadAllCustomers();

        // Auto-generate customer code for new entry
        generateNextCustomerCode();

        // Row selection listener: clicking row loads data into inputs
        customerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                populateFields(newVal);
            }
        });

        // Set up live search filter
        setupLiveSearch();
    }

    // ============================================================
    // LOAD ALL CUSTOMERS FROM DATABASE
    // ============================================================
    private void loadAllCustomers() {
        customerList.clear();
        String sql = "SELECT customer_code, full_name, phone, username, city, address " +
                     "FROM customers ORDER BY customer_id DESC";

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                CustomerTM tm = new CustomerTM(
                        rs.getString("customer_code"),
                        rs.getString("full_name"),
                        rs.getString("phone"),
                        rs.getString("username") != null ? rs.getString("username") : "",
                        rs.getString("city") != null ? rs.getString("city") : "",
                        rs.getString("address") != null ? rs.getString("address") : ""
                );
                customerList.add(tm);
            }

            rs.close();
            ps.close();

            customerTable.setItems(customerList);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load customers: " + e.getMessage());
        }
    }

    // ============================================================
    // AUTO GENERATE NEXT CUSTOMER CODE (e.g. CUS-0003)
    // ============================================================
    private void generateNextCustomerCode() {
        String sql = "SELECT customer_code FROM customers ORDER BY customer_id DESC LIMIT 1";

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String lastCode = rs.getString("customer_code"); // e.g. "CUS-0002"
                String[] parts = lastCode.split("-");
                if (parts.length == 2) {
                    int nextNum = Integer.parseInt(parts[1]) + 1;
                    customerCodeField.setText(String.format("CUS-%04d", nextNum));
                } else {
                    customerCodeField.setText("CUS-0001");
                }
            } else {
                customerCodeField.setText("CUS-0001");
            }

            rs.close();
            ps.close();

        } catch (Exception e) {
            customerCodeField.setText("CUS-0001");
        }
    }

    // ============================================================
    // POPULATE INPUT FIELDS FROM TABLE SELECTION
    // ============================================================
    private void populateFields(CustomerTM tm) {
        customerCodeField.setText(tm.getCustomerCode());
        fullNameField.setText(tm.getFullName());
        phoneField.setText(tm.getPhone());
        usernameField.setText(tm.getUsername());
        cityField.setText(tm.getCity());
        addressField.setText(tm.getAddress());

        // Disable code field when editing existing customer
        customerCodeField.setDisable(true);
        saveButton.setDisable(true);
        updateButton.setDisable(false);
        deleteButton.setDisable(false);
    }

    // ============================================================
    // SAVE CUSTOMER
    // ============================================================
    @FXML
    private void handleSaveCustomer() {
        String code    = customerCodeField.getText().trim();
        String name    = fullNameField.getText().trim();
        String phone   = phoneField.getText().trim();
        String username = usernameField.getText().trim();
        String city    = cityField.getText().trim();
        String address = addressField.getText().trim();

        // Validation
        if (name.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter the customer's full name.");
            fullNameField.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter the customer's phone number.");
            phoneField.requestFocus();
            return;
        }

        int userId = Session.getCurrentUser() != null ? Session.getCurrentUser().getUserId() : 1;

        String sql = "INSERT INTO customers (customer_code, full_name, phone, username, address, city, created_by) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, code);
            ps.setString(2, name);
            ps.setString(3, phone);
            ps.setString(4, username.isEmpty() ? null : username);
            ps.setString(5, address.isEmpty() ? null : address);
            ps.setString(6, city.isEmpty() ? null : city);
            ps.setInt(7, userId);

            int rows = ps.executeUpdate();
            ps.close();

            if (rows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Customer " + name + " saved successfully!");
                handleClear();
                loadAllCustomers();
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save customer: " + e.getMessage());
        }
    }

    // ============================================================
    // UPDATE CUSTOMER
    // ============================================================
    @FXML
    private void handleUpdateCustomer() {
        String code    = customerCodeField.getText().trim();
        String name    = fullNameField.getText().trim();
        String phone   = phoneField.getText().trim();
        String username = usernameField.getText().trim();
        String city    = cityField.getText().trim();
        String address = addressField.getText().trim();

        if (name.isEmpty() || phone.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Full Name and Phone Number are required.");
            return;
        }

        String sql = "UPDATE customers SET full_name = ?, phone = ?, username = ?, address = ?, city = ? " +
                     "WHERE customer_code = ?";

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, phone);
            ps.setString(3, username.isEmpty() ? null : username);
            ps.setString(4, address.isEmpty() ? null : address);
            ps.setString(5, city.isEmpty() ? null : city);
            ps.setString(6, code);

            int rows = ps.executeUpdate();
            ps.close();

            if (rows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Customer details updated successfully!");
                handleClear();
                loadAllCustomers();
            } else {
                showAlert(Alert.AlertType.WARNING, "Update Failed", "No customer found with code " + code);
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update customer: " + e.getMessage());
        }
    }

    // ============================================================
    // DELETE CUSTOMER
    // ============================================================
    @FXML
    private void handleDeleteCustomer() {
        String code = customerCodeField.getText().trim();
        String name = fullNameField.getText().trim();

        if (code.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a customer from the table to delete.");
            return;
        }

        // Confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Customer: " + name + " (" + code + ")");
        confirmAlert.setContentText("Are you sure you want to permanently delete this customer?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (!result.isPresent() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            Connection con = DBConnection.getConnection();

            // Check if this customer has existing orders first
            String checkOrdersSql = "SELECT COUNT(*) FROM orders o " +
                                   "JOIN customers c ON o.customer_id = c.customer_id " +
                                   "WHERE c.customer_code = ?";
            PreparedStatement checkPs = con.prepareStatement(checkOrdersSql);
            checkPs.setString(1, code);
            ResultSet checkRs = checkPs.executeQuery();
            checkRs.next();
            int orderCount = checkRs.getInt(1);
            checkRs.close();
            checkPs.close();

            if (orderCount > 0) {
                showAlert(Alert.AlertType.WARNING, "Cannot Delete",
                        "This customer has " + orderCount + " order(s) recorded in the system.\nCannot delete a customer with order history.");
                return;
            }

            // Proceed with delete
            String sql = "DELETE FROM customers WHERE customer_code = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, code);
            int rows = ps.executeUpdate();
            ps.close();

            if (rows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Deleted", "Customer deleted successfully.");
                handleClear();
                loadAllCustomers();
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete customer: " + e.getMessage());
        }
    }

    // ============================================================
    // CLEAR / RESET FORM
    // ============================================================
    @FXML
    private void handleClear() {
        fullNameField.clear();
        phoneField.clear();
        usernameField.clear();
        cityField.clear();
        addressField.clear();
        searchField.clear();

        customerCodeField.setDisable(false);
        saveButton.setDisable(false);
        updateButton.setDisable(true);
        deleteButton.setDisable(true);

        customerTable.getSelectionModel().clearSelection();
        generateNextCustomerCode();
    }

    // ============================================================
    // LIVE SEARCH FILTER
    // ============================================================
    private void setupLiveSearch() {
        FilteredList<CustomerTM> filteredData = new FilteredList<>(customerList, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(customer -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase().trim();

                if (customer.getCustomerCode().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (customer.getFullName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (customer.getPhone().contains(lowerCaseFilter)) {
                    return true;
                } else if (customer.getCity().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });

        SortedList<CustomerTM> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(customerTable.comparatorProperty());
        customerTable.setItems(sortedData);
    }

    // ============================================================
    // HELPER: ALERT DIALOG
    // ============================================================
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
