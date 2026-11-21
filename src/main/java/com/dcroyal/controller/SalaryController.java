package com.dcroyal.controller;

import com.dcroyal.db.DBConnection;
import com.dcroyal.model.User;
import com.dcroyal.tm.SalaryTM;
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * SalaryController.java
 * Manages employee monthly payroll processing, overtime/deduction adjustments,
 * Owner approval workflows, disbursement tracking, and payslip generation with direct JDBC.
 */
public class SalaryController {

    // ============================================================
    // FXML: TOP KPI SUMMARY LABELS
    // ============================================================
    @FXML private Label totalPayrollNetLabel;
    @FXML private Label pendingApprovalCountLabel;
    @FXML private Label approvedCountLabel;
    @FXML private Label paidCountLabel;

    // ============================================================
    // FXML: TAB 1 - MONTHLY PAYROLL SHEET
    // ============================================================
    @FXML private ComboBox<String> monthFilterComboBox;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private TextField        searchField;

    @FXML private TableView<SalaryTM> salaryTable;
    @FXML private TableColumn<SalaryTM, String> colEmployee;
    @FXML private TableColumn<SalaryTM, String> colRole;
    @FXML private TableColumn<SalaryTM, String> colMonth;
    @FXML private TableColumn<SalaryTM, String> colBasic;
    @FXML private TableColumn<SalaryTM, String> colOt;
    @FXML private TableColumn<SalaryTM, String> colDeductions;
    @FXML private TableColumn<SalaryTM, String> colNet;
    @FXML private TableColumn<SalaryTM, String> colStatus;
    @FXML private TableColumn<SalaryTM, String> colSubmittedBy;
    @FXML private TableColumn<SalaryTM, String> colApprovedBy;

    // Action Panel
    @FXML private Label  selectedEmpLabel;
    @FXML private Label  selectedMonthLabel;
    @FXML private Label  selectedBasicLabel;
    @FXML private Label  selectedOtLabel;
    @FXML private Label  selectedDeductionsLabel;
    @FXML private Label  selectedNetLabel;
    @FXML private Button approveSalaryButton;
    @FXML private Button markPaidButton;
    @FXML private Button printPaySlipButton;

    // ============================================================
    // FXML: TAB 2 - PROCESS & SUBMIT PAYROLL
    // ============================================================
    @FXML private ComboBox<String> processMonthComboBox;
    @FXML private ComboBox<String> processStaffComboBox;
    @FXML private TextField        processBasicSalaryField;
    @FXML private TextField        processOtField;
    @FXML private TextField        processDeductionsField;
    @FXML private Label            calculatedNetPayLabel;

    @FXML private Button submitSalaryButton;
    @FXML private Button autoGeneratePayrollButton;

    // Data lists
    private ObservableList<SalaryTM> salaryList = FXCollections.observableArrayList();

    // Mapping for Process Staff dropdown: user_id and default basic_salary
    private List<Integer> processStaffUserIdList = new ArrayList<>();
    private List<Double>  processStaffSalaryList = new ArrayList<>();

    // Selected Salary record
    private SalaryTM selectedSalary = null;

    // ============================================================
    // INITIALIZE
    // ============================================================
    @FXML
    public void initialize() {
        // --- 1. Table Columns Setup ---
        colEmployee.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("roleName"));
        colMonth.setCellValueFactory(new PropertyValueFactory<>("salaryMonth"));
        colBasic.setCellValueFactory(new PropertyValueFactory<>("basicAmountFormatted"));
        colOt.setCellValueFactory(new PropertyValueFactory<>("overtimeAllowanceFormatted"));
        colDeductions.setCellValueFactory(new PropertyValueFactory<>("deductionsFormatted"));
        colNet.setCellValueFactory(new PropertyValueFactory<>("netSalaryFormatted"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("approvalStatus"));
        colSubmittedBy.setCellValueFactory(new PropertyValueFactory<>("submittedByName"));
        colApprovedBy.setCellValueFactory(new PropertyValueFactory<>("approvedByName"));
        salaryTable.setItems(salaryList);

        // --- 2. Populate Month Filter & Processing Month ---
        populateMonths();

        // --- 3. Status Filter Options ---
        statusFilterComboBox.setItems(FXCollections.observableArrayList(
                "ALL", "PENDING_OWNER", "APPROVED", "PAID"
        ));
        statusFilterComboBox.getSelectionModel().selectFirst();
        statusFilterComboBox.setOnAction(e -> filterSalaries());

        // Month filter listener
        monthFilterComboBox.setOnAction(e -> {
            loadSalariesForMonth(monthFilterComboBox.getValue());
            loadPayrollKpis(monthFilterComboBox.getValue());
        });

        // --- 4. Selection Listener for Payroll Table ---
        salaryTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedSalary = newVal;
            updateActionPanel(newVal);
        });

        // --- 5. Tab 2 Staff Selection Listener (auto-populate basic salary) ---
        processStaffComboBox.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            int idx = newVal.intValue();
            if (idx >= 0 && idx < processStaffSalaryList.size()) {
                double basic = processStaffSalaryList.get(idx);
                processBasicSalaryField.setText(String.format("%.2f", basic));
                recalculateNetPay();
            }
        });

        // Real-time calculation listeners
        processBasicSalaryField.textProperty().addListener((o, ov, nv) -> recalculateNetPay());
        processOtField.textProperty().addListener((o, ov, nv) -> recalculateNetPay());
        processDeductionsField.textProperty().addListener((o, ov, nv) -> recalculateNetPay());

        // Initial default inputs
        processOtField.setText("0.00");
        processDeductionsField.setText("0.00");

        // --- 6. Initial Data Load ---
        String currentMonth = YearMonth.now().toString();
        loadPayrollKpis(currentMonth);
        loadSalariesForMonth(currentMonth);
        loadProcessStaffDropdown();

        // --- 7. Live Search ---
        setupLiveSearch();
    }

    // ============================================================
    // POPULATE MONTH DROPDOWNS (CURRENT, PAST 5, NEXT 2)
    // ============================================================
    private void populateMonths() {
        monthFilterComboBox.getItems().clear();
        processMonthComboBox.getItems().clear();

        YearMonth now = YearMonth.now();
        // 5 months back to 2 months ahead
        for (int i = -5; i <= 2; i++) {
            YearMonth ym = now.plusMonths(i);
            String monthStr = ym.toString(); // YYYY-MM
            monthFilterComboBox.getItems().add(monthStr);
            processMonthComboBox.getItems().add(monthStr);
        }

        monthFilterComboBox.setValue(now.toString());
        processMonthComboBox.setValue(now.toString());
    }

    // ============================================================
    // LOAD KPIS FOR SELECTED MONTH
    // ============================================================
    private void loadPayrollKpis(String month) {
        if (month == null || month.isEmpty()) return;

        try {
            Connection con = DBConnection.getConnection();

            // 1. Total Net Payroll for Month
            String sql1 = "SELECT COALESCE(SUM(net_salary), 0) FROM employee_salaries WHERE salary_month = ?";
            PreparedStatement ps1 = con.prepareStatement(sql1);
            ps1.setString(1, month);
            ResultSet rs1 = ps1.executeQuery();
            if (rs1.next()) {
                totalPayrollNetLabel.setText(String.format("Rs. %,.2f", rs1.getDouble(1)));
            }
            rs1.close(); ps1.close();

            // 2. Pending Owner Approval
            String sql2 = "SELECT COUNT(*) FROM employee_salaries WHERE salary_month = ? AND approval_status = 'PENDING_OWNER'";
            PreparedStatement ps2 = con.prepareStatement(sql2);
            ps2.setString(1, month);
            ResultSet rs2 = ps2.executeQuery();
            if (rs2.next()) {
                pendingApprovalCountLabel.setText(String.valueOf(rs2.getInt(1)));
            }
            rs2.close(); ps2.close();

            // 3. Approved
            String sql3 = "SELECT COUNT(*) FROM employee_salaries WHERE salary_month = ? AND approval_status = 'APPROVED'";
            PreparedStatement ps3 = con.prepareStatement(sql3);
            ps3.setString(1, month);
            ResultSet rs3 = ps3.executeQuery();
            if (rs3.next()) {
                approvedCountLabel.setText(String.valueOf(rs3.getInt(1)));
            }
            rs3.close(); ps3.close();

            // 4. Paid
            String sql4 = "SELECT COUNT(*) FROM employee_salaries WHERE salary_month = ? AND approval_status = 'PAID'";
            PreparedStatement ps4 = con.prepareStatement(sql4);
            ps4.setString(1, month);
            ResultSet rs4 = ps4.executeQuery();
            if (rs4.next()) {
                paidCountLabel.setText(String.valueOf(rs4.getInt(1)));
            }
            rs4.close(); ps4.close();

        } catch (SQLException e) {
            System.out.println("Error loading payroll KPIs: " + e.getMessage());
        }
    }

    // ============================================================
    // LOAD SALARIES FOR SELECTED MONTH
    // ============================================================
    private void loadSalariesForMonth(String month) {
        if (month == null || month.isEmpty()) return;
        salaryList.clear();

        String sql = "SELECT s.salary_id, s.user_id, u.full_name as emp_name, r.role_name, s.salary_month, " +
                     "s.basic_amount, s.overtime_allowance, s.deductions, s.net_salary, s.approval_status, " +
                     "COALESCE(sub.full_name, 'System') as sub_by, " +
                     "COALESCE(app.full_name, '-') as app_by " +
                     "FROM employee_salaries s " +
                     "JOIN users u ON s.user_id = u.user_id " +
                     "JOIN roles r ON u.role_id = r.role_id " +
                     "LEFT JOIN users sub ON s.submitted_by = sub.user_id " +
                     "LEFT JOIN users app ON s.approved_by = app.user_id " +
                     "WHERE s.salary_month = ? " +
                     "ORDER BY s.salary_id ASC";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, month);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                SalaryTM tm = new SalaryTM(
                        rs.getInt("salary_id"),
                        rs.getInt("user_id"),
                        rs.getString("emp_name"),
                        rs.getString("role_name"),
                        rs.getString("salary_month"),
                        rs.getDouble("basic_amount"),
                        rs.getDouble("overtime_allowance"),
                        rs.getDouble("deductions"),
                        rs.getDouble("net_salary"),
                        rs.getString("approval_status"),
                        rs.getString("sub_by"),
                        rs.getString("app_by")
                );
                salaryList.add(tm);
            }
            rs.close(); ps.close();

        } catch (SQLException e) {
            System.out.println("Error loading salaries: " + e.getMessage());
        }
    }

    // ============================================================
    // LOAD TAB 2 ACTIVE STAFF DROPDOWN
    // ============================================================
    private void loadProcessStaffDropdown() {
        processStaffComboBox.getItems().clear();
        processStaffUserIdList.clear();
        processStaffSalaryList.clear();

        String sql = "SELECT u.user_id, u.full_name, r.role_name, u.basic_salary " +
                     "FROM users u " +
                     "JOIN roles r ON u.role_id = r.role_id " +
                     "WHERE u.is_active = 1 " +
                     "ORDER BY u.user_id ASC";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int uId = rs.getInt("user_id");
                String name = rs.getString("full_name");
                String role = rs.getString("role_name");
                double salary = rs.getDouble("basic_salary");

                processStaffUserIdList.add(uId);
                processStaffSalaryList.add(salary);
                processStaffComboBox.getItems().add(name + " (" + role + ")");
            }
            rs.close(); ps.close();

            if (!processStaffComboBox.getItems().isEmpty()) {
                processStaffComboBox.getSelectionModel().selectFirst();
            }

        } catch (SQLException e) {
            System.out.println("Error loading staff dropdown: " + e.getMessage());
        }
    }

    // ============================================================
    // RECALCULATE NET PAY LIVE
    // ============================================================
    private void recalculateNetPay() {
        try {
            double basic = parseDouble(processBasicSalaryField.getText().trim());
            double ot = parseDouble(processOtField.getText().trim());
            double ded = parseDouble(processDeductionsField.getText().trim());
            double net = basic + ot - ded;
            if (net < 0) net = 0.00;
            calculatedNetPayLabel.setText(String.format("Rs. %,.2f", net));
        } catch (Exception e) {
            calculatedNetPayLabel.setText("Rs. 0.00");
        }
    }

    private double parseDouble(String str) {
        if (str == null || str.isEmpty()) return 0.00;
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return 0.00;
        }
    }

    // ============================================================
    // UPDATE ACTION PANEL ON TABLE ROW SELECTION
    // ============================================================
    private void updateActionPanel(SalaryTM tm) {
        if (tm == null) {
            selectedEmpLabel.setText("No record selected");
            selectedMonthLabel.setText("-");
            selectedBasicLabel.setText("-");
            selectedOtLabel.setText("-");
            selectedDeductionsLabel.setText("-");
            selectedNetLabel.setText("-");
            approveSalaryButton.setDisable(true);
            markPaidButton.setDisable(true);
            printPaySlipButton.setDisable(true);
            return;
        }

        selectedEmpLabel.setText(tm.getEmployeeName() + " (" + tm.getRoleName() + ")");
        selectedMonthLabel.setText("Month: " + tm.getSalaryMonth() + " [" + tm.getApprovalStatus() + "]");
        selectedBasicLabel.setText("Basic: " + tm.getBasicAmountFormatted());
        selectedOtLabel.setText("Overtime / Allowance: " + tm.getOvertimeAllowanceFormatted());
        selectedDeductionsLabel.setText("Deductions: " + tm.getDeductionsFormatted());
        selectedNetLabel.setText("Net Payable: " + tm.getNetSalaryFormatted());

        String status = tm.getApprovalStatus();
        approveSalaryButton.setDisable(!status.equals("PENDING_OWNER"));
        markPaidButton.setDisable(status.equals("PAID"));
        printPaySlipButton.setDisable(false);
    }

    // ============================================================
    // ACTION: APPROVE SALARY (OWNER ACTION)
    // ============================================================
    @FXML
    private void handleApproveSalary() {
        if (selectedSalary == null) return;

        User currentUser = Session.getCurrentUser();
        int approverId = (currentUser != null) ? currentUser.getUserId() : 1;

        String sql = "UPDATE employee_salaries SET approval_status = 'APPROVED', approved_by = ? WHERE salary_id = ?";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, approverId);
            ps.setInt(2, selectedSalary.getSalaryId());
            ps.executeUpdate();
            ps.close();

            showAlert(Alert.AlertType.INFORMATION, "Salary Approved",
                    "Payroll for " + selectedSalary.getEmployeeName() + " (" + selectedSalary.getSalaryMonth() + ") has been APPROVED!");

            loadPayrollKpis(monthFilterComboBox.getValue());
            loadSalariesForMonth(monthFilterComboBox.getValue());
            updateActionPanel(null);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to approve salary: " + e.getMessage());
        }
    }

    // ============================================================
    // ACTION: MARK AS PAID / DISBURSED
    // ============================================================
    @FXML
    private void handleMarkPaid() {
        if (selectedSalary == null) return;

        User currentUser = Session.getCurrentUser();
        int approverId = (currentUser != null) ? currentUser.getUserId() : 1;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Payout");
        confirm.setHeaderText("Disburse " + selectedSalary.getNetSalaryFormatted() + " to " + selectedSalary.getEmployeeName());
        confirm.setContentText("Mark this payroll as PAID / Disbursed?");

        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                String sql = "UPDATE employee_salaries SET approval_status = 'PAID', approved_by = COALESCE(approved_by, ?) " +
                             "WHERE salary_id = ?";
                try {
                    Connection con = DBConnection.getConnection();
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setInt(1, approverId);
                    ps.setInt(2, selectedSalary.getSalaryId());
                    ps.executeUpdate();
                    ps.close();

                    showAlert(Alert.AlertType.INFORMATION, "Salary Disbursed",
                            "Salary marked as PAID for " + selectedSalary.getEmployeeName() + "!");

                    loadPayrollKpis(monthFilterComboBox.getValue());
                    loadSalariesForMonth(monthFilterComboBox.getValue());
                    updateActionPanel(null);

                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update payout: " + e.getMessage());
                }
            }
        });
    }

    // ============================================================
    // ACTION: PRINT OFFICIAL PAYSLIP MODAL
    // ============================================================
    @FXML
    private void handlePrintPaySlip() {
        if (selectedSalary == null) return;

        Stage slipStage = new Stage();
        slipStage.initModality(Modality.APPLICATION_MODAL);
        slipStage.setTitle("DC Royal Printing — Official Salary Pay Slip");

        VBox root = new VBox(14);
        root.setPadding(new Insets(26));
        root.setStyle("-fx-background-color: white; -fx-font-family: 'Segoe UI', sans-serif;");
        root.setAlignment(Pos.CENTER);
        root.setPrefWidth(480);

        Label brand = new Label("DC ROYAL PRINTING");
        brand.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #14161F;");

        Label sub = new Label("OFFICIAL SALARY & REMUNERATION SLIP");
        sub.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: #07768F; -fx-letter-spacing: 1px;");

        Separator sep1 = new Separator();

        // Employee Info Grid
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);
        grid.setAlignment(Pos.CENTER);

        grid.add(new Label("Employee Name:"), 0, 0);
        Label nameVal = new Label(selectedSalary.getEmployeeName());
        nameVal.setStyle("-fx-font-weight: 700; -fx-font-size: 13px;");
        grid.add(nameVal, 1, 0);

        grid.add(new Label("Designation / Role:"), 0, 1);
        grid.add(new Label(selectedSalary.getRoleName()), 1, 1);

        grid.add(new Label("Salary Month:"), 0, 2);
        Label monthVal = new Label(selectedSalary.getSalaryMonth());
        monthVal.setStyle("-fx-font-weight: 700;");
        grid.add(monthVal, 1, 2);

        grid.add(new Label("Payment Status:"), 0, 3);
        Label statVal = new Label(selectedSalary.getApprovalStatus());
        statVal.setStyle("-fx-font-weight: 700; -fx-text-fill: #1A7A46;");
        grid.add(statVal, 1, 3);

        Separator sep2 = new Separator();

        // Earnings and Deductions
        GridPane finGrid = new GridPane();
        finGrid.setHgap(16);
        finGrid.setVgap(8);
        finGrid.setAlignment(Pos.CENTER);

        finGrid.add(new Label("Basic Salary:"), 0, 0);
        finGrid.add(new Label(selectedSalary.getBasicAmountFormatted()), 1, 0);

        finGrid.add(new Label("Overtime & Allowances:"), 0, 1);
        finGrid.add(new Label("+ " + selectedSalary.getOvertimeAllowanceFormatted()), 1, 1);

        finGrid.add(new Label("Deductions (EPF/ETF/Adv):"), 0, 2);
        Label dedVal = new Label("- " + selectedSalary.getDeductionsFormatted());
        dedVal.setStyle("-fx-text-fill: #B8134F;");
        finGrid.add(dedVal, 1, 2);

        Separator sep3 = new Separator();

        HBox netBox = new HBox(12);
        netBox.setAlignment(Pos.CENTER);
        netBox.setStyle("-fx-background-color: #F6F3EC; -fx-padding: 12; -fx-background-radius: 8;");
        Label netLbl = new Label("NET REMUNERATION PAYABLE: ");
        netLbl.setStyle("-fx-font-weight: 700; -fx-font-size: 13px; -fx-text-fill: #15171F;");
        Label netAmount = new Label(selectedSalary.getNetSalaryFormatted());
        netAmount.setStyle("-fx-font-weight: 700; -fx-font-size: 18px; -fx-text-fill: #07768F;");
        netBox.getChildren().addAll(netLbl, netAmount);

        HBox sigBox = new HBox(20);
        sigBox.setAlignment(Pos.CENTER);
        sigBox.setPadding(new Insets(10, 0, 0, 0));
        Label empSig = new Label("Staff Signature: __________________");
        empSig.setStyle("-fx-font-size: 11px; -fx-text-fill: #62656F;");
        Label appSig = new Label("Authorized: " + selectedSalary.getApprovedByName());
        appSig.setStyle("-fx-font-size: 11px; -fx-text-fill: #62656F; -fx-font-weight: 600;");
        sigBox.getChildren().addAll(empSig, appSig);

        Button closeBtn = new Button("✔  Print & Close");
        closeBtn.setMaxWidth(Double.MAX_VALUE);
        closeBtn.setPrefHeight(38);
        closeBtn.setStyle("-fx-background-color: #07768F; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> slipStage.close());

        root.getChildren().addAll(brand, sub, sep1, grid, sep2, finGrid, sep3, netBox, sigBox, closeBtn);

        Scene scene = new Scene(root);
        slipStage.setScene(scene);
        slipStage.showAndWait();
    }

    // ============================================================
    // TAB 2: SUBMIT SALARY RECORD (CREATE OR UPDATE)
    // ============================================================
    @FXML
    private void handleSubmitSalary() {
        int staffIdx = processStaffComboBox.getSelectionModel().getSelectedIndex();
        if (staffIdx < 0 || staffIdx >= processStaffUserIdList.size()) {
            showAlert(Alert.AlertType.WARNING, "Staff Required", "Please select an employee.");
            return;
        }

        int targetUserId = processStaffUserIdList.get(staffIdx);
        String month = processMonthComboBox.getValue();
        if (month == null || month.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Month Required", "Please select a salary month.");
            return;
        }

        double basic = parseDouble(processBasicSalaryField.getText().trim());
        double ot = parseDouble(processOtField.getText().trim());
        double ded = parseDouble(processDeductionsField.getText().trim());
        double net = basic + ot - ded;
        if (net < 0) net = 0.00;

        User currentUser = Session.getCurrentUser();
        int submitterId = (currentUser != null) ? currentUser.getUserId() : 1;
        boolean isOwner = (currentUser != null && currentUser.getRoleCode().equals("OWNER"));
        String initialStatus = isOwner ? "APPROVED" : "PENDING_OWNER";
        Integer approverId = isOwner ? submitterId : null;

        String checkSql = "SELECT salary_id FROM employee_salaries WHERE user_id = ? AND salary_month = ?";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement checkPs = con.prepareStatement(checkSql);
            checkPs.setInt(1, targetUserId);
            checkPs.setString(2, month);
            ResultSet rs = checkPs.executeQuery();
            boolean exists = rs.next();
            rs.close(); checkPs.close();

            if (exists) {
                // Update existing record
                String updateSql = "UPDATE employee_salaries SET basic_amount = ?, overtime_allowance = ?, " +
                                   "deductions = ?, net_salary = ?, approval_status = ?, submitted_by = ?, approved_by = ? " +
                                   "WHERE user_id = ? AND salary_month = ?";
                PreparedStatement ps = con.prepareStatement(updateSql);
                ps.setDouble(1, basic);
                ps.setDouble(2, ot);
                ps.setDouble(3, ded);
                ps.setDouble(4, net);
                ps.setString(5, initialStatus);
                ps.setInt(6, submitterId);
                if (approverId != null) ps.setInt(7, approverId); else ps.setNull(7, java.sql.Types.INTEGER);
                ps.setInt(8, targetUserId);
                ps.setString(9, month);
                ps.executeUpdate();
                ps.close();

                showAlert(Alert.AlertType.INFORMATION, "Salary Updated",
                        "Salary for month " + month + " updated successfully!");
            } else {
                // Insert new record
                String insertSql = "INSERT INTO employee_salaries (user_id, salary_month, basic_amount, overtime_allowance, " +
                                   "deductions, net_salary, approval_status, submitted_by, approved_by) " +
                                   "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement ps = con.prepareStatement(insertSql);
                ps.setInt(1, targetUserId);
                ps.setString(2, month);
                ps.setDouble(3, basic);
                ps.setDouble(4, ot);
                ps.setDouble(5, ded);
                ps.setDouble(6, net);
                ps.setString(7, initialStatus);
                ps.setInt(8, submitterId);
                if (approverId != null) ps.setInt(9, approverId); else ps.setNull(9, java.sql.Types.INTEGER);
                ps.executeUpdate();
                ps.close();

                showAlert(Alert.AlertType.INFORMATION, "Salary Submitted",
                        "Salary record created for month " + month + " with status [" + initialStatus + "]!");
            }

            monthFilterComboBox.setValue(month);
            loadPayrollKpis(month);
            loadSalariesForMonth(month);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to submit salary: " + e.getMessage());
        }
    }

    // ============================================================
    // TAB 2: BATCH ACTION - AUTO GENERATE PAYROLL FOR ALL ACTIVE STAFF
    // ============================================================
    @FXML
    private void handleAutoGeneratePayroll() {
        String month = processMonthComboBox.getValue();
        if (month == null || month.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Month Required", "Please select a salary month.");
            return;
        }

        User currentUser = Session.getCurrentUser();
        int submitterId = (currentUser != null) ? currentUser.getUserId() : 1;
        boolean isOwner = (currentUser != null && currentUser.getRoleCode().equals("OWNER"));
        String initialStatus = isOwner ? "APPROVED" : "PENDING_OWNER";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Auto-Generate Payroll Batch");
        confirm.setHeaderText("Generate Monthly Payroll for " + month);
        confirm.setContentText("This will auto-generate draft salary records for all active staff who do not have a salary record for " + month + " using their basic salary. Continue?");

        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                String sql = "INSERT INTO employee_salaries (user_id, salary_month, basic_amount, overtime_allowance, deductions, net_salary, approval_status, submitted_by, approved_by) " +
                             "SELECT u.user_id, ?, u.basic_salary, 0.00, 0.00, u.basic_salary, ?, ?, ? " +
                             "FROM users u " +
                             "WHERE u.is_active = 1 " +
                             "AND u.user_id NOT IN (" +
                             "    SELECT user_id FROM employee_salaries WHERE salary_month = ?" +
                             ")";
                try {
                    Connection con = DBConnection.getConnection();
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setString(1, month);
                    ps.setString(2, initialStatus);
                    ps.setInt(3, submitterId);
                    if (isOwner) ps.setInt(4, submitterId); else ps.setNull(4, java.sql.Types.INTEGER);
                    ps.setString(5, month);

                    int generatedCount = ps.executeUpdate();
                    ps.close();

                    showAlert(Alert.AlertType.INFORMATION, "Batch Payroll Complete",
                            generatedCount + " employee salary records generated for " + month + "!");

                    monthFilterComboBox.setValue(month);
                    loadPayrollKpis(month);
                    loadSalariesForMonth(month);

                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to generate payroll batch: " + e.getMessage());
                }
            }
        });
    }

    // ============================================================
    // FILTER SALARIES BY STATUS
    // ============================================================
    private void filterSalaries() {
        String filter = statusFilterComboBox.getValue();
        if (filter == null || filter.equals("ALL")) {
            salaryTable.setItems(salaryList);
            return;
        }

        ObservableList<SalaryTM> filtered = FXCollections.observableArrayList();
        for (SalaryTM tm : salaryList) {
            if (tm.getApprovalStatus().equalsIgnoreCase(filter)) {
                filtered.add(tm);
            }
        }
        salaryTable.setItems(filtered);
    }

    // ============================================================
    // LIVE SEARCH
    // ============================================================
    private void setupLiveSearch() {
        FilteredList<SalaryTM> filteredData = new FilteredList<>(salaryList, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(tm -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }
                String filter = newValue.toLowerCase().trim();
                return tm.getEmployeeName().toLowerCase().contains(filter) ||
                       tm.getRoleName().toLowerCase().contains(filter) ||
                       tm.getApprovalStatus().toLowerCase().contains(filter) ||
                       tm.getSalaryMonth().contains(filter);
            });
        });

        SortedList<SalaryTM> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(salaryTable.comparatorProperty());
        salaryTable.setItems(sortedData);
    }

    // ============================================================
    // REFRESH
    // ============================================================
    @FXML
    private void handleRefresh() {
        String currentMonth = monthFilterComboBox.getValue();
        if (currentMonth == null) currentMonth = YearMonth.now().toString();
        loadPayrollKpis(currentMonth);
        loadSalariesForMonth(currentMonth);
        loadProcessStaffDropdown();
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
