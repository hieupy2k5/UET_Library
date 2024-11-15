package org.example.uet_library.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.example.uet_library.Book;


import java.io.IOException;
import java.util.Stack;

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

    public void setUserHomeController(UserHomeController userHomeController) {
        this.userHomeController = userHomeController;
    }

    private String[] colors = {"#B9E5FF","#BDB2FE", "#FB9AA8", "#FF5056"};

    public void setData(Book book) {
        this.book = book;
        Image image = new ImageView(book.getImageLink()).getImage();
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
