package org.example.uet_library.Controllers;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.uet_library.Book;
import org.example.uet_library.BookService;

import java.io.IOException;

/**
 * This is a feature for admins.
 */
public class DocumentsController {

    @FXML
    public Label documentsLabel;

    @FXML
    public void BackOnAction(ActionEvent event) throws IOException {
//        ChangeSceneMachine.getInstance().changeScene("Menu.fxml",event,1180,900);
    }

    ObservableList<Book> books;
    @FXML
    private ProgressIndicator waitprogress;
    @FXML
    private TableColumn<Book, String> isbnShow;
    @FXML
    private TableColumn<Book, String> authorShow;
    @FXML
    private TableColumn<Book, Integer> quantityShow;
    @FXML
    private TextField searchField;
    @FXML
    private TableColumn<Book, String> titleShow;
    @FXML
    private TableColumn<Book, String> typeShow;
    @FXML
    private TableColumn<Book, Integer> yearShow;
    @FXML
    private TableView<Book> tableView;
    @FXML
    private TableColumn<Book, String> image;
    @FXML
    void backButtonOnAction(ActionEvent event) throws IOException {
        ChangeSceneMachine.getInstance().changeScene("LibController.fxml", event, 800, 600);
    }

    @FXML
    void searchOnAction(ActionEvent event) {
        // Search logic here
    }


    // update table
    public void initialize() {
        titleShow.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorShow.setCellValueFactory(new PropertyValueFactory<>("author"));
        yearShow.setCellValueFactory(new PropertyValueFactory<>("year"));
        typeShow.setCellValueFactory(new PropertyValueFactory<>("type"));
        isbnShow.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        quantityShow.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        //setupSearch();
        waitprogress.setVisible(true);

        // create task
        Task<ObservableList<Book>> task = BookService.getInstance().fetchBookFromDB();

        // when task is running, show indicator progress
        task.setOnRunning(event -> {
            Platform.runLater(() -> {
                waitprogress.setVisible(true);
                waitprogress.setProgress(-1);  // set rotate
            });
        });

        // when task successful, show
        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                books = task.getValue();
                tableView.setItems(books);
                //updateTable();
                waitprogress.setVisible(false);  // blind progess when success
                setupSearch();
            });
        });

        // when task fail, show error and turn off
        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                System.err.println("Error fetching books in DocumentsController.java: " + task.getException().getMessage());
                waitprogress.setVisible(false);
            });
        });

        // start task on new thread
        new Thread(task).start();
    }

    private void setupSearch() {
        if (books == null || books.isEmpty()) {
            System.err.println("Book list is empty or null, cannot set up search.");
            return;
        }
        FilteredList<Book> filteredData = new FilteredList<>(books, b -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(book -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();
                return book.getTitle().toLowerCase().contains(lowerCaseFilter)
                        || book.getAuthor().toLowerCase().contains(lowerCaseFilter)
                        || book.getIsbn().toLowerCase().contains(lowerCaseFilter);
            });
        });

        SortedList<Book> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedData);
    }

}
