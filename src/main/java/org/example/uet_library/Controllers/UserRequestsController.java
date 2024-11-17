package org.example.uet_library.Controllers;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import org.example.uet_library.AlertHelper;
import org.example.uet_library.BookService;
import org.example.uet_library.Request;

public class UserRequestsController {

    public TableView<Request> tableView;
    public TableColumn<Request, String> usernameColumn;
    public TableColumn<Request, String> titleColumn;
    public TableColumn<Request, String> authorColumn;
    public TableColumn<Request, String> statusColumn;
    public TableColumn<Request, Void> actionColumn;
    public ProgressIndicator waitProgress;
    public TextField searchField;
    private ObservableList<Request> userRequestsList;

    public void initialize() {
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        waitProgress.setVisible(true);

        fetchFromDB();
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
                "Error fetching books from database: " + task.getException().getMessage());
            waitProgress.setVisible(false);
        }));

        // Start the task on a new thread
        new Thread(task).start();

    }

    private void setupSearch() {
        if (userRequestsList == null || userRequestsList.isEmpty()) {
            System.err.println("User list is empty");
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
                // Configure the Borrow button
                Image borrowImage = new Image(
                    getClass().getResource("/Images/yes.png").toExternalForm());
                ImageView borrowImageView = new ImageView(borrowImage);
                borrowImageView.setFitWidth(32);
                borrowImageView.setFitHeight(32);
                acceptButton.setGraphic(borrowImageView);
                acceptButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand; ");

                acceptButton.setOnAction(event -> {
                    Request selectedRequest = getTableView().getItems().get(getIndex());
                    BookService.getInstance().adminAcceptRequest(selectedRequest.getUser_id(),
                        selectedRequest.getBook_id());
                    fetchFromDB();
                    AlertHelper.showAlert(AlertType.INFORMATION, "Successfully accepted request",
                        String.format(
                            "You have granted permission for this user to borrow the book %s",
                            selectedRequest.getTitle()));
                });

                // Configure the Return button
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
