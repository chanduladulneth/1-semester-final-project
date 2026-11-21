package com.dcroyal.controller;

import com.dcroyal.db.DBConnection;
import com.dcroyal.tm.RoleTM;
import com.dcroyal.tm.UserTM;
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

/**
 * UserController.java
 * Manages system user accounts, credentials, role assignments,
 * account suspension/reactivation, and Role-Based Access Control (RBAC) matrix with direct JDBC.
 */
public class UserController {

    // ============================================================
    // FXML: TOP KPI SUMMARY LABELS
    // ============================================================
    @FXML private Label totalUsersLabel;
    @FXML private Label activeUsersLabel;
    @FXML private Label suspendedUsersLabel;
    @FXML private Label rolesCountLabel;

    // ============================================================
    // FXML: TAB 1 - USER ACCOUNTS TABLE & FILTERS
    // ============================================================
    @FXML private TableView<UserTM> userTable;
    @FXML private TableColumn<UserTM, Integer> colId;
    @FXML private TableColumn<UserTM, String>  colName;
    @FXML private TableColumn<UserTM, String>  colEmail;
    @FXML private TableColumn<UserTM, String>  colRole;
    @FXML private TableColumn<UserTM, String>  colStatus;
    @FXML private TableColumn<UserTM, String>  colRegistered;

    @FXML private TextField        searchField;
    @FXML private ComboBox<String> roleFilterComboBox;

    // User Form Controls
    @FXML private Label            userAvatarLabel;
    @FXML private TextField        nameField;
    @FXML private TextField        emailField;
    @FXML private PasswordField    passwordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private CheckBox         activeCheckBox;

    @FXML private Button addUserButton;
    @FXML private Button updateUserButton;
    @FXML private Button toggleSuspendButton;
    @FXML private Button resetPasswordButton;
    @FXML private Button clearFormButton;

    // ============================================================
    // FXML: TAB 2 - RBAC & ROLES DIRECTORY
    // ============================================================
    @FXML private TableView<RoleTM> roleTable;
    @FXML private TableColumn<RoleTM, Integer> colRoleId;
    @FXML private TableColumn<RoleTM, String>  colRoleCode;
    @FXML private TableColumn<RoleTM, String>  colRoleName;
    @FXML private TableColumn<RoleTM, String>  colRoleDesc;
    @FXML private TableColumn<RoleTM, Integer> colRoleUsers;

    // Data lists
    private ObservableList<UserTM> userList = FXCollections.observableArrayList();
    private ObservableList<RoleTM> roleList = FXCollections.observableArrayList();

    // Mappings for roles
    private List<Integer> roleIdList = new ArrayList<>();

    // Selected user in table
    private UserTM selectedUser = null;

    // ============================================================
    // INITIALIZE
    // ============================================================
    @FXML
    public void initialize() {
        // --- 1. Tab 1 User Table Setup ---
        colId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("roleName"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colRegistered.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        userTable.setItems(userList);

        // --- 2. Tab 2 Role Table Setup ---
        colRoleId.setCellValueFactory(new PropertyValueFactory<>("roleId"));
        colRoleCode.setCellValueFactory(new PropertyValueFactory<>("roleCode"));
        colRoleName.setCellValueFactory(new PropertyValueFactory<>("roleName"));
        colRoleDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colRoleUsers.setCellValueFactory(new PropertyValueFactory<>("userCount"));
        roleTable.setItems(roleList);

        // --- 3. Selection Listener for User Table ---
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedUser = newVal;
            populateUserForm(newVal);
        });

        // --- 4. Initial Data Load ---
        loadRoles();
        loadUserKpis();
        loadAllUsers();
        loadRoleDirectory();

        // --- 5. Setup Live Search ---
        setupLiveSearch();
    }

    // ============================================================
    // LOAD KPIS
    // ============================================================
    private void loadUserKpis() {
        try {
            Connection con = DBConnection.getConnection();

            // 1. Total Users
            String sql1 = "SELECT COUNT(*) FROM users";
            PreparedStatement ps1 = con.prepareStatement(sql1);
            ResultSet rs1 = ps1.executeQuery();
            if (rs1.next()) {
                totalUsersLabel.setText(String.valueOf(rs1.getInt(1)));
            }
            rs1.close(); ps1.close();

            // 2. Active Users
            String sql2 = "SELECT COUNT(*) FROM users WHERE is_active = 1";
            PreparedStatement ps2 = con.prepareStatement(sql2);
            ResultSet rs2 = ps2.executeQuery();
            if (rs2.next()) {
                activeUsersLabel.setText(String.valueOf(rs2.getInt(1)));
            }
            rs2.close(); ps2.close();

            // 3. Suspended Users
            String sql3 = "SELECT COUNT(*) FROM users WHERE is_active = 0";
            PreparedStatement ps3 = con.prepareStatement(sql3);
            ResultSet rs3 = ps3.executeQuery();
            if (rs3.next()) {
                suspendedUsersLabel.setText(String.valueOf(rs3.getInt(1)));
            }
            rs3.close(); ps3.close();

            // 4. System Roles
            String sql4 = "SELECT COUNT(*) FROM roles";
            PreparedStatement ps4 = con.prepareStatement(sql4);
            ResultSet rs4 = ps4.executeQuery();
            if (rs4.next()) {
                rolesCountLabel.setText(String.valueOf(rs4.getInt(1)));
            }
            rs4.close(); ps4.close();

        } catch (SQLException e) {
            System.out.println("Error loading user KPIs: " + e.getMessage());
        }
    }

    // ============================================================
    // LOAD ROLES FOR FORM & FILTER
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
            roleFilterComboBox.setOnAction(e -> filterUsersByRole());

        } catch (SQLException e) {
            System.out.println("Error loading roles: " + e.getMessage());
        }
    }

    // ============================================================
    // LOAD ALL USERS
    // ============================================================
    private void loadAllUsers() {
        userList.clear();
        String sql = "SELECT u.user_id, u.full_name, u.email, u.role_id, r.role_code, r.role_name, " +
                     "u.is_active, DATE_FORMAT(u.created_at, '%Y-%m-%d %H:%i') as reg_date " +
                     "FROM users u " +
                     "JOIN roles r ON u.role_id = r.role_id " +
                     "ORDER BY u.user_id ASC";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                UserTM tm = new UserTM(
                        rs.getInt("user_id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getInt("role_id"),
                        rs.getString("role_code"),
                        rs.getString("role_name"),
                        rs.getBoolean("is_active"),
                        rs.getString("reg_date")
                );
                userList.add(tm);
            }
            rs.close(); ps.close();

        } catch (SQLException e) {
            System.out.println("Error loading users: " + e.getMessage());
        }
    }

    // ============================================================
    // LOAD TAB 2 ROLES DIRECTORY
    // ============================================================
    private void loadRoleDirectory() {
        roleList.clear();
        String sql = "SELECT r.role_id, r.role_code, r.role_name, r.description, " +
                     "COUNT(u.user_id) as user_count " +
                     "FROM roles r " +
                     "LEFT JOIN users u ON r.role_id = u.role_id " +
                     "GROUP BY r.role_id, r.role_code, r.role_name, r.description " +
                     "ORDER BY r.role_id ASC";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                RoleTM tm = new RoleTM(
                        rs.getInt("role_id"),
                        rs.getString("role_code"),
                        rs.getString("role_name"),
                        rs.getString("description"),
                        rs.getInt("user_count")
                );
                roleList.add(tm);
            }
            rs.close(); ps.close();

        } catch (SQLException e) {
            System.out.println("Error loading role directory: " + e.getMessage());
        }
    }

    // ============================================================
    // POPULATE FORM ON SELECTION
    // ============================================================
    private void populateUserForm(UserTM tm) {
        if (tm == null) {
            clearForm();
            return;
        }

        nameField.setText(tm.getFullName());
        emailField.setText(tm.getEmail());
        passwordField.clear();
        roleComboBox.setValue(tm.getRoleName());
        activeCheckBox.setSelected(tm.isActive());

        userAvatarLabel.setText(getInitials(tm.getFullName()));

        addUserButton.setDisable(true);
        updateUserButton.setDisable(false);
        toggleSuspendButton.setDisable(false);
        resetPasswordButton.setDisable(false);

        toggleSuspendButton.setText(tm.isActive() ? "⛔  Suspend Account" : "✔  Reactivate Account");
    }

    // ============================================================
    // ACTION: ADD USER
    // ============================================================
    @FXML
    private void handleAddUser() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        int roleIdx = roleComboBox.getSelectionModel().getSelectedIndex();
        boolean isActive = activeCheckBox.isSelected();

        if (name.isEmpty() || email.isEmpty() || roleIdx < 0) {
            showAlert(Alert.AlertType.WARNING, "Missing Fields", "Full Name, Email, and Role are mandatory.");
            return;
        }

        if (password.isEmpty()) {
            password = "123"; // Default initial login password
        }

        int roleId = roleIdList.get(roleIdx);

        String sql = "INSERT INTO users (role_id, full_name, email, password_hash, is_active) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, roleId);
            ps.setString(2, name);
            ps.setString(3, email);
            ps.setString(4, password);
            ps.setInt(5, isActive ? 1 : 0);

            int rows = ps.executeUpdate();
            ps.close();

            if (rows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "User Created",
                        "User account for '" + name + "' created successfully!");
                loadUserKpis();
                loadAllUsers();
                loadRoleDirectory();
                clearForm();
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to create user: " + e.getMessage());
        }
    }

    // ============================================================
    // ACTION: UPDATE USER
    // ============================================================
    @FXML
    private void handleUpdateUser() {
        if (selectedUser == null) return;

        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        int roleIdx = roleComboBox.getSelectionModel().getSelectedIndex();
        boolean isActive = activeCheckBox.isSelected();

        if (name.isEmpty() || email.isEmpty() || roleIdx < 0) {
            showAlert(Alert.AlertType.WARNING, "Missing Fields", "Full Name, Email, and Role are mandatory.");
            return;
        }

        int roleId = roleIdList.get(roleIdx);

        String sql;
        if (password.isEmpty()) {
            sql = "UPDATE users SET role_id = ?, full_name = ?, email = ?, is_active = ? WHERE user_id = ?";
        } else {
            sql = "UPDATE users SET role_id = ?, full_name = ?, email = ?, password_hash = ?, is_active = ? WHERE user_id = ?";
        }

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, roleId);
            ps.setString(2, name);
            ps.setString(3, email);

            int pIdx = 4;
            if (!password.isEmpty()) {
                ps.setString(pIdx++, password);
            }
            ps.setInt(pIdx++, isActive ? 1 : 0);
            ps.setInt(pIdx, selectedUser.getUserId());

            int rows = ps.executeUpdate();
            ps.close();

            if (rows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "User Updated",
                        "Account details for '" + name + "' updated successfully!");
                loadUserKpis();
                loadAllUsers();
                loadRoleDirectory();
                clearForm();
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update user: " + e.getMessage());
        }
    }

    // ============================================================
    // ACTION: SUSPEND / REACTIVATE
    // ============================================================
    @FXML
    private void handleToggleSuspend() {
        if (selectedUser == null) return;

        if (selectedUser.getUserId() == 1) {
            showAlert(Alert.AlertType.WARNING, "Protected Account", "The Primary System Owner account cannot be suspended.");
            return;
        }

        boolean newActive = !selectedUser.isActive();
        String actionName = newActive ? "REACTIVATE" : "SUSPEND";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Account Status Change");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to " + actionName + " access for " + selectedUser.getFullName() + "?");

        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                String sql = "UPDATE users SET is_active = ? WHERE user_id = ?";
                try {
                    Connection con = DBConnection.getConnection();
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setInt(1, newActive ? 1 : 0);
                    ps.setInt(2, selectedUser.getUserId());
                    ps.executeUpdate();
                    ps.close();

                    showAlert(Alert.AlertType.INFORMATION, "Status Changed",
                            "Account " + selectedUser.getFullName() + " has been " + (newActive ? "REACTIVATED" : "SUSPENDED") + ".");
                    loadUserKpis();
                    loadAllUsers();
                    clearForm();

                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update status: " + e.getMessage());
                }
            }
        });
    }

    // ============================================================
    // ACTION: RESET PASSWORD
    // ============================================================
    @FXML
    private void handleResetPassword() {
        if (selectedUser == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Reset Password");
        confirm.setHeaderText("Reset password for " + selectedUser.getFullName());
        confirm.setContentText("The password will be reset to default '123'. Continue?");

        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                String sql = "UPDATE users SET password_hash = '123' WHERE user_id = ?";
                try {
                    Connection con = DBConnection.getConnection();
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setInt(1, selectedUser.getUserId());
                    ps.executeUpdate();
                    ps.close();

                    showAlert(Alert.AlertType.INFORMATION, "Password Reset",
                            "Password for " + selectedUser.getFullName() + " has been reset to '123'.");

                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to reset password: " + e.getMessage());
                }
            }
        });
    }

    // ============================================================
    // CLEAR FORM
    // ============================================================
    @FXML
    private void handleClearForm() {
        clearForm();
    }

    private void clearForm() {
        selectedUser = null;
        userTable.getSelectionModel().clearSelection();
        nameField.clear();
        emailField.clear();
        passwordField.clear();
        activeCheckBox.setSelected(true);
        userAvatarLabel.setText("DC");
        if (!roleComboBox.getItems().isEmpty()) {
            roleComboBox.getSelectionModel().selectFirst();
        }

        addUserButton.setDisable(false);
        updateUserButton.setDisable(true);
        toggleSuspendButton.setDisable(true);
        resetPasswordButton.setDisable(true);
    }

    // ============================================================
    // LIVE SEARCH
    // ============================================================
    private void setupLiveSearch() {
        FilteredList<UserTM> filteredData = new FilteredList<>(userList, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(u -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }
                String filter = newValue.toLowerCase().trim();
                return u.getFullName().toLowerCase().contains(filter) ||
                       u.getEmail().toLowerCase().contains(filter) ||
                       u.getRoleName().toLowerCase().contains(filter) ||
                       u.getStatus().toLowerCase().contains(filter);
            });
        });

        SortedList<UserTM> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(userTable.comparatorProperty());
        userTable.setItems(sortedData);
    }

    private void filterUsersByRole() {
        String filter = roleFilterComboBox.getValue();
        if (filter == null || filter.equals("ALL")) {
            userTable.setItems(userList);
            return;
        }

        ObservableList<UserTM> filtered = FXCollections.observableArrayList();
        for (UserTM u : userList) {
            if (u.getRoleName().equalsIgnoreCase(filter)) {
                filtered.add(u);
            }
        }
        userTable.setItems(filtered);
    }

    // ============================================================
    // REFRESH
    // ============================================================
    @FXML
    private void handleRefresh() {
        loadUserKpis();
        loadAllUsers();
        loadRoleDirectory();
        clearForm();
    }

    private String getInitials(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return "DC";
        String[] parts = fullName.trim().split(" ");
        if (parts.length == 1) return String.valueOf(parts[0].charAt(0)).toUpperCase();
        return (String.valueOf(parts[0].charAt(0)) + String.valueOf(parts[parts.length - 1].charAt(0))).toUpperCase();
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
