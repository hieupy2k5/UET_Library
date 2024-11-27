package org.example.uet_library.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.example.uet_library.database.Config;
import org.example.uet_library.database.Database;
import org.example.uet_library.utilities.SessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

abstract class ServiceTest {
    @BeforeEach
    public void setUp() throws SQLException {
        Config config = Config.getInstance();
        config.set("DATABASE_NAME", "library_test");
        SessionManager.getInstance().setUserId(1);
        try {
            setUpDB();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    public void tearDown() {
        try {
            clearDB();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Config config = Config.getInstance();
        config.reset();
    }

    abstract void setUpDB() throws SQLException;


    public void clearDB() throws SQLException {
        Database db = new Database();
        Connection conn = db.getConnection();

        String[] queries = new String[] {
            "DELETE FROM borrow;",
            "DELETE FROM requests;",
            "DELETE FROM admins;",
            "DELETE FROM favors;",
            "DELETE FROM ratings;",
            "DELETE FROM books;",
            "DELETE FROM users;",
        };

        for (String query : queries) {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.executeUpdate();
        }
    }
}
