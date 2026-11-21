package com.dcroyal.controller;

import com.dcroyal.db.DBConnection;
import com.dcroyal.dto.EmployeeDTO;
import com.dcroyal.model.User;
import com.dcroyal.tm.AttendanceTM;
import com.dcroyal.tm.EmployeeTM;
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

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * EmployeeController.java
 * Manages employee records, roles, credentials, monthly payroll bases,
 * and daily staff attendance tracking with direct JDBC.
 */
public class EmployeeController {

    // ============================================================
    // FXML: TOP KPI SUMMARY LABELS
    // ============================================================
    @FXML private Label totalStaffCountLabel;
    @FXML private Label presentTodayCountLabel;
    @FXML private Label absentTodayCountLabel;
    @FXML private Label payrollBaseLabel;

    // ============================================================
    // FXML: TAB 1 - STAFF DIRECTORY
    // ============================================================
    @FXML private TableView<EmployeeTM> employeeTable;
    @FXML private TableColumn<EmployeeTM, Integer> colId;
    @FXML private TableColumn<EmployeeTM, String>  colName;
    @FXML private TableColumn<EmployeeTM, String>  colRole;
    @FXML private TableColumn<EmployeeTM, String>  colEmail;
    @FXML private TableColumn<EmployeeTM, String>  colPhone;
    @FXML private TableColumn<EmployeeTM, String>  colNic;
    @FXML private TableColumn<EmployeeTM, String>  colBasicSalary;
    @FXML private TableColumn<EmployeeTM, String>  colStatus;

    @FXML private TextField        searchField;
    @FXML private ComboBox<String> roleFilterComboBox;

    // Staff Form Controls
    @FXML private TextField        nameField;
    @FXML private TextField        emailField;
    @FXML private PasswordField    passwordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private TextField        phoneField;
    @FXML private TextField        nicField;
    @FXML private TextField        basicSalaryField;
    @FXML private CheckBox         activeCheckBox;

    @FXML private Button addEmployeeButton;
    @FXML private Button updateEmployeeButton;
    @FXML private Button toggleStatusButton;
    @FXML private Button resetPasswordButton;
    @FXML private Button clearFormButton;
    @FXML private Button printStaffCardButton;

    // ============================================================
    // FXML: TAB 2 - ATTENDANCE MANAGEMENT
    // ============================================================
    @FXML private DatePicker                 attendanceDatePicker;
    @FXML private TableView<AttendanceTM>    attendanceTable;
    @FXML private TableColumn<AttendanceTM, String> colAttName;
    @FXML private TableColumn<AttendanceTM, String> colAttRole;
    @FXML private TableColumn<AttendanceTM, String> colAttStatus;
    @FXML private TableColumn<AttendanceTM, String> colAttCheckIn;
    @FXML private TableColumn<AttendanceTM, String> colAttCheckOut;
    @FXML private TableColumn<AttendanceTM, String> colAttRecordedBy;

    @FXML private TextField        attSearchField;
    @FXML private ComboBox<String> attStatusFilterComboBox;

    // Attendance Logger Panel
    @FXML private ComboBox<String> attStaffComboBox;
    @FXML private ComboBox<String> attStatusComboBox;
    @FXML private TextField        attCheckInField;
    @FXML private TextField        attCheckOutField;

    @FXML private Button recordAttendanceButton;
    @FXML private Button markAllPresentButton;
    @FXML private Button printAttendanceReportButton;

    // ============================================================
    // DATA LISTS & MAPPINGS
    // ============================================================
    private ObservableList<EmployeeTM>   employeeList   = FXCollections.observableArrayList();
    private ObservableList<AttendanceTM> attendanceList = FXCollections.observableArrayList();

    // Roles mapping: list of role_id corresponding to roleComboBox items
    private List<Integer> roleIdList = new ArrayList<>();
    // Staff mapping for Tab 2 attendance: list of user_id corresponding to attStaffComboBox
    private List<Integer> staffUserIdList = new ArrayList<>();

    // Selected Employee in Tab 1
    private EmployeeTM selectedEmployee = null;

    // ============================================================
    // INITIALIZE
    // ============================================================
    @FXML
    public void initialize() {
        // --- 1. Tab 1 Table Setup ---
        colId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("roleName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colNic.setCellValueFactory(new PropertyValueFactory<>("nic"));
        colBasicSalary.setCellValueFactory(new PropertyValueFactory<>("basicSalaryFormatted"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        employeeTable.setItems(employeeList);

        // --- 2. Tab 2 Table Setup ---
        colAttName.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        colAttRole.setCellValueFactory(new PropertyValueFactory<>("roleName"));
        colAttStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colAttCheckIn.setCellValueFactory(new PropertyValueFactory<>("checkIn"));
        colAttCheckOut.setCellValueFactory(new PropertyValueFactory<>("checkOut"));
        colAttRecordedBy.setCellValueFactory(new PropertyValueFactory<>("recordedByName"));
        attendanceTable.setItems(attendanceList);

        // --- 3. DatePicker Default to Today ---
        attendanceDatePicker.setValue(LocalDate.now());
        attendanceDatePicker.setOnAction(e -> loadAttendanceForDate(attendanceDatePicker.getValue()));

        // --- 4. Attendance Status Options ---
        attStatusComboBox.setItems(FXCollections.observableArrayList(
                "PRESENT", "LATE", "HALF_DAY", "LEAVE", "ABSENT"
        ));
        attStatusComboBox.getSelectionModel().selectFirst();

        attStatusFilterComboBox.setItems(FXCollections.observableArrayList(
                "ALL", "PRESENT", "LATE", "HALF_DAY", "LEAVE", "ABSENT"
        ));
        attStatusFilterComboBox.getSelectionModel().selectFirst();
        attStatusFilterComboBox.setOnAction(e -> filterAttendance());

        // Default time helpers
        attCheckInField.setText("08:30:00");
        attCheckOutField.setText("17:00:00");

        // --- 5. Selection Listener for Employee Table ---
        employeeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedEmployee = newVal;
            populateStaffForm(newVal);
        });

        // --- 6. Initial Data Load ---
        loadRoles();
        loadEmployeeKpis();
        loadAllEmployees();
        loadAttendanceStaffDropdown();
        loadAttendanceForDate(attendanceDatePicker.getValue());

        // --- 7. Live Searches ---
        setupEmployeeSearch();
        setupAttendanceSearch();
    }

    // ============================================================
    // LOAD KPIS
    // ============================================================
    private void loadEmployeeKpis() {
        try {
            Connection con = DBConnection.getConnection();

            // 1. Total Active Staff
            String sql1 = "SELECT COUNT(*) FROM users WHERE is_active = 1";
            PreparedStatement ps1 = con.prepareStatement(sql1);
            ResultSet rs1 = ps1.executeQuery();
            if (rs1.next()) {
                totalStaffCountLabel.setText(String.valueOf(rs1.getInt(1)));
            }
            rs1.close(); ps1.close();

            // 2. Present Today
            String sql2 = "SELECT COUNT(*) FROM employee_attendance " +
                          "WHERE attendance_date = CURDATE() AND status IN ('PRESENT', 'LATE')";
            PreparedStatement ps2 = con.prepareStatement(sql2);
            ResultSet rs2 = ps2.executeQuery();
            if (rs2.next()) {
                presentTodayCountLabel.setText(String.valueOf(rs2.getInt(1)));
            }
            rs2.close(); ps2.close();

            // 3. Absent / Leave Today
            String sql3 = "SELECT COUNT(*) FROM employee_attendance " +
                          "WHERE attendance_date = CURDATE() AND status IN ('ABSENT', 'LEAVE', 'HALF_DAY')";
            PreparedStatement ps3 = con.prepareStatement(sql3);
            ResultSet rs3 = ps3.executeQuery();
            if (rs3.next()) {
                absentTodayCountLabel.setText(String.valueOf(rs3.getInt(1)));
            }
            rs3.close(); ps3.close();

            // 4. Monthly Payroll Base
            String sql4 = "SELECT COALESCE(SUM(basic_salary), 0) FROM users WHERE is_active = 1";
            PreparedStatement ps4 = con.prepareStatement(sql4);
            ResultSet rs4 = ps4.executeQuery();
            if (rs4.next()) {
                payrollBaseLabel.setText(String.format("Rs. %,.2f", rs4.getDouble(1)));
            }
            rs4.close(); ps4.close();

        } catch (SQLException e) {
            System.out.println("Error loading employee KPIs: " + e.getMessage());
        }
    }

    // ============================================================
    // LOAD ROLES (FOR FORM & FILTER)
    // ============================================================
    private void loadRoles() {
        roleComboBox.getItems().clear();
        roleIdList.clear();
        roleFilterComboBox.getItems().clear();
        roleFilterComboBox.getItems().add("ALL");

        String sql = "SELECT role_id, role_name FROM roles ORDER BY role_id ASC";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int rId = rs.getInt("role_id");
                String rName = rs.getString("role_name");
                roleIdList.add(rId);
                roleComboBox.getItems().add(rName);
                roleFilterComboBox.getItems().add(rName);
            }
            rs.close(); ps.close();

            if (!roleComboBox.getItems().isEmpty()) {
                roleComboBox.getSelectionModel().selectFirst();
            }
            roleFilterComboBox.getSelectionModel().selectFirst();
            roleFilterComboBox.setOnAction(e -> filterEmployeesByRole());

        } catch (SQLException e) {
            System.out.println("Error loading roles: " + e.getMessage());
        }
    }

    // ============================================================
    // LOAD ALL EMPLOYEES
    // ============================================================
    private void loadAllEmployees() {
        employeeList.clear();
        String sql = "SELECT u.user_id, u.full_name, r.role_name, u.email, u.phone, u.nic, " +
                     "u.basic_salary, u.is_active " +
                     "FROM users u " +
                     "JOIN roles r ON u.role_id = r.role_id " +
                     "ORDER BY u.user_id ASC";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                EmployeeTM tm = new EmployeeTM(
                        rs.getInt("user_id"),
                        rs.getString("full_name"),
                        rs.getString("role_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("nic"),
                        rs.getDouble("basic_salary"),
                        rs.getBoolean("is_active")
                );
                employeeList.add(tm);
            }
            rs.close(); ps.close();

        } catch (SQLException e) {
            System.out.println("Error loading employees: " + e.getMessage());
        }
    }

    // ============================================================
    // LOAD TAB 2 STAFF COMBOBOX
    // ============================================================
    private void loadAttendanceStaffDropdown() {
        attStaffComboBox.getItems().clear();
        staffUserIdList.clear();

        String sql = "SELECT u.user_id, u.full_name, r.role_name " +
                     "FROM users u " +
                     "JOIN roles r ON u.role_id = r.role_id " +
                     "WHERE u.is_active = 1 " +
                     "ORDER BY u.full_name ASC";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                staffUserIdList.add(rs.getInt("user_id"));
                attStaffComboBox.getItems().add(
                        rs.getString("full_name") + " (" + rs.getString("role_name") + ")"
                );
            }
            rs.close(); ps.close();

            if (!attStaffComboBox.getItems().isEmpty()) {
                attStaffComboBox.getSelectionModel().selectFirst();
            }

        } catch (SQLException e) {
            System.out.println("Error loading staff dropdown: " + e.getMessage());
        }
    }

    // ============================================================
    // LOAD ATTENDANCE FOR SELECTED DATE
    // ============================================================
    private void loadAttendanceForDate(LocalDate date) {
        if (date == null) return;
        attendanceList.clear();

        String sql = "SELECT ea.attendance_id, ea.user_id, u.full_name as emp_name, r.role_name, " +
                     "DATE_FORMAT(ea.attendance_date, '%Y-%m-%d') as att_date, " +
                     "TIME_FORMAT(ea.check_in, '%H:%i:%s') as c_in, " +
                     "TIME_FORMAT(ea.check_out, '%H:%i:%s') as c_out, " +
                     "ea.status, " +
                     "COALESCE(rec.full_name, 'System') as rec_by " +
                     "FROM employee_attendance ea " +
                     "JOIN users u ON ea.user_id = u.user_id " +
                     "JOIN roles r ON u.role_id = r.role_id " +
                     "LEFT JOIN users rec ON ea.recorded_by = rec.user_id " +
                     "WHERE ea.attendance_date = ? " +
                     "ORDER BY ea.attendance_id ASC";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setDate(1, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                AttendanceTM tm = new AttendanceTM(
                        rs.getInt("attendance_id"),
                        rs.getInt("user_id"),
                        rs.getString("emp_name"),
                        rs.getString("role_name"),
                        rs.getString("att_date"),
                        rs.getString("c_in"),
                        rs.getString("c_out"),
                        rs.getString("status"),
                        rs.getString("rec_by")
                );
                attendanceList.add(tm);
            }
            rs.close(); ps.close();

        } catch (SQLException e) {
            System.out.println("Error loading attendance: " + e.getMessage());
        }
    }

    // ============================================================
    // POPULATE STAFF FORM ON SELECTION
    // ============================================================
    private void populateStaffForm(EmployeeTM tm) {
        if (tm == null) {
            clearStaffForm();
            return;
        }

        nameField.setText(tm.getFullName());
        emailField.setText(tm.getEmail());
        passwordField.clear(); // Leave blank unless resetting
        roleComboBox.setValue(tm.getRoleName());
        phoneField.setText(tm.getPhone().equals("-") ? "" : tm.getPhone());
        nicField.setText(tm.getNic().equals("-") ? "" : tm.getNic());
        basicSalaryField.setText(String.format("%.2f", tm.getBasicSalary()));
        activeCheckBox.setSelected(tm.isActive());

        addEmployeeButton.setDisable(true);
        updateEmployeeButton.setDisable(false);
        toggleStatusButton.setDisable(false);
        resetPasswordButton.setDisable(false);
        printStaffCardButton.setDisable(false);

        toggleStatusButton.setText(tm.isActive() ? "⛔  Deactivate" : "✔  Activate");
    }

    // ============================================================
    // ACTION: ADD EMPLOYEE
    // ============================================================
    @FXML
    private void handleAddEmployee() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String phone = phoneField.getText().trim();
        String nic = nicField.getText().trim();
        String salaryStr = basicSalaryField.getText().trim();
        int roleIdx = roleComboBox.getSelectionModel().getSelectedIndex();

        if (name.isEmpty() || email.isEmpty() || roleIdx < 0) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Full Name, Email, and Role are mandatory.");
            return;
        }

        if (password.isEmpty()) {
            password = "123"; // Default initial password
        }

        double basicSalary = 0.00;
        if (!salaryStr.isEmpty()) {
            try {
                basicSalary = Double.parseDouble(salaryStr);
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Basic salary must be a valid number.");
                return;
            }
        }

        int roleId = roleIdList.get(roleIdx);
        boolean isActive = activeCheckBox.isSelected();

        String sql = "INSERT INTO users (role_id, full_name, email, password_hash, phone, nic, basic_salary, is_active) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, roleId);
            ps.setString(2, name);
            ps.setString(3, email);
            ps.setString(4, password);
            ps.setString(5, phone.isEmpty() ? null : phone);
            ps.setString(6, nic.isEmpty() ? null : nic);
            ps.setDouble(7, basicSalary);
            ps.setInt(8, isActive ? 1 : 0);

            int rows = ps.executeUpdate();
            ps.close();

            if (rows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Staff member '" + name + "' added successfully!");
                loadEmployeeKpis();
                loadAllEmployees();
                loadAttendanceStaffDropdown();
                clearStaffForm();
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to add employee: " + e.getMessage());
        }
    }

    // ============================================================
    // ACTION: UPDATE EMPLOYEE
    // ============================================================
    @FXML
    private void handleUpdateEmployee() {
        if (selectedEmployee == null) return;

        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String phone = phoneField.getText().trim();
        String nic = nicField.getText().trim();
        String salaryStr = basicSalaryField.getText().trim();
        int roleIdx = roleComboBox.getSelectionModel().getSelectedIndex();

        if (name.isEmpty() || email.isEmpty() || roleIdx < 0) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Full Name, Email, and Role are mandatory.");
            return;
        }

        double basicSalary = 0.00;
        if (!salaryStr.isEmpty()) {
            try {
                basicSalary = Double.parseDouble(salaryStr);
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Basic salary must be a valid number.");
                return;
            }
        }

        int roleId = roleIdList.get(roleIdx);
        boolean isActive = activeCheckBox.isSelected();

        String sql;
        if (password.isEmpty()) {
            sql = "UPDATE users SET role_id = ?, full_name = ?, email = ?, phone = ?, nic = ?, " +
                  "basic_salary = ?, is_active = ? WHERE user_id = ?";
        } else {
            sql = "UPDATE users SET role_id = ?, full_name = ?, email = ?, password_hash = ?, " +
                  "phone = ?, nic = ?, basic_salary = ?, is_active = ? WHERE user_id = ?";
        }

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, roleId);
            ps.setString(2, name);
            ps.setString(3, email);

            int paramIndex = 4;
            if (!password.isEmpty()) {
                ps.setString(paramIndex++, password);
            }
            ps.setString(paramIndex++, phone.isEmpty() ? null : phone);
            ps.setString(paramIndex++, nic.isEmpty() ? null : nic);
            ps.setDouble(paramIndex++, basicSalary);
            ps.setInt(paramIndex++, isActive ? 1 : 0);
            ps.setInt(paramIndex, selectedEmployee.getUserId());

            int rows = ps.executeUpdate();
            ps.close();

            if (rows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Employee '" + name + "' updated successfully!");
                loadEmployeeKpis();
                loadAllEmployees();
                loadAttendanceStaffDropdown();
                clearStaffForm();
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update employee: " + e.getMessage());
        }
    }

    // ============================================================
    // ACTION: TOGGLE ACTIVE / INACTIVE STATUS
    // ============================================================
    @FXML
    private void handleToggleStatus() {
        if (selectedEmployee == null) return;

        // Prevent deactivating primary system owner
        if (selectedEmployee.getUserId() == 1) {
            showAlert(Alert.AlertType.WARNING, "Protected User", "The Primary System Owner cannot be deactivated.");
            return;
        }

        boolean newStatus = !selectedEmployee.isActive();
        String actionName = newStatus ? "activate" : "deactivate";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Status Change");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to " + actionName + " " + selectedEmployee.getFullName() + "?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String sql = "UPDATE users SET is_active = ? WHERE user_id = ?";
                try {
                    Connection con = DBConnection.getConnection();
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setInt(1, newStatus ? 1 : 0);
                    ps.setInt(2, selectedEmployee.getUserId());
                    ps.executeUpdate();
                    ps.close();

                    showAlert(Alert.AlertType.INFORMATION, "Status Updated",
                            selectedEmployee.getFullName() + " has been " + (newStatus ? "ACTIVATED" : "DEACTIVATED") + ".");
                    loadEmployeeKpis();
                    loadAllEmployees();
                    loadAttendanceStaffDropdown();
                    clearStaffForm();

                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to update status: " + e.getMessage());
                }
            }
        });
    }

    // ============================================================
    // ACTION: RESET PASSWORD TO DEFAULT
    // ============================================================
    @FXML
    private void handleResetPassword() {
        if (selectedEmployee == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Reset Password");
        confirm.setHeaderText("Reset password for " + selectedEmployee.getFullName());
        confirm.setContentText("The password will be reset to default '123'. Continue?");

        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                String sql = "UPDATE users SET password_hash = '123' WHERE user_id = ?";
                try {
                    Connection con = DBConnection.getConnection();
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setInt(1, selectedEmployee.getUserId());
                    ps.executeUpdate();
                    ps.close();

                    showAlert(Alert.AlertType.INFORMATION, "Password Reset",
                            "Password for " + selectedEmployee.getFullName() + " has been reset to '123'.");

                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to reset password: " + e.getMessage());
                }
            }
        });
    }

    // ============================================================
    // ACTION: CLEAR FORM
    // ============================================================
    @FXML
    private void handleClearForm() {
        clearStaffForm();
    }

    private void clearStaffForm() {
        selectedEmployee = null;
        employeeTable.getSelectionModel().clearSelection();
        nameField.clear();
        emailField.clear();
        passwordField.clear();
        phoneField.clear();
        nicField.clear();
        basicSalaryField.clear();
        activeCheckBox.setSelected(true);
        if (!roleComboBox.getItems().isEmpty()) {
            roleComboBox.getSelectionModel().selectFirst();
        }

        addEmployeeButton.setDisable(false);
        updateEmployeeButton.setDisable(true);
        toggleStatusButton.setDisable(true);
        resetPasswordButton.setDisable(true);
        printStaffCardButton.setDisable(true);
    }

    // ============================================================
    // ACTION: PRINT STAFF ID BADGE / CREDENTIAL MODAL
    // ============================================================
    @FXML
    private void handlePrintStaffCard() {
        if (selectedEmployee == null) return;

        Stage cardStage = new Stage();
        cardStage.initModality(Modality.APPLICATION_MODAL);
        cardStage.setTitle("DC Royal Printing — Staff Credential ID Badge");

        VBox root = new VBox(12);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: white; -fx-font-family: 'Segoe UI', sans-serif;");
        root.setAlignment(Pos.CENTER);
        root.setPrefWidth(400);

        Label brand = new Label("DC ROYAL PRINTING");
        brand.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #14161F;");

        Label badgeTitle = new Label("OFFICIAL STAFF IDENTIFICATION");
        badgeTitle.setStyle("-fx-font-size: 10px; -fx-font-weight: 700; -fx-text-fill: #07768F; -fx-letter-spacing: 1px;");

        Separator sep1 = new Separator();

        // Staff Initial Avatar
        Label avatar = new Label(getInitials(selectedEmployee.getFullName()));
        avatar.setStyle("-fx-background-color: #14161F; -fx-text-fill: #FFE600; -fx-font-size: 22px; -fx-font-weight: 700; " +
                        "-fx-padding: 18 24 18 24; -fx-background-radius: 999;");

        Label name = new Label(selectedEmployee.getFullName());
        name.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #15171F;");

        Label roleBadge = new Label(selectedEmployee.getRoleName().toUpperCase());
        roleBadge.setStyle("-fx-background-color: #D8F1F7; -fx-text-fill: #07768F; -fx-font-weight: 700; " +
                           "-fx-font-size: 11px; -fx-padding: 4 12; -fx-background-radius: 12;");

        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(10);
        detailsGrid.setVgap(6);
        detailsGrid.setAlignment(Pos.CENTER);

        detailsGrid.add(new Label("Staff ID:"), 0, 0);
        Label idVal = new Label("EMP-00" + selectedEmployee.getUserId());
        idVal.setStyle("-fx-font-weight: 700;");
        detailsGrid.add(idVal, 1, 0);

        detailsGrid.add(new Label("Email:"), 0, 1);
        detailsGrid.add(new Label(selectedEmployee.getEmail()), 1, 1);

        detailsGrid.add(new Label("Phone:"), 0, 2);
        detailsGrid.add(new Label(selectedEmployee.getPhone()), 1, 2);

        detailsGrid.add(new Label("NIC:"), 0, 3);
        detailsGrid.add(new Label(selectedEmployee.getNic()), 1, 3);

        Label qrSimulation = new Label("[ QR / BARCODE: DC-EMP-00" + selectedEmployee.getUserId() + " ]");
        qrSimulation.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 12px; -fx-font-weight: 700; " +
                              "-fx-text-fill: #62656F; -fx-padding: 10; -fx-background-color: #F6F3EC; -fx-background-radius: 8;");

        Separator sep2 = new Separator();

        Button closeBtn = new Button("✔  Print & Close");
        closeBtn.setMaxWidth(Double.MAX_VALUE);
        closeBtn.setPrefHeight(38);
        closeBtn.setStyle("-fx-background-color: #07768F; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> cardStage.close());

        root.getChildren().addAll(brand, badgeTitle, sep1, avatar, name, roleBadge, detailsGrid, qrSimulation, sep2, closeBtn);

        Scene scene = new Scene(root);
        cardStage.setScene(scene);
        cardStage.showAndWait();
    }

    private String getInitials(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return "DC";
        String[] parts = fullName.trim().split(" ");
        if (parts.length == 1) return String.valueOf(parts[0].charAt(0)).toUpperCase();
        return (String.valueOf(parts[0].charAt(0)) + String.valueOf(parts[parts.length - 1].charAt(0))).toUpperCase();
    }

    // ============================================================
    // TAB 2: TIME HELPER BUTTONS
    // ============================================================
    @FXML
    private void handleSetCheckInNow() {
        attCheckInField.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }

    @FXML
    private void handleSetCheckOutNow() {
        attCheckOutField.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }

    // ============================================================
    // TAB 2: RECORD / UPDATE ATTENDANCE
    // ============================================================
    @FXML
    private void handleRecordAttendance() {
        int staffIdx = attStaffComboBox.getSelectionModel().getSelectedIndex();
        if (staffIdx < 0 || staffIdx >= staffUserIdList.size()) {
            showAlert(Alert.AlertType.WARNING, "Staff Required", "Please select an employee.");
            return;
        }

        int targetUserId = staffUserIdList.get(staffIdx);
        LocalDate attDate = attendanceDatePicker.getValue();
        if (attDate == null) {
            attDate = LocalDate.now();
        }

        String status = attStatusComboBox.getValue();
        if (status == null) status = "PRESENT";

        String checkInStr = attCheckInField.getText().trim();
        String checkOutStr = attCheckOutField.getText().trim();

        // Get current logged-in user id as recorder
        User currentUser = Session.getCurrentUser();
        int recordedBy = (currentUser != null) ? currentUser.getUserId() : 1;

        // Check if record exists for this employee on this date
        String checkSql = "SELECT attendance_id FROM employee_attendance WHERE user_id = ? AND attendance_date = ?";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement checkPs = con.prepareStatement(checkSql);
            checkPs.setInt(1, targetUserId);
            checkPs.setDate(2, Date.valueOf(attDate));
            ResultSet rs = checkPs.executeQuery();

            boolean exists = rs.next();
            rs.close(); checkPs.close();

            if (exists) {
                // Update
                String updateSql = "UPDATE employee_attendance SET check_in = ?, check_out = ?, status = ?, recorded_by = ? " +
                                   "WHERE user_id = ? AND attendance_date = ?";
                PreparedStatement ps = con.prepareStatement(updateSql);
                ps.setTime(1, checkInStr.isEmpty() ? null : Time.valueOf(checkInStr));
                ps.setTime(2, checkOutStr.isEmpty() ? null : Time.valueOf(checkOutStr));
                ps.setString(3, status);
                ps.setInt(4, recordedBy);
                ps.setInt(5, targetUserId);
                ps.setDate(6, Date.valueOf(attDate));
                ps.executeUpdate();
                ps.close();

                showAlert(Alert.AlertType.INFORMATION, "Attendance Updated",
                        "Attendance record updated successfully for " + attDate + "!");
            } else {
                // Insert
                String insertSql = "INSERT INTO employee_attendance (user_id, attendance_date, check_in, check_out, status, recorded_by) " +
                                   "VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement ps = con.prepareStatement(insertSql);
                ps.setInt(1, targetUserId);
                ps.setDate(2, Date.valueOf(attDate));
                ps.setTime(3, checkInStr.isEmpty() ? null : Time.valueOf(checkInStr));
                ps.setTime(4, checkOutStr.isEmpty() ? null : Time.valueOf(checkOutStr));
                ps.setString(5, status);
                ps.setInt(6, recordedBy);
                ps.executeUpdate();
                ps.close();

                showAlert(Alert.AlertType.INFORMATION, "Attendance Recorded",
                        "Attendance logged successfully for " + attDate + "!");
            }

            loadEmployeeKpis();
            loadAttendanceForDate(attDate);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save attendance: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.WARNING, "Time Format Error", "Time format must be HH:mm:ss (e.g., 08:30:00).");
        }
    }

    // ============================================================
    // TAB 2: QUICK BATCH - MARK ALL ACTIVE STAFF PRESENT
    // ============================================================
    @FXML
    private void handleMarkAllPresent() {
        LocalDate attDate = attendanceDatePicker.getValue();
        if (attDate == null) attDate = LocalDate.now();

        User currentUser = Session.getCurrentUser();
        int recordedBy = (currentUser != null) ? currentUser.getUserId() : 1;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Quick Batch Attendance");
        confirm.setHeaderText("Mark All Active Staff Present");
        confirm.setContentText("This will record PRESENT (Check-in 08:30:00) for all active staff who do not have attendance recorded for " + attDate + ". Continue?");

        final LocalDate finalDate = attDate;
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                String sql = "INSERT INTO employee_attendance (user_id, attendance_date, check_in, status, recorded_by) " +
                             "SELECT u.user_id, ?, '08:30:00', 'PRESENT', ? " +
                             "FROM users u " +
                             "WHERE u.is_active = 1 " +
                             "AND u.user_id NOT IN (" +
                             "    SELECT user_id FROM employee_attendance WHERE attendance_date = ?" +
                             ")";
                try {
                    Connection con = DBConnection.getConnection();
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setDate(1, Date.valueOf(finalDate));
                    ps.setInt(2, recordedBy);
                    ps.setDate(3, Date.valueOf(finalDate));

                    int inserted = ps.executeUpdate();
                    ps.close();

                    showAlert(Alert.AlertType.INFORMATION, "Batch Recorded",
                            inserted + " staff members marked as PRESENT for " + finalDate + "!");

                    loadEmployeeKpis();
                    loadAttendanceForDate(finalDate);

                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to mark all present: " + e.getMessage());
                }
            }
        });
    }

    // ============================================================
    // TAB 2: PRINT DAILY ATTENDANCE REPORT
    // ============================================================
    @FXML
    private void handlePrintAttendanceReport() {
        LocalDate date = attendanceDatePicker.getValue();
        if (date == null) date = LocalDate.now();

        Stage reportStage = new Stage();
        reportStage.initModality(Modality.APPLICATION_MODAL);
        reportStage.setTitle("DC Royal Printing — Daily Attendance Report");

        VBox root = new VBox(12);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: white; -fx-font-family: 'Segoe UI', sans-serif;");
        root.setAlignment(Pos.TOP_CENTER);
        root.setPrefWidth(550);

        Label brand = new Label("DC ROYAL PRINTING");
        brand.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #14161F;");

        Label sub = new Label("Daily Staff Attendance & Punctuality Report — " + date.toString());
        sub.setStyle("-fx-font-size: 11px; -fx-text-fill: #62656F; -fx-font-weight: 600;");

        Separator sep1 = new Separator();

        VBox rowsBox = new VBox(8);
        int presentCount = 0;
        int lateCount = 0;
        int absentCount = 0;

        for (AttendanceTM a : attendanceList) {
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);

            Label name = new Label(a.getEmployeeName() + " (" + a.getRoleName() + ")");
            name.setStyle("-fx-font-weight: 700; -fx-font-size: 12px;");
            HBox.setHgrow(name, Priority.ALWAYS);

            Label times = new Label(a.getCheckIn() + " - " + a.getCheckOut());
            times.setStyle("-fx-font-size: 11px; -fx-text-fill: #62656F;");

            Label status = new Label("[" + a.getStatus() + "]");
            if (a.getStatus().equals("PRESENT")) {
                status.setStyle("-fx-text-fill: #1A7A46; -fx-font-weight: 700; -fx-font-size: 11px;");
                presentCount++;
            } else if (a.getStatus().equals("LATE")) {
                status.setStyle("-fx-text-fill: #07768F; -fx-font-weight: 700; -fx-font-size: 11px;");
                lateCount++;
            } else {
                status.setStyle("-fx-text-fill: #B8134F; -fx-font-weight: 700; -fx-font-size: 11px;");
                absentCount++;
            }

            row.getChildren().addAll(name, times, status);
            rowsBox.getChildren().add(row);
        }

        if (attendanceList.isEmpty()) {
            rowsBox.getChildren().add(new Label("No attendance recorded for this date."));
        }

        Separator sep2 = new Separator();

        HBox summaryBox = new HBox(16);
        summaryBox.setAlignment(Pos.CENTER);
        Label pLbl = new Label("Present: " + presentCount);
        pLbl.setStyle("-fx-font-weight: 700; -fx-text-fill: #1A7A46;");
        Label lLbl = new Label("Late: " + lateCount);
        lLbl.setStyle("-fx-font-weight: 700; -fx-text-fill: #07768F;");
        Label aLbl = new Label("Absent / Leave: " + absentCount);
        aLbl.setStyle("-fx-font-weight: 700; -fx-text-fill: #B8134F;");
        summaryBox.getChildren().addAll(pLbl, lLbl, aLbl);

        Label sign = new Label("Certified By Manager: _______________________   Date: " + date);
        sign.setStyle("-fx-font-size: 11px; -fx-font-weight: 600; -fx-text-fill: #15171F;");

        Button closeBtn = new Button("✔  Print & Close");
        closeBtn.setMaxWidth(Double.MAX_VALUE);
        closeBtn.setPrefHeight(38);
        closeBtn.setStyle("-fx-background-color: #07768F; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> reportStage.close());

        root.getChildren().addAll(brand, sub, sep1, rowsBox, sep2, summaryBox, sign, closeBtn);

        Scene scene = new Scene(root);
        reportStage.setScene(scene);
        reportStage.showAndWait();
    }

    // ============================================================
    // LIVE SEARCH: TAB 1 EMPLOYEES
    // ============================================================
    private void setupEmployeeSearch() {
        FilteredList<EmployeeTM> filteredData = new FilteredList<>(employeeList, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(emp -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }
                String filter = newValue.toLowerCase().trim();
                return emp.getFullName().toLowerCase().contains(filter) ||
                       emp.getEmail().toLowerCase().contains(filter) ||
                       emp.getRoleName().toLowerCase().contains(filter) ||
                       emp.getPhone().contains(filter) ||
                       emp.getNic().toLowerCase().contains(filter);
            });
        });

        SortedList<EmployeeTM> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(employeeTable.comparatorProperty());
        employeeTable.setItems(sortedData);
    }

    private void filterEmployeesByRole() {
        String selectedRole = roleFilterComboBox.getValue();
        if (selectedRole == null || selectedRole.equals("ALL")) {
            employeeTable.setItems(employeeList);
            return;
        }

        ObservableList<EmployeeTM> filtered = FXCollections.observableArrayList();
        for (EmployeeTM emp : employeeList) {
            if (emp.getRoleName().equalsIgnoreCase(selectedRole)) {
                filtered.add(emp);
            }
        }
        employeeTable.setItems(filtered);
    }

    // ============================================================
    // LIVE SEARCH: TAB 2 ATTENDANCE
    // ============================================================
    private void setupAttendanceSearch() {
        FilteredList<AttendanceTM> filteredData = new FilteredList<>(attendanceList, p -> true);

        attSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(att -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }
                String filter = newValue.toLowerCase().trim();
                return att.getEmployeeName().toLowerCase().contains(filter) ||
                       att.getRoleName().toLowerCase().contains(filter) ||
                       att.getStatus().toLowerCase().contains(filter);
            });
        });

        SortedList<AttendanceTM> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(attendanceTable.comparatorProperty());
        attendanceTable.setItems(sortedData);
    }

    private void filterAttendance() {
        String filter = attStatusFilterComboBox.getValue();
        if (filter == null || filter.equals("ALL")) {
            attendanceTable.setItems(attendanceList);
            return;
        }

        ObservableList<AttendanceTM> filtered = FXCollections.observableArrayList();
        for (AttendanceTM a : attendanceList) {
            if (a.getStatus().equalsIgnoreCase(filter)) {
                filtered.add(a);
            }
        }
        attendanceTable.setItems(filtered);
    }

    // ============================================================
    // REFRESH ALL
    // ============================================================
    @FXML
    private void handleRefresh() {
        loadEmployeeKpis();
        loadAllEmployees();
        loadAttendanceStaffDropdown();
        loadAttendanceForDate(attendanceDatePicker.getValue());
        clearStaffForm();
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
