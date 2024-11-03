package org.example.uet_library.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class LogInController {

    public TextField usernameFld;
    public Text registerBtn;
    public PasswordField passwordFld;
    public Button loginBtn;
    public Label messageLbl;
    private UserController userController = new UserController();

    public void handleLogInButton(ActionEvent event) throws Exception {
        String username = usernameFld.getText();
        String password = passwordFld.getText();

        if (userController.checkLoginCredentials(username, password)) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Menu.fxml"));
            Parent menuParent = loader.load();

            MenuController menuController = loader.getController();
            //menuController.setWelcomeMessage(username);

            Scene menuScene = new Scene(menuParent);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(menuScene);

            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

            window.setWidth(1180);
            window.setHeight(850);

            window.setX((screenBounds.getWidth() - 1180) / 2);
            window.setY((screenBounds.getHeight() - 850) / 2);

            window.show();
        } else {
            messageLbl.setText("Wrong user or password!");
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

