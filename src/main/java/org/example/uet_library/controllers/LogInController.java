package org.example.uet_library.controllers;

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
import javafx.util.Pair;
import org.example.uet_library.utilities.AlertHelper;
import org.example.uet_library.utilities.SessionManager;

/**
 * Controller class for the login screen in the library management system.
 * Handles user interactions and transitions between scenes based on login results.
 */
public class LogInController implements Initializable {
    public TextField usernameFld;
    public Text registerBtn;
    public PasswordField passwordFld;
    public Button logInBtn;
    private UserController userController = new UserController();

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Handles the login button click event. Validates user credentials,
     * sets session information, and transitions to the menu screen if successful.
     *
     * @param event the ActionEvent triggered by the login button
     * @throws Exception if there is an error loading the next scene
     */
    public void handleLogInButton(ActionEvent event) throws Exception {
        String username = usernameFld.getText();
        String password = passwordFld.getText();
        // Save username for later use in ratings.
        RatingDialogController.userName = username.toLowerCase();
        // Check credentials with the user controller
        Pair<Integer, Boolean> logInResult = userController.checkLogInCredentials(username, password);
        Integer userID = logInResult.getKey();
        Boolean isAdmin = logInResult.getValue();
        if (userID != null && userID != -1) {
            // Set session details for the logged-in user
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
                menuController.loadHomeUser();
            }

            Scene menuScene = new Scene(menuParent);

            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(menuScene);

            // Adjust the stage size and center it on the screen
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

    /**
     * Handles the sign-up button click event. Transitions to the sign-up screen.
     *
     * @param event the MouseEvent triggered by clicking the sign-up button
     * @throws Exception if there is an error loading the sign-up scene
     */
    public void handleSignUpButton(MouseEvent event) throws Exception {
        ChangeSceneMachine.getInstance().changeScene("SignUp.fxml", event, -1, -1);
    }


    /**
     * Initializes the controller after its root element has been completely loaded.
     * This method is called automatically by the JavaFX runtime.
     *
     * @param url the location used to resolve relative paths for the root object
     * @param resourceBundle the resources used to localize the root object
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }
}
