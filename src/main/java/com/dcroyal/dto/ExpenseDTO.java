package com.dcroyal.dto;

/**
 * ExpenseDTO.java
 * Data Transfer Object for expenses table.
 * Student-level clean Java code with standard getters/setters.
 */
public class ExpenseDTO {

    private int     expenseId;
    private String  expenseCode;
    private String  expenseCategory; // RAW_MATERIAL, UTILITY_BILL, MAINTENANCE, DELIVERY, SALARY_BATCH, OTHER
    private String  description;
    private double  amount;
    private String  expenseDate;
    private String  approvalStatus; // PENDING_OWNER, APPROVED, SETTLED, REJECTED
    private String  transferReference;
    private Integer submittedBy;
    private Integer approvedBy;
    private String  createdAt;

    public ExpenseDTO() {
    }

    public ExpenseDTO(int expenseId, String expenseCode, String expenseCategory,
                      String description, double amount, String expenseDate,
                      String approvalStatus, String transferReference,
                      Integer submittedBy, Integer approvedBy, String createdAt) {
        this.expenseId         = expenseId;
        this.expenseCode       = expenseCode;
        this.expenseCategory   = expenseCategory;
        this.description       = description;
        this.amount            = amount;
        this.expenseDate       = expenseDate;
        this.approvalStatus    = approvalStatus;
        this.transferReference = transferReference;
        this.submittedBy       = submittedBy;
        this.approvedBy        = approvedBy;
        this.createdAt         = createdAt;
    }

    public int     getExpenseId()                         { return expenseId; }
    public void    setExpenseId(int expenseId)            { this.expenseId = expenseId; }

    public String  getExpenseCode()                       { return expenseCode; }
    public void    setExpenseCode(String expenseCode)     { this.expenseCode = expenseCode; }

    public String  getExpenseCategory()                   { return expenseCategory; }
    public void    setExpenseCategory(String category)    { this.expenseCategory = category; }

    public String  getDescription()                       { return description; }
    public void    setDescription(String description)     { this.description = description; }

    public double  getAmount()                            { return amount; }
    public void    setAmount(double amount)               { this.amount = amount; }

    public String  getExpenseDate()                       { return expenseDate; }
    public void    setExpenseDate(String expenseDate)     { this.expenseDate = expenseDate; }

    public String  getApprovalStatus()                    { return approvalStatus; }
    public void    setApprovalStatus(String status)       { this.approvalStatus = status; }

    public String  getTransferReference()                 { return transferReference; }
    public void    setTransferReference(String ref)       { this.transferReference = ref; }

    public Integer getSubmittedBy()                       { return submittedBy; }
    public void    setSubmittedBy(Integer submittedBy)    { this.submittedBy = submittedBy; }

    public Integer getApprovedBy()                        { return approvedBy; }
    public void    setApprovedBy(Integer approvedBy)      { this.approvedBy = approvedBy; }

    public String  getCreatedAt()                         { return createdAt; }
    public void    setCreatedAt(String createdAt)         { this.createdAt = createdAt; }
}
