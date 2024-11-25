package org.example.uet_library.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.example.uet_library.models.Book;


import java.io.IOException;

public class BookCardController {
    @FXML
    private Label bookTitle;

    @FXML
    private ImageView ImageBook;

    @FXML
    private VBox bookCard;


    private Book book;

    @FXML
    private ImageView rating_star;

    private UserHomeController userHomeController;

    private ShowMoreResultController showMoreResultController;

    public void setUserHomeController(UserHomeController userHomeController) {
        this.userHomeController = userHomeController;
    }

    private String[] colors = {"#B9E5FF","#BDB2FE", "#FB9AA8", "#FF5056"};

    public void setData(Book book) {
        this.book = book;
        String url = book.getImageUrl();
        Image image;
        if (url == null || url.equals("")) {
            image = new Image(getClass().getResource("/Images/imageNotFound.jpg").toExternalForm());
        } else {
            image = new Image(url, true);
        }
        ImageBook.setImage(image);
        bookTitle.setText(book.getTitle());
        String randomColor = colors[(int)(Math.random() * colors.length)];
        bookTitle.setStyle("-fx-background-color: " + randomColor + ";"
                + "-fx-background-radius: 20;" + "-fx-effect: dropShadow(three-pass-box, rgba(0,0,0,0),10,0,0,10);");
    }

    @FXML
    public void bookButtonClicked(ActionEvent event) throws IOException {
        try {
            if (userHomeController != null && book != null) {
                userHomeController.openBookDetails(book);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
