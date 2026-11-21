package com.dcroyal.tm;

/**
 * DesignTM.java
 * Table Model class used to display designs in the Designs TableView.
 */
public class DesignTM {

    private int designId;
    private String designCode;
    private String customerName;
    private String customerPhone;
    private String printCategory;
    private String materialType;
    private String sourceType;
    private String designerName;
    private String status;
    private String filePath;
    private String notes;
    private String createdAt;

    public DesignTM() {
    }

    public DesignTM(int designId, String designCode, String customerName, String customerPhone,
                    String printCategory, String materialType, String sourceType,
                    String designerName, String status, String filePath, String notes, String createdAt) {
        this.designId = designId;
        this.designCode = designCode;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.printCategory = printCategory;
        this.materialType = materialType;
        this.sourceType = sourceType;
        this.designerName = designerName;
        this.status = status;
        this.filePath = filePath;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public int getDesignId() {
        return designId;
    }

    public void setDesignId(int designId) {
        this.designId = designId;
    }

    public String getDesignCode() {
        return designCode;
    }

    public void setDesignCode(String designCode) {
        this.designCode = designCode;
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

    public String getPrintCategory() {
        return printCategory;
    }

    public void setPrintCategory(String printCategory) {
        this.printCategory = printCategory;
    }

    public String getMaterialType() {
        return materialType;
    }

    public void setMaterialType(String materialType) {
        this.materialType = materialType;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getDesignerName() {
        return designerName;
    }

    public void setDesignerName(String designerName) {
        this.designerName = designerName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
