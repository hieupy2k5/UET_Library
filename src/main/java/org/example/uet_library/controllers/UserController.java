/**
 * The {@code UserController} class handles user-related operations, including signing up
 * users/admins and validating login credentials.
 * It interacts with the database to manage user and admin records.
 */
package org.example.uet_library.controllers;

import at.favre.lib.crypto.bcrypt.BCrypt;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.util.Pair;
import org.example.uet_library.database.Database;
import org.example.uet_library.enums.SignUpResult;

public class UserController {

    /**
     * Registers a new user or admin in the system.
     * <p>
     * This method checks if the username already exists in either the "admins" or "users" table.
     * If the username is unique, it inserts a new record into the appropriate table.
     * Passwords are hashed using BCrypt before being stored in the database.
     * </p>
     *
     * @param username  the username for the new user/admin
     * @param password  the plaintext password to be hashed and stored
     * @param firstName the first name of the user/admin
     * @param lastName  the last name of the user/admin
     * @param email     the email address of the user/admin
     * @param isAdmin   a flag indicating whether the user is an admin
     * @return a {@link SignUpResult} indicating the outcome of the operation:
     *         {@code USER_CREATED}, {@code ADMIN_CREATED}, {@code ALREADY_EXISTS}, or {@code FAILED}
     */
    public SignUpResult signUpUser(String username, String password, String firstName,
        String lastName,
        String email, boolean isAdmin) {

        String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        Database connection = new Database();
        String tableName = isAdmin ? "admins" : "users";
        String checkQuery = """
            SELECT COUNT(*) FROM admins WHERE username = ?
            UNION
            SELECT COUNT(*) FROM users WHERE username = ?
            """;
        String insertUserQuery = "INSERT INTO " + tableName
            + " (username, password, first_name, last_name, email) VALUES (?, ?, ?, ?, ?)";

        try (Connection conDB = connection.getConnection()) {
            PreparedStatement checkStatement = conDB.prepareStatement(checkQuery);
            checkStatement.setString(1, username);
            checkStatement.setString(2, username);
            ResultSet rs = checkStatement.executeQuery();
            while (rs.next()) {
                int count = rs.getInt(1);
                if (count > 0) {
                    return SignUpResult.ALREADY_EXISTS;
                }
            }

            PreparedStatement statement = conDB.prepareStatement(insertUserQuery);
            statement.setString(1, username);
            statement.setString(2, hashedPassword);
            statement.setString(3, firstName);
            statement.setString(4, lastName);
            statement.setString(5, email);
            statement.executeUpdate();

            if (isAdmin) {
                return SignUpResult.ADMIN_CREATED;
            }
            return SignUpResult.USER_CREATED;
        } catch (SQLException e) {
            e.printStackTrace();
            return SignUpResult.FAILED;
        }
    }

    /**
     * Validates login credentials for a user or admin.
     * <p>
     * This method queries the "users" and "admins" tables to find a record matching the provided
     * username. If a match is found, it verifies the provided password against the stored hashed
     * password. It also determines whether the record belongs to a user or admin.
     * </p>
     *
     * @param username the username to be authenticated
     * @param password the plaintext password to be verified
     * @return a {@link Pair} containing:
     *         - {@code Integer}: the ID of the authenticated user/admin, or {@code null} if authentication fails.
     *         - {@code Boolean}: {@code true} if the authenticated entity is an admin, {@code false} otherwise.
     *         Returns {@code Pair<>(null, null)} if authentication fails.
     */
    public Pair<Integer, Boolean> checkLogInCredentials(String username, String password) {
        String query = """
                SELECT id, password, 'user' AS user_type FROM users WHERE username = ?
                UNION
                SELECT id, password, 'admin' AS user_type FROM admins WHERE username = ?
            """;

        Database db = new Database();
        try (Connection connection = db.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, username);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String storedPassword = resultSet.getString("password");
                boolean isAdmin = resultSet.getString("user_type").equals("admin");

                if (BCrypt.verifyer().verify(password.toCharArray(), storedPassword).verified) {
                    Integer id = resultSet.getInt("id");
                    return new Pair<>(id, isAdmin);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new Pair<>(-1, false);
        }

        return new Pair<>(null, null);
    }
}
