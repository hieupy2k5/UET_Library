package org.example.uet_library.Controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.example.uet_library.AlertHelper;
import org.example.uet_library.SessionManager;

public class LogInController implements Initializable {

    public TextField usernameFld;
    public Text registerBtn;
    public PasswordField passwordFld;
    public Button loginBtn;
    private UserController userController = new UserController();

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void handleLogInButton(ActionEvent event) throws Exception {
        String username = usernameFld.getText();
        String password = passwordFld.getText();
        Integer userID = userController.checkLoginCredentials(username, password).getKey();
        Boolean isAdmin = userController.checkLoginCredentials(username, password).getValue();
        if (userID != null && userID != -1) {
            SessionManager.getInstance().setUserId(userID);
            SessionManager.getInstance().setAdmin(isAdmin);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXMLs/Menu.fxml"));
            Parent menuParent = loader.load();

            MenuController menuController = loader.getController();
            menuController.setStage(this.stage);
            menuController.configureMenu(isAdmin);
            if (isAdmin) {
                menuController.loadView("/FXMLs/HomeView.fxml");
            } else {
                menuController.loadViewForUserHome();
            }

            Scene menuScene = new Scene(menuParent);

            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(menuScene);

            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

            window.setWidth(1180);
            window.setHeight(900);

            window.setX((screenBounds.getWidth() - 1180) / 2);
            window.setY((screenBounds.getHeight() - 900) / 2);

            window.show();
        } else if (userID == null) {
            AlertHelper.showAlert(AlertType.ERROR, "Log in unsuccessfully",
                "Wrong username or password. Please try again.");
        } else {
            AlertHelper.showAlert(AlertType.ERROR, "Log in unsuccessfully",
                "An unexpected error occurred. Please try again later.");
        }
    }

    public void handleSignUpButton(MouseEvent event) throws Exception {
        ChangeSceneMachine.getInstance().changeScene("SignUp.fxml", event, -1, -1);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }
}
