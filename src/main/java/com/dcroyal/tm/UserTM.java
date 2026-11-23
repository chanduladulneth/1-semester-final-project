package com.dcroyal.tm;

/**
 * UserTM.java
 * Table Model for displaying system user accounts in the Users TableView.
 * Student-level clean Java code with standard properties.
 */
public class UserTM {

    private int     userId;
    private String  fullName;
    private String  username;
    private int     roleId;
    private String  roleCode;
    private String  roleName;
    private String  status; // "ACTIVE" or "SUSPENDED"
    private boolean active;
    private String  createdAt;

    public UserTM(int userId, String fullName, String username, int roleId,
                  String roleCode, String roleName, boolean active, String createdAt) {
        this.userId    = userId;
        this.fullName  = fullName;
        this.username  = username;
        this.roleId    = roleId;
        this.roleCode  = roleCode;
        this.roleName  = roleName;
        this.active    = active;
        this.status    = active ? "ACTIVE" : "SUSPENDED";
        this.createdAt = (createdAt != null) ? createdAt : "-";
    }

    public int     getUserId()               { return userId; }
    public void    setUserId(int userId)     { this.userId = userId; }

    public String  getFullName()             { return fullName; }
    public void    setFullName(String name)  { this.fullName = name; }

    public String  getUsername()             { return username; }
    public void    setUsername(String username) { this.username = username; }

    public int     getRoleId()               { return roleId; }
    public void    setRoleId(int roleId)     { this.roleId = roleId; }

    public String  getRoleCode()             { return roleCode; }
    public void    setRoleCode(String code)  { this.roleCode = code; }

    public String  getRoleName()             { return roleName; }
    public void    setRoleName(String role)  { this.roleName = role; }

    public String  getStatus()               { return status; }
    public void    setStatus(String status)  { this.status = status; }

    public boolean isActive()                { return active; }
    public void    setActive(boolean active) {
        this.active = active;
        this.status = active ? "ACTIVE" : "SUSPENDED";
    }

    public String  getCreatedAt()            { return createdAt; }
    public void    setCreatedAt(String date) { this.createdAt = date; }
}
