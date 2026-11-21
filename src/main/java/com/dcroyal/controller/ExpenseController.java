package com.dcroyal.controller;

import com.dcroyal.db.DBConnection;
import com.dcroyal.model.User;
import com.dcroyal.tm.ExpenseTM;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * ExpenseController.java
 * Manages operational expense tracking, category breakdown,
 * Manager reimbursement submission, Owner approval, and bank fund transfer settlements with direct JDBC.
 */
public class ExpenseController {

    // ============================================================
    // FXML: TOP KPI SUMMARY LABELS
    // ============================================================
    @FXML private Label totalSettledExpensesLabel;
    @FXML private Label pendingOwnerCountLabel;
    @FXML private Label approvedCountLabel;
    @FXML private Label rejectedCountLabel;

    // ============================================================
    // FXML: TAB 1 - EXPENSE LEDGER & APPROVALS
    // ============================================================
    @FXML private ComboBox<String> categoryFilterComboBox;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private TextField        searchField;

    @FXML private TableView<ExpenseTM> expenseTable;
    @FXML private TableColumn<ExpenseTM, String> colCode;
    @FXML private TableColumn<ExpenseTM, String> colCategory;
    @FXML private TableColumn<ExpenseTM, String> colDescription;
    @FXML private TableColumn<ExpenseTM, String> colAmount;
    @FXML private TableColumn<ExpenseTM, String> colDate;
    @FXML private TableColumn<ExpenseTM, String> colStatus;
    @FXML private TableColumn<ExpenseTM, String> colTransferRef;
    @FXML private TableColumn<ExpenseTM, String> colSubmittedBy;
    @FXML private TableColumn<ExpenseTM, String> colApprovedBy;

    // Action Panel
    @FXML private Label  selectedCodeLabel;
    @FXML private Label  selectedCategoryLabel;
    @FXML private Label  selectedAmountLabel;
    @FXML private Label  selectedDescLabel;
    @FXML private Label  selectedRefLabel;
    @FXML private Button approveExpenseButton;
    @FXML private Button settleExpenseButton;
    @FXML private Button rejectExpenseButton;
    @FXML private Button printVoucherButton;

    // ============================================================
    // FXML: TAB 2 - RECORD & SUBMIT EXPENSE
    // ============================================================
    @FXML private TextField        expenseCodeField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private DatePicker       expenseDatePicker;
    @FXML private TextField        amountField;
    @FXML private TextField        descriptionField;
    @FXML private TextField        transferRefField;
    @FXML private ComboBox<String> statusComboBox;

    @FXML private Button saveExpenseButton;
    @FXML private Button clearFormButton;

    // Data lists
    private ObservableList<ExpenseTM> expenseList = FXCollections.observableArrayList();

    // Selected expense record
    private ExpenseTM selectedExpense = null;

    // ============================================================
    // INITIALIZE
    // ============================================================
    @FXML
    public void initialize() {
        // --- 1. Table Columns Setup ---
        colCode.setCellValueFactory(new PropertyValueFactory<>("expenseCode"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("expenseCategory"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amountFormatted"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("expenseDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("approvalStatus"));
        colTransferRef.setCellValueFactory(new PropertyValueFactory<>("transferReference"));
        colSubmittedBy.setCellValueFactory(new PropertyValueFactory<>("submittedByName"));
        colApprovedBy.setCellValueFactory(new PropertyValueFactory<>("approvedByName"));
        expenseTable.setItems(expenseList);

        // --- 2. Filter Dropdowns ---
        categoryFilterComboBox.setItems(FXCollections.observableArrayList(
                "ALL", "RAW_MATERIAL", "UTILITY_BILL", "MAINTENANCE", "DELIVERY", "SALARY_BATCH", "OTHER"
        ));
        categoryFilterComboBox.getSelectionModel().selectFirst();
        categoryFilterComboBox.setOnAction(e -> applyFilters());

        statusFilterComboBox.setItems(FXCollections.observableArrayList(
                "ALL", "PENDING_OWNER", "APPROVED", "SETTLED", "REJECTED"
        ));
        statusFilterComboBox.getSelectionModel().selectFirst();
        statusFilterComboBox.setOnAction(e -> applyFilters());

        // --- 3. Tab 2 Dropdowns & Defaults ---
        categoryComboBox.setItems(FXCollections.observableArrayList(
                "RAW_MATERIAL", "UTILITY_BILL", "MAINTENANCE", "DELIVERY", "SALARY_BATCH", "OTHER"
        ));
        categoryComboBox.getSelectionModel().selectFirst();

        expenseDatePicker.setValue(LocalDate.now());

        User currentUser = Session.getCurrentUser();
        boolean isOwner = (currentUser != null && currentUser.getRoleCode().equals("OWNER"));

        if (isOwner) {
            statusComboBox.setItems(FXCollections.observableArrayList("APPROVED", "SETTLED", "PENDING_OWNER"));
            statusComboBox.setValue("APPROVED");
        } else {
            statusComboBox.setItems(FXCollections.observableArrayList("PENDING_OWNER"));
            statusComboBox.setValue("PENDING_OWNER");
        }

        // --- 4. Selection Listener for Table ---
        expenseTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedExpense = newVal;
            updateActionPanel(newVal);
        });

        // --- 5. Initial Data Load ---
        loadExpenseKpis();
        loadAllExpenses();
        generateNextExpenseCode();

        // --- 6. Live Search ---
        setupLiveSearch();
    }

    // ============================================================
    // LOAD SUMMARY KPIS
    // ============================================================
    private void loadExpenseKpis() {
        try {
            Connection con = DBConnection.getConnection();

            // 1. Total Settled Expenses (LKR)
            String sql1 = "SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE approval_status = 'SETTLED'";
            PreparedStatement ps1 = con.prepareStatement(sql1);
            ResultSet rs1 = ps1.executeQuery();
            if (rs1.next()) {
                totalSettledExpensesLabel.setText(String.format("Rs. %,.2f", rs1.getDouble(1)));
            }
            rs1.close(); ps1.close();

            // 2. Pending Owner Approval
            String sql2 = "SELECT COUNT(*) FROM expenses WHERE approval_status = 'PENDING_OWNER'";
            PreparedStatement ps2 = con.prepareStatement(sql2);
            ResultSet rs2 = ps2.executeQuery();
            if (rs2.next()) {
                pendingOwnerCountLabel.setText(String.valueOf(rs2.getInt(1)));
            }
            rs2.close(); ps2.close();

            // 3. Approved / Awaiting Transfer
            String sql3 = "SELECT COUNT(*) FROM expenses WHERE approval_status = 'APPROVED'";
            PreparedStatement ps3 = con.prepareStatement(sql3);
            ResultSet rs3 = ps3.executeQuery();
            if (rs3.next()) {
                approvedCountLabel.setText(String.valueOf(rs3.getInt(1)));
            }
            rs3.close(); ps3.close();

            // 4. Rejected Claims
            String sql4 = "SELECT COUNT(*) FROM expenses WHERE approval_status = 'REJECTED'";
            PreparedStatement ps4 = con.prepareStatement(sql4);
            ResultSet rs4 = ps4.executeQuery();
            if (rs4.next()) {
                rejectedCountLabel.setText(String.valueOf(rs4.getInt(1)));
            }
            rs4.close(); ps4.close();

        } catch (SQLException e) {
            System.out.println("Error loading expense KPIs: " + e.getMessage());
        }
    }

    // ============================================================
    // LOAD ALL EXPENSES
    // ============================================================
    private void loadAllExpenses() {
        expenseList.clear();
        String sql = "SELECT e.expense_id, e.expense_code, e.expense_category, e.description, " +
                     "e.amount, DATE_FORMAT(e.expense_date, '%Y-%m-%d') as exp_date, " +
                     "e.approval_status, e.transfer_reference, " +
                     "COALESCE(sub.full_name, 'System') as sub_by, " +
                     "COALESCE(app.full_name, '-') as app_by " +
                     "FROM expenses e " +
                     "LEFT JOIN users sub ON e.submitted_by = sub.user_id " +
                     "LEFT JOIN users app ON e.approved_by = app.user_id " +
                     "ORDER BY e.expense_id DESC";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ExpenseTM tm = new ExpenseTM(
                        rs.getInt("expense_id"),
                        rs.getString("expense_code"),
                        rs.getString("expense_category"),
                        rs.getString("description"),
                        rs.getDouble("amount"),
                        rs.getString("exp_date"),
                        rs.getString("approval_status"),
                        rs.getString("transfer_reference"),
                        rs.getString("sub_by"),
                        rs.getString("app_by")
                );
                expenseList.add(tm);
            }
            rs.close(); ps.close();

        } catch (SQLException e) {
            System.out.println("Error loading expenses: " + e.getMessage());
        }
    }

    // ============================================================
    // AUTO-GENERATE NEXT EXPENSE CODE (EXP-0001)
    // ============================================================
    private void generateNextExpenseCode() {
        String sql = "SELECT MAX(expense_id) FROM expenses";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            int nextId = 1;
            if (rs.next()) {
                nextId = rs.getInt(1) + 1;
            }
            rs.close(); ps.close();

            expenseCodeField.setText(String.format("EXP-%04d", nextId));

        } catch (SQLException e) {
            expenseCodeField.setText("EXP-0001");
        }
    }

    // ============================================================
    // UPDATE ACTION PANEL ON ROW SELECTION
    // ============================================================
    private void updateActionPanel(ExpenseTM tm) {
        if (tm == null) {
            selectedCodeLabel.setText("No expense selected");
            selectedCategoryLabel.setText("Category: -");
            selectedAmountLabel.setText("Amount: -");
            selectedDescLabel.setText("Description: -");
            selectedRefLabel.setText("Bank Transfer Ref: -");
            approveExpenseButton.setDisable(true);
            settleExpenseButton.setDisable(true);
            rejectExpenseButton.setDisable(true);
            printVoucherButton.setDisable(true);
            return;
        }

        selectedCodeLabel.setText(tm.getExpenseCode() + " [" + tm.getApprovalStatus() + "]");
        selectedCategoryLabel.setText("Category: " + tm.getExpenseCategory());
        selectedAmountLabel.setText("Amount: " + tm.getAmountFormatted());
        selectedDescLabel.setText("Desc: " + tm.getDescription());
        selectedRefLabel.setText("Bank Transfer Ref: " + tm.getTransferReference());

        String status = tm.getApprovalStatus();
        approveExpenseButton.setDisable(!status.equals("PENDING_OWNER"));
        settleExpenseButton.setDisable(status.equals("SETTLED") || status.equals("REJECTED"));
        rejectExpenseButton.setDisable(status.equals("SETTLED") || status.equals("REJECTED"));
        printVoucherButton.setDisable(false);
    }

    // ============================================================
    // ACTION: APPROVE EXPENSE (OWNER ACTION)
    // ============================================================
    @FXML
    private void handleApproveExpense() {
        if (selectedExpense == null) return;

        User currentUser = Session.getCurrentUser();
        int approverId = (currentUser != null) ? currentUser.getUserId() : 1;

        String sql = "UPDATE expenses SET approval_status = 'APPROVED', approved_by = ? WHERE expense_id = ?";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, approverId);
            ps.setInt(2, selectedExpense.getExpenseId());
            ps.executeUpdate();
            ps.close();

            showAlert(Alert.AlertType.INFORMATION, "Expense Approved",
                    "Expense claim " + selectedExpense.getExpenseCode() + " has been APPROVED for disbursement!");

            loadExpenseKpis();
            loadAllExpenses();
            updateActionPanel(null);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to approve expense: " + e.getMessage());
        }
    }

    // ============================================================
    // ACTION: RECORD SETTLEMENT & BANK FUND TRANSFER
    // ============================================================
    @FXML
    private void handleSettleExpense() {
        if (selectedExpense == null) return;

        User currentUser = Session.getCurrentUser();
        int approverId = (currentUser != null) ? currentUser.getUserId() : 1;

        TextInputDialog dialog = new TextInputDialog("BOC-TR-" + System.currentTimeMillis() % 1000000);
        dialog.setTitle("Record Fund Transfer & Settlement");
        dialog.setHeaderText("Settlement for " + selectedExpense.getExpenseCode() + " (" + selectedExpense.getAmountFormatted() + ")");
        dialog.setContentText("Enter Bank Transfer Reference or Cash Voucher Number:");

        dialog.showAndWait().ifPresent(ref -> {
            String transferRef = ref.trim();
            if (transferRef.isEmpty()) {
                transferRef = "DIRECT-TRANSFER";
            }

            String sql = "UPDATE expenses SET approval_status = 'SETTLED', transfer_reference = ?, " +
                         "approved_by = COALESCE(approved_by, ?) WHERE expense_id = ?";
            try {
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, transferRef);
                ps.setInt(2, approverId);
                ps.setInt(3, selectedExpense.getExpenseId());
                ps.executeUpdate();
                ps.close();

                showAlert(Alert.AlertType.INFORMATION, "Expense Settled",
                        "Expense " + selectedExpense.getExpenseCode() + " marked as SETTLED!\n\n" +
                        "Bank Transfer Reference: " + transferRef);

                loadExpenseKpis();
                loadAllExpenses();
                updateActionPanel(null);

            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to settle expense: " + e.getMessage());
            }
        });
    }

    // ============================================================
    // ACTION: REJECT EXPENSE
    // ============================================================
    @FXML
    private void handleRejectExpense() {
        if (selectedExpense == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Rejection");
        confirm.setHeaderText("Reject Expense Claim " + selectedExpense.getExpenseCode());
        confirm.setContentText("Are you sure you want to REJECT this expense claim of " + selectedExpense.getAmountFormatted() + "?");

        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                User currentUser = Session.getCurrentUser();
                int approverId = (currentUser != null) ? currentUser.getUserId() : 1;

                String sql = "UPDATE expenses SET approval_status = 'REJECTED', approved_by = ? WHERE expense_id = ?";
                try {
                    Connection con = DBConnection.getConnection();
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setInt(1, approverId);
                    ps.setInt(2, selectedExpense.getExpenseId());
                    ps.executeUpdate();
                    ps.close();

                    showAlert(Alert.AlertType.INFORMATION, "Claim Rejected",
                            "Expense claim " + selectedExpense.getExpenseCode() + " has been rejected.");

                    loadExpenseKpis();
                    loadAllExpenses();
                    updateActionPanel(null);

                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to reject expense: " + e.getMessage());
                }
            }
        });
    }

    // ============================================================
    // ACTION: PRINT OFFICIAL PAYMENT DISBURSEMENT VOUCHER
    // ============================================================
    @FXML
    private void handlePrintVoucher() {
        if (selectedExpense == null) return;

        Stage voucherStage = new Stage();
        voucherStage.initModality(Modality.APPLICATION_MODAL);
        voucherStage.setTitle("DC Royal Printing — Business Expense Payment Voucher");

        VBox root = new VBox(14);
        root.setPadding(new Insets(26));
        root.setStyle("-fx-background-color: white; -fx-font-family: 'Segoe UI', sans-serif;");
        root.setAlignment(Pos.CENTER);
        root.setPrefWidth(500);

        Label brand = new Label("DC ROYAL PRINTING");
        brand.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #14161F;");

        Label sub = new Label("OFFICIAL BUSINESS EXPENDITURE & DISBURSEMENT VOUCHER");
        sub.setStyle("-fx-font-size: 10px; -fx-font-weight: 700; -fx-text-fill: #07768F; -fx-letter-spacing: 1px;");

        Separator sep1 = new Separator();

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(8);
        grid.setAlignment(Pos.CENTER);

        grid.add(new Label("Voucher / Expense Code:"), 0, 0);
        Label codeVal = new Label(selectedExpense.getExpenseCode());
        codeVal.setStyle("-fx-font-weight: 700; -fx-font-size: 14px; -fx-text-fill: #14161F;");
        grid.add(codeVal, 1, 0);

        grid.add(new Label("Expense Category:"), 0, 1);
        grid.add(new Label(selectedExpense.getExpenseCategory()), 1, 1);

        grid.add(new Label("Date Incurred:"), 0, 2);
        grid.add(new Label(selectedExpense.getExpenseDate()), 1, 2);

        grid.add(new Label("Description / Purpose:"), 0, 3);
        Label descVal = new Label(selectedExpense.getDescription());
        descVal.setWrapText(true);
        descVal.setMaxWidth(260);
        grid.add(descVal, 1, 3);

        grid.add(new Label("Status:"), 0, 4);
        Label statVal = new Label(selectedExpense.getApprovalStatus());
        statVal.setStyle("-fx-font-weight: 700; -fx-text-fill: #1A7A46;");
        grid.add(statVal, 1, 4);

        grid.add(new Label("Bank Transfer Ref:"), 0, 5);
        Label refVal = new Label(selectedExpense.getTransferReference());
        refVal.setStyle("-fx-font-weight: 700; -fx-text-fill: #07768F;");
        grid.add(refVal, 1, 5);

        Separator sep2 = new Separator();

        HBox amountBox = new HBox(12);
        amountBox.setAlignment(Pos.CENTER);
        amountBox.setStyle("-fx-background-color: #F6F3EC; -fx-padding: 12; -fx-background-radius: 8;");
        Label amtLbl = new Label("AMOUNT DISBURSED: ");
        amtLbl.setStyle("-fx-font-weight: 700; -fx-font-size: 14px; -fx-text-fill: #15171F;");
        Label amtVal = new Label(selectedExpense.getAmountFormatted());
        amtVal.setStyle("-fx-font-weight: 700; -fx-font-size: 20px; -fx-text-fill: #B8134F;");
        amountBox.getChildren().addAll(amtLbl, amtVal);

        HBox sigBox = new HBox(20);
        sigBox.setAlignment(Pos.CENTER);
        sigBox.setPadding(new Insets(10, 0, 0, 0));
        Label subSig = new Label("Claimant: " + selectedExpense.getSubmittedByName());
        subSig.setStyle("-fx-font-size: 11px; -fx-text-fill: #62656F;");
        Label appSig = new Label("Authorized: " + selectedExpense.getApprovedByName());
        appSig.setStyle("-fx-font-size: 11px; -fx-text-fill: #62656F; -fx-font-weight: 600;");
        sigBox.getChildren().addAll(subSig, appSig);

        Button closeBtn = new Button("✔  Print & Close");
        closeBtn.setMaxWidth(Double.MAX_VALUE);
        closeBtn.setPrefHeight(38);
        closeBtn.setStyle("-fx-background-color: #07768F; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> voucherStage.close());

        root.getChildren().addAll(brand, sub, sep1, grid, sep2, amountBox, sigBox, closeBtn);

        Scene scene = new Scene(root);
        voucherStage.setScene(scene);
        voucherStage.showAndWait();
    }

    // ============================================================
    // TAB 2: SUBMIT NEW EXPENSE
    // ============================================================
    @FXML
    private void handleSubmitExpense() {
        String code = expenseCodeField.getText().trim();
        String category = categoryComboBox.getValue();
        LocalDate date = expenseDatePicker.getValue() != null ? expenseDatePicker.getValue() : LocalDate.now();
        String amtStr = amountField.getText().trim();
        String desc = descriptionField.getText().trim();
        String ref = transferRefField.getText().trim();
        String status = statusComboBox.getValue();

        if (desc.isEmpty() || amtStr.isEmpty() || category == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Fields", "Category, Amount, and Description are required.");
            return;
        }

        double amount = 0.00;
        try {
            amount = Double.parseDouble(amtStr);
            if (amount <= 0) {
                showAlert(Alert.AlertType.WARNING, "Invalid Amount", "Amount must be greater than zero.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Invalid Amount", "Amount must be a valid number.");
            return;
        }

        User currentUser = Session.getCurrentUser();
        int submitterId = (currentUser != null) ? currentUser.getUserId() : 1;
        Integer approverId = (status.equals("APPROVED") || status.equals("SETTLED")) ? submitterId : null;

        String sql = "INSERT INTO expenses (expense_code, expense_category, description, amount, expense_date, " +
                     "approval_status, transfer_reference, submitted_by, approved_by) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, code);
            ps.setString(2, category);
            ps.setString(3, desc);
            ps.setDouble(4, amount);
            ps.setDate(5, Date.valueOf(date));
            ps.setString(6, status);
            ps.setString(7, ref.isEmpty() ? null : ref);
            ps.setInt(8, submitterId);
            if (approverId != null) ps.setInt(9, approverId); else ps.setNull(9, java.sql.Types.INTEGER);

            int rows = ps.executeUpdate();
            ps.close();

            if (rows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Expense Recorded",
                        "Expense record " + code + " for Rs. " + String.format("%,.2f", amount) + " submitted successfully!");

                loadExpenseKpis();
                loadAllExpenses();
                clearForm();
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to record expense: " + e.getMessage());
        }
    }

    // ============================================================
    // CLEAR FORM
    // ============================================================
    @FXML
    private void handleClearForm() {
        clearForm();
    }

    private void clearForm() {
        generateNextExpenseCode();
        amountField.clear();
        descriptionField.clear();
        transferRefField.clear();
        expenseDatePicker.setValue(LocalDate.now());
        if (!categoryComboBox.getItems().isEmpty()) {
            categoryComboBox.getSelectionModel().selectFirst();
        }
    }

    // ============================================================
    // APPLY FILTERS (CATEGORY & STATUS)
    // ============================================================
    private void applyFilters() {
        String catFilter = categoryFilterComboBox.getValue();
        String statFilter = statusFilterComboBox.getValue();

        ObservableList<ExpenseTM> filtered = FXCollections.observableArrayList();
        for (ExpenseTM tm : expenseList) {
            boolean matchCat = (catFilter == null || catFilter.equals("ALL") || tm.getExpenseCategory().equalsIgnoreCase(catFilter));
            boolean matchStat = (statFilter == null || statFilter.equals("ALL") || tm.getApprovalStatus().equalsIgnoreCase(statFilter));

            if (matchCat && matchStat) {
                filtered.add(tm);
            }
        }
        expenseTable.setItems(filtered);
    }

    // ============================================================
    // LIVE SEARCH
    // ============================================================
    private void setupLiveSearch() {
        FilteredList<ExpenseTM> filteredData = new FilteredList<>(expenseList, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(tm -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }
                String filter = newValue.toLowerCase().trim();
                return tm.getExpenseCode().toLowerCase().contains(filter) ||
                       tm.getExpenseCategory().toLowerCase().contains(filter) ||
                       tm.getDescription().toLowerCase().contains(filter) ||
                       tm.getApprovalStatus().toLowerCase().contains(filter) ||
                       tm.getTransferReference().toLowerCase().contains(filter);
            });
        });

        SortedList<ExpenseTM> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(expenseTable.comparatorProperty());
        expenseTable.setItems(sortedData);
    }

    // ============================================================
    // REFRESH
    // ============================================================
    @FXML
    private void handleRefresh() {
        loadExpenseKpis();
        loadAllExpenses();
        generateNextExpenseCode();
        updateActionPanel(null);
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
