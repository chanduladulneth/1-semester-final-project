package com.dcroyal.tm;

/**
 * OrderItemTM.java
 * Table Model class used to display line items added to an order during billing.
 */
public class OrderItemTM {

    private String printType;
    private String itemName;
    private String substrateMaterial;
    private int quantity;
    private double unitPrice;
    private double lineTotal;
    private String specifications;

    public OrderItemTM() {
    }

    public OrderItemTM(String printType, String itemName, String substrateMaterial, int quantity, double unitPrice, double lineTotal, String specifications) {
        this.printType = printType;
        this.itemName = itemName;
        this.substrateMaterial = substrateMaterial;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
        this.specifications = specifications;
    }

    public String getPrintType() {
        return printType;
    }

    public void setPrintType(String printType) {
        this.printType = printType;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getSubstrateMaterial() {
        return substrateMaterial;
    }

    public void setSubstrateMaterial(String substrateMaterial) {
        this.substrateMaterial = substrateMaterial;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(double lineTotal) {
        this.lineTotal = lineTotal;
    }

    public String getSpecifications() {
        return specifications;
    }

    public void setSpecifications(String specifications) {
        this.specifications = specifications;
    }
}
