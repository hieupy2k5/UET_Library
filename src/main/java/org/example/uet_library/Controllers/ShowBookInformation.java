package org.example.uet_library.Controllers;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextAlignment;
import org.example.uet_library.Book;
import javafx.scene.text.Text;
import org.example.uet_library.BookService;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class ShowBookInformation {
    @FXML
    private Text Author_label;

    @FXML
    private Text BookTitle;

    @FXML
    private ImageView imageBook;

    @FXML
    private Text description;

    @FXML
    private HBox cardLayout;

    @FXML
    private ImageView qrCode;

    private ObservableList<Book> recommendBooks;

    private Book bookCurrent;

    private UserHomeController userHomeController;

    public void setUserHomeController(UserHomeController userHomeController) {
        this.userHomeController = userHomeController;
    }

    @FXML
    void BackOnAction(ActionEvent event) throws IOException {
        if (userHomeController != null) {
            userHomeController.goBack();
        }
    }

    public void setDate(Book book) {
        this.bookCurrent = book;
        description.setText(book.getDescription());
        description.setTextAlignment(TextAlignment.JUSTIFY);
        Author_label.setText(book.getAuthor());
        BookTitle.setText(book.getTitle());
        Image image = new Image(book.getImageUrl());
        imageBook.setImage(image);
        byte[] qrCode = new byte[1];
        qrCode = book.getqrCode();
        ByteArrayInputStream bis = new ByteArrayInputStream(qrCode);
        this.qrCode.setImage(new Image(bis));
        fetchBookForRecommendBook();
    }



    public void fetchBookForRecommendBook() {
        Task<ObservableList<Book>> task = BookService.getInstance().fetchBookForPage(this.bookCurrent);
        task.setOnSucceeded(event -> {
            recommendBooks = task.getValue();
            for(Book book : recommendBooks) {

                try {
                    FXMLLoader fxmlloader = new FXMLLoader(getClass().getResource("/FXMLs/Card.fxml"));
                    HBox cardBox = fxmlloader.load();

                    CardController card = fxmlloader.getController();
                    card.setUserHomeController(this.userHomeController);
                    card.setData(book);
                    cardLayout.getChildren().add(cardBox);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        task.setOnFailed(event -> {
            System.out.println("Failed");
        });

        new Thread(task).start();
    }


}
