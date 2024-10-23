package org.example.uet_library.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import javax.swing.plaf.nimbus.State;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MenuController implements Initializable {
    @FXML
    public Button button_Documents;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadView("/fxml/homeview.fxml");
    }

    @FXML
    public void loadHomeView() {
        loadView("/fxml/homeview.fxml");
    }

    @FXML
    public void loadDocumentsView() {
        loadView("/fxml/documentsView.fxml");
    }

    @FXML
    public void loadUsersView() {
        loadView("/fxml/usersView.fxml");
    }

    @FXML
    public void loadBorrowDocumentsView() {
        loadView("/fxml/borrowDocumentView.fxml");
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
    public void handleLogOut() throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
        Scene logInScene = new Scene(root);

        Stage currentStage = (Stage) button_LogOut.getScene().getWindow();
        currentStage.close();

        Stage newStage = new Stage();
        newStage.setScene(logInScene);
        newStage.show();
    }
}
