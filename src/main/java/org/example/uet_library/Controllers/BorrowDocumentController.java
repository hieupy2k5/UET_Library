package org.example.uet_library.Controllers;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.example.uet_library.AlertHelper;
import org.example.uet_library.Book;
import org.example.uet_library.BookService;
import org.example.uet_library.SessionManager;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Map;

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
    private TableColumn<Book, Void> titleAuthorColumn;

    @FXML
    private AnchorPane slidingPane;

    private void setupTitleAuthorColumn() {
        titleAuthorColumn.setCellFactory(column -> new TableCell<>() {
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
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Book book = getTableView().getItems().get(getIndex());
                    titleLabel.setText(book.getTitle());
                    authorLabel.setText(book.getAuthor());
                    setGraphic(hbox);
                }
            }
        });
    }

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
        }));

        task.setOnFailed(event -> Platform.runLater(() -> {
            System.err.println(
                "Error fetching books from database: " + task.getException().getMessage());
            waitProgress.setVisible(false);
        }));

        // Start the task on a new thread
        new Thread(task).start();
    }

    // Initializes the controller
    public void initialize() {

        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        waitProgress.setVisible(true);

        setupTitleAuthorColumn();
        fetchFromDB();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);


        slidingPane.setTranslateX(900);
        slidingPane.setTranslateY(0);


        selectedBooksTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
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

            {
                Image borrowImage = new Image(getClass().getResource("/Images/insertToCart.png").toExternalForm());
                ImageView imageView = new ImageView(borrowImage);
                imageView.setFitWidth(16);
                imageView.setFitHeight(16);
                borrowButton.setGraphic(imageView);
                borrowButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

                borrowButton.setOnAction(event -> {
                    Book selectedBook = getTableView().getItems().get(getIndex());
                    showQuantityDialog(selectedBook);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(borrowButton);
                }
            }
        });
    }


    private void showQuantityDialog(Book book) {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Borrow Quantity");
        dialog.setHeaderText("Specify the quantity to borrow for " + book.getTitle());

        VBox dialogVBox = new VBox();
        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantity");

        dialogVBox.getChildren().add(quantityField);
        dialog.getDialogPane().setContent(dialogVBox);

        ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                try {
                    return Integer.parseInt(quantityField.getText());
                } catch (NumberFormatException e) {
                    AlertHelper.showAlert(AlertType.ERROR, "Invalid input",
                        "Please enter a positive integer.");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(quantity -> {
            if (quantity > 0 && quantity <= book.getQuantity()) {
                selectedBooksMap.put(book, quantity);
            } else if (quantity.equals(0)) {
                AlertHelper.showAlert(AlertType.ERROR, "Invalid number of books entered",
                    "You came here to borrow 0 book? None? Seriously?");
            } else if (book.getQuantity() == 0) {
                AlertHelper.showAlert(AlertType.ERROR, "Out of stock",
                    "Sorry, we ran out of that book in stock today.");
            } else if (quantity < 0) {
                AlertHelper.showAlert(AlertType.ERROR, "Invalid number of books entered",
                    "How on earth are you going to borrow a NEGATIVE number of books???");
            } else if (quantity > book.getQuantity()) {
                AlertHelper.showAlert(AlertType.ERROR, "Insufficient number of available books",
                    "Sorry, we do not have that many books.");
            }
        });
    }

    private void updateSelectedBooksTable() {
        ObservableList<Map.Entry<Book, Integer>> selectedBooksList = FXCollections.observableArrayList(selectedBooksMap.entrySet());

        selectedBooksTable.setItems(selectedBooksList);

        //detailColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getKey().getTitle()));
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
        quantityBorrowedColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getValue()).asObject());
    }


    @FXML
    private void borrowAllBooks() {
        int userID = SessionManager.getInstance().getUserId();

        if (selectedBooksMap.isEmpty()) {
            AlertHelper.showAlert(AlertType.INFORMATION, "Are you forgot to do something ?","You haven't add any book to your cart");
            return;
        }

        for (Map.Entry<Book, Integer> entry : selectedBooksMap.entrySet()) {
            Book book = entry.getKey();
            int quantity = entry.getValue();

            if (quantity > 0 && quantity <= book.getQuantity()) {
                if (BookService.getInstance().borrowBook(userID, book.getIsbn(), quantity)) {
                    fetchFromDB();
                } else {
                    AlertHelper.showAlert(AlertType.ERROR, "Error", "Database Error");
                    return;
                }
            } else {
                AlertHelper.showAlert(AlertType.ERROR, "Invalid number of books entered",
                        "Invalid quantity: " + quantity + " for book " + book.getTitle());
                return;
            }
        }

        AlertHelper.showAlert(AlertType.INFORMATION, "Borrow successfully",
                "You have successfully borrowed all selected books.");

        selectedBooksMap.clear();
        cartButtonClicked();
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

    private ObservableMap<Book, Integer> selectedBooksMap = FXCollections.observableHashMap();
}
