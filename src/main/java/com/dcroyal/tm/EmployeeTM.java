package com.dcroyal.tm;

/**
 * EmployeeTM.java
 * Table Model for displaying employee rows in the Staff Directory TableView.
 * Student-level clean Java code with standard properties.
 */
public class EmployeeTM {

    private int     userId;
    private String  fullName;
    private String  roleName;
    private String  email;
    private String  phone;
    private String  nic;
    private double  basicSalary;
    private String  basicSalaryFormatted;
    private String  status; // "ACTIVE" or "INACTIVE"
    private boolean active;

    public EmployeeTM(int userId, String fullName, String roleName, String email,
                      String phone, String nic, double basicSalary, boolean active) {
        this.userId               = userId;
        this.fullName             = fullName;
        this.roleName             = roleName;
        this.email                = email;
        this.phone                = (phone != null && !phone.isEmpty()) ? phone : "-";
        this.nic                  = (nic != null && !nic.isEmpty()) ? nic : "-";
        this.basicSalary          = basicSalary;
        this.basicSalaryFormatted = String.format("Rs. %,.2f", basicSalary);
        this.active               = active;
        this.status               = active ? "ACTIVE" : "INACTIVE";
    }

    public int     getUserId()               { return userId; }
    public void    setUserId(int userId)     { this.userId = userId; }

    public String  getFullName()             { return fullName; }
    public void    setFullName(String name)  { this.fullName = name; }

    public String  getRoleName()             { return roleName; }
    public void    setRoleName(String role)  { this.roleName = role; }

    public String  getEmail()                { return email; }
    public void    setEmail(String email)    { this.email = email; }

    public String  getPhone()                { return phone; }
    public void    setPhone(String phone)    { this.phone = phone; }

    public String  getNic()                  { return nic; }
    public void    setNic(String nic)        { this.nic = nic; }

    public double  getBasicSalary()          { return basicSalary; }
    public void    setBasicSalary(double b)  {
        this.basicSalary = b;
        this.basicSalaryFormatted = String.format("Rs. %,.2f", b);
    }

    public String  getBasicSalaryFormatted() { return basicSalaryFormatted; }

    public String  getStatus()               { return status; }
    public void    setStatus(String status)  { this.status = status; }

    public boolean isActive()                { return active; }
    public void    setActive(boolean active) {
        this.active = active;
        this.status = active ? "ACTIVE" : "INACTIVE";
    }
}
