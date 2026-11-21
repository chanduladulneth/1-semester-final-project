package com.dcroyal.util;

import com.dcroyal.model.User;

/**
 * Session.java
 * Stores the currently logged-in user.
 * Use Session.setCurrentUser() after login.
 * Use Session.getCurrentUser() anywhere you need user info.
 * Use Session.clearSession() when logging out.
 */
public class Session {

    // The currently logged-in user (static = shared across the whole app)
    private static User currentUser = null;

    // Save the logged-in user
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    // Get the logged-in user
    public static User getCurrentUser() {
        return currentUser;
    }

    // Clear session on logout
    public static void clearSession() {
        currentUser = null;
    }

    // Check if someone is logged in
    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}
