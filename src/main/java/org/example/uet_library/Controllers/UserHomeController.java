package org.example.uet_library.Controllers;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import org.example.uet_library.Book;
import org.example.uet_library.BookService;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.Stack;

public class UserHomeController implements Initializable {
    private static final int ITEMS_PER_PAGE = 10;
    private static final int COLUMNS = 5;
    private static final int ROWS = 1;
    private ObservableList<Book> books;

    private Stack<Parent> sceneStack = new Stack<>(); // Storing previous scenes
    private Stage stage;

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private HBox cardLayout;

    @FXML
    private Pagination pagina;

    @FXML
    private GridPane gridPane;

    private HashMap<Integer, VBox> pageCache = new HashMap<>();

    private int currentPageIndex = 0;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private boolean isPush = true;

    private MenuController menuController;

    private Popup searchPopup;

    private ObservableList<Book> searchBooks;

    @FXML
    private TextField searchField;

    public void setMenuController(MenuController menuController) {
        this.menuController = menuController;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadAllBooks();
        loadTop5Books();
        setUpPagination();
        setUpSearchPopup();
    }

    public void loadAllBooks() {
        Task<ObservableList<Book>> task = BookService.getInstance().fetchBookFromDB();
        task.setOnSucceeded(e -> {
            this.searchBooks = task.getValue();
        });
        task.setOnFailed(e -> {
            System.out.println("Failed to load books: " + task.getException());
        });
        new Thread(task).start();
    }

    private void setUpSearchPopup() {
        searchPopup = new Popup();
        searchPopup.setAutoHide(true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                searchPopup.hide();
            } else {
                Task<ObservableList<Book>> task = BookService.getInstance().fetchBookByTitleOrAuthor(newValue);
                task.setOnSucceeded(e -> {
                    this.searchBooks = task.getValue();
                    displaySearchResults(searchBooks);
                });
                task.setOnFailed(e -> System.out.println("Error fetching search results: " + task.getException()));
                new Thread(task).start();
            }
        });
    }

    private void displaySearchResults(ObservableList<Book> books) {
        ListView<String> listView = new ListView<>();
        listView.setPrefWidth(400);
        listView.setPrefHeight(Math.min(books.size(), 3) * 60);

        for (int i = 0; i < Math.min(books.size(), 3); i++) {
            Book book = books.get(i);
            listView.getItems().add(book.getTitle());
        }

        if (books.size() > 3) {
            Label showAll = new Label("Show all results");
            showAll.setOnMouseClicked(event -> {
                openAllSearchResults(searchField.getText());
                searchPopup.hide();
            });
            listView.getItems().add(showAll.getText()); // Spacer for "Show all results"
        }

        listView.setOnMouseClicked(event -> {
            if (!listView.getSelectionModel().isEmpty()) {
                int selectedIndex = listView.getSelectionModel().getSelectedIndex();
                Book selectedBook = books.get(selectedIndex);
                try {
                    openBookDetails(selectedBook);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        searchPopup.getContent().clear();
        searchPopup.getContent().add(listView);

        if (!searchPopup.isShowing()) {
            double xPosition = searchField.getScene().getWindow().getX() + searchField.getLayoutX() + 210;
            double yPosition = searchField.getScene().getWindow().getY() + searchField.getLayoutY() + searchField.getHeight() + 20;

            double padding = 6;
            yPosition += padding;
            searchPopup.show(searchField, xPosition, yPosition);

            searchPopup.setWidth(searchField.getWidth() + 500);
        }

    }

    private void openAllSearchResults(String query) {
        System.out.println("Show all search results for: " + query);
    }

    public void setUpPagination() {
        Task<Integer> countPage = BookService.getInstance().fetchTotalBook();
        countPage.setOnSucceeded(event -> {
            int totalBooks = countPage.getValue();
            int pageCount = (int) Math.ceil(totalBooks / (double) ITEMS_PER_PAGE);
            pagina.setPageCount(pageCount);
            pagina.setPageFactory(this::createPage);
            pagina.setStyle("-fx-background-color: #B1DCB8");
            progressIndicator.setVisible(false);
        });

        pagina.currentPageIndexProperty().addListener((observable, oldValue, newValue) -> {
            this.currentPageIndex = newValue.intValue();
        });

        new Thread(countPage).start();
    }

    private void loadTop5Books() {
        cardLayout.getChildren().clear();
        Task<ObservableList<Book>> task = BookService.getInstance().top5BookRecentlyAdded();
        task.setOnSucceeded(event -> {
            books = task.getValue();
            for (Book book : books) {
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/FXMLs/Card.fxml"));
                    HBox cardBox = fxmlLoader.load();
                    CardController cardController = fxmlLoader.getController();
                    cardController.setData(book);
                    cardController.setUserHomeController(this);
                    cardLayout.getChildren().add(cardBox);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        task.setOnFailed(e -> System.out.println("Failed to load top 5 books: " + task.getException()));
        new Thread(task).start();
    }

    private VBox createPage(int pageIndex) {
        if (pageCache.containsKey(pageIndex)) {
            return pageCache.get(pageIndex);
        }

        VBox pageBox = new VBox();
        GridPane newGridPane = new GridPane();
        newGridPane.getChildren().clear();
        newGridPane.setHgap(10);
        newGridPane.setVgap(10);
        newGridPane.setPadding(new Insets(10));
        pageBox.setPrefWidth(960);
        pageBox.setPrefHeight(460);
        newGridPane.setStyle("-fx-background-color: #B1DCB8");

        int start = pageIndex * ITEMS_PER_PAGE;

        Task<ObservableList<Book>> task = BookService.getInstance().fetchBookForPage(start, ITEMS_PER_PAGE);
        task.setOnSucceeded(event -> {
            progressIndicator.setVisible(false);
            ObservableList<Book> bookPerPage = task.getValue();
            for (int i = 0; i < bookPerPage.size(); i++) {
                Book book = bookPerPage.get(i);
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/FXMLs/BookCard.fxml"));
                    VBox cardBox = fxmlLoader.load();
                    BookCardController bookCardController = fxmlLoader.getController();
                    bookCardController.setData(book);
                    bookCardController.setUserHomeController(this);
                    int column = i % COLUMNS;
                    int row = i / COLUMNS;
                    newGridPane.add(cardBox, column, row);
                    GridPane.setMargin(cardBox, new Insets(10));
                } catch (IOException e) {
                    System.err.println("Failed to load book card: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        task.setOnRunning(event -> {
            progressIndicator.setVisible(true);
        });
        new Thread(task).start();

        ScrollPane scrollPane = new ScrollPane(newGridPane);
        scrollPane.setPrefWidth(960);
        scrollPane.setPrefHeight(460);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        pageBox.getChildren().add(scrollPane);
        pageCache.put(pageIndex, pageBox);
        return pageBox;
    }

    public void openBookDetails(Book book) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/FXMLs/User_BookView.fxml"));
        Parent root = fxmlLoader.load();

        ShowBookInformation showBookInformation = fxmlLoader.getController();
        showBookInformation.setDate(book);
        showBookInformation.setUserHomeController(this);

        if (isPush) {
            sceneStack.push(menuController.getContent());
            isPush = false;
        }
        this.menuController.setContent(root);
    }

    public void goBack() throws IOException {
        if (!sceneStack.isEmpty()) {
            this.menuController.setContent(sceneStack.pop());
            isPush = true;
        }
    }
}
