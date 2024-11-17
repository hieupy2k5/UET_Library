package org.example.uet_library.Controllers;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import org.example.uet_library.AlertHelper;

public class SignUpController {

    public TextField usernameFld;
    public PasswordField passwordFld;
    public PasswordField confirmPasswordFld;
    public TextField firstNameFld;
    public TextField lastNameFld;
    public TextField emailFld;
    public Button registerBtn;
    public Text loginBtn;
    UserController userController = new UserController();

    public void handleSignUpButton(ActionEvent event) throws Exception {
        String username = usernameFld.getText();
        String password = passwordFld.getText();
        String confirmPassword = confirmPasswordFld.getText();
        String firstName = firstNameFld.getText();
        String lastName = lastNameFld.getText();
        String email = emailFld.getText();

        if (username.equals("")) {
            AlertHelper.showAlert(AlertType.WARNING, "Empty username",
                "You cannot leave your username empty!");
            return;
        } else if (password.equals("")) {
            AlertHelper.showAlert(AlertType.WARNING, "Empty password",
                "You cannot leave your password empty!");
            return;
        } else if (password.length() < 8) {
            AlertHelper.showAlert(AlertType.WARNING, "Your password is too short",
                "Your password must be at least 8 characters");
            return;
        } else if (password.length() >= 30) {
            AlertHelper.showAlert(AlertType.WARNING, "Your password is too long",
                "Are you gonna remember all this? Your password should be less than 30 characters");
            return;
        } else if (confirmPassword.equals("")) {
            AlertHelper.showAlert(AlertType.WARNING, "Empty password confirmation",
                "You haven't confirm your password yet!");
            return;
        }
        if (!password.equals(confirmPassword)) {
            AlertHelper.showAlert(AlertType.WARNING, "Passwords do not match",
                "Please confirm your password again");
            return;
        } else if (firstName.equals("")) {
            AlertHelper.showAlert(AlertType.WARNING, "Empty first name",
                "Don't you have a first name?");
            return;
        } else if (lastName.equals("")) {
            AlertHelper.showAlert(AlertType.WARNING, "Empty last name",
                "Don't you have a last name?");
            return;
        } else if (email.equals("")) {
            AlertHelper.showAlert(AlertType.WARNING, "Empty email",
                "Please provide a valid email address!");
            return;
        }

        if (userController.signUpUser(username, password, firstName, lastName, email)) {
            AlertHelper.showAlert(AlertType.INFORMATION, "Successfully create a new account",
                "You can log in using your new account now!");
        } else {
            AlertHelper.showAlert(AlertType.WARNING, "Username already exists",
                "Please choose another username");
        }
    }

    public void handleLogInButton(javafx.scene.input.MouseEvent event) throws Exception {
        ChangeSceneMachine.getInstance().changeScene("LogIn.fxml", event, -1, -1);
    }

}
