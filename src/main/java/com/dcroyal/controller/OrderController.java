package com.dcroyal.controller;

import com.dcroyal.db.DBConnection;
import com.dcroyal.tm.OrderItemTM;
import com.dcroyal.tm.OrderTM;
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
 * OrderController.java
 * Handles complete Order details entry, billing, built-in calculator,
 * line items management, and real-time order history tracking.
 */
public class OrderController {

    // ============================================================
    // TAB 1: NEW ORDER & BILLING CONTROLS
    // ============================================================
    @FXML private ComboBox<String> customerComboBox;
    @FXML private Label            customerInfoLabel;
    @FXML private TextField        orderNumberField;
    @FXML private Label            qrTokenLabel;
    @FXML private DatePicker       dueDatePicker;

    // Item Entry
    @FXML private ComboBox<String> printTypeComboBox;
    @FXML private TextField        itemNameField;
    @FXML private ComboBox<String> materialComboBox;
    @FXML private TextField        quantityField;
    @FXML private TextField        unitPriceField;
    @FXML private TextField        specificationsField;
    @FXML private Button           addItemButton;

    // Order Items Table (Cart)
    @FXML private TableView<OrderItemTM> itemsTable;
    @FXML private TableColumn<OrderItemTM, String> colItemPrintType;
    @FXML private TableColumn<OrderItemTM, String> colItemName;
    @FXML private TableColumn<OrderItemTM, String> colItemMaterial;
    @FXML private TableColumn<OrderItemTM, Integer> colItemQty;
    @FXML private TableColumn<OrderItemTM, Double> colItemUnitPrice;
    @FXML private TableColumn<OrderItemTM, Double> colItemLineTotal;
    @FXML private TableColumn<OrderItemTM, String> colItemSpecs;

    // Billing & Calculator
    @FXML private Label            subtotalLabel;
    @FXML private TextField        discountField;
    @FXML private CheckBox         deliveryCheckBox;
    @FXML private TextField        deliveryCityField;
    @FXML private TextArea         deliveryAddressArea;
    @FXML private TextField        deliveryFeeField;
    @FXML private Label            netTotalLabel;
    @FXML private ComboBox<String> paymentStatusComboBox;

    // Quick Calculator
    @FXML private TextField        cashPaidField;
    @FXML private Label            balanceChangeLabel;

    // ============================================================
    // TAB 2: ORDER HISTORY & TRACKING CONTROLS
    // ============================================================
    @FXML private TableView<OrderTM> ordersTable;
    @FXML private TableColumn<OrderTM, String> colOrderNumber;
    @FXML private TableColumn<OrderTM, String> colOrderQr;
    @FXML private TableColumn<OrderTM, String> colOrderCustomer;
    @FXML private TableColumn<OrderTM, String> colOrderPhone;
    @FXML private TableColumn<OrderTM, Double> colOrderTotal;
    @FXML private TableColumn<OrderTM, String> colOrderPayment;
    @FXML private TableColumn<OrderTM, String> colOrderStatus;
    @FXML private TableColumn<OrderTM, String> colOrderDate;
    @FXML private TextField        searchOrderField;
    @FXML private ComboBox<String> filterStatusComboBox;

    // Data lists
    private ObservableList<OrderItemTM> cartList = FXCollections.observableArrayList();
    private ObservableList<OrderTM> ordersList = FXCollections.observableArrayList();

    // Map customer combo string to customer ID
    private List<Integer> customerIdList = new ArrayList<>();

    // Running totals
    private double currentSubtotal = 0.0;
    private double currentNetTotal = 0.0;

    // ============================================================
    // INITIALIZE
    // ============================================================
    @FXML
    public void initialize() {
        // Setup Order Items table columns
        colItemPrintType.setCellValueFactory(new PropertyValueFactory<>("printType"));
        colItemName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colItemMaterial.setCellValueFactory(new PropertyValueFactory<>("substrateMaterial"));
        colItemQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colItemUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colItemLineTotal.setCellValueFactory(new PropertyValueFactory<>("lineTotal"));
        colItemSpecs.setCellValueFactory(new PropertyValueFactory<>("specifications"));
        itemsTable.setItems(cartList);

        // Setup Orders History table columns
        colOrderNumber.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        colOrderQr.setCellValueFactory(new PropertyValueFactory<>("qrCodeToken"));
        colOrderCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colOrderPhone.setCellValueFactory(new PropertyValueFactory<>("customerPhone"));
        colOrderTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colOrderPayment.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        colOrderStatus.setCellValueFactory(new PropertyValueFactory<>("orderStatus"));
        colOrderDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        ordersTable.setItems(ordersList);

        // Populate dropdown options
        initDropdowns();

        // Load customers into dropdown
        loadCustomers();

        // Auto-generate next order number & QR token
        generateNextOrderNumber();

        // Default due date = today + 2 days
        dueDatePicker.setValue(LocalDate.now().plusDays(2));

        // Delivery checkbox toggle listener
        deliveryCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            deliveryCityField.setDisable(!newVal);
            deliveryAddressArea.setDisable(!newVal);
            deliveryFeeField.setDisable(!newVal);
            if (!newVal) {
                deliveryFeeField.setText("0.00");
            } else if (deliveryFeeField.getText().trim().equals("0.00") || deliveryFeeField.getText().trim().isEmpty()) {
                deliveryFeeField.setText("350.00");
            }
            recalculateTotals();
        });

        // Billing dynamic recalculation listeners
        discountField.textProperty().addListener((obs, oldVal, newVal) -> recalculateTotals());
        deliveryFeeField.textProperty().addListener((obs, oldVal, newVal) -> recalculateTotals());

        // Quick calculator listener
        cashPaidField.textProperty().addListener((obs, oldVal, newVal) -> calculateChange());

        // Customer selection listener (auto-fill delivery info)
        customerComboBox.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            int idx = newVal.intValue();
            if (idx >= 0 && idx < customerIdList.size()) {
                loadCustomerDetails(customerIdList.get(idx));
            }
        });

        // Load initial orders history
        loadAllOrders();

        // Setup live search on orders history
        setupOrdersLiveSearch();
    }

    // ============================================================
    // DROPDOWNS INITIALIZATION
    // ============================================================
    private void initDropdowns() {
        printTypeComboBox.setItems(FXCollections.observableArrayList(
                "3D_PRINTING", "DTF", "UV_FLATBED", "UV_ROTARY"
        ));
        printTypeComboBox.getSelectionModel().selectFirst();

        materialComboBox.setItems(FXCollections.observableArrayList(
                "Cotton Fabric", "PLA Plastic", "Resin", "Acrylic", "Wood",
                "Stainless Steel", "Glass", "Ceramic Mug", "Aluminum", "Composite Board"
        ));
        materialComboBox.getSelectionModel().selectFirst();

        paymentStatusComboBox.setItems(FXCollections.observableArrayList(
                "PAID", "PARTIAL", "UNPAID"
        ));
        paymentStatusComboBox.getSelectionModel().selectFirst();

        filterStatusComboBox.setItems(FXCollections.observableArrayList(
                "ALL", "PLACED", "DESIGNING", "PRINT_QUEUE", "PRINTING",
                "PRINTED_READY_FOR_PACKING", "PACKED", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELLED"
        ));
        filterStatusComboBox.getSelectionModel().selectFirst();
        filterStatusComboBox.setOnAction(e -> filterOrdersByStatus());
    }

    // ============================================================
    // LOAD CUSTOMERS INTO COMBOBOX
    // ============================================================
    private void loadCustomers() {
        customerComboBox.getItems().clear();
        customerIdList.clear();

        String sql = "SELECT customer_id, customer_code, full_name, phone FROM customers ORDER BY full_name ASC";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("customer_id");
                String code = rs.getString("customer_code");
                String name = rs.getString("full_name");
                String phone = rs.getString("phone");

                customerComboBox.getItems().add(code + " — " + name + " (" + phone + ")");
                customerIdList.add(id);
            }
            rs.close();
            ps.close();

            if (!customerComboBox.getItems().isEmpty()) {
                customerComboBox.getSelectionModel().selectFirst();
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load customers: " + e.getMessage());
        }
    }

    // ============================================================
    // AUTO-FILL CUSTOMER DELIVERY INFO UPON SELECTION
    // ============================================================
    private void loadCustomerDetails(int customerId) {
        String sql = "SELECT full_name, phone, address, city FROM customers WHERE customer_id = ?";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String name = rs.getString("full_name");
                String phone = rs.getString("phone");
                String address = rs.getString("address");
                String city = rs.getString("city");

                customerInfoLabel.setText("👤 " + name + " | 📞 " + phone);
                if (address != null && !address.isEmpty()) {
                    deliveryAddressArea.setText(address);
                }
                if (city != null && !city.isEmpty()) {
                    deliveryCityField.setText(city);
                }
            }
            rs.close();
            ps.close();

        } catch (SQLException e) {
            System.out.println("Error fetching customer details: " + e.getMessage());
        }
    }

    // ============================================================
    // AUTO GENERATE NEXT ORDER NUMBER & QR TOKEN
    // ============================================================
    private void generateNextOrderNumber() {
        int currentYear = LocalDate.now().getYear();
        String sql = "SELECT order_number FROM orders WHERE order_number LIKE 'ORD-" + currentYear + "-%' " +
                     "ORDER BY order_id DESC LIMIT 1";

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String lastCode = rs.getString("order_number"); // e.g. "ORD-2026-0002"
                String[] parts = lastCode.split("-");
                if (parts.length == 3) {
                    int nextNum = Integer.parseInt(parts[2]) + 1;
                    String newOrderNo = String.format("ORD-%d-%04d", currentYear, nextNum);
                    orderNumberField.setText(newOrderNo);
                    qrTokenLabel.setText("QR-" + newOrderNo);
                } else {
                    orderNumberField.setText("ORD-" + currentYear + "-0001");
                    qrTokenLabel.setText("QR-ORD-" + currentYear + "-0001");
                }
            } else {
                orderNumberField.setText("ORD-" + currentYear + "-0001");
                qrTokenLabel.setText("QR-ORD-" + currentYear + "-0001");
            }
            rs.close();
            ps.close();

        } catch (Exception e) {
            orderNumberField.setText("ORD-" + currentYear + "-0001");
            qrTokenLabel.setText("QR-ORD-" + currentYear + "-0001");
        }
    }

    // ============================================================
    // ADD ITEM TO ORDER CART
    // ============================================================
    @FXML
    private void handleAddItem() {
        String printType = printTypeComboBox.getValue();
        String itemName  = itemNameField.getText().trim();
        String material  = materialComboBox.getValue();
        String qtyStr    = quantityField.getText().trim();
        String priceStr  = unitPriceField.getText().trim();
        String specs     = specificationsField.getText().trim();

        if (itemName.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Item Required", "Please enter the item name (e.g. Custom T-Shirt, Water Bottle).");
            itemNameField.requestFocus();
            return;
        }

        int quantity = 1;
        try {
            quantity = Integer.parseInt(qtyStr);
            if (quantity <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Invalid Quantity", "Please enter a valid positive integer quantity.");
            quantityField.requestFocus();
            return;
        }

        double unitPrice = 0.0;
        try {
            unitPrice = Double.parseDouble(priceStr);
            if (unitPrice < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Invalid Unit Price", "Please enter a valid unit price in LKR.");
            unitPriceField.requestFocus();
            return;
        }

        double lineTotal = quantity * unitPrice;

        // Add to cart list
        cartList.add(new OrderItemTM(printType, itemName, material, quantity, unitPrice, lineTotal, specs));

        // Reset item fields
        itemNameField.clear();
        quantityField.setText("1");
        unitPriceField.clear();
        specificationsField.clear();

        // Recalculate bill totals
        recalculateTotals();
    }

    // ============================================================
    // REMOVE SELECTED ITEM FROM CART
    // ============================================================
    @FXML
    private void handleRemoveItem() {
        OrderItemTM selected = itemsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            cartList.remove(selected);
            recalculateTotals();
        } else {
            showAlert(Alert.AlertType.WARNING, "No Item Selected", "Please select an item from the cart table to remove.");
        }
    }

    // ============================================================
    // RECALCULATE TOTALS (SUBTOTAL, DISCOUNT, DELIVERY, NET TOTAL)
    // ============================================================
    private void recalculateTotals() {
        currentSubtotal = 0.0;
        for (OrderItemTM item : cartList) {
            currentSubtotal += item.getLineTotal();
        }

        double discount = 0.0;
        try {
            String discStr = discountField.getText().trim();
            if (!discStr.isEmpty()) {
                discount = Double.parseDouble(discStr);
            }
        } catch (NumberFormatException ignored) {}

        double delivery = 0.0;
        if (deliveryCheckBox.isSelected()) {
            try {
                String delStr = deliveryFeeField.getText().trim();
                if (!delStr.isEmpty()) {
                    delivery = Double.parseDouble(delStr);
                }
            } catch (NumberFormatException ignored) {}
        }

        currentNetTotal = Math.max(0, currentSubtotal - discount + delivery);

        subtotalLabel.setText("LKR " + String.format("%,.2f", currentSubtotal));
        netTotalLabel.setText("LKR " + String.format("%,.2f", currentNetTotal));

        // Update change calculation if cash entered
        calculateChange();
    }

    // ============================================================
    // BUILT-IN CALCULATOR: CASH PAID & CHANGE
    // ============================================================
    private void calculateChange() {
        String cashStr = cashPaidField.getText().trim();
        if (cashStr.isEmpty()) {
            balanceChangeLabel.setText("LKR 0.00");
            balanceChangeLabel.setStyle("-fx-text-fill: #15171F; -fx-font-weight: 700; -fx-font-size: 15px;");
            return;
        }

        try {
            double cash = Double.parseDouble(cashStr);
            double change = cash - currentNetTotal;
            if (change >= 0) {
                balanceChangeLabel.setText("Change: LKR " + String.format("%,.2f", change));
                balanceChangeLabel.setStyle("-fx-text-fill: #1A7A46; -fx-font-weight: 700; -fx-font-size: 15px;");
            } else {
                balanceChangeLabel.setText("Due: LKR " + String.format("%,.2f", Math.abs(change)));
                balanceChangeLabel.setStyle("-fx-text-fill: #B8134F; -fx-font-weight: 700; -fx-font-size: 15px;");
            }
        } catch (NumberFormatException e) {
            balanceChangeLabel.setText("Invalid amount");
            balanceChangeLabel.setStyle("-fx-text-fill: #B8134F; -fx-font-size: 13px;");
        }
    }

    // ============================================================
    // PLACE ORDER & GENERATE BILL (DB TRANSACTION)
    // ============================================================
    @FXML
    private void handlePlaceOrder() {
        int selectedCustomerIdx = customerComboBox.getSelectionModel().getSelectedIndex();
        if (selectedCustomerIdx < 0 || selectedCustomerIdx >= customerIdList.size()) {
            showAlert(Alert.AlertType.WARNING, "Customer Required", "Please select a registered customer.");
            return;
        }

        if (cartList.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Items", "Please add at least one print item to the order.");
            itemNameField.requestFocus();
            return;
        }

        int customerId = customerIdList.get(selectedCustomerIdx);
        String orderNumber = orderNumberField.getText().trim();
        String qrToken = qrTokenLabel.getText().trim();
        LocalDate dueDate = dueDatePicker.getValue() != null ? dueDatePicker.getValue() : LocalDate.now().plusDays(2);
        boolean deliveryReq = deliveryCheckBox.isSelected();
        String deliveryCity = deliveryCityField.getText().trim();
        String deliveryAddr = deliveryAddressArea.getText().trim();

        double discount = 0.0;
        try {
            discount = Double.parseDouble(discountField.getText().trim());
        } catch (Exception ignored) {}

        double deliveryFee = 0.0;
        if (deliveryReq) {
            try {
                deliveryFee = Double.parseDouble(deliveryFeeField.getText().trim());
            } catch (Exception ignored) {}
        }

        String paymentStatus = paymentStatusComboBox.getValue();
        int userId = Session.getCurrentUser() != null ? Session.getCurrentUser().getUserId() : 1;

        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false); // Begin database transaction

            // 1. Insert into orders table
            String orderSql = "INSERT INTO orders (order_number, qr_code_token, customer_id, created_by, " +
                              "order_status, delivery_required, delivery_address, delivery_city, " +
                              "subtotal_amount, discount_amount, delivery_fee, total_amount, payment_status, due_date) " +
                              "VALUES (?, ?, ?, ?, 'PLACED', ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement psOrder = con.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS);
            psOrder.setString(1, orderNumber);
            psOrder.setString(2, qrToken);
            psOrder.setInt(3, customerId);
            psOrder.setInt(4, userId);
            psOrder.setBoolean(5, deliveryReq);
            psOrder.setString(6, deliveryAddr.isEmpty() ? null : deliveryAddr);
            psOrder.setString(7, deliveryCity.isEmpty() ? null : deliveryCity);
            psOrder.setDouble(8, currentSubtotal);
            psOrder.setDouble(9, discount);
            psOrder.setDouble(10, deliveryFee);
            psOrder.setDouble(11, currentNetTotal);
            psOrder.setString(12, paymentStatus);
            psOrder.setDate(13, Date.valueOf(dueDate));

            psOrder.executeUpdate();
            ResultSet rsKeys = psOrder.getGeneratedKeys();
            rsKeys.next();
            int orderId = rsKeys.getInt(1);
            rsKeys.close();
            psOrder.close();

            // 2. Insert line items into order_items table
            String itemSql = "INSERT INTO order_items (order_id, print_type, item_name, substrate_material, " +
                             "quantity, unit_price, line_total, specifications) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement psItem = con.prepareStatement(itemSql);
            for (OrderItemTM item : cartList) {
                psItem.setInt(1, orderId);
                psItem.setString(2, item.getPrintType());
                psItem.setString(3, item.getItemName());
                psItem.setString(4, item.getSubstrateMaterial());
                psItem.setInt(5, item.getQuantity());
                psItem.setDouble(6, item.getUnitPrice());
                psItem.setDouble(7, item.getLineTotal());
                psItem.setString(8, item.getSpecifications().isEmpty() ? null : item.getSpecifications());
                psItem.addBatch();
            }
            psItem.executeBatch();
            psItem.close();

            // Commit transaction
            con.commit();
            con.setAutoCommit(true);

            // Show Bill / Receipt Preview Dialog
            showReceiptPreview(orderNumber, qrToken, customerComboBox.getValue(), paymentStatus);

            // Reset order inputs
            handleResetOrder();

            // Refresh history table
            loadAllOrders();

        } catch (SQLException e) {
            if (con != null) {
                try { con.rollback(); con.setAutoCommit(true); } catch (SQLException ignored) {}
            }
            showAlert(Alert.AlertType.ERROR, "Order Placement Failed", "Database error: " + e.getMessage());
        }
    }

    // ============================================================
    // BILL / RECEIPT PREVIEW DIALOG WITH QR CODE SPEC
    // ============================================================
    private void showReceiptPreview(String orderNumber, String qrToken, String customerStr, String payStatus) {
        Stage receiptStage = new Stage();
        receiptStage.initModality(Modality.APPLICATION_MODAL);
        receiptStage.setTitle("DC Royal Printing — Bill & QR Receipt (" + orderNumber + ")");

        VBox root = new VBox(14);
        root.setPadding(new Insets(26));
        root.setStyle("-fx-background-color: white; -fx-font-family: 'Segoe UI', sans-serif;");
        root.setAlignment(Pos.TOP_CENTER);
        root.setPrefWidth(440);

        // Header
        Label brand = new Label("DC ROYAL PRINTING");
        brand.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #14161F;");

        Label sub = new Label("Official Customer Invoice & QR Bill");
        sub.setStyle("-fx-font-size: 11px; -fx-text-fill: #62656F; -fx-font-weight: 600;");

        Separator sep1 = new Separator();

        // Meta info
        VBox meta = new VBox(4);
        meta.getChildren().addAll(
            new Label("Invoice No  : " + orderNumber),
            new Label("QR Token    : " + qrToken),
            new Label("Customer    : " + customerStr),
            new Label("Date        : " + LocalDate.now().toString()),
            new Label("Payment     : " + payStatus)
        );
        meta.setStyle("-fx-font-size: 12px; -fx-text-fill: #15171F;");

        Separator sep2 = new Separator();

        // Items summary list
        VBox itemsBox = new VBox(6);
        itemsBox.getChildren().add(new Label("ITEMS ORDERED:"));
        for (OrderItemTM it : cartList) {
            HBox row = new HBox(10);
            Label nameLbl = new Label(it.getQuantity() + "x " + it.getItemName() + " (" + it.getPrintType() + ")");
            Label priceLbl = new Label("LKR " + String.format("%,.2f", it.getLineTotal()));
            nameLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #15171F;");
            priceLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #15171F;");
            HBox.setHgrow(nameLbl, Priority.ALWAYS);
            row.getChildren().addAll(nameLbl, priceLbl);
            itemsBox.getChildren().add(row);
        }

        Separator sep3 = new Separator();

        // Totals summary
        VBox totalsBox = new VBox(4);
        totalsBox.getChildren().addAll(
            new Label("Subtotal  : LKR " + String.format("%,.2f", currentSubtotal)),
            new Label("Discount  : LKR " + String.format("%,.2f", discountField.getText().trim().isEmpty() ? 0.0 : Double.parseDouble(discountField.getText().trim()))),
            new Label("Delivery  : LKR " + String.format("%,.2f", deliveryFeeField.getText().trim().isEmpty() ? 0.0 : Double.parseDouble(deliveryFeeField.getText().trim()))),
            new Label("TOTAL     : LKR " + String.format("%,.2f", currentNetTotal))
        );
        totalsBox.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: #07768F;");

        // QR Code Simulated Badge
        VBox qrBox = new VBox(6);
        qrBox.setAlignment(Pos.CENTER);
        qrBox.setStyle("-fx-background-color: #F6F3EC; -fx-background-radius: 10; -fx-padding: 12;");
        Label qrIcon = new Label("📱 [ QR CODE ]");
        qrIcon.setStyle("-fx-font-size: 22px; -fx-font-weight: 700; -fx-text-fill: #14161F;");
        Label qrLabel = new Label(qrToken);
        qrLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: #07768F;");
        qrBox.getChildren().addAll(qrIcon, qrLabel);

        // Close Button
        Button closeBtn = new Button("✔  Done / Print Receipt");
        closeBtn.setMaxWidth(Double.MAX_VALUE);
        closeBtn.setPrefHeight(38);
        closeBtn.setStyle("-fx-background-color: #07768F; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> receiptStage.close());

        root.getChildren().addAll(brand, sub, sep1, meta, sep2, itemsBox, sep3, totalsBox, qrBox, closeBtn);

        Scene scene = new Scene(root);
        receiptStage.setScene(scene);
        receiptStage.showAndWait();
    }

    // ============================================================
    // RESET NEW ORDER FORM
    // ============================================================
    @FXML
    private void handleResetOrder() {
        cartList.clear();
        discountField.setText("0.00");
        deliveryCheckBox.setSelected(false);
        deliveryFeeField.setText("0.00");
        deliveryFeeField.setDisable(true);
        deliveryCityField.clear();
        deliveryCityField.setDisable(true);
        deliveryAddressArea.clear();
        deliveryAddressArea.setDisable(true);
        cashPaidField.clear();
        balanceChangeLabel.setText("LKR 0.00");

        generateNextOrderNumber();
        recalculateTotals();
    }

    // ============================================================
    // LOAD ALL ORDERS FOR HISTORY TABLE
    // ============================================================
    private void loadAllOrders() {
        ordersList.clear();
        String sql = "SELECT o.order_id, o.order_number, o.qr_code_token, c.full_name, c.phone, " +
                     "o.total_amount, o.payment_status, o.order_status, " +
                     "DATE_FORMAT(o.created_at, '%Y-%m-%d %H:%i') as created_date, " +
                     "COALESCE(o.delivery_city, 'Pickup') as city " +
                     "FROM orders o " +
                     "JOIN customers c ON o.customer_id = c.customer_id " +
                     "ORDER BY o.order_id DESC";

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                OrderTM tm = new OrderTM(
                        rs.getInt("order_id"),
                        rs.getString("order_number"),
                        rs.getString("qr_code_token"),
                        rs.getString("full_name"),
                        rs.getString("phone"),
                        rs.getDouble("total_amount"),
                        rs.getString("payment_status"),
                        rs.getString("order_status"),
                        rs.getString("created_date"),
                        rs.getString("city")
                );
                ordersList.add(tm);
            }
            rs.close();
            ps.close();

        } catch (SQLException e) {
            System.out.println("Failed to load orders history: " + e.getMessage());
        }
    }

    // ============================================================
    // ADVANCE ORDER STATUS (PLACED -> PRINT_QUEUE, etc.)
    // ============================================================
    @FXML
    private void handleAdvanceStatus() {
        OrderTM selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Order Selected", "Please select an order from the table to advance its status.");
            return;
        }

        String currentStatus = selected.getOrderStatus();
        String nextStatus;
        switch (currentStatus) {
            case "PLACED":                    nextStatus = "PRINT_QUEUE"; break;
            case "PRINT_QUEUE":               nextStatus = "PRINTING"; break;
            case "PRINTING":                  nextStatus = "PRINTED_READY_FOR_PACKING"; break;
            case "PRINTED_READY_FOR_PACKING": nextStatus = "PACKED"; break;
            case "PACKED":                    nextStatus = "OUT_FOR_DELIVERY"; break;
            case "OUT_FOR_DELIVERY":          nextStatus = "DELIVERED"; break;
            default:
                showAlert(Alert.AlertType.INFORMATION, "Status Complete", "Order is already at status: " + currentStatus);
                return;
        }

        String sql = "UPDATE orders SET order_status = ? WHERE order_id = ?";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, nextStatus);
            ps.setInt(2, selected.getOrderId());
            int rows = ps.executeUpdate();
            ps.close();

            if (rows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Status Updated",
                        "Order " + selected.getOrderNumber() + " advanced to: " + nextStatus);
                loadAllOrders();
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update status: " + e.getMessage());
        }
    }

    // ============================================================
    // FILTER ORDERS BY STATUS
    // ============================================================
    private void filterOrdersByStatus() {
        String filter = filterStatusComboBox.getValue();
        if (filter == null || filter.equals("ALL")) {
            ordersTable.setItems(ordersList);
            return;
        }

        ObservableList<OrderTM> filtered = FXCollections.observableArrayList();
        for (OrderTM o : ordersList) {
            if (o.getOrderStatus().equalsIgnoreCase(filter)) {
                filtered.add(o);
            }
        }
        ordersTable.setItems(filtered);
    }

    // ============================================================
    // LIVE SEARCH FILTER ON ORDERS HISTORY
    // ============================================================
    private void setupOrdersLiveSearch() {
        FilteredList<OrderTM> filteredData = new FilteredList<>(ordersList, p -> true);

        searchOrderField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(order -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }
                String filter = newValue.toLowerCase().trim();
                return order.getOrderNumber().toLowerCase().contains(filter) ||
                       order.getQrCodeToken().toLowerCase().contains(filter) ||
                       order.getCustomerName().toLowerCase().contains(filter) ||
                       order.getCustomerPhone().contains(filter);
            });
        });

        SortedList<OrderTM> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(ordersTable.comparatorProperty());
        ordersTable.setItems(sortedData);
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
