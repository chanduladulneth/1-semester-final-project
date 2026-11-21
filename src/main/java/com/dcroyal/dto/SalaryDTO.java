package com.dcroyal.dto;

/**
 * SalaryDTO.java
 * Data Transfer Object for employee_salaries table.
 * Student-level clean Java code with standard getters/setters.
 */
public class SalaryDTO {

    private int     salaryId;
    private int     userId;
    private String  employeeName;
    private String  roleName;
    private String  salaryMonth; // Format: YYYY-MM
    private double  basicAmount;
    private double  overtimeAllowance;
    private double  deductions;
    private double  netSalary;
    private String  approvalStatus; // PENDING_OWNER, APPROVED, PAID
    private Integer submittedBy;
    private Integer approvedBy;
    private String  createdAt;

    public SalaryDTO() {
    }

    public SalaryDTO(int salaryId, int userId, String employeeName, String roleName,
                     String salaryMonth, double basicAmount, double overtimeAllowance,
                     double deductions, double netSalary, String approvalStatus,
                     Integer submittedBy, Integer approvedBy, String createdAt) {
        this.salaryId          = salaryId;
        this.userId            = userId;
        this.employeeName      = employeeName;
        this.roleName          = roleName;
        this.salaryMonth       = salaryMonth;
        this.basicAmount       = basicAmount;
        this.overtimeAllowance = overtimeAllowance;
        this.deductions        = deductions;
        this.netSalary         = netSalary;
        this.approvalStatus    = approvalStatus;
        this.submittedBy       = submittedBy;
        this.approvedBy        = approvedBy;
        this.createdAt         = createdAt;
    }

    public int     getSalaryId()                      { return salaryId; }
    public void    setSalaryId(int salaryId)          { this.salaryId = salaryId; }

    public int     getUserId()                        { return userId; }
    public void    setUserId(int userId)              { this.userId = userId; }

    public String  getEmployeeName()                  { return employeeName; }
    public void    setEmployeeName(String name)       { this.employeeName = name; }

    public String  getRoleName()                      { return roleName; }
    public void    setRoleName(String role)           { this.roleName = role; }

    public String  getSalaryMonth()                   { return salaryMonth; }
    public void    setSalaryMonth(String salaryMonth) { this.salaryMonth = salaryMonth; }

    public double  getBasicAmount()                   { return basicAmount; }
    public void    setBasicAmount(double basicAmount) { this.basicAmount = basicAmount; }

    public double  getOvertimeAllowance()             { return overtimeAllowance; }
    public void    setOvertimeAllowance(double ot)    { this.overtimeAllowance = ot; }

    public double  getDeductions()                    { return deductions; }
    public void    setDeductions(double deductions)   { this.deductions = deductions; }

    public double  getNetSalary()                     { return netSalary; }
    public void    setNetSalary(double netSalary)     { this.netSalary = netSalary; }

    public String  getApprovalStatus()                { return approvalStatus; }
    public void    setApprovalStatus(String status)   { this.approvalStatus = status; }

    public Integer getSubmittedBy()                   { return submittedBy; }
    public void    setSubmittedBy(Integer submittedBy){ this.submittedBy = submittedBy; }

    public Integer getApprovedBy()                    { return approvedBy; }
    public void    setApprovedBy(Integer approvedBy)  { this.approvedBy = approvedBy; }

    public String  getCreatedAt()                     { return createdAt; }
    public void    setCreatedAt(String createdAt)     { this.createdAt = createdAt; }
}
