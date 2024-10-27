package org.example.uet_library.Controllers;

import at.favre.lib.crypto.bcrypt.BCrypt;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.example.uet_library.Database;

public class UserController {

    public boolean signUpUser(String username, String password, String firstName, String lastName,
        String email) {
        String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        Database connection = new Database();
        String insertUserQuery = "INSERT INTO users (username, password, first_name, last_name, email) VALUES (?, ?, ?, ?, ?)";

        try(Connection conDB = connection.getConnection()) {
            PreparedStatement statement = conDB.prepareStatement(insertUserQuery);
            statement.setString(1, username);
            statement.setString(2, hashedPassword);
            statement.setString(3, firstName);
            statement.setString(4, lastName);
            statement.setString(5, email);
            statement.executeUpdate();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();

            return false;
        }
    }

    public boolean checkLoginCredentials(String username, String password) {
        String selectUserQuery = "SELECT * FROM users WHERE username = ?";
        Database connection = new Database();
        try(Connection conDB = connection.getConnection()) {
            PreparedStatement statement = conDB.prepareStatement(selectUserQuery);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String storedPassword = resultSet.getString("password");

                if (BCrypt.verifyer().verify(password.toCharArray(), storedPassword).verified) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}
