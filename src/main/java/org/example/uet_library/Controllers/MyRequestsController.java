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

public class MyRequestsController {

    public TableView<Request> tableView;
    public TableColumn<Request, String> titleColumn;
    public TableColumn<Request, String> authorColumn;
    public TableColumn<Request, String> statusColumn;
    public TableColumn<Request, Void> actionColumn;
    public ProgressIndicator waitProgress;
    public TextField searchField;
    private ObservableList<Request> myRequestsList;

    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        waitProgress.setVisible(true);

        fetchFromDB();
    }

    private void fetchFromDB() {
        Task<ObservableList<Request>> task = BookService.getInstance().fetchMyRequestFromDB();

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
                "Error fetching books from database: " + task.getException().getMessage());
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
                buttonImageView.setFitWidth(32);
                buttonImageView.setFitHeight(32);
                actionButton.setGraphic(buttonImageView);
                actionButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

                actionButton.setOnAction(event -> {
                    Request selectedRequest = getTableView().getItems().get(getIndex());
                    if ("accepted".equals(selectedRequest.getStatus())) {
                        BookService.getInstance().userBorrowBook(selectedRequest.getBook_id());
                        fetchFromDB();
                        AlertHelper.showAlert(AlertType.INFORMATION, "Borrow Successful",
                            String.format("You have successfully borrowed the book %s",
                                selectedRequest.getTitle()));
                    } else if ("declined".equals(selectedRequest.getStatus())) {
                        fetchFromDB();
                        AlertHelper.showAlert(AlertType.ERROR, "Borrow Failed",
                            String.format("Please try again for the book %s",
                                selectedRequest.getTitle()));
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