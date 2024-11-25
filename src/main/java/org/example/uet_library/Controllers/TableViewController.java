package org.example.uet_library.Controllers;

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

    public TextField searchField;
    private final Map<String, Image> imageCache = new ConcurrentHashMap<>();

    @FXML
    public final void initialize() {
        this.getTableView().setPlaceholder(new Label("The list is empty..."));
        setUpColumns();
        this.getWaitProgress().setVisible(true);

        setUpInformation();
        fetchFromDB();

        postInitialize();

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

    public void setupSearch() {
    }

    abstract void setUpColumns();

    abstract TableColumn<T, Void> getInformationColumn();

    abstract TableView<T> getTableView();

    abstract ProgressIndicator getWaitProgress();

    abstract Task<ObservableList<T>> getTaskFromDB();

    abstract ObservableList<T> getObservableList();

    abstract void setObservableList(ObservableList<T> list);

    public void fetchBookRating() {
    }

    public void loadFavouriteBooks() {
    }

    public void setUpAdditionalButtons() {
    }

    public ObservableList<T> sortObservableList(ObservableList<T> observableList) {
        return observableList;
    }
}
