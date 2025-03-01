package org.example.uet_library.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Pagination;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.example.uet_library.utilities.AlertHelper;
import org.example.uet_library.models.Book;
import org.example.uet_library.apis.BookAPI;
import java.io.IOException;

/**
 * Controller class for managing book searches via an API in the library system.
 * This class handles the search, pagination, and selection of books fetched from an external API.
 */
public class BookAPISearchController {

    /**
     * Reference to the main menu controller, used to navigate back to the main content area.
     */
    public static MenuController menuController;

    private int currentPage = 0;
    private HBox selectedHBox = null;
    private static Parent pageCache;

    @FXML
    private TextField queryBook;

    @FXML
    private ChoiceBox<String> filterSearch;

    @FXML
    private Pagination pagination;

    private static final int BOOKS_PER_PAGE = 10;
    private ObservableList<Book> allBooks = FXCollections.observableArrayList();
    private Book selectedBook;

    /**
     * Initializes the controller by setting up the filter options for the search functionality.
     */
    @FXML
    public void initialize() {
        filterSearch.setItems(FXCollections.observableArrayList("Title", "Author", "ISBN"));
        filterSearch.setValue("Title");
    }

    /**
     * Sets the menu controller for navigation purposes.
     *
     * @param menuController the {@link MenuController} instance to be used for navigation.
     */
    public void setMenuController(MenuController menuController) {
        this.menuController = menuController;
    }

    /**
     * Event handler for initiating a book search when the search button is clicked.
     *
     * @param event the {@link ActionEvent} triggered by the search button.
     */
    @FXML
    public void searchBookOnAction(ActionEvent event) {
        String query = queryBook.getText();
        String filter = filterSearch.getValue();

        if (query == null || query.isEmpty()) {
            AlertHelper.showAlert(AlertType.ERROR, "Error", "Please enter a search term.");
            return;
        }
        if (filter == null || filter.isEmpty()) {
            AlertHelper.showAlert(AlertType.ERROR, "Error", "Please select a search filter (Title, Author, or ISBN).");
            return;
        }

        searchBooks(query, filter);
    }

    /**
     * Fetches books from the API based on the search query and filter.
     *
     * @param query  the search term to be used for filtering books.
     * @param filter the type of filter to apply (e.g., Title, Author, ISBN).
     */
    private void searchBooks(String query, String filter) {
        Task<ObservableList<Book>> task = BookAPI.searchBooks(query, filter);

        task.setOnSucceeded(event -> {
            allBooks = task.getValue();
            int pageCount = (int) Math.ceil((double) allBooks.size() / BOOKS_PER_PAGE);
            pagination.setPageCount(pageCount);
            pagination.setPageFactory(this::createPage);
        });

        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            exception.printStackTrace();
            AlertHelper.showAlert(AlertType.ERROR, "Error", "Failed to load books: " + exception.getMessage());
        });

        new Thread(task).start();
    }

    /**
     * Creates a paginated view for displaying books.
     *
     * @param pageIndex the current page index to be displayed.
     * @return a {@link ScrollPane} containing the paginated books for the given page.
     */
    private ScrollPane createPage(int pageIndex) {
        this.currentPage = pageIndex;
        VBox pageBox = new VBox(10);
        int start = pageIndex * BOOKS_PER_PAGE;
        int end = Math.min(start + BOOKS_PER_PAGE, allBooks.size());

        for (int i = start; i < end; i++) {
            Book book = allBooks.get(i);
            HBox bookBox = createBookBox(book);
            pageBox.getChildren().add(bookBox);
        }

        ScrollPane scrollPane = new ScrollPane(pageBox);
        scrollPane.setPrefWidth(960);
        scrollPane.setPrefHeight(460);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        return scrollPane;
    }

    /**
     * Creates a visual representation of a book as an HBox.
     *
     * @param book the {@link Book} instance to be displayed.
     * @return an {@link HBox} containing book details and an image.
     */
    private HBox createBookBox(Book book) {
        HBox bookBox = new HBox(10);
        bookBox.setStyle("-fx-padding: 10; -fx-background-color: #E7F5DC; -fx-border-color: #728156; -fx-border-radius: 5px;");
        bookBox.setPrefHeight(100);
        bookBox.setPrefWidth(870);

        ImageView imageView = new ImageView();
        imageView.setFitHeight(80);
        imageView.setPreserveRatio(true);

        Task<Image> imageTask = new Task<>() {
            @Override
            protected Image call() {
                return new Image(book.getImageUrl(), 80, 80, true, true, true);
            }
        };
        imageTask.setOnSucceeded(event -> imageView.setImage(imageTask.getValue()));
        new Thread(imageTask).start();


        VBox detailsBox = new VBox(5);
        detailsBox.getChildren().addAll(
            new Text("Title: " + book.getTitle()),
            new Text("Author: " + book.getAuthor()),
            new Text("Year: " + book.getYear()),
            new Text("ISBN: " + book.getIsbn())
        );

        bookBox.getChildren().addAll(imageView, detailsBox);

        bookBox.setOnMouseClicked(event -> {
            if (selectedHBox != null) {
                selectedHBox.setStyle("-fx-padding: 10; -fx-background-color: #E7F5DC; -fx-border-color: #728156; -fx-border-radius: 5px;");
            }

            selectedHBox = bookBox;
            bookBox.setStyle("-fx-padding: 10; -fx-background-color: #ADD8E6; -fx-border-color: #0078D4; -fx-border-radius: 5;");

            selectedBook = book;
        });

        return bookBox;
    }

    /**
     * Event handler for adding the selected book to the library.
     *
     * @param event the {@link ActionEvent} triggered by the "Add Book" button.
     */
    @FXML
    public void addBookOnAction(ActionEvent event) {
        if (selectedBook == null) {
            AlertHelper.showAlert(AlertType.ERROR, "Error", "Please select a book to add.");
            return;
        }

        try {
            pageCache = menuController.getContent();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXMLs/BookAdd.fxml"));
            Parent root = loader.load();
            BookAddController controller = loader.getController();
            controller.setBookAPISearch(this);
            controller.setNewBook(selectedBook);
            menuController.setContent(root);
        } catch (IOException e) {
            e.printStackTrace();
            AlertHelper.showAlert(AlertType.ERROR, "Error", "Failed to load Add Book screen.");
        }
    }

    /**
     * Navigates back to the previous page stored in the cache.
     *
     * @throws IOException if an error occurs during navigation.
     */
    public static void setBack() throws IOException {
        if (!(pageCache == null)) {
            menuController.setContent(pageCache);
        }
    }
}
