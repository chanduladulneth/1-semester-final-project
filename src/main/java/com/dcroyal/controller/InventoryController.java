package com.dcroyal.controller;

import com.dcroyal.db.DBConnection;
import com.dcroyal.tm.InventoryItemTM;
import com.dcroyal.tm.QrInventoryLogTM;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * InventoryController.java
 * Handles inventory and stock operations:
 * - Stock items CRUD with low-stock warnings
 * - Total stock valuation & KPI metrics
 * - Physical QR card scanning/validation for Printer Operators
 * - Stock deduction and audit logging via QR card access
 */
public class InventoryController {

    // ---- FXML KPI Summary Labels ----
    @FXML private Label totalItemsCountLabel;
    @FXML private Label lowStockCountLabel;
    @FXML private Label stockOkCountLabel;
    @FXML private Label totalStockValueLabel;

    // ---- FXML TAB 1: Stock Item Form ----
    @FXML private TextField        skuCodeField;
    @FXML private TextField        itemNameField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private ComboBox<String> unitComboBox;
    @FXML private TextField        currentStockField;
    @FXML private TextField        reorderLevelField;
    @FXML private TextField        unitCostField;

    @FXML private Button saveButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button clearButton;

    // TAB 1 Table & Filters
    @FXML private TableView<InventoryItemTM> inventoryTable;
    @FXML private TableColumn<InventoryItemTM, String> colSku;
    @FXML private TableColumn<InventoryItemTM, String> colName;
    @FXML private TableColumn<InventoryItemTM, String> colCategory;
    @FXML private TableColumn<InventoryItemTM, Double> colStock;
    @FXML private TableColumn<InventoryItemTM, String> colUnit;
    @FXML private TableColumn<InventoryItemTM, Double> colReorder;
    @FXML private TableColumn<InventoryItemTM, Double> colUnitCost;
    @FXML private TableColumn<InventoryItemTM, String> colStatus;

    @FXML private TextField        searchField;
    @FXML private ComboBox<String> filterCategoryComboBox;

    // ---- FXML TAB 2: QR Card Stock Issue & Logs ----
    @FXML private TextField        cardQrField;
    @FXML private Label            cardValidationLabel;
    @FXML private ComboBox<String> issueItemComboBox;
    @FXML private TextField        issueQuantityField;
    @FXML private ComboBox<String> issueOrderComboBox;
    @FXML private TextField        issueRemarksField;
    @FXML private Button           issueStockButton;

    @FXML private TableView<QrInventoryLogTM> qrLogsTable;
    @FXML private TableColumn<QrInventoryLogTM, Integer> colLogId;
    @FXML private TableColumn<QrInventoryLogTM, String>  colLogOperator;
    @FXML private TableColumn<QrInventoryLogTM, String>  colLogCard;
    @FXML private TableColumn<QrInventoryLogTM, String>  colLogItem;
    @FXML private TableColumn<QrInventoryLogTM, Double>  colLogQty;
    @FXML private TableColumn<QrInventoryLogTM, String>  colLogUnit;
    @FXML private TableColumn<QrInventoryLogTM, String>  colLogOrder;
    @FXML private TableColumn<QrInventoryLogTM, String>  colLogDate;
    @FXML private TableColumn<QrInventoryLogTM, String>  colLogRemarks;

    // Data lists
    private ObservableList<InventoryItemTM> itemList = FXCollections.observableArrayList();
    private ObservableList<QrInventoryLogTM> logList = FXCollections.observableArrayList();

    // Mapping lists for Issue combos
    private List<Integer> issueItemIdList = new ArrayList<>();
    private List<Integer> issueOrderIdList = new ArrayList<>();

    // Validated QR card info
    private Integer validatedCardId = null;
    private Integer validatedOperatorId = null;

    // Currently selected item
    private InventoryItemTM selectedItem = null;

    // ============================================================
    // INITIALIZE
    // ============================================================
    @FXML
    public void initialize() {
        // Link Inventory Table columns
        colSku.setCellValueFactory(new PropertyValueFactory<>("skuCode"));
        colName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("currentStock"));
        colUnit.setCellValueFactory(new PropertyValueFactory<>("unitOfMeasure"));
        colReorder.setCellValueFactory(new PropertyValueFactory<>("reorderLevel"));
        colUnitCost.setCellValueFactory(new PropertyValueFactory<>("unitCost"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("stockStatus"));
        inventoryTable.setItems(itemList);

        // Link QR Logs Table columns
        colLogId.setCellValueFactory(new PropertyValueFactory<>("logId"));
        colLogOperator.setCellValueFactory(new PropertyValueFactory<>("operatorName"));
        colLogCard.setCellValueFactory(new PropertyValueFactory<>("cardQrCode"));
        colLogItem.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colLogQty.setCellValueFactory(new PropertyValueFactory<>("quantityTaken"));
        colLogUnit.setCellValueFactory(new PropertyValueFactory<>("unitOfMeasure"));
        colLogOrder.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        colLogDate.setCellValueFactory(new PropertyValueFactory<>("takenAt"));
        colLogRemarks.setCellValueFactory(new PropertyValueFactory<>("remarks"));
        qrLogsTable.setItems(logList);

        // Initialize form dropdowns
        initDropdowns();

        // Load summary KPIs & tables
        loadInventoryKpis();
        loadAllInventoryItems();
        loadQrLogs();

        // Load items & orders into QR issue dropdowns
        loadIssueDropdowns();

        // Table selection listener
        inventoryTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedItem = newVal;
            if (newVal != null) {
                populateForm(newVal);
            }
        });

        // Setup live search on inventory table
        setupLiveSearch();
    }

    // ============================================================
    // INITIALIZE DROPDOWNS
    // ============================================================
    private void initDropdowns() {
        categoryComboBox.setItems(FXCollections.observableArrayList(
                "FILAMENT_RESIN", "DTF_FILM_INK", "UV_INK", "SUBSTRATE_BLANK", "PACKAGING", "OTHER"
        ));
        categoryComboBox.getSelectionModel().selectFirst();

        unitComboBox.setItems(FXCollections.observableArrayList(
                "PCS", "ROLL", "BOTTLE", "KG", "LITER", "PACK", "BOX"
        ));
        unitComboBox.getSelectionModel().selectFirst();

        filterCategoryComboBox.setItems(FXCollections.observableArrayList(
                "ALL CATEGORIES", "FILAMENT_RESIN", "DTF_FILM_INK", "UV_INK", "SUBSTRATE_BLANK", "PACKAGING", "OTHER"
        ));
        filterCategoryComboBox.getSelectionModel().selectFirst();
        filterCategoryComboBox.setOnAction(e -> filterByCategory());
    }

    // ============================================================
    // LOAD SUMMARY KPIS
    // ============================================================
    private void loadInventoryKpis() {
        try {
            Connection con = DBConnection.getConnection();

            // Total Items
            String sql1 = "SELECT COUNT(*) FROM inventory_items";
            PreparedStatement ps1 = con.prepareStatement(sql1);
            ResultSet rs1 = ps1.executeQuery();
            rs1.next();
            totalItemsCountLabel.setText(String.valueOf(rs1.getInt(1)));
            rs1.close(); ps1.close();

            // Low Stock Items
            String sql2 = "SELECT COUNT(*) FROM inventory_items WHERE current_stock <= reorder_level";
            PreparedStatement ps2 = con.prepareStatement(sql2);
            ResultSet rs2 = ps2.executeQuery();
            rs2.next();
            lowStockCountLabel.setText(String.valueOf(rs2.getInt(1)));
            rs2.close(); ps2.close();

            // Stock OK Items
            String sql3 = "SELECT COUNT(*) FROM inventory_items WHERE current_stock > reorder_level";
            PreparedStatement ps3 = con.prepareStatement(sql3);
            ResultSet rs3 = ps3.executeQuery();
            rs3.next();
            stockOkCountLabel.setText(String.valueOf(rs3.getInt(1)));
            rs3.close(); ps3.close();

            // Total Stock Valuation
            String sql4 = "SELECT COALESCE(SUM(current_stock * unit_cost), 0) FROM inventory_items";
            PreparedStatement ps4 = con.prepareStatement(sql4);
            ResultSet rs4 = ps4.executeQuery();
            rs4.next();
            totalStockValueLabel.setText("LKR " + String.format("%,.0f", rs4.getDouble(1)));
            rs4.close(); ps4.close();

        } catch (SQLException e) {
            System.out.println("Error loading inventory KPIs: " + e.getMessage());
        }
    }

    // ============================================================
    // LOAD ALL INVENTORY ITEMS
    // ============================================================
    private void loadAllInventoryItems() {
        itemList.clear();
        String sql = "SELECT item_id, sku_code, item_name, category, current_stock, " +
                     "unit_of_measure, reorder_level, unit_cost FROM inventory_items ORDER BY item_id DESC";

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                double stock = rs.getDouble("current_stock");
                double reorder = rs.getDouble("reorder_level");
                String status = stock <= reorder ? "LOW STOCK" : "OK";

                InventoryItemTM tm = new InventoryItemTM(
                        rs.getInt("item_id"),
                        rs.getString("sku_code"),
                        rs.getString("item_name"),
                        rs.getString("category"),
                        stock,
                        rs.getString("unit_of_measure"),
                        reorder,
                        rs.getDouble("unit_cost"),
                        status
                );
                itemList.add(tm);
            }
            rs.close(); ps.close();

        } catch (SQLException e) {
            System.out.println("Error loading inventory items: " + e.getMessage());
        }
    }

    // ============================================================
    // LOAD QR INVENTORY LOGS
    // ============================================================
    private void loadQrLogs() {
        logList.clear();
        String sql = "SELECT l.log_id, u.full_name as operator_name, q.card_qr_code, " +
                     "i.item_name, l.quantity_taken, i.unit_of_measure, " +
                     "COALESCE(o.order_number, 'Workshop General') as order_number, " +
                     "COALESCE(l.remarks, '-') as remarks, " +
                     "DATE_FORMAT(l.taken_at, '%Y-%m-%d %H:%i') as taken_time " +
                     "FROM qr_inventory_logs l " +
                     "JOIN qr_access_cards q ON l.card_id = q.card_id " +
                     "JOIN users u ON l.operator_id = u.user_id " +
                     "JOIN inventory_items i ON l.item_id = i.item_id " +
                     "LEFT JOIN orders o ON l.order_id = o.order_id " +
                     "ORDER BY l.log_id DESC";

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                QrInventoryLogTM tm = new QrInventoryLogTM(
                        rs.getInt("log_id"),
                        rs.getString("operator_name"),
                        rs.getString("card_qr_code"),
                        rs.getString("item_name"),
                        rs.getDouble("quantity_taken"),
                        rs.getString("unit_of_measure"),
                        rs.getString("order_number"),
                        rs.getString("remarks"),
                        rs.getString("taken_time")
                );
                logList.add(tm);
            }
            rs.close(); ps.close();

        } catch (SQLException e) {
            System.out.println("Error loading QR logs: " + e.getMessage());
        }
    }

    // ============================================================
    // LOAD DROPDOWNS FOR QR ISSUE FORM
    // ============================================================
    private void loadIssueDropdowns() {
        issueItemComboBox.getItems().clear();
        issueItemIdList.clear();
        issueOrderComboBox.getItems().clear();
        issueOrderIdList.clear();

        try {
            Connection con = DBConnection.getConnection();

            // Load items with current stock info
            String sql1 = "SELECT item_id, item_name, current_stock, unit_of_measure FROM inventory_items ORDER BY item_name ASC";
            PreparedStatement ps1 = con.prepareStatement(sql1);
            ResultSet rs1 = ps1.executeQuery();
            while (rs1.next()) {
                issueItemIdList.add(rs1.getInt("item_id"));
                issueItemComboBox.getItems().add(
                        rs1.getString("item_name") + " (" + rs1.getDouble("current_stock") + " " + rs1.getString("unit_of_measure") + " avail)"
                );
            }
            rs1.close(); ps1.close();

            if (!issueItemComboBox.getItems().isEmpty()) {
                issueItemComboBox.getSelectionModel().selectFirst();
            }

            // Load active orders
            issueOrderComboBox.getItems().add("General Workshop / No Order");
            issueOrderIdList.add(null);

            String sql2 = "SELECT order_id, order_number FROM orders WHERE order_status IN ('PRINT_QUEUE', 'PRINTING') ORDER BY order_id DESC";
            PreparedStatement ps2 = con.prepareStatement(sql2);
            ResultSet rs2 = ps2.executeQuery();
            while (rs2.next()) {
                issueOrderIdList.add(rs2.getInt("order_id"));
                issueOrderComboBox.getItems().add(rs2.getString("order_number"));
            }
            rs2.close(); ps2.close();

            issueOrderComboBox.getSelectionModel().selectFirst();

        } catch (SQLException e) {
            System.out.println("Error loading issue dropdowns: " + e.getMessage());
        }
    }

    // ============================================================
    // POPULATE FORM FIELDS ON ROW CLICK
    // ============================================================
    private void populateForm(InventoryItemTM tm) {
        skuCodeField.setText(tm.getSkuCode());
        itemNameField.setText(tm.getItemName());
        categoryComboBox.setValue(tm.getCategory());
        unitComboBox.setValue(tm.getUnitOfMeasure());
        currentStockField.setText(String.valueOf(tm.getCurrentStock()));
        reorderLevelField.setText(String.valueOf(tm.getReorderLevel()));
        unitCostField.setText(String.valueOf(tm.getUnitCost()));

        saveButton.setDisable(true);
        updateButton.setDisable(false);
        deleteButton.setDisable(false);
    }

    // ============================================================
    // SAVE NEW INVENTORY ITEM
    // ============================================================
    @FXML
    private void handleSaveItem() {
        String sku      = skuCodeField.getText().trim();
        String name     = itemNameField.getText().trim();
        String category = categoryComboBox.getValue();
        String unit     = unitComboBox.getValue();
        String stockStr = currentStockField.getText().trim();
        String reordStr = reorderLevelField.getText().trim();
        String costStr  = unitCostField.getText().trim();

        if (sku.isEmpty() || name.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "SKU Code and Item Name are required.");
            return;
        }

        double stock = 0.0;
        double reorder = 10.0;
        double cost = 0.0;
        try {
            if (!stockStr.isEmpty()) stock = Double.parseDouble(stockStr);
            if (!reordStr.isEmpty()) reorder = Double.parseDouble(reordStr);
            if (!costStr.isEmpty())  cost = Double.parseDouble(costStr);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Invalid Numbers", "Please check stock, reorder level, and unit cost.");
            return;
        }

        String sql = "INSERT INTO inventory_items (sku_code, item_name, category, unit_of_measure, " +
                     "current_stock, reorder_level, unit_cost) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, sku);
            ps.setString(2, name);
            ps.setString(3, category);
            ps.setString(4, unit);
            ps.setDouble(5, stock);
            ps.setDouble(6, reorder);
            ps.setDouble(7, cost);

            int rows = ps.executeUpdate();
            ps.close();

            if (rows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Inventory item " + name + " saved successfully!");
                handleClear();
                loadInventoryKpis();
                loadAllInventoryItems();
                loadIssueDropdowns();
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save item: " + e.getMessage());
        }
    }

    // ============================================================
    // UPDATE INVENTORY ITEM
    // ============================================================
    @FXML
    private void handleUpdateItem() {
        if (selectedItem == null) return;

        String sku      = skuCodeField.getText().trim();
        String name     = itemNameField.getText().trim();
        String category = categoryComboBox.getValue();
        String unit     = unitComboBox.getValue();
        String stockStr = currentStockField.getText().trim();
        String reordStr = reorderLevelField.getText().trim();
        String costStr  = unitCostField.getText().trim();

        if (sku.isEmpty() || name.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "SKU Code and Item Name are required.");
            return;
        }

        double stock = 0.0;
        double reorder = 10.0;
        double cost = 0.0;
        try {
            if (!stockStr.isEmpty()) stock = Double.parseDouble(stockStr);
            if (!reordStr.isEmpty()) reorder = Double.parseDouble(reordStr);
            if (!costStr.isEmpty())  cost = Double.parseDouble(costStr);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Invalid Numbers", "Please check stock, reorder level, and unit cost.");
            return;
        }

        String sql = "UPDATE inventory_items SET sku_code = ?, item_name = ?, category = ?, " +
                     "unit_of_measure = ?, current_stock = ?, reorder_level = ?, unit_cost = ? " +
                     "WHERE item_id = ?";

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, sku);
            ps.setString(2, name);
            ps.setString(3, category);
            ps.setString(4, unit);
            ps.setDouble(5, stock);
            ps.setDouble(6, reorder);
            ps.setDouble(7, cost);
            ps.setInt(8, selectedItem.getItemId());

            int rows = ps.executeUpdate();
            ps.close();

            if (rows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Updated", "Item updated successfully!");
                handleClear();
                loadInventoryKpis();
                loadAllInventoryItems();
                loadIssueDropdowns();
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update item: " + e.getMessage());
        }
    }

    // ============================================================
    // DELETE INVENTORY ITEM
    // ============================================================
    @FXML
    private void handleDeleteItem() {
        if (selectedItem == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete item: " + selectedItem.getItemName());
        confirm.setContentText("Are you sure you want to permanently delete this item?");

        Optional<ButtonType> res = confirm.showAndWait();
        if (!res.isPresent() || res.get() != ButtonType.OK) return;

        String sql = "DELETE FROM inventory_items WHERE item_id = ?";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, selectedItem.getItemId());
            int rows = ps.executeUpdate();
            ps.close();

            if (rows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Deleted", "Item removed successfully.");
                handleClear();
                loadInventoryKpis();
                loadAllInventoryItems();
                loadIssueDropdowns();
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Cannot delete item (it may have log records): " + e.getMessage());
        }
    }

    // ============================================================
    // CLEAR / RESET FORM
    // ============================================================
    @FXML
    private void handleClear() {
        skuCodeField.clear();
        itemNameField.clear();
        currentStockField.clear();
        reorderLevelField.setText("10.00");
        unitCostField.clear();

        saveButton.setDisable(false);
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
        selectedItem = null;
        inventoryTable.getSelectionModel().clearSelection();
    }

    // ============================================================
    // TAB 2: VALIDATE QR ACCESS CARD
    // ============================================================
    @FXML
    private void handleValidateCard() {
        String cardCode = cardQrField.getText().trim();
        if (cardCode.isEmpty()) {
            cardValidationLabel.setText("❌ Please enter or scan a card QR token.");
            cardValidationLabel.setStyle("-fx-text-fill: #B8134F; -fx-font-weight: 700;");
            validatedCardId = null;
            validatedOperatorId = null;
            issueStockButton.setDisable(true);
            return;
        }

        String sql = "SELECT c.card_id, c.assigned_user_id, u.full_name, c.is_active " +
                     "FROM qr_access_cards c " +
                     "JOIN users u ON c.assigned_user_id = u.user_id " +
                     "WHERE c.card_qr_code = ?";

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, cardCode);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                boolean active = rs.getBoolean("is_active");
                if (active) {
                    validatedCardId = rs.getInt("card_id");
                    validatedOperatorId = rs.getInt("assigned_user_id");
                    String opName = rs.getString("full_name");

                    cardValidationLabel.setText("✔ Valid Card: " + opName + " (Authorized)");
                    cardValidationLabel.setStyle("-fx-text-fill: #1A7A46; -fx-font-weight: 700; -fx-font-size: 13px;");
                    issueStockButton.setDisable(false);
                } else {
                    cardValidationLabel.setText("❌ Access Denied: Card is inactive/revoked.");
                    cardValidationLabel.setStyle("-fx-text-fill: #B8134F; -fx-font-weight: 700;");
                    validatedCardId = null;
                    validatedOperatorId = null;
                    issueStockButton.setDisable(true);
                }
            } else {
                cardValidationLabel.setText("❌ Card Not Found: Invalid token.");
                cardValidationLabel.setStyle("-fx-text-fill: #B8134F; -fx-font-weight: 700;");
                validatedCardId = null;
                validatedOperatorId = null;
                issueStockButton.setDisable(true);
            }
            rs.close(); ps.close();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to validate card: " + e.getMessage());
        }
    }

    // ============================================================
    // TAB 2: ISSUE STOCK VIA QR CARD (ATOMIC DB TRANSACTION)
    // ============================================================
    @FXML
    private void handleIssueStock() {
        if (validatedCardId == null || validatedOperatorId == null) {
            showAlert(Alert.AlertType.WARNING, "Card Not Validated", "Please scan or enter and validate an active QR Access Card first.");
            return;
        }

        int itemIdx = issueItemComboBox.getSelectionModel().getSelectedIndex();
        if (itemIdx < 0 || itemIdx >= issueItemIdList.size()) {
            showAlert(Alert.AlertType.WARNING, "Item Required", "Please select an item to issue from inventory.");
            return;
        }
        int itemId = issueItemIdList.get(itemIdx);

        String qtyStr = issueQuantityField.getText().trim();
        double quantity = 0.0;
        try {
            quantity = Double.parseDouble(qtyStr);
            if (quantity <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Invalid Quantity", "Please enter a valid positive quantity to issue.");
            issueQuantityField.requestFocus();
            return;
        }

        int orderIdx = issueOrderComboBox.getSelectionModel().getSelectedIndex();
        Integer orderId = (orderIdx >= 0 && orderIdx < issueOrderIdList.size()) ? issueOrderIdList.get(orderIdx) : null;

        String remarks = issueRemarksField.getText().trim();
        int approverId = Session.getCurrentUser() != null ? Session.getCurrentUser().getUserId() : 1;

        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false); // Begin Transaction

            // 1. Check current stock
            String checkSql = "SELECT item_name, current_stock, unit_of_measure FROM inventory_items WHERE item_id = ? FOR UPDATE";
            PreparedStatement psCheck = con.prepareStatement(checkSql);
            psCheck.setInt(1, itemId);
            ResultSet rsCheck = psCheck.executeQuery();
            rsCheck.next();
            String itemName = rsCheck.getString("item_name");
            double currentStock = rsCheck.getDouble("current_stock");
            String unit = rsCheck.getString("unit_of_measure");
            rsCheck.close(); psCheck.close();

            if (currentStock < quantity) {
                con.rollback(); con.setAutoCommit(true);
                showAlert(Alert.AlertType.WARNING, "Insufficient Stock",
                        "Cannot issue " + quantity + " " + unit + " of " + itemName + ".\nAvailable stock: " + currentStock + " " + unit);
                return;
            }

            // 2. Deduct stock
            String deductSql = "UPDATE inventory_items SET current_stock = current_stock - ? WHERE item_id = ?";
            PreparedStatement psDeduct = con.prepareStatement(deductSql);
            psDeduct.setDouble(1, quantity);
            psDeduct.setInt(2, itemId);
            psDeduct.executeUpdate();
            psDeduct.close();

            // 3. Insert audit log
            String logSql = "INSERT INTO qr_inventory_logs (card_id, item_id, order_id, operator_id, " +
                            "approved_by, quantity_taken, remarks) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement psLog = con.prepareStatement(logSql);
            psLog.setInt(1, validatedCardId);
            psLog.setInt(2, itemId);
            if (orderId != null) psLog.setInt(3, orderId); else psLog.setNull(3, java.sql.Types.INTEGER);
            psLog.setInt(4, validatedOperatorId);
            psLog.setInt(5, approverId);
            psLog.setDouble(6, quantity);
            psLog.setString(7, remarks.isEmpty() ? null : remarks);
            psLog.executeUpdate();
            psLog.close();

            // Commit transaction
            con.commit();
            con.setAutoCommit(true);

            showAlert(Alert.AlertType.INFORMATION, "Stock Issued Successfully",
                    "Issued " + quantity + " " + unit + " of " + itemName + ".\n" +
                    "Remaining stock: " + (currentStock - quantity) + " " + unit + "\n" +
                    "Transaction logged in QR Audit Records.");

            // Reset inputs & refresh
            issueQuantityField.setText("1");
            issueRemarksField.clear();
            loadInventoryKpis();
            loadAllInventoryItems();
            loadQrLogs();
            loadIssueDropdowns();

        } catch (SQLException e) {
            if (con != null) {
                try { con.rollback(); con.setAutoCommit(true); } catch (SQLException ignored) {}
            }
            showAlert(Alert.AlertType.ERROR, "Stock Issue Failed", "Database error: " + e.getMessage());
        }
    }

    // ============================================================
    // FILTER CONTROLS (CATEGORY)
    // ============================================================
    private void filterByCategory() {
        String filter = filterCategoryComboBox.getValue();
        if (filter == null || filter.equals("ALL CATEGORIES")) {
            inventoryTable.setItems(itemList);
            return;
        }

        ObservableList<InventoryItemTM> filtered = FXCollections.observableArrayList();
        for (InventoryItemTM it : itemList) {
            if (it.getCategory().equalsIgnoreCase(filter)) {
                filtered.add(it);
            }
        }
        inventoryTable.setItems(filtered);
    }

    // ============================================================
    // LIVE SEARCH FILTER
    // ============================================================
    private void setupLiveSearch() {
        FilteredList<InventoryItemTM> filteredData = new FilteredList<>(itemList, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(item -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }
                String filter = newValue.toLowerCase().trim();
                return item.getSkuCode().toLowerCase().contains(filter) ||
                       item.getItemName().toLowerCase().contains(filter) ||
                       item.getCategory().toLowerCase().contains(filter);
            });
        });

        SortedList<InventoryItemTM> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(inventoryTable.comparatorProperty());
        inventoryTable.setItems(sortedData);
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
