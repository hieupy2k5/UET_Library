package org.example.uet_library.Controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class MenuController {

    public void handleLogOutButton(ActionEvent event) throws Exception {
        Parent logInScreen = FXMLLoader.load(getClass().getResource("/fxml/LogIn.fxml"));
        Scene logInScene = new Scene(logInScreen);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(logInScene);
        window.show();
    }
}
