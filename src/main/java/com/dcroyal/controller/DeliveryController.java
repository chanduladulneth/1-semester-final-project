package com.dcroyal.controller;

import com.dcroyal.db.DBConnection;
import com.dcroyal.tm.DeliveryTM;
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

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DeliveryController.java
 * Handles island-wide delivery assignments, driver dispatch,
 * automated delivery SMS alerts, and delivery route reports.
 */
public class DeliveryController {

    // ---- FXML KPI Summary Labels ----
    @FXML private Label scheduledTodayCountLabel;
    @FXML private Label inTransitCountLabel;
    @FXML private Label deliveredTodayCountLabel;
    @FXML private Label failedCountLabel;

    // ---- FXML TAB 1: Delivery Table & Filter Controls ----
    @FXML private TableView<DeliveryTM> deliveryTable;
    @FXML private TableColumn<DeliveryTM, String> colOrderNumber;
    @FXML private TableColumn<DeliveryTM, String> colRecipient;
    @FXML private TableColumn<DeliveryTM, String> colPhone;
    @FXML private TableColumn<DeliveryTM, String> colAddress;
    @FXML private TableColumn<DeliveryTM, String> colDate;
    @FXML private TableColumn<DeliveryTM, String> colDriver;
    @FXML private TableColumn<DeliveryTM, String> colStatus;
    @FXML private TableColumn<DeliveryTM, String> colDeliveredTime;
    @FXML private TableColumn<DeliveryTM, String> colNotes;

    @FXML private TextField        searchField;
    @FXML private ComboBox<String> statusFilterComboBox;

    // ---- FXML Action Panel ----
    @FXML private Label  selectedOrderLabel;
    @FXML private Label  selectedRecipientLabel;
    @FXML private Label  selectedAddressLabel;
    @FXML private Label  selectedDriverLabel;
    @FXML private Button dispatchButton;
    @FXML private Button deliverButton;
    @FXML private Button failButton;
    @FXML private Button printRouteSheetButton;

    // ---- FXML TAB 2: Assign Delivery Form ----
    @FXML private ComboBox<String> assignOrderComboBox;
    @FXML private ComboBox<String> assignDriverComboBox;
    @FXML private DatePicker       scheduledDatePicker;
    @FXML private TextField        recipientNameField;
    @FXML private TextField        recipientPhoneField;
    @FXML private TextArea         deliveryAddressArea;
    @FXML private TextField        assignNotesField;
    @FXML private Button           createAssignmentButton;

    // Data lists
    private ObservableList<DeliveryTM> deliveryList = FXCollections.observableArrayList();

    // Mappings for Tab 2 combo boxes
    private List<Integer> unassignedOrderIdList = new ArrayList<>();
    private List<Integer> driverIdList = new ArrayList<>();

    // Selected delivery
    private DeliveryTM selectedDelivery = null;

    // ============================================================
    // INITIALIZE
    // ============================================================
    @FXML
    public void initialize() {
        // Link table columns
        colOrderNumber.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        colRecipient.setCellValueFactory(new PropertyValueFactory<>("recipientName"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("recipientPhone"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("deliveryAddress"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("scheduledDate"));
        colDriver.setCellValueFactory(new PropertyValueFactory<>("driverName"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("deliveryStatus"));
        colDeliveredTime.setCellValueFactory(new PropertyValueFactory<>("deliveredAt"));
        colNotes.setCellValueFactory(new PropertyValueFactory<>("deliveryNotes"));
        deliveryTable.setItems(deliveryList);

        // Status filter
        statusFilterComboBox.setItems(FXCollections.observableArrayList(
                "ALL", "ASSIGNED", "IN_TRANSIT", "DELIVERED", "FAILED_RETRY"
        ));
        statusFilterComboBox.getSelectionModel().selectFirst();
        statusFilterComboBox.setOnAction(e -> filterDeliveries());

        // Default scheduled date = today
        scheduledDatePicker.setValue(LocalDate.now());

        // Load data
        loadDeliveryKpis();
        loadAllDeliveries();
        loadAssignDropdowns();

        // Row selection listener
        deliveryTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedDelivery = newVal;
            updateActionPanel(newVal);
        });

        // Tab 2: Order selection listener (auto-populate recipient & address)
        assignOrderComboBox.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            int idx = newVal.intValue();
            if (idx >= 0 && idx < unassignedOrderIdList.size()) {
                loadOrderRecipientDetails(unassignedOrderIdList.get(idx));
            }
        });

        // Setup live search
        setupLiveSearch();
    }

    // ============================================================
    // LOAD SUMMARY KPIS
    // ============================================================
    private void loadDeliveryKpis() {
        try {
            Connection con = DBConnection.getConnection();

            // 1. Scheduled Today
            String sql1 = "SELECT COUNT(*) FROM delivery_assignments WHERE scheduled_date = CURDATE()";
            PreparedStatement ps1 = con.prepareStatement(sql1);
            ResultSet rs1 = ps1.executeQuery();
            rs1.next();
            scheduledTodayCountLabel.setText(String.valueOf(rs1.getInt(1)));
            rs1.close(); ps1.close();

            // 2. In Transit
            String sql2 = "SELECT COUNT(*) FROM delivery_assignments WHERE delivery_status = 'IN_TRANSIT'";
            PreparedStatement ps2 = con.prepareStatement(sql2);
            ResultSet rs2 = ps2.executeQuery();
            rs2.next();
            inTransitCountLabel.setText(String.valueOf(rs2.getInt(1)));
            rs2.close(); ps2.close();

            // 3. Delivered Today
            String sql3 = "SELECT COUNT(*) FROM delivery_assignments WHERE delivery_status = 'DELIVERED' " +
                          "AND DATE(delivered_at) = CURDATE()";
            PreparedStatement ps3 = con.prepareStatement(sql3);
            ResultSet rs3 = ps3.executeQuery();
            rs3.next();
            deliveredTodayCountLabel.setText(String.valueOf(rs3.getInt(1)));
            rs3.close(); ps3.close();

            // 4. Failed / Retry
            String sql4 = "SELECT COUNT(*) FROM delivery_assignments WHERE delivery_status = 'FAILED_RETRY'";
            PreparedStatement ps4 = con.prepareStatement(sql4);
            ResultSet rs4 = ps4.executeQuery();
            rs4.next();
            failedCountLabel.setText(String.valueOf(rs4.getInt(1)));
            rs4.close(); ps4.close();

        } catch (SQLException e) {
            System.out.println("Error loading delivery KPIs: " + e.getMessage());
        }
    }

    // ============================================================
    // LOAD ALL DELIVERIES
    // ============================================================
    private void loadAllDeliveries() {
        deliveryList.clear();
        String sql = "SELECT da.delivery_id, da.order_id, o.order_number, da.recipient_name, " +
                     "da.recipient_phone, da.delivery_address, " +
                     "DATE_FORMAT(da.scheduled_date, '%Y-%m-%d') as sched_date, " +
                     "u.full_name as driver_name, da.delivery_status, " +
                     "COALESCE(DATE_FORMAT(da.delivered_at, '%Y-%m-%d %H:%i'), '-') as deliv_time, " +
                     "COALESCE(da.delivery_notes, '-') as delivery_notes " +
                     "FROM delivery_assignments da " +
                     "JOIN orders o ON da.order_id = o.order_id " +
                     "JOIN users u ON da.driver_id = u.user_id " +
                     "ORDER BY da.delivery_id DESC";

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                DeliveryTM tm = new DeliveryTM(
                        rs.getInt("delivery_id"),
                        rs.getInt("order_id"),
                        rs.getString("order_number"),
                        rs.getString("recipient_name"),
                        rs.getString("recipient_phone"),
                        rs.getString("delivery_address"),
                        rs.getString("sched_date"),
                        rs.getString("driver_name"),
                        rs.getString("delivery_status"),
                        rs.getString("deliv_time"),
                        rs.getString("delivery_notes")
                );
                deliveryList.add(tm);
            }
            rs.close(); ps.close();

        } catch (SQLException e) {
            System.out.println("Error loading deliveries: " + e.getMessage());
        }
    }

    // ============================================================
    // LOAD TAB 2 ASSIGN DROPDOWNS (UNASSIGNED ORDERS & DRIVERS)
    // ============================================================
    private void loadAssignDropdowns() {
        assignOrderComboBox.getItems().clear();
        unassignedOrderIdList.clear();
        assignDriverComboBox.getItems().clear();
        driverIdList.clear();

        try {
            Connection con = DBConnection.getConnection();

            // Load packed orders that require delivery and have not been assigned yet
            String sqlOrders = "SELECT o.order_id, o.order_number, c.full_name " +
                               "FROM orders o " +
                               "JOIN customers c ON o.customer_id = c.customer_id " +
                               "WHERE o.delivery_required = 1 " +
                               "AND o.order_id NOT IN (SELECT order_id FROM delivery_assignments) " +
                               "ORDER BY o.order_id DESC";
            PreparedStatement psOrd = con.prepareStatement(sqlOrders);
            ResultSet rsOrd = psOrd.executeQuery();
            while (rsOrd.next()) {
                unassignedOrderIdList.add(rsOrd.getInt("order_id"));
                assignOrderComboBox.getItems().add(
                        rsOrd.getString("order_number") + " — " + rsOrd.getString("full_name")
                );
            }
            rsOrd.close(); psOrd.close();

            if (!assignOrderComboBox.getItems().isEmpty()) {
                assignOrderComboBox.getSelectionModel().selectFirst();
            }

            // Load users with role DELIVERY_STAFF
            String sqlDrivers = "SELECT u.user_id, u.full_name FROM users u " +
                                "JOIN roles r ON u.role_id = r.role_id " +
                                "WHERE r.role_code = 'DELIVERY_STAFF' AND u.is_active = 1";
            PreparedStatement psDrv = con.prepareStatement(sqlDrivers);
            ResultSet rsDrv = psDrv.executeQuery();
            while (rsDrv.next()) {
                driverIdList.add(rsDrv.getInt("user_id"));
                assignDriverComboBox.getItems().add(rsDrv.getString("full_name"));
            }
            rsDrv.close(); psDrv.close();

            if (!assignDriverComboBox.getItems().isEmpty()) {
                assignDriverComboBox.getSelectionModel().selectFirst();
            }

        } catch (SQLException e) {
            System.out.println("Error loading assign dropdowns: " + e.getMessage());
        }
    }

    // ============================================================
    // LOAD ORDER RECIPIENT DETAILS ON ORDER COMBO SELECTION
    // ============================================================
    private void loadOrderRecipientDetails(int orderId) {
        String sql = "SELECT c.full_name, c.phone, " +
                     "COALESCE(o.delivery_address, c.address) as deliv_addr, " +
                     "COALESCE(o.delivery_city, c.city) as deliv_city " +
                     "FROM orders o " +
                     "JOIN customers c ON o.customer_id = c.customer_id " +
                     "WHERE o.order_id = ?";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                recipientNameField.setText(rs.getString("full_name"));
                recipientPhoneField.setText(rs.getString("phone"));
                String addr = rs.getString("deliv_addr");
                String city = rs.getString("deliv_city");
                deliveryAddressArea.setText((addr != null ? addr : "") + (city != null ? ", " + city : ""));
            }
            rs.close(); ps.close();

        } catch (SQLException e) {
            System.out.println("Error loading order recipient details: " + e.getMessage());
        }
    }

    // ============================================================
    // UPDATE ACTION PANEL ON ROW SELECTION
    // ============================================================
    private void updateActionPanel(DeliveryTM d) {
        if (d == null) {
            selectedOrderLabel.setText("No delivery selected");
            selectedRecipientLabel.setText("-");
            selectedAddressLabel.setText("-");
            selectedDriverLabel.setText("-");
            dispatchButton.setDisable(true);
            deliverButton.setDisable(true);
            failButton.setDisable(true);
            return;
        }

        selectedOrderLabel.setText(d.getOrderNumber() + " [" + d.getDeliveryStatus() + "]");
        selectedRecipientLabel.setText("Recipient: " + d.getRecipientName() + " (" + d.getRecipientPhone() + ")");
        selectedAddressLabel.setText("Address: " + d.getDeliveryAddress());
        selectedDriverLabel.setText("Driver: " + d.getDriverName() + " | Sched: " + d.getScheduledDate());

        String status = d.getDeliveryStatus();
        dispatchButton.setDisable(!status.equals("ASSIGNED"));
        deliverButton.setDisable(!status.equals("IN_TRANSIT"));
        failButton.setDisable(status.equals("DELIVERED"));
    }

    // ============================================================
    // ACTION 1: DISPATCH / IN TRANSIT (AUTO-SMS TRIGGERED)
    // ============================================================
    @FXML
    private void handleDispatch() {
        if (selectedDelivery == null) return;

        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false); // Begin Transaction

            // 1. Update delivery status
            String sql1 = "UPDATE delivery_assignments SET delivery_status = 'IN_TRANSIT' WHERE delivery_id = ?";
            PreparedStatement ps1 = con.prepareStatement(sql1);
            ps1.setInt(1, selectedDelivery.getDeliveryId());
            ps1.executeUpdate();
            ps1.close();

            // 2. Update orders table status
            String sql2 = "UPDATE orders SET order_status = 'OUT_FOR_DELIVERY' WHERE order_id = ?";
            PreparedStatement ps2 = con.prepareStatement(sql2);
            ps2.setInt(1, selectedDelivery.getOrderId());
            ps2.executeUpdate();
            ps2.close();

            // 3. Dispatch Auto-SMS
            String smsBody = "Dear " + selectedDelivery.getRecipientName() + ", your DC Royal Printing order (" +
                             selectedDelivery.getOrderNumber() + ") is now OUT FOR DELIVERY with our driver " +
                             selectedDelivery.getDriverName() + "! Helpline: 011-2345678.";

            String sql3 = "INSERT INTO sms_notifications (order_id, customer_id, phone_number, trigger_status, message_body, delivery_state) " +
                          "VALUES (?, (SELECT customer_id FROM orders WHERE order_id = ?), ?, 'OUT_FOR_DELIVERY', ?, 'SENT')";
            PreparedStatement ps3 = con.prepareStatement(sql3);
            ps3.setInt(1, selectedDelivery.getOrderId());
            ps3.setInt(2, selectedDelivery.getOrderId());
            ps3.setString(3, selectedDelivery.getRecipientPhone());
            ps3.setString(4, smsBody);
            ps3.executeUpdate();
            ps3.close();

            // Commit transaction
            con.commit();
            con.setAutoCommit(true);

            showAlert(Alert.AlertType.INFORMATION, "Dispatch Confirmed",
                    "Order " + selectedDelivery.getOrderNumber() + " is now IN TRANSIT!\n\n" +
                    "📲 AUTO-SMS TRIGGERED:\n" +
                    "Recipient: " + selectedDelivery.getRecipientPhone() + "\n" +
                    "Message: \"" + smsBody + "\"");

            loadDeliveryKpis();
            loadAllDeliveries();
            updateActionPanel(null);

        } catch (SQLException e) {
            if (con != null) {
                try { con.rollback(); con.setAutoCommit(true); } catch (SQLException ignored) {}
            }
            showAlert(Alert.AlertType.ERROR, "Dispatch Error", "Failed to dispatch order: " + e.getMessage());
        }
    }

    // ============================================================
    // ACTION 2: MARK AS DELIVERED (AUTO-SMS TRIGGERED)
    // ============================================================
    @FXML
    private void handleMarkDelivered() {
        if (selectedDelivery == null) return;

        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false); // Begin Transaction

            // 1. Update delivery status & delivered_at
            String sql1 = "UPDATE delivery_assignments SET delivery_status = 'DELIVERED', delivered_at = NOW() WHERE delivery_id = ?";
            PreparedStatement ps1 = con.prepareStatement(sql1);
            ps1.setInt(1, selectedDelivery.getDeliveryId());
            ps1.executeUpdate();
            ps1.close();

            // 2. Update orders table status
            String sql2 = "UPDATE orders SET order_status = 'DELIVERED' WHERE order_id = ?";
            PreparedStatement ps2 = con.prepareStatement(sql2);
            ps2.setInt(1, selectedDelivery.getOrderId());
            ps2.executeUpdate();
            ps2.close();

            // 3. Dispatch Delivery Success SMS
            String smsBody = "Dear " + selectedDelivery.getRecipientName() + ", your DC Royal Printing order (" +
                             selectedDelivery.getOrderNumber() + ") has been DELIVERED successfully! " +
                             "Thank you for choosing DC Royal Printing.";

            String sql3 = "INSERT INTO sms_notifications (order_id, customer_id, phone_number, trigger_status, message_body, delivery_state) " +
                          "VALUES (?, (SELECT customer_id FROM orders WHERE order_id = ?), ?, 'DELIVERED', ?, 'SENT')";
            PreparedStatement ps3 = con.prepareStatement(sql3);
            ps3.setInt(1, selectedDelivery.getOrderId());
            ps3.setInt(2, selectedDelivery.getOrderId());
            ps3.setString(3, selectedDelivery.getRecipientPhone());
            ps3.setString(4, smsBody);
            ps3.executeUpdate();
            ps3.close();

            // Commit transaction
            con.commit();
            con.setAutoCommit(true);

            showAlert(Alert.AlertType.INFORMATION, "Delivered Successfully",
                    "Order " + selectedDelivery.getOrderNumber() + " marked as DELIVERED!\n\n" +
                    "📲 AUTO-SMS TRIGGERED:\n" +
                    "Recipient: " + selectedDelivery.getRecipientPhone() + "\n" +
                    "Message: \"" + smsBody + "\"");

            loadDeliveryKpis();
            loadAllDeliveries();
            updateActionPanel(null);

        } catch (SQLException e) {
            if (con != null) {
                try { con.rollback(); con.setAutoCommit(true); } catch (SQLException ignored) {}
            }
            showAlert(Alert.AlertType.ERROR, "Delivery Error", "Failed to mark delivered: " + e.getMessage());
        }
    }

    // ============================================================
    // ACTION 3: MARK AS FAILED / RETRY
    // ============================================================
    @FXML
    private void handleMarkFailed() {
        if (selectedDelivery == null) return;

        TextInputDialog dialog = new TextInputDialog("Customer unavailable / Incorrect address");
        dialog.setTitle("Delivery Failed / Retry");
        dialog.setHeaderText("Reason for failed delivery (" + selectedDelivery.getOrderNumber() + ")");
        dialog.setContentText("Enter notes:");

        dialog.showAndWait().ifPresent(reason -> {
            String sql = "UPDATE delivery_assignments SET delivery_status = 'FAILED_RETRY', delivery_notes = ? " +
                         "WHERE delivery_id = ?";
            try {
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, reason);
                ps.setInt(2, selectedDelivery.getDeliveryId());
                ps.executeUpdate();
                ps.close();

                showAlert(Alert.AlertType.INFORMATION, "Marked for Retry",
                        "Order " + selectedDelivery.getOrderNumber() + " marked for retry delivery.");
                loadDeliveryKpis();
                loadAllDeliveries();
                updateActionPanel(null);

            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update status: " + e.getMessage());
            }
        });
    }

    // ============================================================
    // ACTION 4: PRINT DAILY DELIVERY ROUTE SHEET
    // ============================================================
    @FXML
    private void handlePrintRouteSheet() {
        Stage routeStage = new Stage();
        routeStage.initModality(Modality.APPLICATION_MODAL);
        routeStage.setTitle("DC Royal Printing — Daily Delivery Manifest & Route Sheet");

        VBox root = new VBox(14);
        root.setPadding(new Insets(26));
        root.setStyle("-fx-background-color: white; -fx-font-family: 'Segoe UI', sans-serif;");
        root.setAlignment(Pos.TOP_CENTER);
        root.setPrefWidth(540);

        Label brand = new Label("DC ROYAL PRINTING");
        brand.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #14161F;");

        Label sub = new Label("Island-Wide Delivery Manifest & Route Sheet — " + LocalDate.now().toString());
        sub.setStyle("-fx-font-size: 11px; -fx-text-fill: #62656F; -fx-font-weight: 600;");

        Separator sep1 = new Separator();

        VBox itemsBox = new VBox(8);
        itemsBox.getChildren().add(new Label("TODAY'S SCHEDULED PARCELS:"));

        int count = 0;
        for (DeliveryTM d : deliveryList) {
            if (d.getScheduledDate().equals(LocalDate.now().toString()) || d.getDeliveryStatus().equals("IN_TRANSIT")) {
                count++;
                HBox row = new HBox(10);
                VBox info = new VBox(2);
                Label title = new Label(count + ". " + d.getOrderNumber() + " — " + d.getRecipientName() + " (" + d.getRecipientPhone() + ")");
                title.setStyle("-fx-font-weight: 700; -fx-font-size: 12px;");
                Label addr = new Label("   📍 " + d.getDeliveryAddress());
                addr.setStyle("-fx-font-size: 11px; -fx-text-fill: #62656F;");
                info.getChildren().addAll(title, addr);
                HBox.setHgrow(info, Priority.ALWAYS);

                Label statusBadge = new Label("[" + d.getDeliveryStatus() + "]");
                statusBadge.setStyle("-fx-font-weight: 700; -fx-text-fill: #07768F; -fx-font-size: 11px;");

                row.getChildren().addAll(info, statusBadge);
                itemsBox.getChildren().add(row);
            }
        }

        if (count == 0) {
            itemsBox.getChildren().add(new Label("No parcels scheduled for delivery today."));
        }

        Separator sep2 = new Separator();

        Label footer = new Label("Driver Signature: _________________________    Date: " + LocalDate.now());
        footer.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #15171F;");

        Button closeBtn = new Button("✔  Done / Print Route Sheet");
        closeBtn.setMaxWidth(Double.MAX_VALUE);
        closeBtn.setPrefHeight(38);
        closeBtn.setStyle("-fx-background-color: #07768F; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> routeStage.close());

        root.getChildren().addAll(brand, sub, sep1, itemsBox, sep2, footer, closeBtn);

        Scene scene = new Scene(root);
        routeStage.setScene(scene);
        routeStage.showAndWait();
    }

    // ============================================================
    // TAB 2: CREATE NEW DELIVERY ASSIGNMENT
    // ============================================================
    @FXML
    private void handleCreateAssignment() {
        int orderIdx = assignOrderComboBox.getSelectionModel().getSelectedIndex();
        if (orderIdx < 0 || orderIdx >= unassignedOrderIdList.size()) {
            showAlert(Alert.AlertType.WARNING, "Order Required", "Please select a packed order requiring delivery.");
            return;
        }

        int driverIdx = assignDriverComboBox.getSelectionModel().getSelectedIndex();
        if (driverIdx < 0 || driverIdx >= driverIdList.size()) {
            showAlert(Alert.AlertType.WARNING, "Driver Required", "Please select a delivery staff driver.");
            return;
        }

        int orderId = unassignedOrderIdList.get(orderIdx);
        int driverId = driverIdList.get(driverIdx);
        LocalDate scheduledDate = scheduledDatePicker.getValue() != null ? scheduledDatePicker.getValue() : LocalDate.now();
        String recipient = recipientNameField.getText().trim();
        String phone = recipientPhoneField.getText().trim();
        String address = deliveryAddressArea.getText().trim();
        String notes = assignNotesField.getText().trim();

        if (recipient.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Details", "Recipient name, phone, and delivery address are required.");
            return;
        }

        String sql = "INSERT INTO delivery_assignments (order_id, driver_id, scheduled_date, recipient_name, " +
                     "recipient_phone, delivery_address, delivery_status, delivery_notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, 'ASSIGNED', ?)";

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, orderId);
            ps.setInt(2, driverId);
            ps.setDate(3, Date.valueOf(scheduledDate));
            ps.setString(4, recipient);
            ps.setString(5, phone);
            ps.setString(6, address);
            ps.setString(7, notes.isEmpty() ? null : notes);

            int rows = ps.executeUpdate();
            ps.close();

            if (rows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Assignment Created",
                        "Delivery assignment created successfully for " + recipient + "!");
                loadDeliveryKpis();
                loadAllDeliveries();
                loadAssignDropdowns();
                recipientNameField.clear();
                recipientPhoneField.clear();
                deliveryAddressArea.clear();
                assignNotesField.clear();
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to create delivery assignment: " + e.getMessage());
        }
    }

    // ============================================================
    // REFRESH
    // ============================================================
    @FXML
    private void handleRefresh() {
        loadDeliveryKpis();
        loadAllDeliveries();
        loadAssignDropdowns();
        updateActionPanel(null);
    }

    // ============================================================
    // FILTER DELIVERIES BY STATUS
    // ============================================================
    private void filterDeliveries() {
        String filter = statusFilterComboBox.getValue();
        if (filter == null || filter.equals("ALL")) {
            deliveryTable.setItems(deliveryList);
            return;
        }

        ObservableList<DeliveryTM> filtered = FXCollections.observableArrayList();
        for (DeliveryTM d : deliveryList) {
            if (d.getDeliveryStatus().equalsIgnoreCase(filter)) {
                filtered.add(d);
            }
        }
        deliveryTable.setItems(filtered);
    }

    // ============================================================
    // LIVE SEARCH
    // ============================================================
    private void setupLiveSearch() {
        FilteredList<DeliveryTM> filteredData = new FilteredList<>(deliveryList, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(d -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }
                String filter = newValue.toLowerCase().trim();
                return d.getOrderNumber().toLowerCase().contains(filter) ||
                       d.getRecipientName().toLowerCase().contains(filter) ||
                       d.getRecipientPhone().contains(filter) ||
                       d.getDeliveryAddress().toLowerCase().contains(filter) ||
                       d.getDriverName().toLowerCase().contains(filter);
            });
        });

        SortedList<DeliveryTM> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(deliveryTable.comparatorProperty());
        deliveryTable.setItems(sortedData);
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
