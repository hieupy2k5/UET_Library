package org.example.uet_library.controllers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javafx.collections.ObservableList;
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
import org.example.uet_library.models.Request;
import org.example.uet_library.services.AdminService;
import org.example.uet_library.utilities.AlertHelper;

public class UserRequestsController extends TableViewController<Request> {

    public TableView<Request> tableView;
    public TableColumn<Request, String> usernameColumn;
    public TableColumn<Request, Void> informationColumn;
    public TableColumn<Request, String> statusColumn;
    public TableColumn<Request, Void> actionColumn;
    public ProgressIndicator waitProgress;
    public TextField searchField;
    private ObservableList<Request> userRequestsList;

    private final Map<String, Image> imageCache = new ConcurrentHashMap<>();

    public void setUpColumns() {
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    @Override
    TableColumn<Request, Void> getInformationColumn() {
        return this.informationColumn;
    }

    @Override
    TableView<Request> getTableView() {
        return this.tableView;
    }

    @Override
    ProgressIndicator getWaitProgress() {
        return this.waitProgress;
    }

    @Override
    Task<ObservableList<Request>> getTaskFromDB() {
        return AdminService.getInstance().fetchUserRequestFromDB();
    }

    @Override
    ObservableList<Request> getObservableList() {
        return userRequestsList;
    }

    @Override
    TextField getSearchField() {
        return searchField;
    }

    @Override
    void setObservableList(ObservableList<Request> list) {
        userRequestsList = list;
    }

    @Override
    final boolean searchPredicate(Request request, String query) {
        return request.getUsername().toLowerCase().contains(query)
            || request.getTitle().toLowerCase().contains(query)
            || request.getAuthor().toLowerCase().contains(query)
            || request.getStatus().toLowerCase().contains(query);
    }

    public void setUpAdditionalButtons() {
        actionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button acceptButton = new Button();
            private final Button denyButton = new Button();

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
                    Integer quantityInStock = AdminService.getInstance()
                        .bookQuantityForRequest(selectedRequest.getId());
                    if (quantityInStock > 0) {
                        AdminService.getInstance().acceptRequest(selectedRequest.getUserId(),
                            selectedRequest.getBookId());
                        fetchFromDB();
                        AlertHelper.showAlert(AlertType.INFORMATION,
                            "Successfully accepted request",
                            String.format(
                                "You have granted permission for %s to borrow the book %s",
                                selectedRequest.getUsername(),
                                selectedRequest.getTitle()));
                    } else {
                        AlertHelper.showAlert(AlertType.ERROR, "Cannot approve request",
                            "We have ran out of copies for this book in stock.");
                    }
                });

                Image returnImage = new Image(
                    getClass().getResource("/Images/no.png").toExternalForm());
                ImageView returnImageView = new ImageView(returnImage);
                returnImageView.setFitWidth(32);
                returnImageView.setFitHeight(32);
                denyButton.setGraphic(returnImageView);
                denyButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand; ");

                denyButton.setOnAction(event -> {
                    Request selectedRequest = getTableView().getItems().get(getIndex());
                    AdminService.getInstance().denyRequest(selectedRequest.getUserId(),
                        selectedRequest.getBookId());
                    fetchFromDB();
                    AlertHelper.showAlert(AlertType.INFORMATION, "Successfully denied request",
                        String.format(
                            "You have rejected %s's request to borrow the book %s",
                            selectedRequest.getUsername(),
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
                        denyButton); // Add spacing between buttons
                    hbox.setStyle("-fx-alignment: center;");
                    setGraphic(hbox);
                }
            }
        });
    }
}
