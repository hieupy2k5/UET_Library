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
import org.example.uet_library.*;

import java.util.Optional;

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
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("first_name"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("last_name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        waitProgress.setVisible(true);

        fetchFromDB();
        setupActionButtons();
    }

    public void fetchFromDB() {
        Task<ObservableList<User>> task = BookService.getInstance().fetchUserFromDB();

        // Bind progress indicator to task status
        task.setOnRunning(event -> Platform.runLater(() -> {
            waitProgress.setVisible(true);
            waitProgress.setProgress(-1);
        }));

        task.setOnSucceeded(event -> Platform.runLater(() -> {
            userObservableList = task.getValue();
            tableView.setItems(userObservableList);
            waitProgress.setVisible(false);
            setupSearch();
        }));

        task.setOnFailed(event -> Platform.runLater(() -> {
            System.err.println(
                "Error fetching books in fetchFromDB() (UserManagerController.java): " + task.getException().getMessage());
            waitProgress.setVisible(false);
        }));

        // Start the task on a new thread
        new Thread(task).start();
    }

    private void setupSearch() {
        if (userObservableList == null || userObservableList.isEmpty()) {
            System.err.println("User list is empty");
            return;
        }
        FilteredList<User> filteredData = new FilteredList<>(userObservableList, b -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(user -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();
                return user.getUsername().toLowerCase().contains(lowerCaseFilter)
                    || user.getFirst_name().toLowerCase().contains(lowerCaseFilter)
                    || user.getLast_name().toLowerCase().contains(lowerCaseFilter)
                    || user.getEmail().toLowerCase().contains(lowerCaseFilter);
            });
        });

        SortedList<User> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedData);
    }

    private void setupActionButtons() {
        actionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = new Button();
            private final Button deleteButton = new Button();

            {
                // Tạo nút Edit
                Image editImage = new Image(getClass().getResource("/Images/edit.png").toExternalForm());
                ImageView editImageView = new ImageView(editImage);
                editImageView.setFitWidth(16);
                editImageView.setFitHeight(16);
                editButton.setGraphic(editImageView);
                editButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                editButton.setOnAction(event -> {
                    User selectedUser = getTableView().getItems().get(getIndex());
                    if (selectedUser != null) {
                        handleEditUser(selectedUser);
                    }
                });

                // Tạo nút Delete
                Image deleteImage = new Image(getClass().getResource("/Images/bin.png").toExternalForm());
                ImageView deleteImageView = new ImageView(deleteImage);
                deleteImageView.setFitWidth(16);
                deleteImageView.setFitHeight(16);
                deleteButton.setGraphic(deleteImageView);
                deleteButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                deleteButton.setOnAction(event -> {
                    User selectedUser = getTableView().getItems().get(getIndex());
                    if (selectedUser != null) {
                        handleDeleteUser(selectedUser);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(10, editButton, deleteButton); // Add spacing between buttons
                    hbox.setStyle("-fx-alignment: center;");
                    setGraphic(hbox);
                }
            }
        });
    }

    private void handleEditUser(User user) {
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);

        TextField usernameField = new TextField(user.getUsername());
        usernameField.setPromptText("Enter Username");

        TextField firstNameField = new TextField(user.getFirst_name());
        firstNameField.setPromptText("Enter First Name");

        TextField lastNameField = new TextField(user.getLast_name());
        lastNameField.setPromptText("Enter Last Name");

        TextField emailField = new TextField(user.getEmail());
        emailField.setPromptText("Enter Email");

        grid.add(usernameField, 0, 0);
        grid.add(firstNameField, 0, 1);
        grid.add(lastNameField, 0, 2);
        grid.add(emailField, 0, 3);

        Alert signUpAlert = new Alert(Alert.AlertType.CONFIRMATION);
        signUpAlert.setTitle("Edit User");
        signUpAlert.setHeaderText("Edit User Information");

        signUpAlert.getDialogPane().setContent(grid);

        ButtonType confirmButton = new ButtonType("Save");
        ButtonType cancelButton = new ButtonType("Cancel");
        signUpAlert.getButtonTypes().setAll(confirmButton, cancelButton);

        signUpAlert.showAndWait().ifPresent(response -> {
            if (response == confirmButton) {
                String username = usernameField.getText();
                String firstName = firstNameField.getText();
                String lastName = lastNameField.getText();
                String email = emailField.getText();

                if (username.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Error", "All fields must be filled.");
                } else {
                    user.setUsername(username);
                    user.setFirstName(firstName);
                    user.setLastName(lastName);
                    user.setEmail(email);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Edit User Information Successfully");
                    fetchFromDB();
                }
            }
        });
    }


    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    private void handleDeleteUser(User user) {
        boolean confirmDelete = false;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete User");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete user " + user.getUsername() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            confirmDelete = true;
        }

        if (confirmDelete) {
            if (BookService.getInstance().deleteUser(user.getId())) {
                Alert alert1 = new Alert(Alert.AlertType.INFORMATION);
                fetchFromDB();
                alert1.setTitle("Success");
                alert1.setHeaderText("User Deleted");
                alert1.setContentText("User has been deleted successfully.");
                alert1.showAndWait();
            } else {
                System.out.println("Failed to delete user.");
            }
        }


        if (confirmDelete) {
            boolean success = BookService.getInstance().deleteUser(user.getId());
            if (success) {
                userObservableList.remove(user);
            } else {
                AlertHelper.showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete user.");
            }
        }
    }

}
