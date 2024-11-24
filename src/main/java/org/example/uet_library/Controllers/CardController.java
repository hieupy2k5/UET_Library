package org.example.uet_library.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.example.uet_library.models.Book;

import java.io.IOException;


public class CardController {
    @FXML
    Button button;

    @FXML
    private ImageView BookImage;

    @FXML
    private Label BookTitle;

    @FXML
    private Label authorBook;

    @FXML
    private ImageView ratingStar;

    private UserHomeController userHomeController;

    public void setUserHomeController(UserHomeController userHomeController) {
        this.userHomeController = userHomeController;
    }

    @FXML
    private HBox bookBox;

    private Stage previousStage;
    private int pageIndex;

    private String[] colors = {"#B9E5FF","#BDB2FE", "#FB9AA8", "#FF5056"};

    Book book;
    public void setData(Book book) {
        this.book = book;
        String url = book.getImageUrl();
        Image image;
        if (url == null || url.equals("")) {
            image = new Image(getClass().getResource("/Images/imageNotFound.jpg").toExternalForm());
        } else {
            image = new Image(url, true);
        }
        BookImage.setImage(image);
        BookTitle.setText(book.getTitle());
        authorBook.setText(book.getAuthor());
        String randomColor = colors[(int)(Math.random() * colors.length)];
        String base_Style = "-fx-background-color: " + randomColor + ";"
                + "-fx-background-radius: 20;" + "-fx-effect: dropShadow(three-pass-box, rgba(0,0,0,0),10,0,0,10);";
        bookBox.setStyle(base_Style);
        button.setStyle("-fx-background-color: transparent;" + "-fx-border-color: transparent;" + "-fx-text-fill: transparent;" + "-fx-background-insets: 0;" + "-fx-padding: 0;" + "-fx-effect: null;" + "-fx-focus-color: transparent;" + "-fx-faint-focus-color: transparent;" + "-fx-hover-base: transparent;" + "-fx-border-insets: 0;" + "-fx-background-radius: 20;" + "-fx-border-radius: 20;");

        String hoverStyle = "-fx-background-color: " + "#F89D7B" + ";"
                + "-fx-text-fill: white; -fx-border-radius: 20px;" + "-fx-background-radius: 20px;";

        bookBox.setOnMouseEntered(event -> button.setStyle(hoverStyle));
        bookBox.setOnMouseExited(event -> button.setStyle(base_Style));
    }


    public void latestBookButtonOnAction(ActionEvent event) throws IOException {
        try {
            if (userHomeController != null && book != null) {
                userHomeController.openBookDetails(book);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
