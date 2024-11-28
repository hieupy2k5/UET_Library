package org.example.uet_library.controllers;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import org.example.uet_library.models.Favor;
import org.example.uet_library.services.BookService;
import org.example.uet_library.services.UserService;

public class FavorBookController extends TableViewController<Favor> {

    public TextField searchField;
    public TableView<Favor> tableView;
    public TableColumn<Favor, String> categoryColumn;
    @FXML
    private TableColumn<Favor, Void> informationColumn;
    @FXML
    private TableColumn<Favor, Void> optionColumn;
    public ProgressIndicator waitProgress;
    private ObservableList<Favor> favoriteBooks;


    public void setUpColumns() {
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
    }

    @Override
    TableColumn<Favor, Void> getInformationColumn() {
        return this.informationColumn;
    }

    @Override
    TableView<Favor> getTableView() {
        return this.tableView;
    }

    @Override
    ProgressIndicator getWaitProgress() {
        return this.waitProgress;
    }

    @Override
    Task<ObservableList<Favor>> getTaskFromDB() {
        return UserService.getInstance().fetchFavorFromDB();
    }

    @Override
    ObservableList<Favor> getObservableList() {
        return favoriteBooks;
    }

    @Override
    TextField getSearchField() {
        return searchField;
    }

    @Override
    void setObservableList(ObservableList<Favor> list) {
        favoriteBooks = list;
    }

    @Override
    public ObservableList<Favor> sortObservableList(ObservableList<Favor> observableList) {
        SortedList<Favor> sortedFavoriteBooks = new SortedList<>(observableList);
        sortedFavoriteBooks.comparatorProperty().bind(tableView.comparatorProperty());
        return sortedFavoriteBooks;
    }

    /**
     * Save favor button.
     */
    public void setUpAdditionalButtons() {
        optionColumn.setCellFactory(column -> new TableCell<>() {
            private final HBox hbox = new HBox();
            private final Button favorButton = new Button();

            {
                hbox.setAlignment(javafx.geometry.Pos.CENTER);
                hbox.setSpacing(5);

                Image favorImage = new Image(
                    getClass().getResource("/Images/Favor2.png").toExternalForm());
                ImageView favorImageView = new ImageView(favorImage);
                favorImageView.setFitWidth(16);
                favorImageView.setFitHeight(16);

                favorButton.setGraphic(favorImageView);
                favorButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

                hbox.getChildren().add(favorButton);

                favorButton.setOnAction(event -> {
                    Favor favor = getTableView().getItems().get(getIndex());
                    if (favor != null) {
                        setupOption(favor);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(hbox);
                }
            }
        });

    }


    private void setupOption(Favor favor) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Remove Favorite");
        alert.setHeaderText("Remove \"" + favor.getTitle() + "\" from favorites?");
        alert.setContentText("Are you sure you want to remove this book from your favorites?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                removeFromFavorite(favor);
            }
        });
    }

    /**
     * Remove book from favors.
     * @param favor contains book_id and user_id.
     */
    private void removeFromFavorite(Favor favor) {
        Task<Boolean> removeTask = new Task<>() {
            @Override
            protected Boolean call() {
                return BookService.getInstance().removeBookFromFavoritesByID(favor.getId());
            }
        };

        removeTask.setOnSucceeded(event -> {
            if (removeTask.getValue()) {
                Platform.runLater(() -> {
                    favoriteBooks.remove(favor);
                    tableView.refresh();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Remove Successful");
                    alert.setHeaderText(null);
                    alert.setContentText("Book removed from favorites.");
                    alert.showAndWait();
                });
            } else {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Failed to remove book from favorites.");
                    alert.showAndWait();
                });
            }
        });

        new Thread(removeTask).start();
    }

    @Override
    final boolean searchPredicate(Favor favorBook, String query) {
        return favorBook.getTitle().toLowerCase().contains(query)
            || favorBook.getAuthor().toLowerCase().contains(query)
            || favorBook.getCategory().toLowerCase().contains(query);
    }
}
