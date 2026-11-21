package com.dcroyal.dto;

/**
 * InventoryItemDTO.java
 * Data Transfer Object representing an inventory item / raw material stock.
 */
public class InventoryItemDTO {

    private int itemId;
    private String skuCode;
    private String itemName;
    private String category;       // FILAMENT_RESIN, DTF_FILM_INK, UV_INK, SUBSTRATE_BLANK, PACKAGING, OTHER
    private String unitOfMeasure;  // PCS, ROLL, BOTTLE, KG, LITER, PACK, BOX
    private double currentStock;
    private double reorderLevel;
    private double unitCost;

    public InventoryItemDTO() {
    }

    public InventoryItemDTO(String skuCode, String itemName, String category, String unitOfMeasure,
                            double currentStock, double reorderLevel, double unitCost) {
        this.skuCode = skuCode;
        this.itemName = itemName;
        this.category = category;
        this.unitOfMeasure = unitOfMeasure;
        this.currentStock = currentStock;
        this.reorderLevel = reorderLevel;
        this.unitCost = unitCost;
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

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public double getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(double currentStock) {
        this.currentStock = currentStock;
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
}
