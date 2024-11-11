package org.example.uet_library.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.example.uet_library.Book;

import java.io.IOException;


public class CardController {
    @FXML
    private ImageView BookImage;

    @FXML
    private Label BookTitle;

    @FXML
    private Label authorBook;

    @FXML
    private ImageView ratingStar;

    private Stage previousStage2;

    public void setPreviousStage2(Stage previousStage2) {
        this.previousStage2 = previousStage2;
    }

    @FXML
    private HBox bookBox;

    private Stage previousStage;
    private int pageIndex;

    private String[] colors = {"#B9E5FF","#BDB2FE", "#FB9AA8", "#FF5056"};

    Book book;
    public void setData(Book book) {
        this.book = book;
        Image image = new ImageView(book.getImageLink()).getImage();
        BookImage.setImage(image);
        BookTitle.setText(book.getTitle());
        authorBook.setText(book.getAuthor());
        String randomColor = colors[(int)(Math.random() * colors.length)];
        bookBox.setStyle("-fx-background-color: " + randomColor + ";"
                          + "-fx-background-radius: 20;" + "-fx-effect: dropShadow(three-pass-box, rgba(0,0,0,0),10,0,0,10);");

    }

    public void latestBookButtonOnAction(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXMLs/User_BookView.fxml"));
        Parent root = loader.load();
        ShowBookInformation showBookInformation = loader.getController();
        showBookInformation.setDate(book);
        showBookInformation.setPreviousStage(this.previousStage, this.pageIndex);

        Scene scene = new Scene(root, 1280, 800);
        Stage stage = new Stage();
        stage.setScene(scene);
        previousStage.hide();
        if (previousStage2 != null) {
            previousStage2.close();
        }
        stage.show();
    }

    public void setPreviousStage(Stage previousStage, int pageIndex) {
        this.previousStage = previousStage;
        this.pageIndex = pageIndex;
    }

    public void setPreviousStage(Stage previousStage, Stage previousStage2, int pageIndex) {
        this.previousStage = previousStage;
        this.previousStage2 = previousStage2;
        this.pageIndex = pageIndex;
    }


}
