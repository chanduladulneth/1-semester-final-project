package com.dcroyal.dto;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * OrderDTO.java
 * Data Transfer Object representing an Order and its line items.
 */
public class OrderDTO {

    private int orderId;
    private String orderNumber;
    private String qrCodeToken;
    private int customerId;
    private int createdBy;
    private String orderStatus;
    private boolean deliveryRequired;
    private String deliveryAddress;
    private String deliveryCity;
    private double subtotalAmount;
    private double discountAmount;
    private double deliveryFee;
    private double totalAmount;
    private String paymentStatus;
    private Date dueDate;
    private List<OrderItemDTO> items = new ArrayList<>();

    public OrderDTO() {
    }

    public OrderDTO(String orderNumber, String qrCodeToken, int customerId, int createdBy,
                    String orderStatus, boolean deliveryRequired, String deliveryAddress,
                    String deliveryCity, double subtotalAmount, double discountAmount,
                    double deliveryFee, double totalAmount, String paymentStatus, Date dueDate) {
        this.orderNumber = orderNumber;
        this.qrCodeToken = qrCodeToken;
        this.customerId = customerId;
        this.createdBy = createdBy;
        this.orderStatus = orderStatus;
        this.deliveryRequired = deliveryRequired;
        this.deliveryAddress = deliveryAddress;
        this.deliveryCity = deliveryCity;
        this.subtotalAmount = subtotalAmount;
        this.discountAmount = discountAmount;
        this.deliveryFee = deliveryFee;
        this.totalAmount = totalAmount;
        this.paymentStatus = paymentStatus;
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

    public String getQrCodeToken() {
        return qrCodeToken;
    }

    public void setQrCodeToken(String qrCodeToken) {
        this.qrCodeToken = qrCodeToken;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public boolean isDeliveryRequired() {
        return deliveryRequired;
    }

    public void setDeliveryRequired(boolean deliveryRequired) {
        this.deliveryRequired = deliveryRequired;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getDeliveryCity() {
        return deliveryCity;
    }

    public void setDeliveryCity(String deliveryCity) {
        this.deliveryCity = deliveryCity;
    }

    public double getSubtotalAmount() {
        return subtotalAmount;
    }

    public void setSubtotalAmount(double subtotalAmount) {
        this.subtotalAmount = subtotalAmount;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public double getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(double deliveryFee) {
        this.deliveryFee = deliveryFee;
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

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public List<OrderItemDTO> getItems() {
        return items;
    }

    public void setItems(List<OrderItemDTO> items) {
        this.items = items;
    }
}
