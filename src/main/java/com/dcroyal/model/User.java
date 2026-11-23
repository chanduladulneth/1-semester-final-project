package com.dcroyal.model;

/**
 * User.java
 * Holds the details of a logged-in user.
 * Simple class with fields, constructor, and getters/setters.
 */
public class User {

    private int    userId;
    private String fullName;
    private String username;
    private String roleCode;   // e.g., OWNER, MANAGER, COUNTER_STAFF
    private String roleName;   // e.g., Owner, Manager, Counter Staff

    // Constructor
    public User(int userId, String fullName, String username, String roleCode, String roleName) {
        this.userId   = userId;
        this.fullName = fullName;
        this.username = username;
        this.roleCode = roleCode;
        this.roleName = roleName;
    }

    // Getters
    public int    getUserId()   { return userId;   }
    public String getFullName() { return fullName; }
    public String getUsername() { return username; }
    public String getRoleCode() { return roleCode; }
    public String getRoleName() { return roleName; }

    // Setters
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setUsername(String username) { this.username = username; }

    /**
     * Returns the first letters of first and last name as initials.
     * Example: "Kasun Perera" -> "KP"
     */
    public String getInitials() {
        if (fullName == null || fullName.isEmpty()) return "?";
        String[] parts = fullName.trim().split(" ");
        if (parts.length == 1) return String.valueOf(parts[0].charAt(0)).toUpperCase();
        return (String.valueOf(parts[0].charAt(0)) + String.valueOf(parts[parts.length - 1].charAt(0))).toUpperCase();
    }
}
