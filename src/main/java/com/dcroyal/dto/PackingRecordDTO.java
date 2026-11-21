package com.dcroyal.dto;

/**
 * PackingRecordDTO.java
 * Data Transfer Object representing an order packaging record.
 */
public class PackingRecordDTO {

    private int packingId;
    private int orderId;
    private int packedBy;
    private int packageCount;
    private double weightKg;
    private String packingNotes;

    public PackingRecordDTO() {
    }

    public PackingRecordDTO(int orderId, int packedBy, int packageCount, double weightKg, String packingNotes) {
        this.orderId = orderId;
        this.packedBy = packedBy;
        this.packageCount = packageCount;
        this.weightKg = weightKg;
        this.packingNotes = packingNotes;
    }

    public int getPackingId() {
        return packingId;
    }

    public void setPackingId(int packingId) {
        this.packingId = packingId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getPackedBy() {
        return packedBy;
    }

    public void setPackedBy(int packedBy) {
        this.packedBy = packedBy;
    }

    public int getPackageCount() {
        return packageCount;
    }

    public void setPackageCount(int packageCount) {
        this.packageCount = packageCount;
    }

    public double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(double weightKg) {
        this.weightKg = weightKg;
    }

    public String getPackingNotes() {
        return packingNotes;
    }

    public void setPackingNotes(String packingNotes) {
        this.packingNotes = packingNotes;
    }
}
