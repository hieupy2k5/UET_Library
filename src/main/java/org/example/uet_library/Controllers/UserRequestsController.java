package org.example.uet_library.Controllers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
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
import org.example.uet_library.*;

public class UserRequestsController {

    public TableView<Request> tableView;
    public TableColumn<Request, String> usernameColumn;
    public TableColumn<Request, Void> informationColumn;
    public TableColumn<Request, String> statusColumn;
    public TableColumn<Request, Void> actionColumn;
    public ProgressIndicator waitProgress;
    public TextField searchField;
    private ObservableList<Request> userRequestsList;

    private final Map<String, Image> imageCache = new ConcurrentHashMap<>();

    public void initialize() {
        tableView.setPlaceholder(new Label("There is no request here..."));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        waitProgress.setVisible(true);

        setupInformation();
        fetchFromDB();
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
                        loadImageTask.setOnFailed(event -> imageView.setImage(null));

                        new Thread(loadImageTask).start();
                    }
                }
            }
        });
    }

    private void fetchFromDB() {
        Task<ObservableList<Request>> task = BookService.getInstance().fetchUserRequestFromDB();

        // Bind progress indicator to task status
        task.setOnRunning(event -> Platform.runLater(() -> {
            waitProgress.setVisible(true);
            waitProgress.setProgress(-1);
        }));

        task.setOnSucceeded(event -> Platform.runLater(() -> {
            userRequestsList = task.getValue();
            tableView.setItems(userRequestsList);
            waitProgress.setVisible(false);
            setupSearch();
            setupActionButtons();
        }));

        task.setOnFailed(event -> Platform.runLater(() -> {
            System.err.println(
                "Error fetching books in fetchFromDB() (UserRequestsController.java): " + task.getException().getMessage());
            waitProgress.setVisible(false);
        }));

        // Start the task on a new thread
        new Thread(task).start();

    }

    private void setupSearch() {
        if (userRequestsList == null || userRequestsList.isEmpty()) {
            System.err.println("List is empty");
            return;
        }
        FilteredList<Request> filteredData = new FilteredList<>(userRequestsList, b -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(request -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();
                return request.getUsername().toLowerCase().contains(lowerCaseFilter)
                    || request.getTitle().toLowerCase().contains(lowerCaseFilter)
                    || request.getAuthor().toLowerCase().contains(lowerCaseFilter)
                    || request.getStatus().toLowerCase().contains(lowerCaseFilter);
            });
        });

        SortedList<Request> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedData);
    }

    private void setupActionButtons() {
        actionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button acceptButton = new Button();
            private final Button declineButton = new Button();

            {
                Image borrowImage = new Image(
                    getClass().getResource("/Images/yes.png").toExternalForm());
                ImageView borrowImageView = new ImageView(borrowImage);
                borrowImageView.setFitWidth(32);
                borrowImageView.setFitHeight(32);
                acceptButton.setGraphic(borrowImageView);
                acceptButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand; ");

                acceptButton.setOnAction(event -> {
                    Request selectedRequest = getTableView().getItems().get(getIndex());
                    Integer quantityInStock = BookService.getInstance()
                        .bookQuantityForRequest(selectedRequest.getId());
                    if (quantityInStock > 0) {
                        BookService.getInstance().adminAcceptRequest(selectedRequest.getUser_id(),
                            selectedRequest.getBook_id());
                        fetchFromDB();
                        AlertHelper.showAlert(AlertType.INFORMATION,
                            "Successfully accepted request",
                            String.format(
                                "You have granted permission for this user to borrow the book %s",
                                selectedRequest.getTitle()));
                    } else {
                        AlertHelper.showAlert(AlertType.ERROR, "Cannot approve request", "We have ran out of copies for this book in stock.");
                    }
                });

                Image returnImage = new Image(
                    getClass().getResource("/Images/no.png").toExternalForm());
                ImageView returnImageView = new ImageView(returnImage);
                returnImageView.setFitWidth(32);
                returnImageView.setFitHeight(32);
                declineButton.setGraphic(returnImageView);
                declineButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand; ");

                declineButton.setOnAction(event -> {
                    Request selectedRequest = getTableView().getItems().get(getIndex());
                    BookService.getInstance().adminDeclineRequest(selectedRequest.getUser_id(),
                        selectedRequest.getBook_id());
                    fetchFromDB();
                    AlertHelper.showAlert(AlertType.INFORMATION, "Successfully declined request",
                        String.format(
                            "You have rejected this user's request to borrow the book %s",
                            selectedRequest.getTitle()));
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(10, acceptButton,
                        declineButton); // Add spacing between buttons
                    hbox.setStyle("-fx-alignment: center;");
                    setGraphic(hbox);
                }
            }
        });
    }
}
