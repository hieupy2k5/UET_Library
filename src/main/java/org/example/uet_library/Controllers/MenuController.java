package org.example.uet_library.Controllers;

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


    public void initialize() {
        loadClock();
    }

    public void configureMenu(boolean isAdmin) {
        if (isAdmin) {
            button_BorrowBook.setVisible(false);
            button_BorrowBook.setManaged(false);

            button_ReturnBook.setVisible(false);
            button_ReturnBook.setManaged(false);

            button_Home_User.setVisible(false);
            button_Home_User.setManaged(false);

            button_MyRequests.setVisible(false);
            button_MyRequests.setManaged(false);

            button_MyFavorite.setVisible(false);
            button_MyFavorite.setManaged(false);
        } else {
            button_AddBook.setVisible(false);
            button_AddBook.setManaged(false);

            button_ManagerBook.setVisible(false);
            button_ManagerBook.setManaged(false);

            button_Home_Ad.setVisible(false);
            button_Home_Ad.setManaged(false);

            button_UserManage.setVisible(false);
            button_UserManage.setManaged(false);

            button_BookShow.setVisible(false);
            button_BookShow.setManaged(false);

            button_UserRequests.setVisible(false);
            button_UserRequests.setManaged(false);
        }

        this.setAdmin(isAdmin);
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
    public void loadMyFavorites(ActionEvent actionEvent) { loadView("/FXMLs/UserFavorites.fxml"); }

    @FXML
    public void loadBookAPISearchView() {
        loadView("/FXMLs/BookAPISearch.fxml");
        BookAPISearch.menuController = this;
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

    public void loadHomeUser() {
        try {
            if (contentPane != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXMLs/UserHome.fxml"));
                AnchorPane newPane = loader.load();
                // Uncomment the lines + change the temporary fxml to see User Home View
                UserHomeController userHomeController = loader.getController();
                userHomeController.setStage(primaryStage);
                userHomeController.setMenuController(this);
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

}
