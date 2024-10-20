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
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class LogInController {

    public TextField usernameFld;
    public PasswordField passwordFld;
    public Button loginBtn;
    public Label messageLbl;
    private UserController userController = new UserController();

    public void handleLogInButton(ActionEvent event) throws Exception {
        String username = usernameFld.getText();
        String password = passwordFld.getText();

        if (userController.checkLoginCredentials(username, password)) {
            Parent menuScreen = FXMLLoader.load(getClass().getResource("/fxml/Menu.fxml"));
            Scene menuScene = new Scene(menuScreen);

            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(menuScene);
            window.show();
        } else {
            messageLbl.setText("Wrong username or password");
        }
    }

    public void handleSignUpButton(MouseEvent event) throws Exception {
        Parent signUpScreen = FXMLLoader.load(getClass().getResource("/fxml/SignUp.fxml"));
        Scene signUpScene = new Scene(signUpScreen);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(signUpScene);
        window.show();
    }
}

