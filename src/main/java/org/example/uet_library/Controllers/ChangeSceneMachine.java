package org.example.uet_library;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;import org.example.uet_library.Controllers.UsersController;

import java.io.IOException;
import java.net.URL;

public class ChangeSceneMachine {
    private static UsersController.ChangeSceneMachine instance;
    private ChangeSceneMachine() {

    }

    public static UsersController.ChangeSceneMachine getInstance() {
        if (instance == null) {
            instance = new UsersController.ChangeSceneMachine();
        }
        return instance;
    }

    public void changeScene(String file, Button button, int width, int height) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
        Scene logInScene = new Scene(root);

        Stage currentStage = (Stage) button.getScene().getWindow();
        currentStage.close();

        Stage newStage = new Stage();
        newStage.setScene(logInScene);
        newStage.show();
    }

    public void changeScene2(String file, ActionEvent event, int width, int height, boolean check) throws IOException {
        URL resourse = getClass().getResource(file);
        if(resourse != null) {
            FXMLLoader loader = new FXMLLoader(resourse);
            Parent root = loader.load();
            Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root,width,height);
            stage.setScene(scene);
            stage.show();
        }
    }



}
