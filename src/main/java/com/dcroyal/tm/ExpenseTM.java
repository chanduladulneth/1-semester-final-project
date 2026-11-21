package com.dcroyal.tm;

/**
 * ExpenseTM.java
 * Table Model for displaying business expenses and ledger records in the TableView.
 * Student-level clean Java code with standard properties.
 */
public class ExpenseTM {

    private int    expenseId;
    private String expenseCode;
    private String expenseCategory;
    private String description;
    private double amount;
    private String amountFormatted;
    private String expenseDate;
    private String approvalStatus;
    private String transferReference;
    private String submittedByName;
    private String approvedByName;

    public ExpenseTM(int expenseId, String expenseCode, String expenseCategory,
                     String description, double amount, String expenseDate,
                     String approvalStatus, String transferReference,
                     String submittedByName, String approvedByName) {
        this.expenseId         = expenseId;
        this.expenseCode       = expenseCode;
        this.expenseCategory   = expenseCategory;
        this.description       = description;
        this.amount            = amount;
        this.amountFormatted   = String.format("Rs. %,.2f", amount);
        this.expenseDate       = expenseDate;
        this.approvalStatus    = approvalStatus;
        this.transferReference = (transferReference != null && !transferReference.isEmpty()) ? transferReference : "-";
        this.submittedByName   = (submittedByName != null && !submittedByName.isEmpty()) ? submittedByName : "System";
        this.approvedByName    = (approvedByName != null && !approvedByName.isEmpty()) ? approvedByName : "Pending";
    }

    public int    getExpenseId()                  { return expenseId; }
    public void   setExpenseId(int id)            { this.expenseId = id; }

    public String getExpenseCode()                { return expenseCode; }
    public void   setExpenseCode(String code)     { this.expenseCode = code; }

    public String getExpenseCategory()            { return expenseCategory; }
    public void   setExpenseCategory(String cat)  { this.expenseCategory = cat; }

    public String getDescription()                { return description; }
    public void   setDescription(String desc)     { this.description = desc; }

    public double getAmount()                     { return amount; }
    public void   setAmount(double amt)           {
        this.amount = amt;
        this.amountFormatted = String.format("Rs. %,.2f", amt);
    }
    public String getAmountFormatted()            { return amountFormatted; }

    public String getExpenseDate()                { return expenseDate; }
    public void   setExpenseDate(String date)     { this.expenseDate = date; }

    public String getApprovalStatus()             { return approvalStatus; }
    public void   setApprovalStatus(String s)     { this.approvalStatus = s; }

    public String getTransferReference()          { return transferReference; }
    public void   setTransferReference(String r)  { this.transferReference = r; }

    public String getSubmittedByName()           { return submittedByName; }
    public void   setSubmittedByName(String sub)  { this.submittedByName = sub; }

    public String getApprovedByName()            { return approvedByName; }
    public void   setApprovedByName(String app)   { this.approvedByName = app; }
}
