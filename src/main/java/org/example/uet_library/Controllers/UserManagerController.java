package org.example.uet_library.Controllers;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.example.uet_library.database.Database;
import org.example.uet_library.models.User;
import org.example.uet_library.services.AdminService;
import org.example.uet_library.services.BookService;

public class UserManagerController {

    public TableView<User> tableView;
    public TableColumn<User, String> usernameColumn;
    public TableColumn<User, String> firstNameColumn;
    public TableColumn<User, String> lastNameColumn;
    public TableColumn<User, String> emailColumn;
    public TableColumn<User, Void> actionColumn;
    public ProgressIndicator waitProgress;
    public TextField searchField;
    private ObservableList<User> userObservableList;

    public void initialize() {
        tableView.setPlaceholder(new Label("No user? So sad..."));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        waitProgress.setVisible(true);
        fetchFromDB();
    }

    public void fetchFromDB() {
        Task<ObservableList<User>> task = AdminService.getInstance().fetchUserFromDB();
        task.setOnRunning(event -> Platform.runLater(() -> waitProgress.setVisible(true)));
        task.setOnSucceeded(event -> Platform.runLater(() -> {
            userObservableList = task.getValue();
            tableView.setItems(userObservableList);
            waitProgress.setVisible(false);
            setupSearch();
            setupActionButtons();
        }));
        task.setOnFailed(event -> Platform.runLater(() -> waitProgress.setVisible(false)));
        new Thread(task).start();
    }

    private void setupSearch() {
        if (userObservableList == null || userObservableList.isEmpty()) return;
        FilteredList<User> filteredData = new FilteredList<>(userObservableList, b -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filteredData.setPredicate(user -> {
            if (newValue == null || newValue.isEmpty()) return true;
            String lowerCaseFilter = newValue.toLowerCase();
            return user.getUsername().toLowerCase().contains(lowerCaseFilter) ||
                    user.getFirstName().toLowerCase().contains(lowerCaseFilter) ||
                    user.getLastName().toLowerCase().contains(lowerCaseFilter) ||
                    user.getEmail().toLowerCase().contains(lowerCaseFilter);
        }));
        SortedList<User> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedData);
    }

    private void setupActionButtons() {
        actionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = createButton("/Images/edit.png");
            private final Button deleteButton = createButton("/Images/bin.png");

            {
                editButton.setOnAction(event -> handleEditUser(getTableView().getItems().get(getIndex())));
                deleteButton.setOnAction(event -> handleDeleteUser(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new HBox(10, editButton, deleteButton));
            }
        });
    }

    private Button createButton(String imagePath) {
        Button button = new Button();
        ImageView imageView = new ImageView(new Image(getClass().getResource(imagePath).toExternalForm()));
        imageView.setFitWidth(16);
        imageView.setFitHeight(16);
        button.setGraphic(imageView);
        button.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        return button;
    }

    private void handleEditUser(User user) {
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        TextField userNameField = createTextField(user.getUsername(), "Enter Username");
        TextField firstNameField = createTextField(user.getFirstName(), "Enter First Name");
        TextField lastNameField = createTextField(user.getLastName(), "Enter Last Name");
        TextField emailField = createTextField(user.getEmail(), "Enter Email");
        grid.addRow(0, userNameField);
        grid.addRow(1, firstNameField);
        grid.addRow(2, lastNameField);
        grid.addRow(3, emailField);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Edit User");
        alert.setHeaderText("Edit User Information");
        alert.getDialogPane().setContent(grid);
        alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection con = new Database().getConnection()) {
                    PreparedStatement statement = con.prepareStatement(
                            "UPDATE users SET username = ?, first_name = ?, last_name = ?, email = ? WHERE id = ?");
                    statement.setString(1, userNameField.getText());
                    statement.setString(2, firstNameField.getText());
                    statement.setString(3, lastNameField.getText());
                    statement.setString(4, emailField.getText());
                    statement.setInt(5, user.getId());
                    statement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                fetchFromDB();
            }
        });
    }

    private TextField createTextField(String text, String prompt) {
        TextField textField = new TextField(text);
        textField.setPromptText(prompt);
        return textField;
    }

    private void handleDeleteUser(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete User");
        alert.setContentText("Are you sure you want to delete user " + user.getUsername() + "?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (AdminService.getInstance().deleteUser(user.getId())) {
                    userObservableList.remove(user);
                    fetchFromDB();
                }
            }
        });
    }
}
