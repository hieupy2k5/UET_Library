package org.example.uet_library.controllers;

import java.net.URL;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import org.example.uet_library.enums.BookCheckResult;
import org.example.uet_library.models.Book;
import org.example.uet_library.services.AdminService;
import org.example.uet_library.services.BookService;
import org.example.uet_library.utilities.AlertHelper;

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


    /**
     * Initializes the controller and sets up event listeners and UI configurations.
     * - Configures the ListView to display book titles.
     * - Adds a listener to the ISBN search field for dynamic book fetching.
     * - Configures event handling for selecting a book from the ListView.
     */
    public void initialize() {
        this.resetImage();
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
            if (newValue == null || newValue.isEmpty()) {
                listViewTable.setItems(FXCollections.observableArrayList());
            } else {
                Task<ObservableList<Book>> task = AdminService.getInstance()
                    .fetchBookFromDB(newValue);
                task.setOnSucceeded(event -> {
                    listViewTable.setItems(task.getValue());
                });
                task.setOnFailed(
                    event -> task.getException().printStackTrace()
                );

                new Thread(task).start();
            }
        });

        listViewTable.setOnMouseClicked(event -> {
            bookSelected = listViewTable.getSelectionModel().getSelectedItem();
            this.setTextField(bookSelected);
            String imageUrl = bookSelected.getImageUrl();
            Image image;

            if (imageUrl == null || imageUrl.isEmpty()) {
                image = new Image(
                    getClass().getResource("/Images/imageNotFound.jpg").toExternalForm());
            } else {
                image = new Image(imageUrl);
            }

            imageOfBook.setImage(image);
            imageOfBook.setFitHeight(imageOfBook.getFitHeight());
            imageOfBook.setFitWidth(imageOfBook.getFitWidth());
            imageOfBook.setPreserveRatio(true);
            imageOfBook.setSmooth(true);
            imageOfBook.setCache(true);
        });

        if (listViewTable.getSelectionModel().getSelectedItem() == null || bookSelected == null) {
            SaveButton.setDisable(true);
            editButton.setDisable(true);
        }
        editButton.setDisable(true);
        SaveButton.setDisable(true);
        removeBut.setDisable(true);
    }


    /**
     * Populates the text fields with the details of the selected book.
     *
     * @param selectedBook the book selected from the ListView.
     */
    private void setTextField(Book selectedBook) {
        if (bookSelected != null) {
            AuthorEdit.setText(bookSelected.getAuthor());
            titleEdit.setText(bookSelected.getTitle());
            ISBNEdit.setText(bookSelected.getIsbn());
            yearOfPublication.setText(bookSelected.getYear() + "");
            QuantityEdit.setText(String.valueOf(bookSelected.getQuantity()));
            categoryBook.setText(bookSelected.getCategory());
            handleCancel();
        }
    }

    /**
     * Enables editing mode for the book details by making the text fields editable.
     */
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

    /**
     * Disables editing mode and restores the text fields to a non-editable state.
     */
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

    /**
     * Disables editing mode and clears the text fields when no book is selected.
     */
    private void handleNull() {
        AuthorEdit.setEditable(false);
        ISBNEdit.setEditable(false);
        titleEdit.setEditable(false);
        QuantityEdit.setEditable(false);
        yearOfPublication.setEditable(false);
        categoryBook.setEditable(false);

        editButton.setDisable(true);
        SaveButton.setDisable(true);
        removeBut.setDisable(true);
    }


    /**
     * Handles the removal of the selected book after confirmation from the user.
     *
     * @param event the ActionEvent triggered by clicking the "Remove" button.
     */
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
            if (result.get() == ButtonType.OK) {
                removeBook(isbn);
                System.out.println(isbn);
                listViewTable.setItems(null);
            }
        }

    }

    /**
     * Removes a book from the database by its ISBN.
     * - Checks if the book is currently borrowed before removing it.
     *
     * @param isbn the ISBN of the book to be removed.
     */
    private void removeBook(String isbn) {
        BookCheckResult bookCheckResult = BookService.getInstance().isBookBorrowedByAnyone(isbn);
        System.out.println("checking availability...");
        if (bookCheckResult == BookCheckResult.ALREADY_BORROWED) {
            System.out.println("someone is borrowing!");
            AlertHelper.showAlert(AlertType.ERROR, "Cannot remove book",
                "Someone is borrowing this book.");

            return;
        }
        System.out.println("no one is borrowing!");
        Task<Boolean> task = AdminService.getInstance().deleteBook(isbn);
        task.setOnSucceeded(_ -> {
            if (task.getValue()) {
                this.resetImage();
                AlertHelper.showAlert(AlertType.INFORMATION, "Successfully delete book",
                    "You have deleted this book.");
            }
        });
        task.setOnFailed(_ -> {
            System.err.println("Error");
        });
        new Thread(task).start();
    }

    /**
     * Clears all text fields in the form.
     */
    private void setFieldsToNull() {
        AuthorEdit.setText(null);
        ISBNEdit.setText(null);
        titleEdit.setText(null);
        ISBNEdit.setText(null);
        QuantityEdit.setText(null);
        ISBNSearch.setText(null);
        yearOfPublication.setText(null);
        categoryBook.setText(null);
    }


    /**
     * Saves the changes made to a book's details and updates the database.
     *
     * @param eventT the ActionEvent triggered by clicking the "Save" button.
     */
    public void SaveBookOnAction(ActionEvent eventT) {
        bookSelected = new Book(titleEdit.getText(), AuthorEdit.getText(), ISBNEdit.getText(),
            bookSelected.getImageUrl(), Integer.parseInt(yearOfPublication.getText()),
            categoryBook.getText(), Integer.parseInt(QuantityEdit.getText()));
        Task<Void> edit = AdminService.getInstance().editBook(bookSelected);
        edit.setOnSucceeded(event -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText("Book edited successfully");
            alert.showAndWait();
            setFieldsToNull();
            URL imageUrl = getClass().getResource("/Images/imageNotFound.jpg");
            this.handleNull();
            if (imageUrl != null) {
                this.resetImage();
            } else {
                System.out.println("Ảnh không tìm thấy hoặc đường dẫn không hợp lệ.");
            }
        });
        edit.setOnFailed(event -> {
            System.out.println("Failed to edit book");
        });
        new Thread(edit).start();
    }

    private void resetImage() {
        URL imageUrl = getClass().getResource("/Images/imageNotFound.jpg");
        Image image = new Image(imageUrl.toExternalForm());
        imageOfBook.setImage(image);
    }
}
