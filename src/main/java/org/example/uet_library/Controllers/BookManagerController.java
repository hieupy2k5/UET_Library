package org.example.uet_library.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import javafx.event.ActionEvent;
import org.example.uet_library.Book;
import org.example.uet_library.BookService;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

public class BookManagerController {

    @FXML
    private TextField AuthorEdit;

    @FXML
    private TextField ISBNEdit;

    @FXML
    private TextField QuantityEdit;

    @FXML
    private TextField titleEdit;

    @FXML
    private TextField yearOfPublication;

    @FXML
    private TextField categoryBook;

    @FXML
    private ListView<Book> listViewTable;

    @FXML
    private TextField ISBNSearch = new TextField();

    private Book bookSelected;

    @FXML
    private Button editButton;

    @FXML
    private Button SaveButton;

    @FXML
    private Button removeBut;

    @FXML
    private ImageView imageOfBook;

    public void initialize() {
        listViewTable.setCellFactory(new Callback<ListView<Book>, ListCell<Book>>() {
            @Override
            public ListCell<Book> call(ListView<Book> bookListView) {
                return new ListCell<Book>() {
                    @Override
                    protected void updateItem(Book book, boolean empty) {
                        super.updateItem(book, empty);
                        if (empty || book == null) {
                            setText(null);
                        } else {
                            setText(book.getTitle());
                        }
                    }
                };
            }
        });

        ISBNSearch.textProperty().addListener((observableValue, oldValue, newValue) -> {
            if(newValue == null || newValue.isEmpty()) {
                listViewTable.setItems(FXCollections.observableArrayList());
            } else {
                Task<ObservableList<Book>> task = BookService.getInstance().fetchBookFromDB(newValue);
                task.setOnSucceeded(event -> {
                    System.out.println("Successfully retrieved book from database");
                    listViewTable.setItems(task.getValue());
                });
                task.setOnFailed(
                        event->task.getException().printStackTrace()
                );

                new Thread(task).start();
            }
        });

        /*String url = bookSelected.getImageUrl();
        if (url != null) {
            Image image = new Image(url);

        }*/
        listViewTable.setOnMouseClicked(event -> {
            bookSelected = listViewTable.getSelectionModel().getSelectedItem();
            this.setTextField(bookSelected);
            Image image = new Image(bookSelected.getImageUrl());
            imageOfBook.setImage(image);
            imageOfBook.setFitHeight(imageOfBook.getFitHeight());
            imageOfBook.setFitWidth(imageOfBook.getFitWidth());
            imageOfBook.setPreserveRatio(true);
            imageOfBook.setSmooth(true);
            imageOfBook.setCache(true);
        });

        if(listViewTable.getSelectionModel().getSelectedItem() == null || bookSelected == null) {
            SaveButton.setDisable(true);
            editButton.setDisable(true);
        }
        editButton.setDisable(true);
        SaveButton.setDisable(true);
        removeBut.setDisable(true);
    }

    private void setTextField(Book selectedBook) {
        if (bookSelected != null) {
            AuthorEdit.setText(bookSelected.getAuthor());
            titleEdit.setText(bookSelected.getTitle());
            ISBNEdit.setText(bookSelected.getIsbn());
            yearOfPublication.setText(bookSelected.getYear()+"");
            QuantityEdit.setText(String.valueOf(bookSelected.getQuantity()));
            categoryBook.setText(bookSelected.getType());
            handleCancel();
        }
    }

    @FXML
    private void handleEdit() {
        AuthorEdit.setEditable(true);
        ISBNEdit.setEditable(true);
        titleEdit.setEditable(true);
        QuantityEdit.setEditable(true);
        yearOfPublication.setEditable(true);
        categoryBook.setEditable(true);

        editButton.setDisable(true);
        SaveButton.setDisable(false);
        removeBut.setDisable(false);

    }

    private void handleCancel() {
        AuthorEdit.setEditable(false);
        ISBNEdit.setEditable(false);
        titleEdit.setEditable(false);
        QuantityEdit.setEditable(false);
        yearOfPublication.setEditable(false);
        categoryBook.setEditable(false);

        editButton.setDisable(false);
        SaveButton.setDisable(true);
        removeBut.setDisable(true);
    }

    @FXML
    private void removeButton(ActionEvent event) {
        String isbn = ISBNEdit.getText();
        if (isbn == null || isbn.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText("ISBN IS EMPTY, FAIL TO REMOVE, PLEASE TRY IT AGAIN");
            setTextField(bookSelected);
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to remove this book?");
            Optional<ButtonType> result = alert.showAndWait();
            //alert.showAndWait();
            if(result.get() == ButtonType.OK) {
                removeBook(isbn);
                listViewTable.setItems(null);
            } else {
                return;
            }
        }

    }

    private void removeBook(String isbn) {
        Task<Void> task = BookService.getInstance().deleteBook(isbn);
        task.setOnSucceeded(event -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Notion");
            alert.setHeaderText(null);
            alert.setContentText("Book deleted successfully");
            AuthorEdit.setText(null);
            ISBNEdit.setText(null);
            titleEdit.setText(null);
            ISBNEdit.setText(null);
            QuantityEdit.setText(null);
            ISBNSearch.setText(null);
            yearOfPublication.setText(null);
            categoryBook.setText(null);
            alert.showAndWait();
            handleCancel();
        });
        task.setOnFailed(event->task.getException().printStackTrace());
        new Thread(task).start();
    }

    public void SaveBookOnAction(ActionEvent eventT) {
        bookSelected = new Book(titleEdit.getText(), AuthorEdit.getText(), ISBNEdit.getText(), bookSelected.getImageLink(),Integer.parseInt(yearOfPublication.getText()),categoryBook.getText());
        Task<Void> edit = BookService.getInstance().editBook(bookSelected);
        System.out.println(bookSelected.toString());
        edit.setOnSucceeded(event->{
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText("Book edited successfully");
            alert.showAndWait();
            AuthorEdit.setText(null);
            ISBNEdit.setText(null);
            titleEdit.setText(null);
            ISBNEdit.setText(null);
            QuantityEdit.setText(null);
            ISBNSearch.setText(null);
            yearOfPublication.setText(null);
            categoryBook.setText(null);
            URL imageUrl = getClass().getResource("/Image/imageNotFound.jpg");

            if (imageUrl != null) {
                Image image = new Image(imageUrl.toExternalForm());
                imageOfBook.setImage(image);
            } else {
                System.out.println("Ảnh không tìm thấy hoặc đường dẫn không hợp lệ.");
            }
        });
        edit.setOnFailed(event->{
           System.out.println("Failed to edit book");
        });
        new Thread(edit).start();
    }
}
