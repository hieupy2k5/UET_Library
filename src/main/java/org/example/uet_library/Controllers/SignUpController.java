package org.example.uet_library.Controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import org.example.uet_library.AlertHelper;

public class SignUpController implements Initializable {

    public TextField usernameFld;
    public PasswordField passwordFld;
    public PasswordField confirmPasswordFld;
    public TextField firstNameFld;
    public TextField lastNameFld;
    public TextField emailFld;
    public Button registerBtn;
    public Text loginBtn;
    public ChoiceBox<String> choiceBox;
    UserController userController = new UserController();
    private final String[] choices = {"Admin", "User"};

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        choiceBox.getItems().addAll(choices);
        choiceBox.setValue(choices[1]);
    }

    public void handleSignUpButton(ActionEvent event) throws Exception {
        String username = usernameFld.getText();
        String password = passwordFld.getText();
        String confirmPassword = confirmPasswordFld.getText();
        String firstName = firstNameFld.getText();
        String lastName = lastNameFld.getText();
        String email = emailFld.getText();
        boolean isAdmin = choiceBox.getValue().equals(choices[0]);

        if (username.equals("")) {
            AlertHelper.showAlert(AlertType.ERROR, "Empty username",
                "You cannot leave your username empty!");
            return;
        } else if (password.equals("")) {
            AlertHelper.showAlert(AlertType.ERROR, "Empty password",
                "You cannot leave your password empty!");
            return;
//        } else if (password.length() < 8) {
//            AlertHelper.showAlert(AlertType.ERROR, "Your password is too short",
//                "Your password must be at least 8 characters");
//            return;
//        } else if (password.length() >= 30) {
//            AlertHelper.showAlert(AlertType.ERROR, "Your password is too long",
//                "Are you going to remember all this? Your password should be less than 30 characters");
//            return;
        } else if (confirmPassword.equals("")) {
            AlertHelper.showAlert(AlertType.ERROR, "Empty password confirmation",
                "You haven't confirm your password yet!");
            return;
        }
        if (!password.equals(confirmPassword)) {
            AlertHelper.showAlert(AlertType.ERROR, "Passwords do not match",
                "Please confirm your password again");
            return;
        } else if (firstName.equals("")) {
            AlertHelper.showAlert(AlertType.ERROR, "Empty first name",
                "Don't you have a first name?");
            return;
        } else if (lastName.equals("")) {
            AlertHelper.showAlert(AlertType.ERROR, "Empty last name",
                "Don't you have a last name?");
            return;
        } else if (email.equals("")) {
            AlertHelper.showAlert(AlertType.ERROR, "Empty email",
                "Please provide a valid email address!");
            return;
        }

        if (userController.signUpUser(username, password, firstName, lastName, email, isAdmin)
            == 1) {
            AlertHelper.showAlert(AlertType.INFORMATION, "Successfully create a new admin account",
                "You can log in using your new admin account now!");
        } else if (
            userController.signUpUser(username, password, firstName, lastName, email, isAdmin)
                == 2) {
            AlertHelper.showAlert(AlertType.INFORMATION, "Successfully create a new user account",
                "You can log in using your new user account now!");
        } else {
            AlertHelper.showAlert(AlertType.ERROR, "Username already exists",
                "Please choose another username");
        }
    }

    public void handleLogInButton(javafx.scene.input.MouseEvent event) throws Exception {
        ChangeSceneMachine.getInstance().changeScene("LogIn.fxml", event, -1, -1);
    }

}
