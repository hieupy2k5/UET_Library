package org.example.uet_library.Controllers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
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
import org.example.uet_library.utilities.AlertHelper;
import org.example.uet_library.services.BookService;
import org.example.uet_library.models.Request;

public class MyRequestsController {

    public TableView<Request> tableView;
    public TableColumn<Request, String> statusColumn;
    public TableColumn<Request, Void> actionColumn;
    public ProgressIndicator waitProgress;
    public TextField searchField;
    private ObservableList<Request> myRequestsList;
    @FXML
    private TableColumn<Request, Void> informationColumn;

    private final Map<String, Image> imageCache = new ConcurrentHashMap<>();


    public void initialize() {
        tableView.setPlaceholder(new Label("Your request list is empty..."));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        waitProgress.setVisible(true);
        setupInformation();

        fetchFromDB();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
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
                    Request request = getTableView().getItems().get(getIndex());

                    titleLabel.setText(request.getTitle());
                    authorLabel.setText(request.getAuthor());
                    setGraphic(hbox);

                    String imageUrl = request.getImageUrl();
                    if (imageCache.containsKey(imageUrl)) {
                        imageView.setImage(imageCache.get(imageUrl));
                    } else {
                        Task<Image> loadImageTask = new Task<>() {
                            @Override
                            protected Image call() {
                                return new Image(request.getImageUrl(), true);
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

    private void fetchFromDB() {
        Task<ObservableList<Request>> task = BookService.getInstance(). fetchMyRequestFromDB();

        // Bind progress indicator to task status
        task.setOnRunning(event -> Platform.runLater(() -> {
            waitProgress.setVisible(true);
            waitProgress.setProgress(-1);
        }));

        task.setOnSucceeded(event -> Platform.runLater(() -> {
            myRequestsList = task.getValue();
            tableView.setItems(myRequestsList);
            waitProgress.setVisible(false);
            setupSearch();
            setUpActionButton();
        }));

        task.setOnFailed(event -> Platform.runLater(() -> {
            System.err.println(
                "Error fetching books in fetchFromDB() in MyRequestsController.java: " + task.getException().getMessage());
            waitProgress.setVisible(false);
        }));

        // Start the task on a new thread
        new Thread(task).start();
    }

    private void setupSearch() {
        if (myRequestsList == null || myRequestsList.isEmpty()) {
            System.err.println("List is empty");
            return;
        }
        FilteredList<Request> filteredData = new FilteredList<>(myRequestsList, b -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(request -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();
                return request.getTitle().toLowerCase().contains(lowerCaseFilter)
                    || request.getAuthor().toLowerCase().contains(lowerCaseFilter)
                    || request.getStatus().toLowerCase().contains(lowerCaseFilter);
            });
        });

        SortedList<Request> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedData);
    }

    private void setUpActionButton() {
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button actionButton = new Button();

            {
                // Configure the button
                ImageView buttonImageView = new ImageView();
                buttonImageView.setFitWidth(16);
                buttonImageView.setFitHeight(16);
                actionButton.setGraphic(buttonImageView);
                actionButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

                actionButton.setOnAction(event -> {
                    Request selectedRequest = getTableView().getItems().get(getIndex());
                    if ("accepted".equals(selectedRequest.getStatus())) {
                        BookService.getInstance().userBorrowBook(selectedRequest.getId(), selectedRequest.getBook_id());
                        fetchFromDB();
                        AlertHelper.showAlert(AlertType.INFORMATION, "Borrow Successful",
                            String.format("You have successfully borrowed the book %s",
                                selectedRequest.getTitle()));
                    } else if ("declined".equals(selectedRequest.getStatus())) {
                        BookService.getInstance().userTryAgain(selectedRequest.getId());
                        fetchFromDB();
                        AlertHelper.showAlert(AlertType.INFORMATION, "Successfully requested again",
                            "Now you need to wait for admins to approve your request.");
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Request currentRequest = getTableView().getItems().get(getIndex());

                    if ("pending".equals(currentRequest.getStatus())) {
                        actionButton.setVisible(false);
                    } else {
                        actionButton.setVisible(true);
                        Image buttonImage;
                        if ("accepted".equalsIgnoreCase(currentRequest.getStatus())) {
                            buttonImage = new Image(
                                getClass().getResource("/Images/borrow.png").toExternalForm());
                        } else {
                            buttonImage = new Image(
                                getClass().getResource("/Images/try-again.png").toExternalForm());
                        }
                        ((ImageView) actionButton.getGraphic()).setImage(buttonImage);
                    }
                    HBox hbox = new HBox(actionButton);
                    hbox.setStyle("-fx-alignment: center;");
                    setGraphic(hbox);
                }
            }
        });
    }

}
