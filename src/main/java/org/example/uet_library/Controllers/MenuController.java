package org.example.uet_library.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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

    // Tải nội dung Home khi bấm nút Home
    @FXML
    public void loadHomeView() {
        loadView("/fxml/homeview.fxml");
    }
    //
    // Tải nội dung Documents khi bấm nút Documents
//    @FXML
//    public void loadDocumentsView() {
//        loadView("documentsView.fxml");
//    }
//    //
//    // Tải nội dung Users khi bấm nút Users
//    @FXML
//    public void loadUsersView() {
//        loadView("usersView.fxml");
//    }
//    //
//    @FXML
//    public void loadBorrowDocumentsView() {
//        loadView("borrowDocumentView.fxml");
//    }



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
    public void handleLogOut() {
        System.exit(0);
    }
}
