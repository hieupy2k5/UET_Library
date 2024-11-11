package org.example.uet_library.Controllers;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.example.uet_library.Book;
import javafx.scene.text.Text;
import org.example.uet_library.BookService;

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

    private Stage previousStage;

    private int pageIndex;

    private ObservableList<Book> recommendBooks;

    private Book bookCurrent;

    @FXML
    void BackOnAction(ActionEvent event) throws IOException {
        previousStage.show();

        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        currentStage.close();
    }

    public void setDate(Book book) {
        this.bookCurrent = book;
        description.setText(book.getDescription());
        description.setTextAlignment(TextAlignment.JUSTIFY);
        Author_label.setText(book.getAuthor());
        BookTitle.setText(book.getTitle());
        Image image = new Image(book.getImageUrl());
        imageBook.setImage(image);
        fetchBookForRecommendBook();
    }

    public void setPreviousStage(Stage stage, int pageIndex) {
        this.previousStage = stage;
        this.pageIndex = pageIndex;
    }

    public void setPreviousStage(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public void fetchBookForRecommendBook() {
        Task<ObservableList<Book>> task = BookService.getInstance().featchBookForPage(this.bookCurrent);
        // Stage stage = (Stage) .getScene().getWindow();
        task.setOnSucceeded(event -> {
            recommendBooks = task.getValue();
            for(Book book : recommendBooks) {

                try {
                    FXMLLoader fxmlloader = new FXMLLoader(getClass().getResource("/FXMLs/Card.fxml"));
                    HBox cardBox = fxmlloader.load();

                    CardController card = fxmlloader.getController();
                    card.setData(book);
                    cardLayout.getChildren().add(cardBox);
                    Stage stage = (Stage) cardLayout.getScene().getWindow();
                    card.setPreviousStage(this.previousStage, stage, pageIndex);
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
