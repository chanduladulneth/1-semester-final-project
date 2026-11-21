package com.dcroyal.tm;

/**
 * QrInventoryLogTM.java
 * Table Model class used to display logs of items taken from inventory via QR access card.
 */
public class QrInventoryLogTM {

    private int logId;
    private String operatorName;
    private String cardQrCode;
    private String itemName;
    private double quantityTaken;
    private String unitOfMeasure;
    private String orderNumber;
    private String remarks;
    private String takenAt;

    public QrInventoryLogTM() {
    }

    public QrInventoryLogTM(int logId, String operatorName, String cardQrCode, String itemName,
                            double quantityTaken, String unitOfMeasure, String orderNumber,
                            String remarks, String takenAt) {
        this.logId = logId;
        this.operatorName = operatorName;
        this.cardQrCode = cardQrCode;
        this.itemName = itemName;
        this.quantityTaken = quantityTaken;
        this.unitOfMeasure = unitOfMeasure;
        this.orderNumber = orderNumber;
        this.remarks = remarks;
        this.takenAt = takenAt;
    }

    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getCardQrCode() {
        return cardQrCode;
    }

    public void setCardQrCode(String cardQrCode) {
        this.cardQrCode = cardQrCode;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public double getQuantityTaken() {
        return quantityTaken;
    }

    public void setQuantityTaken(double quantityTaken) {
        this.quantityTaken = quantityTaken;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getTakenAt() {
        return takenAt;
    }

    public void setTakenAt(String takenAt) {
        this.takenAt = takenAt;
    }
}
