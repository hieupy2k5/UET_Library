package org.example.uet_library.controllers;

import java.io.IOException;
import java.util.Random;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MenuController {

    @FXML
    public Button button_UserManage;

    @FXML
    public Button button_MyRequests;

    @FXML
    public Button button_UserRequests;
    @FXML
    public Button button_MyFavorite;

    private boolean isAdmin;
    private Stage primaryStage;

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    @FXML
    public AnchorPane MenuBar;

    @FXML
    public VBox Clock;

    @FXML
    public Button button_ReturnBook;

    @FXML
    public Button button_BorrowBook;

    @FXML
    private Button button_BookShow;

    @FXML
    public Button button_AddBook;

    @FXML
    public Button button_ManagerBook;

    @FXML
    public Button button_LogOut;

    @FXML
    public Button button_Home_Ad;

    @FXML
    public Button button_Settings;

    @FXML
    public AnchorPane contentPane;

    @FXML
    public Label welcomeText;

    @FXML
    public Button button_Home_User;

    private String currentFXML = "";


    public void initialize() {
        loadClock();
    }

    public void configureMenu(boolean isAdmin) {
        if (isAdmin) {
            hideButtons(button_BorrowBook, button_ReturnBook, button_Home_User, button_MyRequests,
                button_MyFavorite);
        } else {
            hideButtons(button_AddBook, button_ManagerBook, button_Home_Ad, button_UserManage,
                button_BookShow, button_UserRequests);
        }

        this.setAdmin(isAdmin);
    }

    private void hideButtons(Button... buttons) {
        for (Button button : buttons) {
            button.setVisible(false);
            button.setManaged(false);
        }
    }

    public void loadClock() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXMLs/Clock.fxml"));
            VBox clockVBox = loader.load();
            Clock.getChildren().clear();
            Clock.getChildren().add(clockVBox);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void loadHomeView() {
        loadView("/FXMLs/HomeView.fxml");
    }

    @FXML
    public void loadBookView(ActionEvent event) throws IOException {
        loadView("/FXMLs/BooksView.fxml");
    }

    @FXML
    public void loadManagerBookView() {
        loadView("/FXMLs/BookManager.fxml");
    }

    @FXML
    public void loadUserManagerView(ActionEvent actionEvent) {
        loadView("/FXMLs/UserManager.fxml");
    }

    @FXML
    public void loadMyRequests(ActionEvent actionEvent) {
        loadView("/FXMLs/MyRequests.fxml");
    }

    @FXML
    public void loadUserRequests(ActionEvent actionEvent) {
        loadView("/FXMLs/UserRequests.fxml");
    }

    @FXML
    public void loadMyFavorites(ActionEvent actionEvent) {
        loadView("/FXMLs/UserFavorites.fxml");
    }

    @FXML
    public void loadBookAPISearchView() {
        loadView("/FXMLs/BookAPISearch.fxml");
        BookAPISearchController.menuController = this;
    }

    @FXML
    public void loadSettingsView() {
        loadView("/FXMLs/Settings.fxml");
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

    }

    public void loadView(String fxmlFileName) {
        if (!getCurrentFXML().equals(fxmlFileName)) {
            try {
                if (contentPane != null) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
                    AnchorPane newPane = loader.load();

                    contentPane.getChildren().clear();
                    contentPane.getChildren().add(newPane);

                    setCurrentFXML(fxmlFileName);
                } else {
                    System.out.println("contentPane is null. Cannot load the view.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadHomeUser() {

        if (!getCurrentFXML().equals("/FXMLs/UserHome.fxml")) {
            try {
                if (contentPane != null) {
                    FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/FXMLs/UserHome.fxml"));
                    AnchorPane newPane = loader.load();
                    // Uncomment the lines + change the temporary fxml to see User Home View
                    UserHomeController userHomeController = loader.getController();
                    userHomeController.setStage(primaryStage);
                    userHomeController.setMenuController(this);
                    contentPane.getChildren().clear();
                    contentPane.getChildren().add(newPane);
                    setCurrentFXML("/FXMLs/UserHome.fxml");
                } else {
                    System.out.println("contentPane is null. Cannot load the view.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleLogOut() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/FXMLs/LogIn.fxml"));
        Scene logInScene = new Scene(root);

        Stage currentStage = (Stage) button_LogOut.getScene().getWindow();
        currentStage.close();

        Stage newStage = new Stage();
        newStage.setScene(logInScene);
        newStage.show();
    }

    @FXML
    public void loadBorrowView(ActionEvent event) {
        loadView("/FXMLs/BorrowDocumentView.fxml");
    }

    @FXML
    public void loadReturnView(ActionEvent event) throws Exception {
        loadView("/FXMLs/ReturnDocumentView.fxml");
    }

    public void setContent(Parent root) {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(root);
    }

    public void setStage(Stage stage) {
        this.primaryStage = stage;
    }

    public Parent getContent() {
        return contentPane.getChildren().isEmpty() ? null
            : (Parent) contentPane.getChildren().get(0);
    }

    public String getCurrentFXML() {
        return currentFXML;
    }

    public void setCurrentFXML(String currentFXML) {
        this.currentFXML = currentFXML;
    }
}
