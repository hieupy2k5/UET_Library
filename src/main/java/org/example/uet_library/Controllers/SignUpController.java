package org.example.uet_library.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SignUpController {

    public TextField usernameFld;
    public PasswordField passwordFld;
    public PasswordField confirmPasswordFld;
    public TextField firstNameFld;
    public TextField lastNameFld;
    public TextField emailFld;
    UserController userController = new UserController();
    public Label messageLbl;

    public void handleSignUpButton(ActionEvent event) throws Exception {
        String username = usernameFld.getText();
        String password = passwordFld.getText();
        String confirmPassword = confirmPasswordFld.getText();
        String firstName = firstNameFld.getText();
        String lastName = lastNameFld.getText();
        String email = emailFld.getText();

        if (!password.equals(confirmPassword)) {
            messageLbl.setText("Passwords do not match");
            return;
        }

        if (userController.signUpUser(username, password, firstName, lastName, email)) {
            messageLbl.setText("Successfully create a new account");
        } else {
            messageLbl.setText("Username already exists");
        }
    }

    public void handleLogInButton(javafx.scene.input.MouseEvent event) throws Exception {
        Parent logInScreen = FXMLLoader.load(getClass().getResource("/fxml/LogIn.fxml"));
        Scene logInScene = new Scene(logInScreen);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(logInScene);
        window.show();
    }

}
