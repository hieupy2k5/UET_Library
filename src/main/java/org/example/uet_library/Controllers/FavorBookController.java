package org.example.uet_library.Controllers;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.uet_library.BookService;
import org.example.uet_library.Favor;

public class FavorBookController {
    public TextField searchField;
    public TableView<Favor> tableView;
    public TableColumn<Favor, String> categoryColumn;
    @FXML
    private TableColumn<Favor, Void> informationColumn;
    @FXML
    private TableColumn<Favor, Void> optionColumn;
    public ProgressIndicator waitProgress;
    private ObservableList<Favor> favoriteBooks;

    public void initialize() {
        tableView.setPlaceholder(new Label("Your favorite list is empty..."));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        waitProgress.setVisible(true);

        setupInformation();
        setupOptionColumn();

        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        fetchFromDB();

        Platform.runLater(() -> tableView.refresh());
    }

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
                    Favor favor = getTableView().getItems().get(getIndex());
                    titleLabel.setText(favor.getTitle());
                    authorLabel.setText(favor.getAuthor());
                    setGraphic(hbox);

                    Task<Image> loadImageTask = new Task<>() {
                        @Override
                        protected Image call() {
                            return new Image(favor.getImageUrl(), true);
                        }
                    };

                    loadImageTask.setOnSucceeded(event -> imageView.setImage(loadImageTask.getValue()));
                    loadImageTask.setOnFailed(event -> imageView.setImage(null));

                    new Thread(loadImageTask).start();
                }
            }
        });
    }

    private void setupOptionColumn() {
        optionColumn.setCellFactory(column -> new TableCell<>() {
            private final HBox hbox = new HBox();
            private final Button favorButton = new Button();

            {
                hbox.setAlignment(javafx.geometry.Pos.CENTER);
                hbox.setSpacing(5);

                Image favorImage = new Image(getClass().getResource("/Images/Favor2.png").toExternalForm());
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

    public void fetchFromDB() {
        Task<ObservableList<Favor>> task = BookService.getInstance().fetchFavorFromDB();

        task.setOnRunning(event -> {
            waitProgress.setVisible(true);
            waitProgress.setProgress(-1);
        });

        task.setOnSucceeded(event -> {
            favoriteBooks = task.getValue();
            setupSearch();

            SortedList<Favor> sortedFavoriteBooks = new SortedList<>(favoriteBooks);
            sortedFavoriteBooks.comparatorProperty().bind(tableView.comparatorProperty());

            tableView.setItems(sortedFavoriteBooks);
            waitProgress.setVisible(false);
        });

        task.setOnFailed(event -> {
            System.err.println("Error fetching favorite books: " + task.getException().getMessage());
            waitProgress.setVisible(false);
        });

        new Thread(task).start();
    }

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

    private void setupSearch() {
        if (favoriteBooks == null || favoriteBooks.isEmpty()) {
            return;
        }

        FilteredList<Favor> filteredData = new FilteredList<>(favoriteBooks, b -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(favorBook -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();
                return favorBook.getTitle().toLowerCase().contains(lowerCaseFilter)
                        || favorBook.getAuthor().toLowerCase().contains(lowerCaseFilter)
                        || favorBook.getCategory().toLowerCase().contains(lowerCaseFilter);
            });
        });

        SortedList<Favor> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedData);
    }
}
