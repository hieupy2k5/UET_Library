package org.example.uet_library.Controllers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.example.uet_library.utilities.AlertHelper;
import org.example.uet_library.models.Book;
import org.example.uet_library.services.BookService;
import org.example.uet_library.utilities.SessionManager;
import org.example.uet_library.utilities.SharedData;

/**
 * This is a feature for users.
 */
public class BorrowDocumentController {

    @FXML
    public TableView<Map.Entry<Book, Integer>> selectedBooksTable;

    @FXML
    public TableColumn<Map.Entry<Book, Integer>, String> detailColumn;

    @FXML
    public TableColumn<Map.Entry<Book, Integer>, Integer> quantityBorrowedColumn;
    @FXML
    private AnchorPane root;


    @FXML
    public Label documentsLabel;

    @FXML
    private ProgressIndicator waitProgress;
    @FXML
    private TableColumn<Book, String> authorColumn;
    @FXML
    private TableColumn<Book, Integer> quantityColumn;
    @FXML
    private TextField searchField;
    @FXML
    private TableColumn<Book, String> titleColumn;
    @FXML
    private TableColumn<Book, String> typeColumn;
    @FXML
    private TableColumn<Book, Integer> yearColumn;
    @FXML
    private TableView<Book> tableView;
    @FXML
    private TableColumn<Book, String> image;
    @FXML
    private TableColumn<Book, Void> actionColumn;
    private ObservableList<Book> books;

    @FXML
    private TableColumn<Book, Void> informationColumn;

    @FXML
    private AnchorPane slidingPane;

    private ObservableMap<Book, Integer> selectedBooksMap = SharedData.getInstance()
        .getSelectedBooksMap();

    private final Map<String, Image> imageCache = new ConcurrentHashMap<>();

    public void fetchFromDB() {
        Task<ObservableList<Book>> task = BookService.getInstance().fetchBookFromDB();

        // Bind progress indicator to task status
        task.setOnRunning(event -> Platform.runLater(() -> {
            waitProgress.setVisible(true);
            waitProgress.setProgress(-1);
        }));

        task.setOnSucceeded(event -> Platform.runLater(() -> {
            books = task.getValue();
            tableView.setItems(books);
            waitProgress.setVisible(false);
            setupSearch();
            setupBorrowButton();
            setupInformation();
        }));

        task.setOnFailed(event -> Platform.runLater(() -> {
            System.err.println(
                "Error fetching books in fetchFromDB() (BorrowDocumentController.java): "
                    + task.getException().getMessage());
            waitProgress.setVisible(false);
        }));

        // Start the task on a new thread
        new Thread(task).start();
    }

    public void initialize() {
        tableView.setPlaceholder(new Label("Your borrow list is empty..."));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        waitProgress.setVisible(true);

        fetchFromDB();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        slidingPane.setTranslateX(900);
        slidingPane.setTranslateY(60);

        selectedBooksTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupInformation() {
        informationColumn.setText("Document Information");
        informationColumn.setCellFactory(column -> new TableCell<>() {
            private final HBox hbox = new HBox();
            private final VBox vbox = new VBox();
            private final ImageView imageView = new ImageView();
            private final Label titleLabel = new Label();
            private final Label authorLabel = new Label();

            {
                vbox.getChildren().addAll(titleLabel, authorLabel);
                vbox.setSpacing(5);
                hbox.setSpacing(15);
                hbox.getChildren().addAll(imageView, vbox);

                imageView.setFitWidth(50);
                imageView.setFitHeight(50);
                titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                authorLabel.setStyle("-fx-font-style: italic;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null) {
                    setGraphic(null);
                } else {
                    Book book = getTableView().getItems().get(getIndex());

                    titleLabel.setText(book.getTitle());
                    authorLabel.setText(book.getAuthor());
                    setGraphic(hbox);

                    String imageUrl = book.getImageUrl();
                    if (imageCache.containsKey(imageUrl)) {
                        imageView.setImage(imageCache.get(imageUrl));
                    } else {
                        Task<Image> loadImageTask = new Task<>() {
                            @Override
                            protected Image call() {
                                return new Image(book.getImageUrl(), true);
                            }
                        };

                        loadImageTask.setOnSucceeded(
                            event -> {
                                Image img = loadImageTask.getValue();
                                imageCache.put(imageUrl, img);
                                imageView.setImage(loadImageTask.getValue());
                            });
                        loadImageTask.setOnFailed(event -> {
//                        System.err.println(
//                            "Failed to load image: " + loadImageTask.getException().getMessage());
                        });

                        new Thread(loadImageTask).start();
                    }
                }
            }
        });
    }

    private void setupSearch() {
        if (books == null || books.isEmpty()) {
            System.err.println("Book list is empty or null, cannot set up search.");
            return;
        }
        FilteredList<Book> filteredData = new FilteredList<>(books, b -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(book -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();
                return book.getTitle().toLowerCase().contains(lowerCaseFilter)
                    || book.getAuthor().toLowerCase().contains(lowerCaseFilter)
                    || book.getIsbn().toLowerCase().contains(lowerCaseFilter);
            });
        });

        SortedList<Book> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedData);
    }

    private void setupBorrowButton() {
        actionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button borrowButton = new Button();
            private final Button favoriteButton = new Button();
            private final Image favorOnImage = new Image(
                getClass().getResource("/Images/Favor2.png").toExternalForm());
            private final Image favorOffImage = new Image(
                getClass().getResource("/Images/Favor1.png").toExternalForm());
            private final ImageView favorImageView = new ImageView();

            {
                Image borrowImage = new Image(
                    getClass().getResource("/Images/insertToCart.png").toExternalForm());
                ImageView borrowImageView = new ImageView(borrowImage);
                borrowImageView.setFitWidth(16);
                borrowImageView.setFitHeight(16);
                borrowButton.setGraphic(borrowImageView);
                borrowButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

                borrowButton.setOnAction(event -> {
                    Book selectedBook = getTableView().getItems().get(getIndex());
                    SharedData.getInstance().addToCart(selectedBook);
                });

                favorImageView.setFitWidth(16);
                favorImageView.setFitHeight(16);
                favoriteButton.setGraphic(favorImageView);
                favoriteButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

                favoriteButton.setOnAction(event -> {
                    Book selectedBook = getTableView().getItems().get(getIndex());
                    Task<Void> favoriteTask = new Task<>() {
                        @Override
                        protected Void call() {
                            String bookTitle = selectedBook.getTitle(); // Lấy tên sách

                            if (BookService.getInstance().isFavorite(selectedBook)) {
                                BookService.getInstance()
                                    .removeBookFromFavoritesByBookIDAndUserID(selectedBook);

                                Platform.runLater(() -> {
                                    updateFavorImage(favorOffImage);
                                    AlertHelper.showAlert(AlertType.INFORMATION, "Successfully Removed",
                                        "You have removed \"" + bookTitle
                                            + "\" from your favorites.");
                                });
                            } else {
                                BookService.getInstance().addBookToFavorites(selectedBook);

                                Platform.runLater(() -> {
                                    updateFavorImage(favorOnImage);
                                    AlertHelper.showAlert(AlertType.INFORMATION, "Successfully Added",
                                        "The book \"" + bookTitle
                                            + "\" has been added to your favorites.");
                                });
                            }
                            return null;
                        }

                    };
                    new Thread(favoriteTask).start();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null) {
                    setGraphic(null);
                } else {
                    Book currentBook = getTableView().getItems().get(getIndex());
                    Task<Void> checkFavoriteTask = new Task<>() {
                        @Override
                        protected Void call() {
                            if (BookService.getInstance().isFavorite(currentBook)) {
                                updateFavorImage(favorOnImage);
                            } else {
                                updateFavorImage(favorOffImage);
                            }
                            return null;
                        }
                    };
                    new Thread(checkFavoriteTask).start();

                    HBox hbox = new HBox(borrowButton, favoriteButton);
                    hbox.setSpacing(10);
                    hbox.setStyle("-fx-alignment: center;");
                    setGraphic(hbox);
                }
            }

            private void updateFavorImage(Image image) {
                Platform.runLater(() -> favorImageView.setImage(image));
            }
        });
    }


    private void updateSelectedBooksTable() {
        ObservableList<Map.Entry<Book, Integer>> selectedBooksList = FXCollections.observableArrayList(
            selectedBooksMap.entrySet());
        selectedBooksTable.setItems(selectedBooksList);

        detailColumn.setCellFactory(column -> new TableCell<Map.Entry<Book, Integer>, String>() {
            private final VBox hbox = new VBox();
            private final Label titleLabel = new Label();
            private final Label authorLabel = new Label();

            {
                hbox.getChildren().addAll(titleLabel, authorLabel);
                hbox.setSpacing(3);
                titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                authorLabel.setStyle("-fx-font-style: italic;");
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null) {
                    setGraphic(null);
                } else {
                    Map.Entry<Book, Integer> entry = getTableRow().getItem();
                    Book book = entry.getKey();
                    titleLabel.setText(book.getTitle());
                    authorLabel.setText(book.getAuthor());
                    setGraphic(hbox);
                }
            }
        });

        TableColumn<Map.Entry<Book, Integer>, Void> optionColumn = new TableColumn<>("Option");
        optionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button removeButton = new Button();

            {
                Image removeImage = new Image(
                    getClass().getResource("/Images/bin.png").toExternalForm());
                ImageView imageView = new ImageView(removeImage);
                imageView.setFitWidth(16);
                imageView.setFitHeight(16);
                removeButton.setGraphic(imageView);
                removeButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

                removeButton.setOnAction(event -> {
                    Map.Entry<Book, Integer> entry = getTableView().getItems().get(getIndex());
                    selectedBooksMap.remove(entry.getKey());
                    updateSelectedBooksTable();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
//                setGraphic(empty ? null : removeButton);
                HBox hbox = new HBox(removeButton);
                hbox.setStyle("-fx-alignment: center;");
                setGraphic(empty ? null : hbox);
            }
        });

        selectedBooksTable.getColumns().setAll(detailColumn, optionColumn);
    }


    @FXML
    private void requestAllBook() {
        int userID = SessionManager.getInstance().getUserId();

        if (selectedBooksMap.isEmpty()) {
            AlertHelper.showAlert(AlertType.INFORMATION, "Did you forget to do something ?",
                "You haven't added any book to your cart");
            return;
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                for (Map.Entry<Book, Integer> entry : selectedBooksMap.entrySet()) {
                    Book book = entry.getKey();

                    if (book.getQuantity() > 0) {
                        boolean success = BookService.getInstance()
                            .requestBook(userID, book.getIsbn(), 1);

                        if (!success) {
                            throw new RuntimeException(
                                "Database Error while requesting book: " + book.getTitle());
                        }
                    }
                }

                selectedBooksMap.clear();
                fetchFromDB();
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                AlertHelper.showAlert(AlertType.INFORMATION, "Request successfully",
                    "Now you need to wait for admins to approve your request(s)");
                cartButtonClicked();
            }

            @Override
            protected void failed() {
                super.failed();
                Throwable exception = getException();
                AlertHelper.showAlert(AlertType.ERROR, "Error", exception.getMessage());
            }
        };

        new Thread(task).start();
    }

    private boolean isPaneOpen = false;

    public void cartButtonClicked() {
        TranslateTransition slide = new TranslateTransition(Duration.millis(300), slidingPane);

        slide.setFromX(isPaneOpen ? 500 : 900);
        slide.setToX(isPaneOpen ? 900 : 500);
        isPaneOpen = !isPaneOpen;
        updateSelectedBooksTable();

        slide.play();
    }

}
