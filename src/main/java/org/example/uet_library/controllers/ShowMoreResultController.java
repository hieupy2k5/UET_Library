package org.example.uet_library.controllers;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Pagination;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.example.uet_library.models.Book;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.example.uet_library.services.BookService;

import java.io.IOException;
import java.util.Stack;

public class ShowMoreResultController {
    private UserHomeController userHomeController;
    private MenuController menuController;
    private static ObservableList<Book> bookResults;
    private static Label status = new Label("Status");
    private static Book selectedBook;
    private HBox selectedHBox;

    private static final int BOOKPERPAGE = 10;

    private Stack<Parent> screenBookStack = new Stack<>();

    private Stack<Parent> backToSearchStack = new Stack<>();

    private boolean isSearchScreen = true;

    @FXML
    private TextField searchTextField;

    @FXML
    private Pagination pagination;

    private int currentPage;

    public void setMenuController (MenuController menuController) {
        this.menuController = menuController;
    }

    public void setBookResults(ObservableList<Book> bookResults) {
        this.bookResults = bookResults;
        setPagination();
    }

    public void setUserHomeController(UserHomeController userHomeController) {
        this.userHomeController = userHomeController;
    }

    private void setPagination() {
        int pageCount = (int) Math.ceil((double) bookResults.size() / BOOKPERPAGE);
        pagination.setPageCount(pageCount);
        pagination.setPageFactory(this::createPage);
    }

    private ScrollPane createPage(int pageIndex ) {
        this.currentPage = pageIndex;
        VBox pageBox = new VBox(10);
        int start = pageIndex * BOOKPERPAGE;
        int end = Math.min(start + BOOKPERPAGE, bookResults.size());

        for (int i = start; i < end; i++) {
            Book book = bookResults.get(i);
            HBox bookBox = createBookBox(book);
            pageBox.getChildren().add(bookBox);
        }

        ScrollPane scrollPane = new ScrollPane(pageBox);
        scrollPane.setPrefWidth(960);
        scrollPane.setPrefHeight(460);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        return scrollPane;
    }

    private HBox createBookBox(Book book) {
        HBox bookBox = new HBox(10);
        bookBox.setStyle("-fx-padding: 10; -fx-background-color: #E7F5DC; -fx-border-color: #728156; -fx-border-radius: 5px;");
        bookBox.setPrefWidth(870);
        bookBox.setPrefHeight(100);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(80);
        imageView.setPreserveRatio(true);

        Task<Image> imageTask = new Task<>() {
            @Override
            protected Image call() throws Exception {
                return new Image(book.getImageUrl(), 80,80, true, true, true);
            }
        };
        imageTask.setOnSucceeded(e -> {
            imageView.setImage(imageTask.getValue());
        });

        new Thread(imageTask).start();

        VBox detailBox = new VBox(5);
        detailBox.getChildren().addAll(
                new Text("Title: " + book.getTitle()),
                new Text("Author: " + book.getAuthor()),
                new Text("Year: " + book.getYear()),
                new Text("Quantity: " + book.getQuantity())
        );

        bookBox.getChildren().addAll(imageView, detailBox);

        bookBox.setOnMouseClicked(e -> {
            if (selectedHBox != null) {
                selectedHBox.setStyle("-fx-padding: 10; -fx-background-color: #E7F5DC; -fx-border-color: #728156; -fx-border-radius: 5px;");
            }
            selectedHBox = bookBox;
            bookBox.setStyle("-fx-padding: 10; -fx-background-color: #ADD8E6; -fx-border-color: #0078D4; -fx-border-radius: 5;");
            this.selectedBook = book;
            try {
                showBookDetail(selectedBook);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        return bookBox;
    }

    @FXML
    private void goBack(ActionEvent event) throws IOException {
        this.userHomeController.goBackShowMore();
    }

    public void showBookDetail(Book book) throws IOException {
        screenBookStack.push(this.menuController.getContent());
        if (backToSearchStack.size() < 1) {
            backToSearchStack.push(this.menuController.getContent());
        }
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXMLs/User_BookView.fxml"));
        Parent root = loader.load();
        BookInformationController bookInformationController = loader.getController();
        bookInformationController.setData(book);
        bookInformationController.setShowMoreResultController(this);
        this.menuController.setContent(root);
    }

    public void goToSearchPage() throws IOException {
        if (!this.backToSearchStack.isEmpty()) {
            this.menuController.setContent(this.backToSearchStack .pop());
        }
    }

    public void goToPreviousBook() throws IOException {
        if (this.screenBookStack.size() > 1) {
            this.menuController.setContent(this.screenBookStack.pop());
        }
    }

    @FXML
    public void searchBookOnAction(ActionEvent event) {
        if (!searchTextField.getText().isEmpty()) {
            fetchBookForSearch(searchTextField.getText());
        }
    }

    private void fetchBookForSearch(String query) {
        Task<ObservableList<Book>> task = BookService.getInstance()
                .fetchBookByTitleOrAuthor(query);
        task.setOnSucceeded(e -> {
            bookResults = task.getValue();
            setPagination();
        });
        new Thread(task).start();
    }

}
