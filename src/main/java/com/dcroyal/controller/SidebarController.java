package com.dcroyal.controller;

import com.dcroyal.model.User;
import com.dcroyal.util.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * SidebarController.java
 * Controls the left sidebar panel (Sidebar.fxml).
 * - Shows the logo, role-specific nav menu, user card, and logout button
 * - Navigates between screens (Dashboard, Customers, etc.)
 * - Logout button securely clears session and returns to login
 */
public class SidebarController {

    // ---- FXML Elements (connected to Sidebar.fxml) ----
    @FXML private VBox  navBox;           // Sidebar nav buttons go here
    @FXML private Label userInitialLabel; // User initials circle
    @FXML private Label userNameLabel;    // User full name
    @FXML private Label userRoleLabel;    // User role text

    // ============================================================
    // INITIALIZE — called automatically when Sidebar.fxml loads
    // ============================================================
    @FXML
    public void initialize() {

        User user = Session.getCurrentUser();

        if (user == null) {
            System.out.println("SidebarController: No user in session!");
            return;
        }

        // Fill in user info at the bottom of the sidebar
        userInitialLabel.setText(user.getInitials());
        userNameLabel.setText(user.getFullName());
        userRoleLabel.setText(user.getRoleName());

        // Build the nav menu for this role
        buildSidebar(user.getRoleCode());
    }

    // ============================================================
    // BUILD SIDEBAR — adds nav buttons based on role
    // ============================================================
    private void buildSidebar(String roleCode) {
        navBox.getChildren().clear();

        String[][] items = getNavItems(roleCode);

        for (String[] item : items) {
            String label  = item[0]; // e.g. "🏠  Dashboard"
            String screen = item[1]; // e.g. "DASHBOARD"
            navBox.getChildren().add(createNavButton(label, screen));
        }
    }

    /**
     * Returns the nav menu items for each role.
     * Format: { "Button Label", "SCREEN_KEY" }
     */
    private String[][] getNavItems(String roleCode) {
        switch (roleCode) {
            case "OWNER":
                return new String[][]{
                    {"🏠   Dashboard",    "DASHBOARD"},
                    {"👥   Customers",    "CUSTOMERS"},
                    {"📋   Orders",       "ORDERS"},
                    {"🎨   Designs",      "DESIGNS"},
                    {"🖨️   Print Queue",  "PRINT_QUEUE"},
                    {"📦   Inventory",    "INVENTORY"},
                    {"📫   Packing",      "PACKING"},
                    {"🚚   Deliveries",   "DELIVERIES"},
                    {"👷   Employees",    "EMPLOYEES"},
                    {"💰   Salary",       "SALARY"},
                    {"💳   Expenses",     "EXPENSES"},
                    {"📊   Reports",      "REPORTS"},
                    {"👤   Users",        "USERS"},
                    {"⚙️   Settings",     "SETTINGS"},
                    {"🚪   Logout",       "LOGOUT"}
                };
            case "MANAGER":
                return new String[][]{
                    {"🏠   Dashboard",    "DASHBOARD"},
                    {"👥   Customers",    "CUSTOMERS"},
                    {"📋   Orders",       "ORDERS"},
                    {"👷   Employees",    "EMPLOYEES"},
                    {"📅   Attendance",   "ATTENDANCE"},
                    {"💰   Salary",       "SALARY"},
                    {"💳   Expenses",     "EXPENSES"},
                    {"📊   Reports",      "REPORTS"},
                    {"🚪   Logout",       "LOGOUT"}
                };
            case "COUNTER_STAFF":
                return new String[][]{
                    {"🏠   Dashboard",    "DASHBOARD"},
                    {"👥   Customers",    "CUSTOMERS"},
                    {"➕   New Order",    "NEW_ORDER"},
                    {"📋   Orders",       "ORDERS"},
                    {"🔍   QR Scan",      "QR_SCAN"},
                    {"🚪   Logout",       "LOGOUT"}
                };
            case "DESIGNER":
                return new String[][]{
                    {"🏠   Dashboard",    "DASHBOARD"},
                    {"🎨   My Designs",   "MY_DESIGNS"},
                    {"📂   All Designs",  "ALL_DESIGNS"},
                    {"🚪   Logout",       "LOGOUT"}
                };
            case "PRINTER_OPERATOR":
                return new String[][]{
                    {"🏠   Dashboard",    "DASHBOARD"},
                    {"🖨️   Print Queue",  "PRINT_QUEUE"},
                    {"📦   Stock Request","STOCK_REQUEST"},
                    {"🚪   Logout",       "LOGOUT"}
                };
            case "INVENTORY_STAFF":
                return new String[][]{
                    {"🏠   Dashboard",    "DASHBOARD"},
                    {"📦   Inventory",    "INVENTORY"},
                    {"📊   Stock Report", "STOCK_REPORT"},
                    {"🚪   Logout",       "LOGOUT"}
                };
            case "PACKING_STAFF":
                return new String[][]{
                    {"🏠   Dashboard",    "DASHBOARD"},
                    {"📫   Pack Orders",  "PACK_ORDERS"},
                    {"📄   Packing Report","PACKING_REPORT"},
                    {"🚪   Logout",       "LOGOUT"}
                };
            case "DELIVERY_STAFF":
                return new String[][]{
                    {"🏠   Dashboard",    "DASHBOARD"},
                    {"🚚   My Deliveries","MY_DELIVERIES"},
                    {"📄   Delivery Report","DELIVERY_REPORT"},
                    {"🚪   Logout",       "LOGOUT"}
                };
            default:
                return new String[][]{
                    {"🚪   Logout", "LOGOUT"}
                };
        }
    }

    /**
     * Creates a styled sidebar navigation button.
     */
    private Button createNavButton(String label, String screen) {
        Button btn = new Button(label);
        btn.getStyleClass().add("nav-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setOnAction(e -> handleNavClick(screen));
        return btn;
    }

    // ============================================================
    // NAVIGATION CLICK
    // ============================================================
    private void handleNavClick(String screen) {
        if (screen.equals("LOGOUT")) {
            handleLogout();
        } else if (screen.equals("DASHBOARD")) {
            navigateToDashboard();
        } else if (screen.equals("CUSTOMERS")) {
            navigateTo("/com/dcroyal/view/CustomerView.fxml", "DC Royal Printing — Customer Management");
        } else if (screen.equals("ORDERS") || screen.equals("NEW_ORDER")) {
            navigateTo("/com/dcroyal/view/OrderView.fxml", "DC Royal Printing — Order Management & Billing");
        } else if (screen.equals("DESIGNS") || screen.equals("MY_DESIGNS") || screen.equals("ALL_DESIGNS")) {
            navigateTo("/com/dcroyal/view/DesignView.fxml", "DC Royal Printing — Design Management & Approvals");
        } else if (screen.equals("PRINT_QUEUE")) {
            navigateTo("/com/dcroyal/view/PrintQueueView.fxml", "DC Royal Printing — Production Print Queue");
        } else if (screen.equals("STOCK_REQUEST")) {
            navigateTo("/com/dcroyal/view/StockRequestView.fxml",
                    "DC Royal Printing — Stock Request");
        } else if (screen.equals("INVENTORY") || screen.equals("STOCK_REPORT")) {
            navigateTo("/com/dcroyal/view/InventoryView.fxml", "DC Royal Printing — Inventory & QR Card Stock Tracking");
        } else if (screen.equals("PACKING") || screen.equals("PACK_ORDERS") || screen.equals("PACKING_REPORT")) {
            navigateTo("/com/dcroyal/view/PackingView.fxml", "DC Royal Printing — Packing & Delivery Management");
        } else if (screen.equals("DELIVERIES") || screen.equals("MY_DELIVERIES") || screen.equals("DELIVERY_REPORT")) {
            navigateTo("/com/dcroyal/view/DeliveryView.fxml", "DC Royal Printing — Delivery Management & Driver Routing");
        } else if (screen.equals("EMPLOYEES") || screen.equals("ATTENDANCE")) {
            navigateTo("/com/dcroyal/view/EmployeeView.fxml", "DC Royal Printing — Employee & Attendance Management");
        } else if (screen.equals("SALARY")) {
            navigateTo("/com/dcroyal/view/SalaryView.fxml", "DC Royal Printing — Employee Salary & Payroll Management");
        } else if (screen.equals("EXPENSES")) {
            navigateTo("/com/dcroyal/view/ExpenseView.fxml", "DC Royal Printing — Business Expenses & Fund Transfers");
        } else if (screen.equals("USERS")) {
            navigateTo("/com/dcroyal/view/UserView.fxml", "DC Royal Printing — User Accounts & Access Control");
        }
        // Additional screen handlers will be added as each screen is implemented
    }

    /**
     * Navigates back to the specific dashboard for the logged-in user's role.
     */
    private void navigateToDashboard() {
        User user = Session.getCurrentUser();
        if (user == null) return;

        String roleCode = user.getRoleCode();
        String fxmlFile;
        switch (roleCode) {
            case "OWNER":           fxmlFile = "/com/dcroyal/view/OwnerDashboard.fxml"; break;
            case "MANAGER":         fxmlFile = "/com/dcroyal/view/ManagerDashboard.fxml"; break;
            case "COUNTER_STAFF":   fxmlFile = "/com/dcroyal/view/CounterDashboard.fxml"; break;
            case "DESIGNER":        fxmlFile = "/com/dcroyal/view/DesignerDashboard.fxml"; break;
            case "PRINTER_OPERATOR":fxmlFile = "/com/dcroyal/view/OperatorDashboard.fxml"; break;
            case "INVENTORY_STAFF": fxmlFile = "/com/dcroyal/view/InventoryDashboard.fxml"; break;
            case "PACKING_STAFF":   fxmlFile = "/com/dcroyal/view/PackingDashboard.fxml"; break;
            case "DELIVERY_STAFF":  fxmlFile = "/com/dcroyal/view/DeliveryDashboard.fxml"; break;
            default:
                handleLogout();
                return;
        }

        navigateTo(fxmlFile, "DC Royal Printing — " + user.getRoleName() + " Dashboard");
    }

    /**
     * General screen navigation helper.
     */
    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load(), 1280, 760);
            scene.getStylesheets().add(
                    getClass().getResource("/com/dcroyal/css/style.css").toExternalForm()
            );

            Stage stage = (Stage) navBox.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(title);
            stage.setMaximized(true);

        } catch (Exception e) {
            System.out.println("Navigation error to " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ============================================================
    // LOGOUT
    // ============================================================
    @FXML
    private void handleLogout() {
        try {
            // Clear the user session
            Session.clearSession();

            // Load the login screen
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/dcroyal/view/login.fxml")
            );
            Scene scene = new Scene(loader.load(), 900, 600);
            scene.getStylesheets().add(
                    getClass().getResource("/com/dcroyal/css/style.css").toExternalForm()
            );

            // Switch the window back to login
            Stage stage = (Stage) navBox.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("DC Royal Printing — Staff Login");
            stage.setMinWidth(0);
            stage.setMinHeight(0);
            stage.setMaximized(false);
            stage.setResizable(false);
            stage.setWidth(900);
            stage.setHeight(600);
            stage.centerOnScreen();

        } catch (Exception e) {
            System.out.println("Logout error: " + e.getMessage());
        }
    }
}
