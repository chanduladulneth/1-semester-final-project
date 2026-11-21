package com.dcroyal.dto;

/**
 * DesignDTO.java
 * Data Transfer Object representing a Print Design.
 */
public class DesignDTO {

    private int designId;
    private String designCode;
    private int customerId;
    private Integer designerId;
    private String printCategory;   // 3D_PRINTING, DTF, UV_FLATBED, UV_ROTARY
    private String materialType;    // T-shirt, metal, glass, wood, plastic, acrylic, bottle, cup
    private String sourceType;     // CUSTOMER_PROVIDED, IN_HOUSE_DESIGNED
    private String filePath;
    private String notes;
    private String status;          // PENDING, IN_PROGRESS, APPROVED, REJECTED

    public DesignDTO() {
    }

    public DesignDTO(String designCode, int customerId, Integer designerId, String printCategory,
                     String materialType, String sourceType, String filePath, String notes, String status) {
        this.designCode = designCode;
        this.customerId = customerId;
        this.designerId = designerId;
        this.printCategory = printCategory;
        this.materialType = materialType;
        this.sourceType = sourceType;
        this.filePath = filePath;
        this.notes = notes;
        this.status = status;
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

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public Integer getDesignerId() {
        return designerId;
    }

    public void setDesignerId(Integer designerId) {
        this.designerId = designerId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
