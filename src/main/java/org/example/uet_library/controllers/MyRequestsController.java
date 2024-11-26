package org.example.uet_library.controllers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
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
import org.example.uet_library.services.UserService;
import org.example.uet_library.utilities.AlertHelper;

public class MyRequestsController extends TableViewController<Request> {

    public TableView<Request> tableView;
    public TableColumn<Request, String> statusColumn;
    public TableColumn<Request, Void> actionColumn;
    public ProgressIndicator waitProgress;
    public TextField searchField;
    private ObservableList<Request> myRequestsList;
    @FXML
    private TableColumn<Request, Void> informationColumn;

    private final Map<String, Image> imageCache = new ConcurrentHashMap<>();


    public void setUpColumns() {
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
        return UserService.getInstance().fetchMyRequestFromDB();
    }

    @Override
    ObservableList<Request> getObservableList() {
        return myRequestsList;
    }

    @Override
    TextField getSearchField() {
        return searchField;
    }

    @Override
    void setObservableList(ObservableList<Request> list) {
        myRequestsList = list;
    }

    @Override
    final boolean searchPredicate(Request request, String query) {
        return request.getTitle().toLowerCase().contains(query)
            || request.getAuthor().toLowerCase().contains(query)
            || request.getStatus().toLowerCase().contains(query);
    }

    public void setUpAdditionalButtons() {
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
                        UserService.getInstance()
                            .borrowBook(selectedRequest.getId(), selectedRequest.getBookId());
                        fetchFromDB();
                        AlertHelper.showAlert(AlertType.INFORMATION, "Borrow Successful",
                            String.format("You have successfully borrowed the book %s",
                                selectedRequest.getTitle()));
                    } else if ("denied".equals(selectedRequest.getStatus())) {
                        UserService.getInstance().requestAgain(selectedRequest.getId());
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
