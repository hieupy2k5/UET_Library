package org.example.uet_library.Controllers;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.uet_library.Book;
import org.example.uet_library.BookService;
import org.example.uet_library.Borrow;
import org.example.uet_library.User;

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
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("first_name"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("last_name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        waitProgress.setVisible(true);

        fetchFromDB();
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
                "Error fetching books from database: " + task.getException().getMessage());
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
}
