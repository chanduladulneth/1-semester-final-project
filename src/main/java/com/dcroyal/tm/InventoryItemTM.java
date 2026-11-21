package com.dcroyal.tm;

/**
 * InventoryItemTM.java
 * Table Model class used to display inventory items in the TableView.
 */
public class InventoryItemTM {

    private int itemId;
    private String skuCode;
    private String itemName;
    private String category;
    private double currentStock;
    private String unitOfMeasure;
    private double reorderLevel;
    private double unitCost;
    private String stockStatus; // "OK" or "LOW STOCK"

    public InventoryItemTM() {
    }

    public InventoryItemTM(int itemId, String skuCode, String itemName, String category,
                           double currentStock, String unitOfMeasure, double reorderLevel,
                           double unitCost, String stockStatus) {
        this.itemId = itemId;
        this.skuCode = skuCode;
        this.itemName = itemName;
        this.category = category;
        this.currentStock = currentStock;
        this.unitOfMeasure = unitOfMeasure;
        this.reorderLevel = reorderLevel;
        this.unitCost = unitCost;
        this.stockStatus = stockStatus;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getSkuCode() {
        return skuCode;
    }

    public void setSkuCode(String skuCode) {
        this.skuCode = skuCode;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(double currentStock) {
        this.currentStock = currentStock;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public double getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(double reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public double getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(double unitCost) {
        this.unitCost = unitCost;
    }

    public String getStockStatus() {
        return stockStatus;
    }

    public void setStockStatus(String stockStatus) {
        this.stockStatus = stockStatus;
    }
}
