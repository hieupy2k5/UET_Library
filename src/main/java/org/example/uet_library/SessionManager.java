package org.example.uet_library;

/**
 * This records for each session, which user is logging in.
 */
public class SessionManager {
    private static SessionManager instance;
    private int userId;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }
}
