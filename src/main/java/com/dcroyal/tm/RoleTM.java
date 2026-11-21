package com.dcroyal.tm;

/**
 * RoleTM.java
 * Table Model for displaying system role permissions in the RBAC TableView.
 * Student-level clean Java code with standard properties.
 */
public class RoleTM {

    private int    roleId;
    private String roleCode;
    private String roleName;
    private String description;
    private int    userCount;

    public RoleTM(int roleId, String roleCode, String roleName, String description, int userCount) {
        this.roleId      = roleId;
        this.roleCode    = roleCode;
        this.roleName    = roleName;
        this.description = description;
        this.userCount   = userCount;
    }

    public int    getRoleId()                 { return roleId; }
    public void   setRoleId(int roleId)       { this.roleId = roleId; }

    public String getRoleCode()               { return roleCode; }
    public void   setRoleCode(String code)    { this.roleCode = code; }

    public String getRoleName()               { return roleName; }
    public void   setRoleName(String name)    { this.roleName = name; }

    public String getDescription()            { return description; }
    public void   setDescription(String desc) { this.description = desc; }

    public int    getUserCount()              { return userCount; }
    public void   setUserCount(int count)     { this.userCount = count; }
}
