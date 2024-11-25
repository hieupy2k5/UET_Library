package org.example.uet_library.Controllers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javafx.concurrent.Task;
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

    private String tableName = "";

    public void initialize() {
        this.getTableView().setPlaceholder(new Label("The list is empty..."));
        setUpColumns();
        this.getWaitProgress().setVisible(true);

        setUpInformation();
        fetchFromDB();

        setUpSlidingPane();     // Hook for BorrowDocumentController
        setUpSortOrder();       // Hook for ReturnDocumentController

        this.getTableView().setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    public void setUpInformation() {

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

                loadImageTask.setOnSucceeded(
                    _ -> {
                        Image img = loadImageTask.getValue();
                        imageCache.put(imageUrl, img);
                        imageView.setImage(loadImageTask.getValue());
                    });
                loadImageTask.setOnFailed(_ -> imageView.setImage(null));
                return loadImageTask;
            }
        });
    }

    public void fetchFromDB() {
    }

    ;

    abstract void setUpColumns();

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setUpSlidingPane() {
    }

    public void setUpSortOrder() {
    }

    abstract TableColumn<T, Void> getInformationColumn();

    abstract TableView<T> getTableView();

    abstract ProgressIndicator getWaitProgress();
}
