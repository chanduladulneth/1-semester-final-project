package com.dcroyal.controller;

import com.dcroyal.db.DBConnection;
import com.dcroyal.tm.InventoryItemTM;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Read-only stock availability view for printer operators.
 */
public class StockRequestController {

    @FXML private TextField searchField;
    @FXML private TableView<InventoryItemTM> stockTable;
    @FXML private TableColumn<InventoryItemTM, String> colSku;
    @FXML private TableColumn<InventoryItemTM, String> colName;
    @FXML private TableColumn<InventoryItemTM, String> colCategory;
    @FXML private TableColumn<InventoryItemTM, Double> colStock;
    @FXML private TableColumn<InventoryItemTM, String> colUnit;
    @FXML private TableColumn<InventoryItemTM, String> colStatus;

    private final ObservableList<InventoryItemTM> stockList =
            FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colSku.setCellValueFactory(new PropertyValueFactory<>("skuCode"));
        colName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("currentStock"));
        colUnit.setCellValueFactory(new PropertyValueFactory<>("unitOfMeasure"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("stockStatus"));

        loadAvailableStock();
        setupSearch();
    }

    private void loadAvailableStock() {
        stockList.clear();
        String sql = "SELECT item_id, sku_code, item_name, category, current_stock, " +
                     "unit_of_measure, reorder_level, unit_cost " +
                     "FROM inventory_items ORDER BY item_name";

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                double stock = rs.getDouble("current_stock");
                double reorder = rs.getDouble("reorder_level");
                String status = stock <= 0 ? "OUT OF STOCK"
                        : stock <= reorder ? "LOW STOCK" : "AVAILABLE";

                stockList.add(new InventoryItemTM(
                        rs.getInt("item_id"),
                        rs.getString("sku_code"),
                        rs.getString("item_name"),
                        rs.getString("category"),
                        stock,
                        rs.getString("unit_of_measure"),
                        reorder,
                        rs.getDouble("unit_cost"),
                        status
                ));
            }

            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.out.println("Stock request load error: " + e.getMessage());
        }
    }

    private void setupSearch() {
        FilteredList<InventoryItemTM> filtered =
                new FilteredList<>(stockList, item -> true);

        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            String search = newValue == null ? "" : newValue.toLowerCase().trim();
            filtered.setPredicate(item -> {
                if (search.isEmpty()) {
                    return true;
                }
                return item.getSkuCode().toLowerCase().contains(search)
                        || item.getItemName().toLowerCase().contains(search)
                        || item.getCategory().toLowerCase().contains(search);
            });
        });

        SortedList<InventoryItemTM> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(stockTable.comparatorProperty());
        stockTable.setItems(sorted);
    }
}
