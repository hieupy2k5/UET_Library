package org.example.uet_library.Controllers;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Pagination;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import org.example.uet_library.models.Book;
import org.example.uet_library.services.BookService;

public class UserHomeController implements Initializable {

    private static final int ITEMS_PER_PAGE = 8;
    private static final int COLUMNS = 4;
    private static final int ROWS = 1;
    private ObservableList<Book> books;
    private final ExecutorService executorService = Executors.newFixedThreadPool(
        4); // ThreadPool for background tasks


    private Stack<Parent> sceneStack = new Stack<>(); // Storing previous scenes
    private Stack<Parent> bookStack = new Stack<>();

    private Stage stage;
    private int countToBack = 0;
    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private HBox cardLayout;

    @FXML
    private Pagination pagina;


    private HashMap<Integer, VBox> pageCache = new HashMap<>();

    private int currentPageIndex = 0;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private boolean isPush = true;

    @FXML
    private MenuController menuController = new MenuController();

    private Popup searchPopup;

    private ObservableList<Book> searchBooks;

    @FXML
    private TextField searchField;

    public void setMenuController(MenuController menuController) {
        this.menuController = menuController;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> {
            loadAllBooks();
            setUpSearchPopup();
        });
        cardLayout.setStyle(
            "-fx-background-color:  linear-gradient(from 26.52% 5.85% to 73.475% 94.15%, #F1EEF9,  #F6D5D1);");
    }

    public void loadAllBooks() {
        Task<ObservableList<Book>> task = BookService.getInstance().fetchBookFromDB();

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            this.searchBooks = task.getValue();
            loadTop5Books();
            setUpPagination();
        }));

        task.setOnFailed(e -> {
            System.out.println("Failed to load books: " + task.getException());
        });
        executorService.execute(task);
    }

    private void setUpSearchPopup() {
        searchPopup = new Popup();
        searchPopup.setAutoHide(true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                searchPopup.hide();
            } else {
                Task<ObservableList<Book>> task = BookService.getInstance()
                    .fetchBookByTitleOrAuthor(newValue);
                task.setOnSucceeded(e -> Platform.runLater(() -> {
                    this.searchBooks = task.getValue();
                    displaySearchResults(searchBooks);
                }));
                task.setOnFailed(e -> System.out.println(
                    "Error fetching search results: " + task.getException()));
                executorService.submit(task);
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

        if (books.size() > 1) {
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
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        searchPopup.getContent().clear();
        searchPopup.getContent().add(listView);

        if (!searchPopup.isShowing()) {
            double xPosition =
                searchField.getScene().getWindow().getX() + searchField.getLayoutX() + 210;
            double yPosition = searchField.getScene().getWindow().getY() + searchField.getLayoutY()
                + searchField.getHeight() + 20;

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
        if (searchBooks == null) {
            pagina.setPageCount(1);
            pagina.setPageFactory(pageIndex -> {
                VBox placeholderPage = new VBox();
                Label loadingLabel = new Label("Loading books...");
                loadingLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #555;");
                placeholderPage.getChildren().add(loadingLabel);
                return placeholderPage;
            });
            return;
        }
        Task<Integer> countPage = BookService.getInstance().fetchTotalBook();
        countPage.setOnSucceeded(event -> Platform.runLater(() -> {
            int totalBooks = countPage.getValue();
            int pageCount = (int) Math.ceil(totalBooks / (double) ITEMS_PER_PAGE);
            pagina.setPageCount(pageCount);
            pagina.setPageFactory(this::createPage);
            pagina.setStyle(
                "-fx-background-color:  linear-gradient(from 26.52% 5.85% to 73.475% 94.15%, #F1EEF9,  #F6D5D1);");
            progressIndicator.setVisible(false);
        }));

        pagina.currentPageIndexProperty().addListener((observable, oldValue, newValue) -> {
            this.currentPageIndex = newValue.intValue();
        });

        executorService.submit(countPage);
    }

    private void loadTop5Books() {
        cardLayout.getChildren().clear();
        Task<ObservableList<Book>> task = BookService.getInstance().top5BookRecentlyAdded();
        task.setOnSucceeded(event -> Platform.runLater(() -> {
            books = task.getValue();
            for (Book book : books) {
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader(
                        getClass().getResource("/FXMLs/Card.fxml"));
                    HBox cardBox = fxmlLoader.load();
                    CardController cardController = fxmlLoader.getController();
                    cardController.setData(book);
                    cardController.setUserHomeController(this);
                    cardLayout.getChildren().add(cardBox);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            setUpSearchPopup();
        }));
        task.setOnFailed(
            e -> System.out.println("Failed to load top 5 books: " + task.getException()));
        executorService.submit(task);
    }

    private VBox createPage(int pageIndex) {
        if (pageCache.containsKey(pageIndex)) {
            return pageCache.get(pageIndex);
        }

        VBox pageBox = new VBox();
        pageBox.setStyle(
            "-fx-background-color: linear-gradient(from 26.52% 5.85% to 73.475% 94.15%, #F1EEF9,  #F6D5D1);");

        GridPane gridPane = new GridPane();
        gridPane.setHgap(25);
        gridPane.setVgap(15);
        gridPane.setPadding(new Insets(10));
        gridPane.setStyle(
                "-fx-background-color: linear-gradient(from 26.52% 5.85% to 73.475% 94.15%, #F1EEF9,  #F6D5D1);");

        ScrollPane scrollPane = new ScrollPane(gridPane);
        scrollPane.setPrefWidth(960);
        scrollPane.setPrefHeight(460);
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        pageBox.getChildren().add(scrollPane);

        int start = pageIndex * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, searchBooks.size());

        if (start >= searchBooks.size()) {
            Label noBooksLabel = new Label("No books available for this page.");
            noBooksLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #555;");
            pageBox.getChildren().add(noBooksLabel);
            pageCache.put(pageIndex, pageBox);
            return pageBox;
        }

        Task<Void> loadBooksTask = new Task<>() {
            @Override
            public Void call() throws Exception {
                for (int i = start; i < end; i++) {
                    Book book = searchBooks.get(i);
                    int tmp = i;
                    Platform.runLater(() -> {
                        try {
                            FXMLLoader fxmlLoader = new FXMLLoader(
                                getClass().getResource("/FXMLs/BookCard.fxml"));
                            VBox cardBox = fxmlLoader.load();
                            BookCardController bookCardController = fxmlLoader.getController();
                            bookCardController.setData(book);
                            bookCardController.setUserHomeController(UserHomeController.this);

                            int column = (tmp - start) % COLUMNS;
                            int row = (tmp - start) / COLUMNS;
                            gridPane.add(cardBox, column, row);
                            GridPane.setMargin(cardBox, new Insets(10));
                        } catch (IOException e) {
                            System.err.println("Failed to load book card: " + e.getMessage());
                            e.printStackTrace();
                        }
                    });

                }
                return null;
            }

            @Override
            protected void succeeded() {
                progressIndicator.setVisible(false);
            }

            @Override
            protected void failed() {
                System.err.println("Failed to load books for page: " + pageIndex);
                progressIndicator.setVisible(false);
            }
        };

        progressIndicator.setVisible(true);
        executorService.submit(loadBooksTask);

        // Save page in cache
        pageCache.put(pageIndex, pageBox);

        return pageBox;
    }


    public void openBookDetails(Book book) throws IOException, InterruptedException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/FXMLs/User_BookView.fxml"));
        Parent root = fxmlLoader.load();

        ShowBookInformation showBookInformation = fxmlLoader.getController();
        showBookInformation.setData(book);
        showBookInformation.setUserHomeController(this);

        if (isPush) {
            sceneStack.push(menuController.getContent());
            isPush = false;
        }
        bookStack.push(menuController.getContent());
        this.menuController.setContent(root);
    }

    public void goBack() throws IOException {
        if (!sceneStack.isEmpty()) {
            this.menuController.setContent(sceneStack.pop());
            this.bookStack.clear();
            isPush = true;
            countToBack = 0;
        }
    }

    public void goPreviousBook() throws IOException {
        if (bookStack.size() >= 2) {
            this.menuController.setContent(bookStack.pop());
            countToBack--;
        }
    }
}
