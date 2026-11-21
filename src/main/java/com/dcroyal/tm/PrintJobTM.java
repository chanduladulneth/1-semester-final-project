package com.dcroyal.tm;

/**
 * PrintJobTM.java
 * Table Model class used to display print queue orders in the JavaFX TableView.
 */
public class PrintJobTM {

    private int orderId;
    private String orderNumber;
    private String customerName;
    private String printType;
    private String itemName;
    private String material;
    private int quantity;
    private String specifications;
    private String operatorName;
    private String orderStatus;
    private String dueDate;

    public PrintJobTM() {
    }

    public PrintJobTM(int orderId, String orderNumber, String customerName, String printType,
                      String itemName, String material, int quantity, String specifications,
                      String operatorName, String orderStatus, String dueDate) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.customerName = customerName;
        this.printType = printType;
        this.itemName = itemName;
        this.material = material;
        this.quantity = quantity;
        this.specifications = specifications;
        this.operatorName = operatorName;
        this.orderStatus = orderStatus;
        this.dueDate = dueDate;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
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

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getSpecifications() {
        return specifications;
    }

    public void setSpecifications(String specifications) {
        this.specifications = specifications;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }
}
