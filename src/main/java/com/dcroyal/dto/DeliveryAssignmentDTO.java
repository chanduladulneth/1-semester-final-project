package com.dcroyal.dto;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * DeliveryAssignmentDTO.java
 * Data Transfer Object representing an island-wide delivery assignment.
 */
public class DeliveryAssignmentDTO {

    private int deliveryId;
    private int orderId;
    private int driverId;
    private Date scheduledDate;
    private String recipientName;
    private String recipientPhone;
    private String deliveryAddress;
    private String deliveryStatus; // ASSIGNED, IN_TRANSIT, DELIVERED, FAILED_RETRY
    private Timestamp deliveredAt;
    private String deliveryNotes;

    public DeliveryAssignmentDTO() {
    }

    public DeliveryAssignmentDTO(int orderId, int driverId, Date scheduledDate, String recipientName,
                                 String recipientPhone, String deliveryAddress, String deliveryStatus, String deliveryNotes) {
        this.orderId = orderId;
        this.driverId = driverId;
        this.scheduledDate = scheduledDate;
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.deliveryAddress = deliveryAddress;
        this.deliveryStatus = deliveryStatus;
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

    public int getDriverId() {
        return driverId;
    }

    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }

    public Date getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(Date scheduledDate) {
        this.scheduledDate = scheduledDate;
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

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public Timestamp getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(Timestamp deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public String getDeliveryNotes() {
        return deliveryNotes;
    }

    public void setDeliveryNotes(String deliveryNotes) {
        this.deliveryNotes = deliveryNotes;
    }
}
