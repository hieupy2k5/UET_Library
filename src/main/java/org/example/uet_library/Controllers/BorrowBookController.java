package org.example.uet_library.Controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
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
import org.example.uet_library.models.Book;
import org.example.uet_library.services.BookService;
import org.example.uet_library.services.UserService;
import org.example.uet_library.utilities.AlertHelper;
import org.example.uet_library.utilities.SessionManager;
import org.example.uet_library.utilities.SharedData;

/**
 * This is a feature for users.
 */
public class BorrowBookController extends TableViewController<Book> {

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

    private List<String> favoriteBooks = new ArrayList<>();


    private ObservableMap<Book, Integer> selectedBooksMap = SharedData.getInstance()
        .getSelectedBooksMap();

    private final Map<String, Image> imageCache = new ConcurrentHashMap<>();

    public void setUpColumns() {
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
    }

    @Override
    TableColumn<Book, Void> getInformationColumn() {
        return this.informationColumn;
    }

    @Override
    TableView<Book> getTableView() {
        return this.tableView;
    }

    @Override
    ProgressIndicator getWaitProgress() {
        return this.waitProgress;
    }

    @Override
    Task<ObservableList<Book>> getTaskFromDB() {
        return BookService.getInstance().fetchBookFromDB();
    }

    @Override
    ObservableList<Book> getObservableList() {
        return books;
    }

    @Override
    TextField getSearchField() {
        return searchField;
    }

    @Override
    void setObservableList(ObservableList<Book> list) {
        books = list;
    }

    @Override
    public void postInitialize() {
        slidingPane.setTranslateX(900);
        slidingPane.setTranslateY(60);
    }

    @Override
    final boolean searchPredicate(Book book, String query) {
        return book.getTitle().toLowerCase().contains(query)
            || book.getAuthor().toLowerCase().contains(query)
            || book.getIsbn().toLowerCase().contains(query);
    }

    public void loadFavouriteBooks() {
        Task<Set<String>> favoriteTask = new Task<>() {
            @Override
            protected Set<String> call() {
                int userID = SessionManager.getInstance().getUserId();
                return BookService.getInstance().fetchFavoriteBooksByUserID(userID);
            }
        };

        favoriteTask.setOnSucceeded(event -> {
            favoriteBooks.clear();
            favoriteBooks.addAll(favoriteTask.getValue());
            tableView.refresh();
        });

        favoriteTask.setOnFailed(event -> {
            System.err.println(
                "Failed to load favorite books: " + favoriteTask.getException().getMessage());
        });

        new Thread(favoriteTask).start();
    }

    public void setUpAdditionalButtons() {
        actionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button borrowButton = new Button();
            private final Button favoriteButton = new Button();
            private final Image favorOnImage = new Image(
                getClass().getResource("/Images/Favor2.png").toExternalForm());
            private final Image favorOffImage = new Image(
                getClass().getResource("/Images/Favor1.png").toExternalForm());
            private final ImageView favorImageView = new ImageView();

            {
                // Set up borrowButton
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

                // Set up favoriteButton
                favorImageView.setFitWidth(16);
                favorImageView.setFitHeight(16);
                favoriteButton.setGraphic(favorImageView);
                favoriteButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                favoriteButton.setOnAction(event -> toggleFavorite());

            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getTableRow() == null) {
                    setGraphic(null);
                } else {
                    Book currentBook = getTableView().getItems().get(getIndex());
                    boolean isFavorite = favoriteBooks.contains(currentBook.getIsbn());

                    updateFavorImage(isFavorite ? favorOnImage : favorOffImage);

                    HBox hbox = new HBox(borrowButton, favoriteButton);
                    hbox.setSpacing(10);
                    hbox.setStyle("-fx-alignment: center;");
                    setGraphic(hbox);
                }
            }

            private void toggleFavorite() {
                Book selectedBook = getTableView().getItems().get(getIndex());
                String isbn = selectedBook.getIsbn();
                boolean isCurrentlyFavorite = favoriteBooks.contains(isbn);

                Task<Void> favoriteTask = new Task<>() {
                    @Override
                    protected Void call() {
                        String bookTitle = selectedBook.getTitle();
                        if (isCurrentlyFavorite) {
                            BookService.getInstance()
                                .removeBookFromFavoritesByBookIDAndUserID(selectedBook);
                            favoriteBooks.remove(isbn);
                            Platform.runLater(() -> {

                                AlertHelper.showAlert(AlertType.INFORMATION, "Successfully Removed",
                                    "You have removed \"" + bookTitle + "\" from your favorites.");
                            });
                        } else {
                            UserService.getInstance().addBookToFavorites(selectedBook);
                            favoriteBooks.add(isbn);
                            Platform.runLater(() -> {
                                AlertHelper.showAlert(AlertType.INFORMATION, "Successfully Added",
                                    "The book \"" + bookTitle
                                        + "\" has been added to your favorites.");
                            });
                        }
                        return null;
                    }

                    @Override
                    protected void succeeded() {
                        updateFavorImage(isCurrentlyFavorite ? favorOffImage : favorOnImage);
                    }

                    @Override
                    protected void failed() {
                        AlertHelper.showAlert(AlertType.ERROR, "Error",
                            "Failed to update favorite status.");
                    }
                };

                new Thread(favoriteTask).start();
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
                        boolean success = UserService.getInstance()
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
