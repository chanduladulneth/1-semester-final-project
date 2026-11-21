package com.dcroyal.tm;

/**
 * SalaryTM.java
 * Table Model for displaying employee monthly payroll records in the TableView.
 * Student-level clean Java code with standard properties.
 */
public class SalaryTM {

    private int    salaryId;
    private int    userId;
    private String employeeName;
    private String roleName;
    private String salaryMonth;
    private double basicAmount;
    private String basicAmountFormatted;
    private double overtimeAllowance;
    private String overtimeAllowanceFormatted;
    private double deductions;
    private String deductionsFormatted;
    private double netSalary;
    private String netSalaryFormatted;
    private String approvalStatus;
    private String submittedByName;
    private String approvedByName;

    public SalaryTM(int salaryId, int userId, String employeeName, String roleName,
                    String salaryMonth, double basicAmount, double overtimeAllowance,
                    double deductions, double netSalary, String approvalStatus,
                    String submittedByName, String approvedByName) {
        this.salaryId                   = salaryId;
        this.userId                     = userId;
        this.employeeName               = employeeName;
        this.roleName                   = roleName;
        this.salaryMonth                = salaryMonth;
        this.basicAmount                = basicAmount;
        this.basicAmountFormatted       = String.format("Rs. %,.2f", basicAmount);
        this.overtimeAllowance          = overtimeAllowance;
        this.overtimeAllowanceFormatted = String.format("Rs. %,.2f", overtimeAllowance);
        this.deductions                 = deductions;
        this.deductionsFormatted        = String.format("Rs. %,.2f", deductions);
        this.netSalary                  = netSalary;
        this.netSalaryFormatted         = String.format("Rs. %,.2f", netSalary);
        this.approvalStatus             = approvalStatus;
        this.submittedByName            = (submittedByName != null && !submittedByName.isEmpty()) ? submittedByName : "System";
        this.approvedByName             = (approvedByName != null && !approvedByName.isEmpty()) ? approvedByName : "Pending";
    }

    public int    getSalaryId()                   { return salaryId; }
    public void   setSalaryId(int salaryId)       { this.salaryId = salaryId; }

    public int    getUserId()                     { return userId; }
    public void   setUserId(int userId)           { this.userId = userId; }

    public String getEmployeeName()               { return employeeName; }
    public void   setEmployeeName(String name)    { this.employeeName = name; }

    public String getRoleName()                   { return roleName; }
    public void   setRoleName(String role)        { this.roleName = role; }

    public String getSalaryMonth()                { return salaryMonth; }
    public void   setSalaryMonth(String m)        { this.salaryMonth = m; }

    public double getBasicAmount()                { return basicAmount; }
    public void   setBasicAmount(double b)        {
        this.basicAmount = b;
        this.basicAmountFormatted = String.format("Rs. %,.2f", b);
    }
    public String getBasicAmountFormatted()       { return basicAmountFormatted; }

    public double getOvertimeAllowance()          { return overtimeAllowance; }
    public void   setOvertimeAllowance(double ot) {
        this.overtimeAllowance = ot;
        this.overtimeAllowanceFormatted = String.format("Rs. %,.2f", ot);
    }
    public String getOvertimeAllowanceFormatted() { return overtimeAllowanceFormatted; }

    public double getDeductions()                 { return deductions; }
    public void   setDeductions(double d)         {
        this.deductions = d;
        this.deductionsFormatted = String.format("Rs. %,.2f", d);
    }
    public String getDeductionsFormatted()        { return deductionsFormatted; }

    public double getNetSalary()                  { return netSalary; }
    public void   setNetSalary(double n)          {
        this.netSalary = n;
        this.netSalaryFormatted = String.format("Rs. %,.2f", n);
    }
    public String getNetSalaryFormatted()         { return netSalaryFormatted; }

    public String getApprovalStatus()             { return approvalStatus; }
    public void   setApprovalStatus(String s)     { this.approvalStatus = s; }

    public String getSubmittedByName()            { return submittedByName; }
    public void   setSubmittedByName(String sub)  { this.submittedByName = sub; }

    public String getApprovedByName()             { return approvedByName; }
    public void   setApprovedByName(String app)   { this.approvedByName = app; }
}
