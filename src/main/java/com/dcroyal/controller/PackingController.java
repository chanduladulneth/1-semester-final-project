package com.dcroyal.controller;

import com.dcroyal.db.DBConnection;
import com.dcroyal.tm.PackingHistoryTM;
import com.dcroyal.tm.PackingOrderTM;
import com.dcroyal.util.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * PackingController.java
 * Handles manual packing of printed orders, auto-SMS notification triggers,
 * packaging records, and Jasper packing slip generation.
 */
public class PackingController {

    // ---- FXML KPI Summary Labels ----
    @FXML private Label readyToPackCountLabel;
    @FXML private Label packedTodayCountLabel;
    @FXML private Label totalPackedCountLabel;
    @FXML private Label outForDeliveryCountLabel;

    // ---- FXML TAB 1: Ready to Pack Table ----
    @FXML private TableView<PackingOrderTM> readyTable;
    @FXML private TableColumn<PackingOrderTM, String> colOrderNumber;
    @FXML private TableColumn<PackingOrderTM, String> colCustomer;
    @FXML private TableColumn<PackingOrderTM, String> colPhone;
    @FXML private TableColumn<PackingOrderTM, String> colDelivery;
    @FXML private TableColumn<PackingOrderTM, String> colCity;
    @FXML private TableColumn<PackingOrderTM, String> colDueDate;
    @FXML private TableColumn<PackingOrderTM, String> colStatus;
    @FXML private TextField searchReadyField;

    // ---- FXML Action Panel ----
    @FXML private Label     selectedOrderLabel;
    @FXML private Label     selectedCustomerLabel;
    @FXML private Label     selectedAddressLabel;
    @FXML private TextField packageCountField;
    @FXML private TextField weightField;
    @FXML private TextField packingNotesField;
    @FXML private Button    packOrderButton;
    @FXML private Button    printSlipButton;

    // ---- FXML TAB 2: History Table ----
    @FXML private TableView<PackingHistoryTM> historyTable;
    @FXML private TableColumn<PackingHistoryTM, Integer> colPackId;
    @FXML private TableColumn<PackingHistoryTM, String>  colHistOrder;
    @FXML private TableColumn<PackingHistoryTM, String>  colHistCustomer;
    @FXML private TableColumn<PackingHistoryTM, String>  colHistStaff;
    @FXML private TableColumn<PackingHistoryTM, Integer> colHistCount;
    @FXML private TableColumn<PackingHistoryTM, Double>  colHistWeight;
    @FXML private TableColumn<PackingHistoryTM, String>  colHistNotes;
    @FXML private TableColumn<PackingHistoryTM, String>  colHistDate;
    @FXML private TextField searchHistoryField;

    // Data lists
    private ObservableList<PackingOrderTM> readyList = FXCollections.observableArrayList();
    private ObservableList<PackingHistoryTM> historyList = FXCollections.observableArrayList();

    // Selected order
    private PackingOrderTM selectedOrder = null;

    // ============================================================
    // INITIALIZE
    // ============================================================
    @FXML
    public void initialize() {
        // Link Ready Table columns
        colOrderNumber.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("customerPhone"));
        colDelivery.setCellValueFactory(new PropertyValueFactory<>("deliveryRequired"));
        colCity.setCellValueFactory(new PropertyValueFactory<>("deliveryCity"));
        colDueDate.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("orderStatus"));
        readyTable.setItems(readyList);

        // Link History Table columns
        colPackId.setCellValueFactory(new PropertyValueFactory<>("packingId"));
        colHistOrder.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        colHistCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colHistStaff.setCellValueFactory(new PropertyValueFactory<>("staffName"));
        colHistCount.setCellValueFactory(new PropertyValueFactory<>("packageCount"));
        colHistWeight.setCellValueFactory(new PropertyValueFactory<>("weightKg"));
        colHistNotes.setCellValueFactory(new PropertyValueFactory<>("packingNotes"));
        colHistDate.setCellValueFactory(new PropertyValueFactory<>("packedAt"));
        historyTable.setItems(historyList);

        // Load data
        loadPackingKpis();
        loadReadyOrders();
        loadPackingHistory();

        // Row selection listener
        readyTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedOrder = newVal;
            updateActionPanel(newVal);
        });

        // Setup live search
        setupReadyLiveSearch();
        setupHistoryLiveSearch();
    }

    // ============================================================
    // LOAD SUMMARY KPIS
    // ============================================================
    private void loadPackingKpis() {
        try {
            Connection con = DBConnection.getConnection();

            // 1. Ready to Pack
            String sql1 = "SELECT COUNT(*) FROM orders WHERE order_status = 'PRINTED_READY_FOR_PACKING'";
            PreparedStatement ps1 = con.prepareStatement(sql1);
            ResultSet rs1 = ps1.executeQuery();
            rs1.next();
            readyToPackCountLabel.setText(String.valueOf(rs1.getInt(1)));
            rs1.close(); ps1.close();

            // 2. Packed Today
            String sql2 = "SELECT COUNT(*) FROM packing_records WHERE DATE(packed_at) = CURDATE()";
            PreparedStatement ps2 = con.prepareStatement(sql2);
            ResultSet rs2 = ps2.executeQuery();
            rs2.next();
            packedTodayCountLabel.setText(String.valueOf(rs2.getInt(1)));
            rs2.close(); ps2.close();

            // 3. Total Packed
            String sql3 = "SELECT COUNT(*) FROM packing_records";
            PreparedStatement ps3 = con.prepareStatement(sql3);
            ResultSet rs3 = ps3.executeQuery();
            rs3.next();
            totalPackedCountLabel.setText(String.valueOf(rs3.getInt(1)));
            rs3.close(); ps3.close();

            // 4. Out for Delivery
            String sql4 = "SELECT COUNT(*) FROM orders WHERE order_status = 'OUT_FOR_DELIVERY'";
            PreparedStatement ps4 = con.prepareStatement(sql4);
            ResultSet rs4 = ps4.executeQuery();
            rs4.next();
            outForDeliveryCountLabel.setText(String.valueOf(rs4.getInt(1)));
            rs4.close(); ps4.close();

        } catch (SQLException e) {
            System.out.println("Error loading packing KPIs: " + e.getMessage());
        }
    }

    // ============================================================
    // LOAD ORDERS READY FOR PACKING
    // ============================================================
    private void loadReadyOrders() {
        readyList.clear();
        String sql = "SELECT o.order_id, o.customer_id, o.order_number, o.qr_code_token, " +
                     "c.full_name as customer_name, c.phone, " +
                     "IF(o.delivery_required = 1, 'Delivery Required', 'Store Pickup') as delivery_type, " +
                     "COALESCE(o.delivery_city, 'Colombo') as delivery_city, " +
                     "COALESCE(o.delivery_address, c.address) as delivery_address, " +
                     "o.order_status, " +
                     "COALESCE(DATE_FORMAT(o.due_date, '%Y-%m-%d'), 'Flexible') as due_date " +
                     "FROM orders o " +
                     "JOIN customers c ON o.customer_id = c.customer_id " +
                     "WHERE o.order_status IN ('PRINTED_READY_FOR_PACKING', 'PACKED') " +
                     "ORDER BY CASE o.order_status WHEN 'PRINTED_READY_FOR_PACKING' THEN 1 ELSE 2 END, o.due_date ASC";

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                PackingOrderTM tm = new PackingOrderTM(
                        rs.getInt("order_id"),
                        rs.getInt("customer_id"),
                        rs.getString("order_number"),
                        rs.getString("qr_code_token"),
                        rs.getString("customer_name"),
                        rs.getString("phone"),
                        rs.getString("delivery_type"),
                        rs.getString("delivery_city"),
                        rs.getString("delivery_address"),
                        rs.getString("order_status"),
                        rs.getString("due_date")
                );
                readyList.add(tm);
            }
            rs.close(); ps.close();

        } catch (SQLException e) {
            System.out.println("Error loading ready orders: " + e.getMessage());
        }
    }

    // ============================================================
    // LOAD PACKING AUDIT HISTORY
    // ============================================================
    private void loadPackingHistory() {
        historyList.clear();
        String sql = "SELECT p.packing_id, o.order_number, c.full_name as customer_name, " +
                     "u.full_name as staff_name, p.package_count, p.weight_kg, " +
                     "COALESCE(p.packing_notes, 'Standard Packaging') as packing_notes, " +
                     "DATE_FORMAT(p.packed_at, '%Y-%m-%d %H:%i') as packed_time " +
                     "FROM packing_records p " +
                     "JOIN orders o ON p.order_id = o.order_id " +
                     "JOIN customers c ON o.customer_id = c.customer_id " +
                     "JOIN users u ON p.packed_by = u.user_id " +
                     "ORDER BY p.packing_id DESC";

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                PackingHistoryTM tm = new PackingHistoryTM(
                        rs.getInt("packing_id"),
                        rs.getString("order_number"),
                        rs.getString("customer_name"),
                        rs.getString("staff_name"),
                        rs.getInt("package_count"),
                        rs.getDouble("weight_kg"),
                        rs.getString("packing_notes"),
                        rs.getString("packed_time")
                );
                historyList.add(tm);
            }
            rs.close(); ps.close();

        } catch (SQLException e) {
            System.out.println("Error loading packing history: " + e.getMessage());
        }
    }

    // ============================================================
    // UPDATE ACTION PANEL ON SELECTION
    // ============================================================
    private void updateActionPanel(PackingOrderTM order) {
        if (order == null) {
            selectedOrderLabel.setText("No order selected");
            selectedCustomerLabel.setText("-");
            selectedAddressLabel.setText("-");
            packOrderButton.setDisable(true);
            printSlipButton.setDisable(true);
            return;
        }

        selectedOrderLabel.setText(order.getOrderNumber());
        selectedCustomerLabel.setText("Customer: " + order.getCustomerName() + " (" + order.getCustomerPhone() + ")");
        selectedAddressLabel.setText("Destination: " + order.getDeliveryCity() + " — " + order.getDeliveryAddress());

        boolean isAlreadyPacked = order.getOrderStatus().equals("PACKED");
        packOrderButton.setDisable(isAlreadyPacked);
        printSlipButton.setDisable(false);
    }

    // ============================================================
    // ACTION: PACK ORDER & DISPATCH AUTO-SMS (TRANSACTION)
    // ============================================================
    @FXML
    private void handlePackOrder() {
        if (selectedOrder == null) return;

        int packageCount = 1;
        try {
            packageCount = Integer.parseInt(packageCountField.getText().trim());
            if (packageCount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Invalid Packages", "Please enter a valid package count (e.g. 1, 2).");
            return;
        }

        double weight = 1.0;
        try {
            String wStr = weightField.getText().trim();
            if (!wStr.isEmpty()) weight = Double.parseDouble(wStr);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Invalid Weight", "Please enter a valid weight in kg (e.g. 1.25).");
            return;
        }

        String notes = packingNotesField.getText().trim();
        int staffId = Session.getCurrentUser() != null ? Session.getCurrentUser().getUserId() : 6;

        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false); // Begin Transaction

            // 1. Insert into packing_records
            String packSql = "INSERT INTO packing_records (order_id, packed_by, package_count, weight_kg, packing_notes) " +
                             "VALUES (?, ?, ?, ?, ?)";
            PreparedStatement psPack = con.prepareStatement(packSql);
            psPack.setInt(1, selectedOrder.getOrderId());
            psPack.setInt(2, staffId);
            psPack.setInt(3, packageCount);
            psPack.setDouble(4, weight);
            psPack.setString(5, notes.isEmpty() ? "Standard packaging" : notes);
            psPack.executeUpdate();
            psPack.close();

            // 2. Update order status to 'PACKED'
            String orderSql = "UPDATE orders SET order_status = 'PACKED' WHERE order_id = ?";
            PreparedStatement psOrder = con.prepareStatement(orderSql);
            psOrder.setInt(1, selectedOrder.getOrderId());
            psOrder.executeUpdate();
            psOrder.close();

            // 3. Auto-generate SMS notification into sms_notifications table
            String smsBody = "Dear " + selectedOrder.getCustomerName() + ", your DC Royal Printing order (" +
                             selectedOrder.getOrderNumber() + ") has been securely packed (" + packageCount + " pkg) " +
                             "and is ready for dispatch! Hotline: 011-2345678.";

            String smsSql = "INSERT INTO sms_notifications (order_id, customer_id, phone_number, trigger_status, message_body, delivery_state) " +
                            "VALUES (?, ?, ?, 'PACKED', ?, 'SENT')";
            PreparedStatement psSms = con.prepareStatement(smsSql);
            psSms.setInt(1, selectedOrder.getOrderId());
            psSms.setInt(2, selectedOrder.getCustomerId());
            psSms.setString(3, selectedOrder.getCustomerPhone());
            psSms.setString(4, smsBody);
            psSms.executeUpdate();
            psSms.close();

            // Commit transaction
            con.commit();
            con.setAutoCommit(true);

            showAlert(Alert.AlertType.INFORMATION, "Order Packed Successfully",
                    "Order " + selectedOrder.getOrderNumber() + " marked as PACKED!\n" +
                    "Packages: " + packageCount + " (" + weight + " kg)\n\n" +
                    "📲 AUTO-SMS TRIGGERED:\n" +
                    "Recipient: " + selectedOrder.getCustomerPhone() + "\n" +
                    "Message: \"" + smsBody + "\"");

            // Refresh views
            loadPackingKpis();
            loadReadyOrders();
            loadPackingHistory();
            updateActionPanel(null);

        } catch (SQLException e) {
            if (con != null) {
                try { con.rollback(); con.setAutoCommit(true); } catch (SQLException ignored) {}
            }
            showAlert(Alert.AlertType.ERROR, "Packing Failed", "Database error: " + e.getMessage());
        }
    }

    // ============================================================
    // ACTION: VIEW / PRINT PACKING SLIP & QR BADGE
    // ============================================================
    @FXML
    private void handlePrintSlip() {
        if (selectedOrder == null) return;

        Stage slipStage = new Stage();
        slipStage.initModality(Modality.APPLICATION_MODAL);
        slipStage.setTitle("DC Royal Printing — Packing & Delivery Slip (" + selectedOrder.getOrderNumber() + ")");

        VBox root = new VBox(14);
        root.setPadding(new Insets(26));
        root.setStyle("-fx-background-color: white; -fx-font-family: 'Segoe UI', sans-serif;");
        root.setAlignment(Pos.TOP_CENTER);
        root.setPrefWidth(440);

        Label brand = new Label("DC ROYAL PRINTING");
        brand.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #14161F;");

        Label sub = new Label("Dispatch Package Label & Delivery Routing Slip");
        sub.setStyle("-fx-font-size: 11px; -fx-text-fill: #62656F; -fx-font-weight: 600;");

        Separator sep1 = new Separator();

        VBox meta = new VBox(6);
        meta.getChildren().addAll(
            new Label("Order Number  : " + selectedOrder.getOrderNumber()),
            new Label("QR Token      : " + selectedOrder.getQrCodeToken()),
            new Label("Customer      : " + selectedOrder.getCustomerName()),
            new Label("Phone         : " + selectedOrder.getCustomerPhone()),
            new Label("Delivery Type : " + selectedOrder.getDeliveryRequired()),
            new Label("Destination   : " + selectedOrder.getDeliveryCity() + " — " + selectedOrder.getDeliveryAddress()),
            new Label("Date Packed   : " + LocalDate.now().toString())
        );
        meta.setStyle("-fx-font-size: 12px; -fx-text-fill: #15171F;");

        Separator sep2 = new Separator();

        VBox qrBox = new VBox(6);
        qrBox.setAlignment(Pos.CENTER);
        qrBox.setStyle("-fx-background-color: #F6F3EC; -fx-background-radius: 10; -fx-padding: 14;");
        Label qrIcon = new Label("📦 [ QR TRACKING LABEL ]");
        qrIcon.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #14161F;");
        Label qrLabel = new Label(selectedOrder.getQrCodeToken());
        qrLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: #07768F;");
        qrBox.getChildren().addAll(qrIcon, qrLabel);

        Button closeBtn = new Button("✔  Done / Print Slip");
        closeBtn.setMaxWidth(Double.MAX_VALUE);
        closeBtn.setPrefHeight(38);
        closeBtn.setStyle("-fx-background-color: #07768F; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> slipStage.close());

        root.getChildren().addAll(brand, sub, sep1, meta, sep2, qrBox, closeBtn);

        Scene scene = new Scene(root);
        slipStage.setScene(scene);
        slipStage.showAndWait();
    }

    // ============================================================
    // REFRESH BUTTON
    // ============================================================
    @FXML
    private void handleRefresh() {
        loadPackingKpis();
        loadReadyOrders();
        loadPackingHistory();
        updateActionPanel(null);
    }

    // ============================================================
    // LIVE SEARCH: READY ORDERS
    // ============================================================
    private void setupReadyLiveSearch() {
        FilteredList<PackingOrderTM> filteredData = new FilteredList<>(readyList, p -> true);

        searchReadyField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(order -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }
                String filter = newValue.toLowerCase().trim();
                return order.getOrderNumber().toLowerCase().contains(filter) ||
                       order.getCustomerName().toLowerCase().contains(filter) ||
                       order.getCustomerPhone().contains(filter) ||
                       order.getDeliveryCity().toLowerCase().contains(filter);
            });
        });

        SortedList<PackingOrderTM> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(readyTable.comparatorProperty());
        readyTable.setItems(sortedData);
    }

    // ============================================================
    // LIVE SEARCH: HISTORY
    // ============================================================
    private void setupHistoryLiveSearch() {
        FilteredList<PackingHistoryTM> filteredData = new FilteredList<>(historyList, p -> true);

        searchHistoryField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(hist -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }
                String filter = newValue.toLowerCase().trim();
                return hist.getOrderNumber().toLowerCase().contains(filter) ||
                       hist.getCustomerName().toLowerCase().contains(filter) ||
                       hist.getStaffName().toLowerCase().contains(filter) ||
                       hist.getPackingNotes().toLowerCase().contains(filter);
            });
        });

        SortedList<PackingHistoryTM> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(historyTable.comparatorProperty());
        historyTable.setItems(sortedData);
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
