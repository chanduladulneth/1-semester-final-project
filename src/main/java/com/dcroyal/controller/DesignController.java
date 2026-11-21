package com.dcroyal.controller;

import com.dcroyal.db.DBConnection;
import com.dcroyal.tm.DesignTM;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DesignController.java
 * Handles customer and in-house design management:
 * - 3D Printing, DTF, UV Flatbed, and UV Rotary design records
 * - File attachment / file path selection
 * - Status transitions (PENDING -> IN_PROGRESS -> APPROVED)
 * - Filtering and search
 */
public class DesignController {

    // ---- FXML Form Controls ----
    @FXML private TextField        designCodeField;
    @FXML private ComboBox<String> customerComboBox;
    @FXML private ComboBox<String> printCategoryComboBox;
    @FXML private ComboBox<String> materialComboBox;
    @FXML private ComboBox<String> sourceTypeComboBox;
    @FXML private ComboBox<String> designerComboBox;
    @FXML private TextField        filePathField;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private TextArea         notesArea;

    // ---- FXML Buttons ----
    @FXML private Button saveButton;
    @FXML private Button updateButton;
    @FXML private Button approveButton;
    @FXML private Button clearButton;

    // ---- FXML Search & Filter Controls ----
    @FXML private TextField        searchField;
    @FXML private ComboBox<String> filterStatusComboBox;

    // ---- FXML TableView & Columns ----
    @FXML private TableView<DesignTM> designTable;
    @FXML private TableColumn<DesignTM, String> colCode;
    @FXML private TableColumn<DesignTM, String> colCustomer;
    @FXML private TableColumn<DesignTM, String> colPhone;
    @FXML private TableColumn<DesignTM, String> colCategory;
    @FXML private TableColumn<DesignTM, String> colMaterial;
    @FXML private TableColumn<DesignTM, String> colSource;
    @FXML private TableColumn<DesignTM, String> colDesigner;
    @FXML private TableColumn<DesignTM, String> colStatus;
    @FXML private TableColumn<DesignTM, String> colFile;
    @FXML private TableColumn<DesignTM, String> colDate;

    // ID tracking for combo selections
    private List<Integer> customerIdList = new ArrayList<>();
    private List<Integer> designerIdList = new ArrayList<>();

    // Master design list for table
    private ObservableList<DesignTM> designList = FXCollections.observableArrayList();

    // ============================================================
    // INITIALIZE
    // ============================================================
    @FXML
    public void initialize() {
        // Link table columns with DesignTM properties
        colCode.setCellValueFactory(new PropertyValueFactory<>("designCode"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("customerPhone"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("printCategory"));
        colMaterial.setCellValueFactory(new PropertyValueFactory<>("materialType"));
        colSource.setCellValueFactory(new PropertyValueFactory<>("sourceType"));
        colDesigner.setCellValueFactory(new PropertyValueFactory<>("designerName"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colFile.setCellValueFactory(new PropertyValueFactory<>("filePath"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        designTable.setItems(designList);

        // Initialize dropdowns
        initDropdowns();

        // Load customers & designers from DB
        loadCustomers();
        loadDesigners();

        // Load all designs into table
        loadAllDesigns();

        // Generate next design code
        generateNextDesignCode();

        // Row selection listener
        designTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                populateFields(newVal);
            }
        });

        // Setup search filter
        setupLiveSearch();
    }

    // ============================================================
    // INITIALIZE DROPDOWNS
    // ============================================================
    private void initDropdowns() {
        printCategoryComboBox.setItems(FXCollections.observableArrayList(
                "3D_PRINTING", "DTF", "UV_FLATBED", "UV_ROTARY"
        ));
        printCategoryComboBox.getSelectionModel().selectFirst();

        materialComboBox.setItems(FXCollections.observableArrayList(
                "Cotton T-Shirt", "Stainless Steel Bottle", "Glass / Tumbler",
                "Wood Plaque / Sign", "PLA / Resin 3D Print", "Acrylic Block / Trophy",
                "Ceramic Mug", "Metal Plate / Aluminum"
        ));
        materialComboBox.getSelectionModel().selectFirst();

        sourceTypeComboBox.setItems(FXCollections.observableArrayList(
                "CUSTOMER_PROVIDED", "IN_HOUSE_DESIGNED"
        ));
        sourceTypeComboBox.getSelectionModel().selectFirst();

        statusComboBox.setItems(FXCollections.observableArrayList(
                "PENDING", "IN_PROGRESS", "APPROVED", "REJECTED"
        ));
        statusComboBox.getSelectionModel().selectFirst();

        filterStatusComboBox.setItems(FXCollections.observableArrayList(
                "ALL", "PENDING", "IN_PROGRESS", "APPROVED", "REJECTED"
        ));
        filterStatusComboBox.getSelectionModel().selectFirst();
        filterStatusComboBox.setOnAction(e -> filterDesignsByStatus());
    }

    // ============================================================
    // LOAD CUSTOMERS
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
                customerIdList.add(rs.getInt("customer_id"));
                customerComboBox.getItems().add(
                        rs.getString("customer_code") + " — " + rs.getString("full_name") + " (" + rs.getString("phone") + ")"
                );
            }
            rs.close();
            ps.close();

            if (!customerComboBox.getItems().isEmpty()) {
                customerComboBox.getSelectionModel().selectFirst();
            }

        } catch (SQLException e) {
            System.out.println("Error loading customers: " + e.getMessage());
        }
    }

    // ============================================================
    // LOAD DESIGNERS
    // ============================================================
    private void loadDesigners() {
        designerComboBox.getItems().clear();
        designerIdList.clear();

        String sql = "SELECT u.user_id, u.full_name, r.role_code FROM users u " +
                     "JOIN roles r ON u.role_id = r.role_id " +
                     "WHERE r.role_code IN ('DESIGNER', 'OWNER', 'MANAGER') AND u.is_active = 1 " +
                     "ORDER BY u.full_name ASC";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                designerIdList.add(rs.getInt("user_id"));
                designerComboBox.getItems().add(rs.getString("full_name") + " (" + rs.getString("role_code") + ")");
            }
            rs.close();
            ps.close();

            if (!designerComboBox.getItems().isEmpty()) {
                designerComboBox.getSelectionModel().selectFirst();
            }

        } catch (SQLException e) {
            System.out.println("Error loading designers: " + e.getMessage());
        }
    }

    // ============================================================
    // LOAD ALL DESIGNS
    // ============================================================
    private void loadAllDesigns() {
        designList.clear();
        String sql = "SELECT d.design_id, d.design_code, c.full_name as cust_name, c.phone, " +
                     "d.print_category, d.material_type, d.source_type, " +
                     "COALESCE(u.full_name, 'Unassigned') as designer_name, d.status, " +
                     "COALESCE(d.file_path, 'No file attached') as file_path, " +
                     "COALESCE(d.notes, '') as notes, " +
                     "DATE_FORMAT(d.created_at, '%Y-%m-%d') as created_date " +
                     "FROM designs d " +
                     "JOIN customers c ON d.customer_id = c.customer_id " +
                     "LEFT JOIN users u ON d.designer_id = u.user_id " +
                     "ORDER BY d.design_id DESC";

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                DesignTM tm = new DesignTM(
                        rs.getInt("design_id"),
                        rs.getString("design_code"),
                        rs.getString("cust_name"),
                        rs.getString("phone"),
                        rs.getString("print_category"),
                        rs.getString("material_type"),
                        rs.getString("source_type"),
                        rs.getString("designer_name"),
                        rs.getString("status"),
                        rs.getString("file_path"),
                        rs.getString("notes"),
                        rs.getString("created_date")
                );
                designList.add(tm);
            }
            rs.close();
            ps.close();

        } catch (SQLException e) {
            System.out.println("Error loading designs: " + e.getMessage());
        }
    }

    // ============================================================
    // AUTO GENERATE NEXT DESIGN CODE (e.g. DSN-0003)
    // ============================================================
    private void generateNextDesignCode() {
        String sql = "SELECT design_code FROM designs ORDER BY design_id DESC LIMIT 1";

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String lastCode = rs.getString("design_code"); // e.g. "DSN-0002"
                String[] parts = lastCode.split("-");
                if (parts.length == 2) {
                    int nextNum = Integer.parseInt(parts[1]) + 1;
                    designCodeField.setText(String.format("DSN-%04d", nextNum));
                } else {
                    designCodeField.setText("DSN-0001");
                }
            } else {
                designCodeField.setText("DSN-0001");
            }
            rs.close();
            ps.close();

        } catch (Exception e) {
            designCodeField.setText("DSN-0001");
        }
    }

    // ============================================================
    // BROWSE DESIGN FILE
    // ============================================================
    @FXML
    private void handleBrowseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Design File (Artwork / Model)");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Design Files (*.png, *.jpg, *.pdf, *.svg, *.stl)",
                        "*.png", "*.jpg", "*.jpeg", "*.pdf", "*.svg", "*.stl", "*.ai", "*.psd"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        Stage stage = (Stage) designCodeField.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            filePathField.setText(selectedFile.getAbsolutePath());
        }
    }

    // ============================================================
    // POPULATE FIELDS ON ROW SELECTION
    // ============================================================
    private void populateFields(DesignTM tm) {
        designCodeField.setText(tm.getDesignCode());

        // Match customer in combo
        for (int i = 0; i < customerComboBox.getItems().size(); i++) {
            if (customerComboBox.getItems().get(i).contains(tm.getCustomerName())) {
                customerComboBox.getSelectionModel().select(i);
                break;
            }
        }

        printCategoryComboBox.setValue(tm.getPrintCategory());
        materialComboBox.setValue(tm.getMaterialType());
        sourceTypeComboBox.setValue(tm.getSourceType());

        // Match designer in combo
        for (int i = 0; i < designerComboBox.getItems().size(); i++) {
            if (designerComboBox.getItems().get(i).contains(tm.getDesignerName())) {
                designerComboBox.getSelectionModel().select(i);
                break;
            }
        }

        statusComboBox.setValue(tm.getStatus());
        filePathField.setText(tm.getFilePath().equals("No file attached") ? "" : tm.getFilePath());
        notesArea.setText(tm.getNotes());

        saveButton.setDisable(true);
        updateButton.setDisable(false);
        approveButton.setDisable(tm.getStatus().equals("APPROVED"));
    }

    // ============================================================
    // SAVE DESIGN
    // ============================================================
    @FXML
    private void handleSaveDesign() {
        int custIdx = customerComboBox.getSelectionModel().getSelectedIndex();
        if (custIdx < 0 || custIdx >= customerIdList.size()) {
            showAlert(Alert.AlertType.WARNING, "Customer Required", "Please select a registered customer.");
            return;
        }

        int customerId = customerIdList.get(custIdx);
        int desgIdx = designerComboBox.getSelectionModel().getSelectedIndex();
        Integer designerId = (desgIdx >= 0 && desgIdx < designerIdList.size()) ? designerIdList.get(desgIdx) : null;

        String code      = designCodeField.getText().trim();
        String category  = printCategoryComboBox.getValue();
        String material  = materialComboBox.getValue();
        String source    = sourceTypeComboBox.getValue();
        String status    = statusComboBox.getValue();
        String filePath  = filePathField.getText().trim();
        String notes     = notesArea.getText().trim();

        String sql = "INSERT INTO designs (design_code, customer_id, designer_id, print_category, " +
                     "material_type, source_type, file_path, notes, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, code);
            ps.setInt(2, customerId);
            if (designerId != null) ps.setInt(3, designerId); else ps.setNull(3, java.sql.Types.INTEGER);
            ps.setString(4, category);
            ps.setString(5, material);
            ps.setString(6, source);
            ps.setString(7, filePath.isEmpty() ? null : filePath);
            ps.setString(8, notes.isEmpty() ? null : notes);
            ps.setString(9, status);

            int rows = ps.executeUpdate();
            ps.close();

            if (rows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Design " + code + " recorded successfully!");
                handleClear();
                loadAllDesigns();
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save design: " + e.getMessage());
        }
    }

    // ============================================================
    // UPDATE DESIGN
    // ============================================================
    @FXML
    private void handleUpdateDesign() {
        String code = designCodeField.getText().trim();
        int custIdx = customerComboBox.getSelectionModel().getSelectedIndex();
        if (custIdx < 0 || custIdx >= customerIdList.size()) {
            showAlert(Alert.AlertType.WARNING, "Customer Required", "Please select a customer.");
            return;
        }

        int customerId = customerIdList.get(custIdx);
        int desgIdx = designerComboBox.getSelectionModel().getSelectedIndex();
        Integer designerId = (desgIdx >= 0 && desgIdx < designerIdList.size()) ? designerIdList.get(desgIdx) : null;

        String category  = printCategoryComboBox.getValue();
        String material  = materialComboBox.getValue();
        String source    = sourceTypeComboBox.getValue();
        String status    = statusComboBox.getValue();
        String filePath  = filePathField.getText().trim();
        String notes     = notesArea.getText().trim();

        String sql = "UPDATE designs SET customer_id = ?, designer_id = ?, print_category = ?, " +
                     "material_type = ?, source_type = ?, file_path = ?, notes = ?, status = ? " +
                     "WHERE design_code = ?";

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, customerId);
            if (designerId != null) ps.setInt(2, designerId); else ps.setNull(2, java.sql.Types.INTEGER);
            ps.setString(3, category);
            ps.setString(4, material);
            ps.setString(5, source);
            ps.setString(6, filePath.isEmpty() ? null : filePath);
            ps.setString(7, notes.isEmpty() ? null : notes);
            ps.setString(8, status);
            ps.setString(9, code);

            int rows = ps.executeUpdate();
            ps.close();

            if (rows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Design " + code + " updated successfully!");
                handleClear();
                loadAllDesigns();
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update design: " + e.getMessage());
        }
    }

    // ============================================================
    // QUICK APPROVE DESIGN (STATUS -> APPROVED)
    // ============================================================
    @FXML
    private void handleApproveDesign() {
        String code = designCodeField.getText().trim();
        if (code.isEmpty()) return;

        String sql = "UPDATE designs SET status = 'APPROVED' WHERE design_code = ?";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, code);
            int rows = ps.executeUpdate();
            ps.close();

            if (rows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Approved", "Design " + code + " has been APPROVED for printing!");
                handleClear();
                loadAllDesigns();
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to approve design: " + e.getMessage());
        }
    }

    // ============================================================
    // CLEAR / RESET FORM
    // ============================================================
    @FXML
    private void handleClear() {
        filePathField.clear();
        notesArea.clear();
        searchField.clear();

        saveButton.setDisable(false);
        updateButton.setDisable(true);
        approveButton.setDisable(true);

        designTable.getSelectionModel().clearSelection();
        generateNextDesignCode();
    }

    // ============================================================
    // FILTER DESIGNS BY STATUS
    // ============================================================
    private void filterDesignsByStatus() {
        String filter = filterStatusComboBox.getValue();
        if (filter == null || filter.equals("ALL")) {
            designTable.setItems(designList);
            return;
        }

        ObservableList<DesignTM> filtered = FXCollections.observableArrayList();
        for (DesignTM d : designList) {
            if (d.getStatus().equalsIgnoreCase(filter)) {
                filtered.add(d);
            }
        }
        designTable.setItems(filtered);
    }

    // ============================================================
    // LIVE SEARCH FILTER
    // ============================================================
    private void setupLiveSearch() {
        FilteredList<DesignTM> filteredData = new FilteredList<>(designList, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(design -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }
                String filter = newValue.toLowerCase().trim();
                return design.getDesignCode().toLowerCase().contains(filter) ||
                       design.getCustomerName().toLowerCase().contains(filter) ||
                       design.getCustomerPhone().contains(filter) ||
                       design.getMaterialType().toLowerCase().contains(filter) ||
                       design.getPrintCategory().toLowerCase().contains(filter);
            });
        });

        SortedList<DesignTM> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(designTable.comparatorProperty());
        designTable.setItems(sortedData);
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
