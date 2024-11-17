package org.example.uet_library.Controllers;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.mysql.cj.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import javafx.scene.control.TextInputDialog;
import org.example.uet_library.Database;
import org.example.uet_library.SessionManager;

public class SettingsController {

    @FXML
    private TextField firstNameField, lastNameField, emailField, userNameField, passwordField;

    public int userID = SessionManager.getInstance().getUserId();;
    public boolean isAdmin = SessionManager.getInstance().isAdmin();

    public void initialize() {
        loadUserInfo();
        disableFields();
    }

    String username = null;
    String currentPasswordHash = null;

    private void loadUserInfo() {
        Database connection = new Database();
        String tableName = isAdmin ? "admins" : "users";
        String query = "SELECT username, password, first_name, last_name, email FROM " + tableName + " WHERE id = ?";
        try (Connection conDB = connection.getConnection()) {
            PreparedStatement statement = conDB.prepareStatement(query);
            statement.setInt(1, userID);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                username = resultSet.getString("username");
                currentPasswordHash = resultSet.getString("password");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String email = resultSet.getString("email");

                userNameField.setText(username);
                firstNameField.setText(firstName);
                lastNameField.setText(lastName);
                emailField.setText(email);
            } else {
                System.out.println("No account found with ID: " + userID);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void disableFields() {
        firstNameField.setDisable(true);
        lastNameField.setDisable(true);
        emailField.setDisable(true);
        userNameField.setDisable(true);
        passwordField.setDisable(true);
    }

    @FXML
    private void changeFirstName() { firstNameField.setDisable(false); }

    @FXML
    private void doneFirstName() { firstNameField.setDisable(true); }

    @FXML
    private void changeLastName() { lastNameField.setDisable(false); }

    @FXML
    private void doneLastName() { lastNameField.setDisable(true); }

    @FXML
    private void changeEmail() { emailField.setDisable(false); }

    @FXML
    private void doneEmail() { emailField.setDisable(true); }

    @FXML
    private void changeUserName() { userNameField.setDisable(false); }

    @FXML
    private void doneUserName() { userNameField.setDisable(true); }

    @FXML
    private void changePassword() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Xác nhận mật khẩu");
        dialog.setHeaderText("Nhập mật khẩu hiện tại để tiếp tục:");
        dialog.setContentText("Mật khẩu:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String enteredPassword = result.get();
            if (checkPassword(enteredPassword)) {
                passwordField.setDisable(false);
                passwordField.setText("");
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi");
                alert.setHeaderText("Mật khẩu không đúng");
                alert.setContentText("Vui lòng nhập đúng mật khẩu hiện tại.");
                alert.showAndWait();
            }
        }
    }

    private UserController userController = new UserController();

    private boolean checkPassword(String enteredPassword) {
        Database connection = new Database();
        String tableName = isAdmin ? "admins" : "users";
        String query = "SELECT password FROM " + tableName + " WHERE id = ?";
        try (Connection conDB = connection.getConnection()) {
            PreparedStatement statement = conDB.prepareStatement(query);
            statement.setInt(1, userID);
            ResultSet resultSet = statement.executeQuery();
            Integer userID = userController.checkLoginCredentials(username, enteredPassword).getKey();

            if (resultSet.next()) {
                System.out.println(userID);
                return (userID != null && userID != -1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @FXML
    private void donePassword() {
        passwordField.setDisable(true);
    }

    @FXML
    private void saveChanges() {
        String tableName = isAdmin ? "admins" : "users";
        String updateQuery = "UPDATE " + tableName + " SET username = ?, first_name = ?, last_name = ?, email = ?, password = ? WHERE id = ?";
        Database connection = new Database();
        try (Connection conDB = connection.getConnection()) {
            PreparedStatement statement = conDB.prepareStatement(updateQuery);
            statement.setString(1, userNameField.getText());
            statement.setString(2, firstNameField.getText());
            statement.setString(3, lastNameField.getText());
            statement.setString(4, emailField.getText());

            if (!passwordField.getText().isEmpty()) {
                String hashedPassword = BCrypt.withDefaults().hashToString(12, passwordField.getText().toCharArray());
                statement.setString(5, hashedPassword);
                currentPasswordHash = hashedPassword;
            } else {
                statement.setString(5, currentPasswordHash);
            }

            statement.setInt(6, userID);
            statement.executeUpdate();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Account information updated successfully!");
            alert.show();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
