package org.example.uet_library.Controllers;

import java.time.LocalDateTime;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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
    public TableColumn<Borrow, String> categoryColumn;
    public TableColumn<Borrow, LocalDateTime> borrowDateColumn;
    public TableColumn<Borrow, LocalDateTime> returnDateColumn;
    public TableColumn<Borrow, Void> actionColumn;
    public ProgressIndicator waitProgress;
    private ObservableList<Borrow> borrowedBooks;

    @FXML
    private TableColumn<Borrow, Void> informationColumn;

    private void setupInformation() {
        informationColumn.setText("Document Information");
        informationColumn.setCellFactory(column -> new TableCell<>() {
            private final HBox hbox = new HBox();
            private final VBox vbox = new VBox();
            private final ImageView imageView = new ImageView();
            private final Label titleLabel = new Label();
            private final Label authorLabel = new Label();

            {
                vbox.getChildren().addAll(titleLabel, authorLabel);
                vbox.setSpacing(5);
                hbox.setSpacing(15);
                hbox.getChildren().addAll(imageView, vbox);

                imageView.setFitWidth(50);
                imageView.setFitHeight(50);
                titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                authorLabel.setStyle("-fx-font-style: italic;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null) {
                    setGraphic(null);
                } else {
                    Borrow borrow = getTableView().getItems().get(getIndex());

                    titleLabel.setText(borrow.getTitle());
                    authorLabel.setText(borrow.getAuthor());
                    setGraphic(hbox);

                    Task<Image> loadImageTask = new Task<>() {
                        @Override
                        protected Image call() {
                            return new Image(borrow.getImageUrl(), true);
                        }
                    };

                    loadImageTask.setOnSucceeded(
                        event -> imageView.setImage(loadImageTask.getValue()));
                    loadImageTask.setOnFailed(event -> {
//                        System.err.println(
//                            "Failed to load image: " + loadImageTask.getException().getMessage());
                    });

                    new Thread(loadImageTask).start();
                }
            }
        });
    }

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
        tableView.setPlaceholder(new Label("Your return list is empty..."));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        borrowDateColumn.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
        returnDateColumn.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
        waitProgress.setVisible(true);

        setupInformation();
        tableView.getSortOrder().add(returnDateColumn);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

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
            private final Button returnButton = new Button();

            {
                Image returnImage = new Image(
                    getClass().getResource("/Images/returnBook.png").toExternalForm());
                ImageView imageView = new ImageView(returnImage);
                imageView.setFitWidth(16);
                imageView.setFitHeight(16);

                returnButton.setGraphic(imageView);
                returnButton.setStyle("-fx-background-color: transparent;");
                setStyle("-fx-alignment: CENTER;");
                returnButton.setStyle(returnButton.getStyle() + "; -fx-cursor: hand;");

                returnButton.setOnAction(event -> {
                    Borrow selectedBook = getTableView().getItems().get(getIndex());
                    Integer q = selectedBook.getQuantity();

                    if (selectedBook.getReturnDate() != null) {
                        AlertHelper.showAlert(AlertType.ERROR, "Already returned",
                            String.format("You have already returned it"));
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
                    setGraphic(returnButton);
                }
            }
        });
    }

    private void showQuantityDialog(Borrow borrowBook) {
        Platform.runLater(() -> {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Confirmation");
            dialog.setHeaderText(
                String.format("Are you sure you want to return %s?", borrowBook.getTitle()));

            ButtonType confirmButtonType = new ButtonType("YES", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == confirmButtonType) {
                    returnBook(borrowBook);
                }
                return null;
            });

            dialog.showAndWait();
        });
    }

    private void returnBook(Borrow borrowedBook) {
        int userID = SessionManager.getInstance().getUserId();
        LocalDateTime borrowDate = borrowedBook.getBorrowDate();

        Task<Boolean> returnTask = new Task<>() {
            @Override
            protected Boolean call() {
                return BookService.getInstance()
                    .returnBook(userID, borrowedBook.getIsbn(), borrowDate);
            }
        };

        returnTask.setOnSucceeded(event -> {
            if (returnTask.getValue()) {
                Platform.runLater(() -> {
                    fetchFromDB(); // Đảm bảo fetch chạy trên luồng nền
                    AlertHelper.showAlert(AlertType.INFORMATION, "Return successfully",
                        String.format("You have returned %s", borrowedBook.getTitle()));
                });
            } else {
                Platform.runLater(() -> AlertHelper.showAlert(AlertType.ERROR, "Error",
                    "Database Error"));
            }
        });

        returnTask.setOnFailed(event -> Platform.runLater(() -> {
            AlertHelper.showAlert(AlertType.ERROR, "Error",
                "Failed to connect to the database.");
        }));

        new Thread(returnTask).start();
    }


}
