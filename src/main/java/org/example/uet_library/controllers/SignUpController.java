/**
 * The {@code SignUpController} class handles the user interface and logic for the sign-up process.
 * <p>
 * It manages user input validation, communicates with the {@link UserController} to register users,
 * and updates the application scene as required.
 * </p>
 */
package org.example.uet_library.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import org.example.uet_library.utilities.AlertHelper;
import org.example.uet_library.enums.SignUpResult;

public class SignUpController implements Initializable {

    public TextField usernameFld;
    public PasswordField passwordFld;
    public PasswordField confirmPasswordFld;
    public TextField firstNameFld;
    public TextField lastNameFld;
    public TextField emailFld;
    public Button registerBtn;
    public Text logInBtn;
    public ChoiceBox<String> choiceBox;
    UserController userController = new UserController();
    private final String[] choices = {"Admin", "User"};

    /**
     * Initializes the controller class. Sets up default values and options for the choice box.
     *
     * @param url            the location used to resolve relative paths for the root object, or null if not known
     * @param resourceBundle the resources used to localize the root object, or null if not provided
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        choiceBox.getItems().addAll(choices);
        choiceBox.setValue(choices[1]);
    }

    /**
     * Handles the sign-up button click event.
     * <p>
     * Validates user inputs, checks for potential errors, and interacts with the {@link UserController}
     * to register a new user or admin. Displays alerts based on the operation's outcome.
     * </p>
     *
     * @param event the event triggered by the sign-up button
     * @throws Exception if an error occurs while handling the event
     */
    public void handleSignUpButton(ActionEvent event) throws Exception {
        String username = usernameFld.getText();
        String password = passwordFld.getText();
        String confirmPassword = confirmPasswordFld.getText();
        String firstName = firstNameFld.getText();
        String lastName = lastNameFld.getText();
        String email = emailFld.getText();
        boolean isAdmin = choiceBox.getValue().equals(choices[0]);

        if (username.isEmpty()) {
            AlertHelper.showAlert(AlertType.ERROR, "Empty username",
                "You cannot leave your username empty!");
            return;
        } else if (password.isEmpty()) {
            AlertHelper.showAlert(AlertType.ERROR, "Empty password",
                "You cannot leave your password empty!");
            return;
        } else if (password.length() < 8) {
            AlertHelper.showAlert(AlertType.ERROR, "Your password is too short",
                "Your password must be at least 8 characters");
            return;
        } else if (password.length() >= 30) {
            AlertHelper.showAlert(AlertType.ERROR, "Your password is too long",
                "Are you going to remember all this? Your password should be less than 30 characters");
            return;
        } else if (confirmPassword.isEmpty()) {
            AlertHelper.showAlert(AlertType.ERROR, "Empty password confirmation",
                "You haven't confirm your password yet!");
            return;
        }
        if (!password.equals(confirmPassword)) {
            AlertHelper.showAlert(AlertType.ERROR, "Passwords do not match",
                "Please confirm your password again");
            return;
        } else if (firstName.isEmpty()) {
            AlertHelper.showAlert(AlertType.ERROR, "Empty first name",
                "Don't you have a first name?");
            return;
        } else if (lastName.isEmpty()) {
            AlertHelper.showAlert(AlertType.ERROR, "Empty last name",
                "Don't you have a last name?");
            return;
        } else if (email.isEmpty()) {
            AlertHelper.showAlert(AlertType.ERROR, "Empty email",
                "Please provide a valid email address!");
            return;
        }

        SignUpResult signUpResult = userController.signUpUser(username, password, firstName, lastName, email,
            isAdmin);
        if (signUpResult == SignUpResult.ADMIN_CREATED) {
            AlertHelper.showAlert(AlertType.INFORMATION, "Successfully create a new admin account",
                "You can log in using your new admin account now!");
            this.changeToLoginScene(event);
        } else if (signUpResult == SignUpResult.USER_CREATED) {
            AlertHelper.showAlert(AlertType.INFORMATION, "Successfully create a new user account",
                "You can log in using your new user account now!");
            this.changeToLoginScene(event);
        } else if (signUpResult == SignUpResult.ALREADY_EXISTS) {
            AlertHelper.showAlert(AlertType.ERROR, "Username already exists",
                "Please choose another username");
        } else {
            AlertHelper.showAlert(AlertType.ERROR, "Something unexpected occurred",
                "Please try again later.");
        }
    }

    /**
     * Handles the login button click event.
     * <p>
     * Navigates the user to the login scene.
     * </p>
     *
     * @param event the mouse event triggered by clicking the login button
     * @throws Exception if an error occurs while navigating to the login scene
     */
    public void handleLogInButton(javafx.scene.input.MouseEvent event) throws Exception {
        ChangeSceneMachine.getInstance().changeScene("LogIn.fxml", event, -1, -1);
    }

    /**
     * Changes the current scene to the login scene.
     *
     * @param event the event that triggers the scene change
     * @throws IOException if an error occurs while loading the login scene
     */
    private void changeToLoginScene(ActionEvent event) throws IOException {
        ChangeSceneMachine.getInstance().changeScene("LogIn.fxml", event, -1, -1);
    }

}
