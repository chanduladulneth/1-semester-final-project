package com.dcroyal.tm;

/**
 * PackingHistoryTM.java
 * Table Model class used to display historical packing records.
 */
public class PackingHistoryTM {

    private int packingId;
    private String orderNumber;
    private String customerName;
    private String staffName;
    private int packageCount;
    private double weightKg;
    private String packingNotes;
    private String packedAt;

    public PackingHistoryTM() {
    }

    public PackingHistoryTM(int packingId, String orderNumber, String customerName, String staffName,
                            int packageCount, double weightKg, String packingNotes, String packedAt) {
        this.packingId = packingId;
        this.orderNumber = orderNumber;
        this.customerName = customerName;
        this.staffName = staffName;
        this.packageCount = packageCount;
        this.weightKg = weightKg;
        this.packingNotes = packingNotes;
        this.packedAt = packedAt;
    }

    public int getPackingId() {
        return packingId;
    }

    public void setPackingId(int packingId) {
        this.packingId = packingId;
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

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
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

    public String getPackedAt() {
        return packedAt;
    }

    public void setPackedAt(String packedAt) {
        this.packedAt = packedAt;
    }
}
