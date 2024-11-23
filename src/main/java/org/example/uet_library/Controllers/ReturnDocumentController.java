package org.example.uet_library.Controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.uet_library.*;

/**
 * This is a feature for users
 */
public class ReturnDocumentController {

    public TextField searchField;
    public TableView<Borrow> tableView;
    public TableColumn<Borrow, String> categoryColumn;
    public TableColumn<Borrow, Date> borrowDateColumn;
    public TableColumn<Borrow, Date> returnDateColumn;
    public TableColumn<Borrow, Void> actionColumn;
    public ProgressIndicator waitProgress;
    private ObservableList<Borrow> borrowedBooks;
    private HashSet<String> isRatedBooks = new HashSet<>();

    @FXML
    private TableColumn<Borrow, Void> informationColumn;

    private final Map<String, Image> imageCache = new ConcurrentHashMap<>();

    private Borrow borrowSelected;

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

                    String imageUrl = borrow.getImageUrl();
                    if (imageCache.containsKey(imageUrl)) {
                        imageView.setImage(imageCache.get(imageUrl));
                    } else {
                        Task<Image> loadImageTask = new Task<>() {
                            @Override
                            protected Image call() {
                                return new Image(borrow.getImageUrl(), true);
                            }
                        };

                        loadImageTask.setOnSucceeded(
                            event -> {
                                Image img = loadImageTask.getValue();
                                imageCache.put(imageUrl, img);
                                imageView.setImage(loadImageTask.getValue());
                            });
                        loadImageTask.setOnFailed(event -> {
//                        System.err.println(
//                            "Failed to load image: " + loadImageTask.getException().getMessage());
                        });

                        new Thread(loadImageTask).start();
                    }
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
            this.fetchRating();
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
        Platform.runLater(() -> tableView.refresh());
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
            private final Button ratingBook = new Button();
            private final HBox hbox = new HBox();
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

                    if (!selectedBook.getReturnDate().equals("N/A")) {
                        AlertHelper.showAlert(AlertType.ERROR, "Already returned",
                            String.format("You have already returned it"));
                    } else {
                        showQuantityDialog(selectedBook);
                    }
                });

                ratingBook.setText("Rate");
                ratingBook.setOnAction(event -> {
                    borrowSelected = getTableView().getItems().get(getIndex());
                    String type = ratingBook.getText();
                    showRatingDialog(borrowSelected, type);
                });
                hbox.getChildren().addAll(returnButton, ratingBook);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    borrowSelected = getTableView().getItems().get(getIndex());
                    setGraphic(hbox);
                    if (isRatedBooks.contains(borrowSelected.getIsbn())) {
                        ratingBook.setText("Re-Rate");
                    } else {
                        ratingBook.setText("Rate");
                    }

                    // Only show the rating button if the book has been returned
                    ratingBook.setVisible(!borrowSelected.getReturnDate().equals("N/A"));
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
        Task<Boolean> returnTask = new Task<>() {
            @Override
            protected Boolean call() {
                return BookService.getInstance()
                    .returnBook(borrowedBook.getId(), borrowedBook.getIsbn());
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

    private void showRatingDialog(Borrow borrowedBook, String type) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXMLs/RatingBookDialog.fxml"));
            Parent root = loader.load();
            RatingDialogController ratingDialogController = loader.getController();
            ratingDialogController.setData(borrowedBook);
            ratingDialogController.setReturnDocumentController(this);
            ratingDialogController.setTypeFeedBack(type);
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Confirmation");
            dialogStage.setScene(new Scene(root));
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Task<HashSet<String>> rateBook() {
        return new Task<>() {
          @Override
          protected HashSet<String> call() {
              HashSet<String> resultQuery = new HashSet<>();
              Database database = new Database();
              try {
                  Connection connection = database.getConnection();
                  String query = "SELECT ISBN FROM Ratings WHERE user_name = ?";
                  PreparedStatement preparedStatement = connection.prepareStatement(query);
                  preparedStatement.setString(1, RatingDialogController.userName);
                  ResultSet resultSet = preparedStatement.executeQuery();
                  while (resultSet.next()) {
                      resultQuery.add(resultSet.getString(1));
                  }
              } catch (SQLException e) {
                  throw new RuntimeException(e);
              }
              return resultQuery;
          }
        };
    }

    public void fetchRating() {
        Task<HashSet<String>> task = rateBook();
        task.setOnSucceeded(event -> {
            this.isRatedBooks = task.getValue();
            for (Borrow x : borrowedBooks) {
                if (isRatedBooks.contains(x.getIsbn())) {
                    x.setRate(true);
                } else {
                    x.setRate(false);
                }
            }
            Platform.runLater(() -> tableView.refresh());
        });
        task.setOnFailed(event -> {

        });
        new Thread(task).start();
    }

    public void updateTableWithRating() {
        for (Borrow x : borrowedBooks) {
            if (isRatedBooks.contains(x.getIsbn())) {
                x.setRate(true);
                break;
            }
        }
        Platform.runLater(() -> tableView.refresh());
    }

}
