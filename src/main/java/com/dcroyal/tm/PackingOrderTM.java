package com.dcroyal.tm;

/**
 * PackingOrderTM.java
 * Table Model class used to display printed orders waiting to be packed.
 */
public class PackingOrderTM {

    private int orderId;
    private int customerId;
    private String orderNumber;
    private String qrCodeToken;
    private String customerName;
    private String customerPhone;
    private String deliveryRequired;
    private String deliveryCity;
    private String deliveryAddress;
    private String orderStatus;
    private String dueDate;

    public PackingOrderTM() {
    }

    public PackingOrderTM(int orderId, int customerId, String orderNumber, String qrCodeToken,
                          String customerName, String customerPhone, String deliveryRequired,
                          String deliveryCity, String deliveryAddress, String orderStatus, String dueDate) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.orderNumber = orderNumber;
        this.qrCodeToken = qrCodeToken;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.deliveryRequired = deliveryRequired;
        this.deliveryCity = deliveryCity;
        this.deliveryAddress = deliveryAddress;
        this.orderStatus = orderStatus;
        this.dueDate = dueDate;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getQrCodeToken() {
        return qrCodeToken;
    }

    public void setQrCodeToken(String qrCodeToken) {
        this.qrCodeToken = qrCodeToken;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getDeliveryRequired() {
        return deliveryRequired;
    }

    public void setDeliveryRequired(String deliveryRequired) {
        this.deliveryRequired = deliveryRequired;
    }

    public String getDeliveryCity() {
        return deliveryCity;
    }

    public void setDeliveryCity(String deliveryCity) {
        this.deliveryCity = deliveryCity;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
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
