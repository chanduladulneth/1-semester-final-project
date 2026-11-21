package com.dcroyal.controller;

import com.dcroyal.db.DBConnection;
import com.dcroyal.tm.PrintJobTM;
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

/**
 * PrintQueueController.java
 * Handles the production printing queue:
 * - Real-time queue KPI cards
 * - Detailed list of orders waiting to be printed and actively printing
 * - Manual printing workflow: "Start Printing" -> "Finish & Send to Packing"
 * - Live search and technology filtering (3D, DTF, UV Flatbed, UV Rotary)
 */
public class PrintQueueController {

    // ---- FXML KPI Summary Labels ----
    @FXML private Label inQueueCountLabel;
    @FXML private Label printingCountLabel;
    @FXML private Label completedTodayCountLabel;
    @FXML private Label assignedToMeCountLabel;

    // ---- FXML Filter & Search Controls ----
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private ComboBox<String> categoryFilterComboBox;
    @FXML private TextField        searchField;

    // ---- FXML TableView & Columns ----
    @FXML private TableView<PrintJobTM> queueTable;
    @FXML private TableColumn<PrintJobTM, String> colOrderNumber;
    @FXML private TableColumn<PrintJobTM, String> colCustomer;
    @FXML private TableColumn<PrintJobTM, String> colPrintType;
    @FXML private TableColumn<PrintJobTM, String> colItemName;
    @FXML private TableColumn<PrintJobTM, String> colMaterial;
    @FXML private TableColumn<PrintJobTM, Integer> colQty;
    @FXML private TableColumn<PrintJobTM, String> colSpecs;
    @FXML private TableColumn<PrintJobTM, String> colOperator;
    @FXML private TableColumn<PrintJobTM, String> colStatus;
    @FXML private TableColumn<PrintJobTM, String> colDueDate;

    // ---- FXML Action Panel Controls ----
    @FXML private Label  selectedOrderLabel;
    @FXML private Label  selectedCustomerLabel;
    @FXML private Label  selectedStatusLabel;
    @FXML private Label  selectedDueLabel;
    @FXML private Button startPrintingButton;
    @FXML private Button finishPrintingButton;

    // Master list of print jobs
    private ObservableList<PrintJobTM> queueList = FXCollections.observableArrayList();

    // Currently selected job
    private PrintJobTM selectedJob = null;

    // ============================================================
    // INITIALIZE
    // ============================================================
    @FXML
    public void initialize() {
        // Link table columns with PrintJobTM properties
        colOrderNumber.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colPrintType.setCellValueFactory(new PropertyValueFactory<>("printType"));
        colItemName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colMaterial.setCellValueFactory(new PropertyValueFactory<>("material"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colSpecs.setCellValueFactory(new PropertyValueFactory<>("specifications"));
        colOperator.setCellValueFactory(new PropertyValueFactory<>("operatorName"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("orderStatus"));
        colDueDate.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        queueTable.setItems(queueList);

        // Initialize dropdown options
        initDropdowns();

        // Load queue KPIs & table records
        loadQueueKpis();
        loadQueueJobs();

        // Row selection listener
        queueTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedJob = newVal;
            updateActionPanel(newVal);
        });

        // Setup live search
        setupLiveSearch();
    }

    // ============================================================
    // INITIALIZE DROPDOWNS
    // ============================================================
    private void initDropdowns() {
        statusFilterComboBox.setItems(FXCollections.observableArrayList(
                "ALL ACTIVE", "PRINT_QUEUE", "PRINTING", "PRINTED_READY_FOR_PACKING", "ALL"
        ));
        statusFilterComboBox.getSelectionModel().selectFirst();
        statusFilterComboBox.setOnAction(e -> applyFilters());

        categoryFilterComboBox.setItems(FXCollections.observableArrayList(
                "ALL CATEGORIES", "3D_PRINTING", "DTF", "UV_FLATBED", "UV_ROTARY"
        ));
        categoryFilterComboBox.getSelectionModel().selectFirst();
        categoryFilterComboBox.setOnAction(e -> applyFilters());
    }

    // ============================================================
    // LOAD SUMMARY KPIS
    // ============================================================
    private void loadQueueKpis() {
        int userId = Session.getCurrentUser() != null ? Session.getCurrentUser().getUserId() : 1;

        try {
            Connection con = DBConnection.getConnection();

            // 1. In Queue (Waiting)
            String sql1 = "SELECT COUNT(*) FROM orders WHERE order_status = 'PRINT_QUEUE'";
            PreparedStatement ps1 = con.prepareStatement(sql1);
            ResultSet rs1 = ps1.executeQuery();
            rs1.next();
            inQueueCountLabel.setText(String.valueOf(rs1.getInt(1)));
            rs1.close();
            ps1.close();

            // 2. Currently Printing
            String sql2 = "SELECT COUNT(*) FROM orders WHERE order_status = 'PRINTING'";
            PreparedStatement ps2 = con.prepareStatement(sql2);
            ResultSet rs2 = ps2.executeQuery();
            rs2.next();
            printingCountLabel.setText(String.valueOf(rs2.getInt(1)));
            rs2.close();
            ps2.close();

            // 3. Completed Today
            String sql3 = "SELECT COUNT(*) FROM orders WHERE order_status = 'PRINTED_READY_FOR_PACKING' " +
                          "AND DATE(updated_at) = CURDATE()";
            PreparedStatement ps3 = con.prepareStatement(sql3);
            ResultSet rs3 = ps3.executeQuery();
            rs3.next();
            completedTodayCountLabel.setText(String.valueOf(rs3.getInt(1)));
            rs3.close();
            ps3.close();

            // 4. Assigned to currently logged-in user
            String sql4 = "SELECT COUNT(*) FROM orders WHERE assigned_printer_id = ? " +
                          "AND order_status IN ('PRINT_QUEUE', 'PRINTING')";
            PreparedStatement ps4 = con.prepareStatement(sql4);
            ps4.setInt(1, userId);
            ResultSet rs4 = ps4.executeQuery();
            rs4.next();
            assignedToMeCountLabel.setText(String.valueOf(rs4.getInt(1)));
            rs4.close();
            ps4.close();

        } catch (SQLException e) {
            System.out.println("Error loading queue KPIs: " + e.getMessage());
        }
    }

    // ============================================================
    // LOAD QUEUE JOBS FROM DATABASE
    // ============================================================
    private void loadQueueJobs() {
        queueList.clear();

        String sql = "SELECT o.order_id, o.order_number, c.full_name as customer_name, " +
                     "COALESCE(oi.print_type, 'N/A') as print_type, " +
                     "COALESCE(oi.item_name, 'Multiple Items') as item_name, " +
                     "COALESCE(oi.substrate_material, 'Standard') as substrate_material, " +
                     "COALESCE(oi.quantity, 1) as quantity, " +
                     "COALESCE(oi.specifications, 'Standard') as specifications, " +
                     "COALESCE(u.full_name, 'Unassigned') as operator_name, " +
                     "o.order_status, " +
                     "COALESCE(DATE_FORMAT(o.due_date, '%Y-%m-%d'), 'Flexible') as due_date " +
                     "FROM orders o " +
                     "JOIN customers c ON o.customer_id = c.customer_id " +
                     "LEFT JOIN order_items oi ON o.order_id = oi.order_id " +
                     "LEFT JOIN users u ON o.assigned_printer_id = u.user_id " +
                     "WHERE o.order_status IN ('PRINT_QUEUE', 'PRINTING', 'PRINTED_READY_FOR_PACKING', 'PLACED') " +
                     "ORDER BY CASE o.order_status " +
                     "  WHEN 'PRINTING' THEN 1 " +
                     "  WHEN 'PRINT_QUEUE' THEN 2 " +
                     "  WHEN 'PLACED' THEN 3 " +
                     "  ELSE 4 END, o.due_date ASC";

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                PrintJobTM tm = new PrintJobTM(
                        rs.getInt("order_id"),
                        rs.getString("order_number"),
                        rs.getString("customer_name"),
                        rs.getString("print_type"),
                        rs.getString("item_name"),
                        rs.getString("substrate_material"),
                        rs.getInt("quantity"),
                        rs.getString("specifications"),
                        rs.getString("operator_name"),
                        rs.getString("order_status"),
                        rs.getString("due_date")
                );
                queueList.add(tm);
            }
            rs.close();
            ps.close();

        } catch (SQLException e) {
            System.out.println("Error loading print queue jobs: " + e.getMessage());
        }
    }

    // ============================================================
    // UPDATE ACTION PANEL BASED ON SELECTION
    // ============================================================
    private void updateActionPanel(PrintJobTM job) {
        if (job == null) {
            selectedOrderLabel.setText("No order selected");
            selectedCustomerLabel.setText("-");
            selectedStatusLabel.setText("-");
            selectedDueLabel.setText("-");
            startPrintingButton.setDisable(true);
            finishPrintingButton.setDisable(true);
            return;
        }

        selectedOrderLabel.setText(job.getOrderNumber() + " (" + job.getItemName() + ")");
        selectedCustomerLabel.setText("Customer: " + job.getCustomerName());
        selectedStatusLabel.setText("Status: " + job.getOrderStatus());
        selectedDueLabel.setText("Due Date: " + job.getDueDate());

        String status = job.getOrderStatus();
        if (status.equals("PRINT_QUEUE") || status.equals("PLACED")) {
            startPrintingButton.setDisable(false);
            finishPrintingButton.setDisable(true);
        } else if (status.equals("PRINTING")) {
            startPrintingButton.setDisable(true);
            finishPrintingButton.setDisable(false);
        } else {
            // Already finished
            startPrintingButton.setDisable(true);
            finishPrintingButton.setDisable(true);
        }
    }

    // ============================================================
    // ACTION: START PRINTING (STATUS -> PRINTING)
    // ============================================================
    @FXML
    private void handleStartPrinting() {
        if (selectedJob == null) return;

        int userId = Session.getCurrentUser() != null ? Session.getCurrentUser().getUserId() : 5;

        String sql = "UPDATE orders SET order_status = 'PRINTING', assigned_printer_id = ? " +
                     "WHERE order_id = ?";

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, selectedJob.getOrderId());
            int rows = ps.executeUpdate();
            ps.close();

            if (rows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Printing Started",
                        "Order " + selectedJob.getOrderNumber() + " is now set to PRINTING.\n" +
                        "Work is assigned to: " + (Session.getCurrentUser() != null ? Session.getCurrentUser().getFullName() : "Printer Operator"));
                loadQueueKpis();
                loadQueueJobs();
                updateActionPanel(null);
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to start printing: " + e.getMessage());
        }
    }

    // ============================================================
    // ACTION: FINISH & SEND TO PACKING (STATUS -> PRINTED_READY_FOR_PACKING)
    // ============================================================
    @FXML
    private void handleFinishPrinting() {
        if (selectedJob == null) return;

        String sql = "UPDATE orders SET order_status = 'PRINTED_READY_FOR_PACKING' WHERE order_id = ?";

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, selectedJob.getOrderId());
            int rows = ps.executeUpdate();
            ps.close();

            if (rows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Printing Complete",
                        "Order " + selectedJob.getOrderNumber() + " marked as FINISHED!\n" +
                        "Sent to Packing Staff. (Customer SMS notification triggered)");
                loadQueueKpis();
                loadQueueJobs();
                updateActionPanel(null);
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to finish printing: " + e.getMessage());
        }
    }

    // ============================================================
    // ACTION: REFRESH QUEUE
    // ============================================================
    @FXML
    private void handleRefresh() {
        loadQueueKpis();
        loadQueueJobs();
        updateActionPanel(null);
    }

    // ============================================================
    // FILTER CONTROLS (STATUS + CATEGORY)
    // ============================================================
    private void applyFilters() {
        String statusFilter = statusFilterComboBox.getValue();
        String catFilter = categoryFilterComboBox.getValue();

        ObservableList<PrintJobTM> filtered = FXCollections.observableArrayList();

        for (PrintJobTM job : queueList) {
            boolean matchStatus = true;
            if (statusFilter != null && !statusFilter.equals("ALL")) {
                if (statusFilter.equals("ALL ACTIVE")) {
                    matchStatus = job.getOrderStatus().equals("PRINT_QUEUE") ||
                                  job.getOrderStatus().equals("PRINTING") ||
                                  job.getOrderStatus().equals("PLACED");
                } else {
                    matchStatus = job.getOrderStatus().equalsIgnoreCase(statusFilter);
                }
            }

            boolean matchCategory = true;
            if (catFilter != null && !catFilter.equals("ALL CATEGORIES")) {
                matchCategory = job.getPrintType().equalsIgnoreCase(catFilter);
            }

            if (matchStatus && matchCategory) {
                filtered.add(job);
            }
        }

        queueTable.setItems(filtered);
    }

    // ============================================================
    // LIVE SEARCH FILTER
    // ============================================================
    private void setupLiveSearch() {
        FilteredList<PrintJobTM> filteredData = new FilteredList<>(queueList, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(job -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }
                String filter = newValue.toLowerCase().trim();
                return job.getOrderNumber().toLowerCase().contains(filter) ||
                       job.getCustomerName().toLowerCase().contains(filter) ||
                       job.getItemName().toLowerCase().contains(filter) ||
                       job.getPrintType().toLowerCase().contains(filter) ||
                       job.getMaterial().toLowerCase().contains(filter);
            });
        });

        SortedList<PrintJobTM> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(queueTable.comparatorProperty());
        queueTable.setItems(sortedData);
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
