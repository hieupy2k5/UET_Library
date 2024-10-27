package org.example.uet_library.Controllers;

import java.io.IOException;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class MenuController implements Initializable {
    @FXML
    private Button document_show;
    @FXML
    public Button button_BorrowDocument;

    @FXML
    public Button button_Users;

    @FXML
    public Button button_LogOut;

    @FXML
    public Button button_Home;

    @FXML
    public AnchorPane contentPane;

    @FXML
    public Label welcomeText;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadView("/fxml/homeview.fxml");
    }

    @FXML
    public void loadHomeView() {
        loadView("/fxml/homeview.fxml");
    }

    @FXML
    public void loadDocumentsView(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/documentsView.fxml"));
        Scene logInScene = new Scene(root);

        Stage currentStage = (Stage) button_LogOut.getScene().getWindow();
        currentStage.close();

        Stage newStage = new Stage();
        newStage.setScene(logInScene);
        newStage.show();
    }

    @FXML
    public void loadUsersView() {
        loadView("/fxml/BookManager.fxml");
    }

    @FXML
    public void loadBorrowDocumentsView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/BookAPISearch.fxml"));
            AnchorPane newPane = loader.load();

            BookAPISearch controller = loader.getController();
            controller.setMenuController(this);

            contentPane.getChildren().clear();
            contentPane.getChildren().add(newPane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private String[] welcomeStrings = {
        "Welcome, %s! What is on your mind today?",
        "Hey there, %s! What do you want to read today?",
        "Greetings, %s! What book do you want to read?",
        "Hello, %s! Great to see you visiting our library!"
    };

    public void setWelcomeMessage(String username) {
        Random random = new Random();
        int randomIndex = random.nextInt(welcomeStrings.length);
        welcomeText.setText(String.format(welcomeStrings[randomIndex], username));
    }

    public void loadView(String fxmlFileName) {
        try {
            if (contentPane != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
                AnchorPane newPane = loader.load();

                contentPane.getChildren().clear();
                contentPane.getChildren().add(newPane);
            } else {
                System.out.println("contentPane is null. Cannot load the view.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleLogOut() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
        Scene logInScene = new Scene(root);

        Stage currentStage = (Stage) button_LogOut.getScene().getWindow();
        currentStage.close();

        Stage newStage = new Stage();
        newStage.setScene(logInScene);
        newStage.show();
    }

    public void setContent(Parent root) {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(root);
    }
}
