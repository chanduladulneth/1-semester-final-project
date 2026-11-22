package com.dcroyal.dto;

/**
 * EmployeeDTO.java
 * Data Transfer Object for Employee (users table).
 * Student-level clean Java code with getters/setters.
 */
public class EmployeeDTO {

    private int     userId;
    private int     roleId;
    private String  roleCode;
    private String  roleName;
    private String  fullName;
    private String  username;
    private String  password;
    private String  phone;
    private String  nic;
    private double  basicSalary;
    private boolean isActive;

    public EmployeeDTO() {
    }

    public EmployeeDTO(int userId, int roleId, String roleCode, String roleName,
                       String fullName, String username, String password,
                       String phone, String nic, double basicSalary, boolean isActive) {
        this.userId      = userId;
        this.roleId      = roleId;
        this.roleCode    = roleCode;
        this.roleName    = roleName;
        this.fullName    = fullName;
        this.username    = username;
        this.password    = password;
        this.phone       = phone;
        this.nic         = nic;
        this.basicSalary = basicSalary;
        this.isActive    = isActive;
    }

    public int     getUserId()                    { return userId; }
    public void    setUserId(int userId)          { this.userId = userId; }

    public int     getRoleId()                    { return roleId; }
    public void    setRoleId(int roleId)          { this.roleId = roleId; }

    public String  getRoleCode()                  { return roleCode; }
    public void    setRoleCode(String roleCode)   { this.roleCode = roleCode; }

    public String  getRoleName()                  { return roleName; }
    public void    setRoleName(String roleName)   { this.roleName = roleName; }

    public String  getFullName()                  { return fullName; }
    public void    setFullName(String fullName)   { this.fullName = fullName; }

    public String  getUsername()                  { return username; }
    public void    setUsername(String username)   { this.username = username; }

    public String  getPassword()                  { return password; }
    public void    setPassword(String password)   { this.password = password; }

    public String  getPhone()                     { return phone; }
    public void    setPhone(String phone)         { this.phone = phone; }

    public String  getNic()                       { return nic; }
    public void    setNic(String nic)             { this.nic = nic; }

    public double  getBasicSalary()               { return basicSalary; }
    public void    setBasicSalary(double basicSalary) { this.basicSalary = basicSalary; }

    public boolean isActive()                     { return isActive; }
    public void    setActive(boolean active)      { isActive = active; }
}
