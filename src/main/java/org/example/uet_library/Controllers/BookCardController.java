package org.example.uet_library.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.example.uet_library.Book;

import java.io.IOException;
import java.util.Stack;

public class BookCardController {

    @FXML
    private Text bookTitle;

    @FXML
    private ImageView ImageBook;

    private Book book;

    @FXML
    private ImageView rating_star;

    private Stage previousStage;

    private int pageIndex;


    public void setData(Book book) {
        this.book = book;
        Image image = new ImageView(book.getImageLink()).getImage();
        ImageBook.setImage(image);
        bookTitle.setText(book.getTitle());
    }

    @FXML
    public void bookButtonClicked(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXMLs/User_BookView.fxml"));
        Parent root = loader.load();
        ShowBookInformation showBookInformationController = loader.getController();
        showBookInformationController.setDate(book);
        showBookInformationController.setPreviousStage(previousStage, pageIndex);

        Scene scene = new Scene(root, 1280, 800);
        Stage stage = new Stage();
        stage.setScene(scene);

        previousStage.hide();
        stage.show();
    }

    public void setPreviousStage(Stage previousStage, int pageIndex) {
        this.previousStage = previousStage;
    }


}
