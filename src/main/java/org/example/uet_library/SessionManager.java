package org.example.uet_library;

/**
 * This records for each session, which user is logging in.
 */
public class SessionManager {

    private static SessionManager instance;
    private int userId;
    private boolean isAdmin;

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public int getUserId() {
        return userId;
    }

    public boolean isAdmin() {
        return isAdmin;
    }
}
