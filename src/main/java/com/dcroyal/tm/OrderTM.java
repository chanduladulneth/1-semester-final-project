package com.dcroyal.tm;

/**
 * OrderTM.java
 * Table Model class used to display orders in the Order Management TableView.
 */
public class OrderTM {

    private int orderId;
    private String orderNumber;
    private String qrCodeToken;
    private String customerName;
    private String customerPhone;
    private double totalAmount;
    private String paymentStatus;
    private String orderStatus;
    private String createdAt;
    private String deliveryCity;

    public OrderTM() {
    }

    public OrderTM(int orderId, String orderNumber, String qrCodeToken, String customerName,
                   String customerPhone, double totalAmount, String paymentStatus,
                   String orderStatus, String createdAt, String deliveryCity) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.qrCodeToken = qrCodeToken;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.totalAmount = totalAmount;
        this.paymentStatus = paymentStatus;
        this.orderStatus = orderStatus;
        this.createdAt = createdAt;
        this.deliveryCity = deliveryCity;
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

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getDeliveryCity() {
        return deliveryCity;
    }

    public void setDeliveryCity(String deliveryCity) {
        this.deliveryCity = deliveryCity;
    }
}
