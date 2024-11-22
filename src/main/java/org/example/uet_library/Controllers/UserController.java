package org.example.uet_library.Controllers;

import at.favre.lib.crypto.bcrypt.BCrypt;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.util.Pair;
import org.example.uet_library.Database;
import org.example.uet_library.enums.SignUpResult;

public class UserController {

    public SignUpResult signUpUser(String username, String password, String firstName, String lastName,
        String email, boolean isAdmin) {

        String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        Database connection = new Database();
        String tableName = isAdmin ? "admins" : "users";
        String checkQuery = "SELECT COUNT(*) FROM " + tableName + " WHERE username = ?";
        String insertUserQuery = "INSERT INTO " + tableName
            + " (username, password, first_name, last_name, email) VALUES (?, ?, ?, ?, ?)";

        try (Connection conDB = connection.getConnection()) {
            PreparedStatement checkStatement = conDB.prepareStatement(checkQuery);
            checkStatement.setString(1, username);
            ResultSet rs = checkStatement.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            if (count > 0) {
                return SignUpResult.ALREADY_EXISTS;
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
     * For checking log ins.
     *
     * @return a pair of Integer and Boolean, whereas Integer denotes the ID of the user/admin and
     * Boolean denotes whether the person is admin or not.
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
