package org.example.uet_library.Controllers;

import java.time.LocalDateTime;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.uet_library.AlertHelper;
import org.example.uet_library.BookService;
import org.example.uet_library.Borrow;
import org.example.uet_library.SessionManager;

/**
 * This is a feature for users
 */
public class ReturnDocumentController {

    public TextField searchField;
    public TableView<Borrow> tableView;
    public TableColumn<Borrow, String> titleColumn;
    public TableColumn<Borrow, String> authorColumn;
    public TableColumn<Borrow, String> categoryColumn;
    public TableColumn<Borrow, Integer> quantityColumn;
    public TableColumn<Borrow, LocalDateTime> borrowDateColumn;
    public TableColumn<Borrow, LocalDateTime> returnDateColumn;
    public TableColumn<Borrow, Void> actionColumn;
    public ProgressIndicator waitProgress;
    private ObservableList<Borrow> borrowedBooks;

    public void fetchFromDB() {
        Task<ObservableList<Borrow>> task = BookService.getInstance().fetchBorrowFromDB();

        // Bind progress indicator to task status
        task.setOnRunning(_ -> Platform.runLater(() -> {
            waitProgress.setVisible(true);
            waitProgress.setProgress(-1);
        }));

        task.setOnSucceeded(_ -> Platform.runLater(() -> {
            borrowedBooks = task.getValue();
            SortedList<Borrow> sortedBorrowedBooks = new SortedList<>(borrowedBooks);

            sortedBorrowedBooks.setComparator((b1, b2) -> {
                if (b1.getReturnDate() == null && b2.getReturnDate() != null) {
                    return -1;
                } else if (b1.getReturnDate() != null && b2.getReturnDate() == null) {
                    return 1;
                } else {
                    return 0;
                }
            });
            tableView.setItems(sortedBorrowedBooks);
            waitProgress.setVisible(false);
            setupSearch();
            setUpReturnButton();
        }));

        task.setOnFailed(event -> Platform.runLater(() -> {
            System.err.println(
                "Error fetching books from database: " + task.getException().getMessage());
            waitProgress.setVisible(false);
        }));

        // Start the task on a new thread
        new Thread(task).start();
    }

    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        borrowDateColumn.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
        returnDateColumn.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
        waitProgress.setVisible(true);

        tableView.getSortOrder().add(returnDateColumn);

        fetchFromDB();
    }

    private void setupSearch() {
        if (borrowedBooks == null || borrowedBooks.isEmpty()) {
            System.err.println("Book list is empty or null, cannot set up search.");
            return;
        }
        FilteredList<Borrow> filteredData = new FilteredList<>(borrowedBooks, b -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(borrowedBook -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();
                return borrowedBook.getTitle().toLowerCase().contains(lowerCaseFilter)
                    || borrowedBook.getAuthor().toLowerCase().contains(lowerCaseFilter)
                    || borrowedBook.getCategory().toLowerCase().contains(lowerCaseFilter);
            });
        });

        SortedList<Borrow> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedData);
    }

    private void setUpReturnButton() {
        actionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button returnButton = new Button("Return");

            {
                returnButton.setOnAction(event -> {
                    Borrow selectedBook = getTableView().getItems().get(getIndex());
                    Integer q = selectedBook.getQuantity();

                    if (selectedBook.getReturnDate() != null) {
                        AlertHelper.showAlert(AlertType.WARNING, "Already returned",
                            String.format("You already have returned %s",
                                q.equals(1) ? "it" : "them"));
                    } else {
                        showQuantityDialog(selectedBook);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
//                    Borrow selectedBook = getTableView().getItems().get(getIndex());
//                    returnButton.setDisable(selectedBook != null);

                    setGraphic(returnButton);
                }
            }
        });
    }

    private void showQuantityDialog(Borrow borrowBook) {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Confirmation");
        Integer q = borrowBook.getQuantity();
        dialog.setHeaderText(String.format("Are you sure you want to return %d %s of %s?", q,
            q.equals(1) ? "copy" : "copies", borrowBook.getTitle()));

        ButtonType confirmButtonType = new ButtonType("YES", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                returnBook(borrowBook, q);
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void returnBook(Borrow borrowedBook, int quantity) {
        int userID = SessionManager.getInstance().getUserId();
        LocalDateTime borrowDate = borrowedBook.getBorrowDate();

        if (BookService.getInstance()
            .returnBook(userID, borrowedBook.getIsbn(), borrowDate)) {
            fetchFromDB(); // Update the table after modifying the database

            AlertHelper.showAlert(AlertType.INFORMATION, "Return successfully",
                String.format("You have returned %d copies of %s", quantity,
                    borrowedBook.getTitle()));
        } else {
            AlertHelper.showAlert(AlertType.ERROR, "Error",
                "Database Error");
        }
    }
}
