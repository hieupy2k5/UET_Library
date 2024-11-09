package org.example.uet_library.Controllers;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.example.uet_library.AlertHelper;
import org.example.uet_library.Book;
import org.example.uet_library.BookService;
import org.example.uet_library.SessionManager;

/**
 * This is a feature for users.
 */
public class BorrowDocumentController {

    @FXML
    public Label documentsLabel;

    @FXML
    private ProgressIndicator waitProgress;
    @FXML
    private TableColumn<Book, String> authorColumn;
    @FXML
    private TableColumn<Book, Integer> quantityColumn;
    @FXML
    private TextField searchField;
    @FXML
    private TableColumn<Book, String> titleColumn;
    @FXML
    private TableColumn<Book, String> typeColumn;
    @FXML
    private TableColumn<Book, Integer> yearColumn;
    @FXML
    private TableView<Book> tableView;
    @FXML
    private TableColumn<Book, String> image;
    @FXML
    private TableColumn<Book, Void> actionColumn;  // New column for the "Borrow" button
    private ObservableList<Book> books;

    public void refresh() {
        Task<ObservableList<Book>> task = BookService.getInstance().fetchBookFromDB();

        // Bind progress indicator to task status
        task.setOnRunning(event -> Platform.runLater(() -> {
            waitProgress.setVisible(true);
            waitProgress.setProgress(-1);
        }));

        task.setOnSucceeded(event -> Platform.runLater(() -> {
            books = task.getValue();
            tableView.setItems(books);
            waitProgress.setVisible(false);
            setupSearch();
            setupBorrowButton();
        }));

        task.setOnFailed(event -> Platform.runLater(() -> {
            System.err.println(
                "Error fetching books from database: " + task.getException().getMessage());
            waitProgress.setVisible(false);
        }));

        // Start the task on a new thread
        new Thread(task).start();
    }

    // Initializes the controller
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        waitProgress.setVisible(true);

        refresh();
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

    private void setupBorrowButton() {
        actionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button borrowButton = new Button("Borrow");

            {
                borrowButton.setOnAction(event -> {
                    Book selectedBook = getTableView().getItems().get(getIndex());
                    showQuantityDialog(selectedBook);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(borrowButton);
                }
            }
        });
    }

    private void showQuantityDialog(Book book) {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Borrow Quantity");
        dialog.setHeaderText("Specify the quantity to borrow for " + book.getTitle());

        VBox dialogVBox = new VBox();
        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantity");

        dialogVBox.getChildren().add(quantityField);
        dialog.getDialogPane().setContent(dialogVBox);

        ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                try {
                    return Integer.parseInt(quantityField.getText());
                } catch (NumberFormatException e) {
                    AlertHelper.showAlert(AlertType.ERROR, "Invalid input",
                        "Please enter a positive integer.");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(quantity -> {
            if (quantity > 0 && quantity <= book.getQuantity()) {
                borrowBook(book, quantity);
            } else if (quantity.equals(0)) {
                AlertHelper.showAlert(AlertType.ERROR, "Invalid number of books entered",
                    "You came here to borrow 0 book? None? Seriously?");
            } else if (book.getQuantity() == 0) {
                AlertHelper.showAlert(AlertType.ERROR, "Out of stock",
                    "Sorry, we ran out of that book in stock today.");
            } else if (quantity < 0) {
                AlertHelper.showAlert(AlertType.ERROR, "Invalid number of books entered",
                    "How on earth are you going to borrow a NEGATIVE number of books???");
            } else if (quantity > book.getQuantity()) {
                AlertHelper.showAlert(AlertType.ERROR, "Insufficient number of available books",
                    "Sorry, we do not have that many books.");
            }
        });
    }

    private void borrowBook(Book book, int quantity) {
        int userID = SessionManager.getInstance().getUserId();

        // Decrement quantity in the database, update the view as necessary
        if (BookService.getInstance().borrowBook(userID, book.getIsbn(), quantity)) {
            refresh(); // Update the table after modifying the database

            AlertHelper.showAlert(AlertType.INFORMATION, "Borrow successfully",
                String.format("You have borrowed %d copies of %s", quantity, book.getTitle()));
        } else {
            AlertHelper.showAlert(AlertType.ERROR, "Error",
                "Database Error");
        }
    }
}
