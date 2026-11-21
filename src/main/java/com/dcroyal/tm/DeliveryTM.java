package com.dcroyal.tm;

/**
 * DeliveryTM.java
 * Table Model class used to display delivery tasks in the Delivery Management TableView.
 */
public class DeliveryTM {

    private int deliveryId;
    private int orderId;
    private String orderNumber;
    private String recipientName;
    private String recipientPhone;
    private String deliveryAddress;
    private String scheduledDate;
    private String driverName;
    private String deliveryStatus;
    private String deliveredAt;
    private String deliveryNotes;

    public DeliveryTM() {
    }

    public DeliveryTM(int deliveryId, int orderId, String orderNumber, String recipientName,
                      String recipientPhone, String deliveryAddress, String scheduledDate,
                      String driverName, String deliveryStatus, String deliveredAt, String deliveryNotes) {
        this.deliveryId = deliveryId;
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.deliveryAddress = deliveryAddress;
        this.scheduledDate = scheduledDate;
        this.driverName = driverName;
        this.deliveryStatus = deliveryStatus;
        this.deliveredAt = deliveredAt;
        this.deliveryNotes = deliveryNotes;
    }

    public int getDeliveryId() {
        return deliveryId;
    }

    public void setDeliveryId(int deliveryId) {
        this.deliveryId = deliveryId;
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

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientPhone() {
        return recipientPhone;
    }

    public void setRecipientPhone(String recipientPhone) {
        this.recipientPhone = recipientPhone;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(String scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public String getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(String deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public String getDeliveryNotes() {
        return deliveryNotes;
    }

    public void setDeliveryNotes(String deliveryNotes) {
        this.deliveryNotes = deliveryNotes;
    }
}
