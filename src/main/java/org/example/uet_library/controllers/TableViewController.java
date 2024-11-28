package org.example.uet_library.controllers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.uet_library.models.TableItem;
import org.jetbrains.annotations.NotNull;

abstract class TableViewController<T extends TableItem> {

    private final Map<String, Image> imageCache = new ConcurrentHashMap<>();

    @FXML
    public final void initialize() {
        this.getTableView().setPlaceholder(new Label("The list is empty..."));
        this.setUpColumns();
        this.getWaitProgress().setVisible(true);

        this.setUpInformation();
        this.fetchFromDB();

        this.postInitialize();

        this.getTableView().setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    protected void postInitialize() {
    }

    private void setUpInformation() {

        this.getInformationColumn().setText("Document Information");
        this.getInformationColumn().setCellFactory(_ -> new TableCell<>() {
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
                    TableItem tableItem = getTableView().getItems().get(getIndex());
                    titleLabel.setText(tableItem.getTitle());
                    authorLabel.setText(tableItem.getAuthor());
                    setGraphic(hbox);

                    String imageUrl = tableItem.getImageUrl();
                    if (imageCache.containsKey(imageUrl)) {
                        imageView.setImage(imageCache.get(imageUrl));
                    } else {
                        Task<Image> loadImageTask = createImageTask(tableItem, imageUrl);

                        new Thread(loadImageTask).start();
                    }
                }
            }

            @NotNull
            private Task<Image> createImageTask(TableItem tableItem, String imageUrl) {
                Task<Image> loadImageTask = new Task<>() {
                    @Override
                    protected Image call() {
                        return new Image(tableItem.getImageUrl(), true);
                    }
                };

                loadImageTask.setOnSucceeded(_ -> {
                    Image img = loadImageTask.getValue();
                    imageCache.put(imageUrl, img);
                    imageView.setImage(loadImageTask.getValue());
                });
                loadImageTask.setOnFailed(_ -> imageView.setImage(null));
                return loadImageTask;
            }
        });
    }

    protected void fetchFromDB() {
        Task<ObservableList<T>> task = getTaskFromDB();

        task.setOnRunning(_ -> Platform.runLater(() -> {
            this.getWaitProgress().setVisible(true);
            this.getWaitProgress().setProgress(-1);
        }));

        task.setOnSucceeded(_ -> Platform.runLater(() -> {
            this.setObservableList(task.getValue());

            fetchBookRating();

            this.setObservableList(sortObservableList(this.getObservableList()));
            this.getTableView().setItems(this.getObservableList());
            this.getWaitProgress().setVisible(false);

            loadFavouriteBooks();
            setupSearch();
            setUpAdditionalButtons();
        }));

        task.setOnFailed(_ -> Platform.runLater(() -> {
            System.err.println(
                "Error fetching from DB in " + getClass().getSimpleName() + task.getException()
                    .getMessage());
            this.getWaitProgress().setVisible(false);
        }));

        new Thread(task).start();
    }

    public final void setupSearch() {
        if (this.getObservableList() == null || this.getObservableList().isEmpty()) {
            return;
        }
        FilteredList<T> filteredData = new FilteredList<>(this.getObservableList(), b -> true);

        this.getSearchField().textProperty().addListener((_, _, query) -> {
            filteredData.setPredicate(tableItem -> {
                if (query == null || query.isEmpty()) {
                    return true;
                }

                return searchPredicate(tableItem, query.toLowerCase());
            });
        });

        SortedList<T> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(this.getTableView().comparatorProperty());
        this.getTableView().setItems(sortedData);
    }

    /**
     * Set up the search bar and decide which field should match the user's input.
     * @param tableItem the items in the table that we want to search.
     * @param query the search text that user enters.
     * @return whether any result is found.
     */
    abstract boolean searchPredicate(T tableItem, String query);

    /**
     * For each table view, set up the appropriate columns.
     */
    abstract void setUpColumns();

    /**
     * Merge the information of book title, author name and book image.
     * @return the merged column.
     */
    abstract TableColumn<T, Void> getInformationColumn();

    abstract TableView<T> getTableView();

    abstract ProgressIndicator getWaitProgress();

    abstract Task<ObservableList<T>> getTaskFromDB();

    abstract ObservableList<T> getObservableList();

    abstract TextField getSearchField();

    abstract void setObservableList(ObservableList<T> list);

    public void fetchBookRating() {
    }

    public void loadFavouriteBooks() {
    }

    /**
     * Each table view will have their own options buttons.
     * This is for setting up them.
     */
    public void setUpAdditionalButtons() {
    }

    /**
     * For table that needs to sort their list of TableItem.
     * (The tables that need sorting will override this)
     * @param observableList contains the items to be sorted.
     * @return the sorted List.
     */
    public ObservableList<T> sortObservableList(ObservableList<T> observableList) {
        return observableList;
    }
}
