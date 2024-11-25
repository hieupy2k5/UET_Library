package org.example.uet_library.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.uet_library.database.Database;
import org.example.uet_library.models.Borrow;
import org.example.uet_library.services.UserService;
import org.example.uet_library.utilities.AlertHelper;

/**
 * This is a feature for users
 */
public class ReturnBookController extends TableViewController<Borrow> {

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

    public void setUpColumns() {
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        borrowDateColumn.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
        returnDateColumn.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
    }

    @Override
    TableColumn<Borrow, Void> getInformationColumn() {
        return this.informationColumn;
    }

    @Override
    TableView<Borrow> getTableView() {
        return this.tableView;
    }

    @Override
    ProgressIndicator getWaitProgress() {
        return this.waitProgress;
    }

    @Override
    Task<ObservableList<Borrow>> getTaskFromDB() {
        return UserService.getInstance().fetchBorrowFromDB();
    }

    @Override
    ObservableList<Borrow> getObservableList() {
        return borrowedBooks;
    }

    @Override
    TextField getSearchField() {
        return searchField;
    }

    @Override
    void setObservableList(ObservableList<Borrow> list) {
        borrowedBooks = list;
    }

    @Override
    public ObservableList<Borrow> sortObservableList(ObservableList<Borrow> observableList) {

        return new SortedList<>(observableList);
    }

    @Override
    public void postInitialize() {
        tableView.getSortOrder().add(returnDateColumn);
    }

    @Override
    final boolean searchPredicate(Borrow borrow, String query) {
        return borrow.getTitle().toLowerCase().contains(query)
            || borrow.getAuthor().toLowerCase().contains(query)
            || borrow.getCategory().toLowerCase().contains(query);
    }

    public void setUpAdditionalButtons() {
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
                return UserService.getInstance()
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
                Platform.runLater(
                    () -> AlertHelper.showAlert(AlertType.ERROR, "Error", "Database Error"));
            }
        });

        returnTask.setOnFailed(event -> Platform.runLater(() -> {
            AlertHelper.showAlert(AlertType.ERROR, "Error", "Failed to connect to the database.");
        }));

        new Thread(returnTask).start();
    }

    private void showRatingDialog(Borrow borrowedBook, String type) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/FXMLs/RatingBookDialog.fxml"));
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

    @Override
    public void fetchBookRating() {
        Task<HashSet<String>> task = rateBook();
        task.setOnSucceeded(_ -> {
            this.isRatedBooks = task.getValue();
            for (Borrow x : borrowedBooks) {
                x.setIsRated(isRatedBooks.contains(x.getIsbn()));
            }
            Platform.runLater(() -> tableView.refresh());
        });
        task.setOnFailed(_ -> {

        });
        new Thread(task).start();
    }

    public void updateTableWithRating() {
        for (Borrow x : borrowedBooks) {
            if (isRatedBooks.contains(x.getIsbn())) {
                x.setIsRated(true);
                break;
            }
        }
        Platform.runLater(() -> tableView.refresh());
    }

}
