package org.example.uet_library.Controllers;

import javafx.concurrent.Service;
import javafx.geometry.Insets;
import javafx.scene.control.Pagination;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.uet_library.Book;
import org.example.uet_library.BookService;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

public class UserHomeController implements Initializable {
    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private HBox cardLayout;
    @FXML
    Pagination pagina;
    @FXML
    private GridPane gridPane;

    private static final int ITEMS_PER_PAGE = 5;
    private static final int COLUMNS = 5;
    private static final int ROWS = 1;
    private ObservableList<Book> books;

    private HashMap<Integer, VBox> demo = new HashMap<>();
    private Stage primaryStage;
    private int currentPageIndex = 0;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadTop5Books();
        Task<Integer> countPage = BookService.getInstance().fetchTotalBook();

        countPage.setOnSucceeded(event -> {
            int totalBooks = countPage.getValue();
            int pageCount = (int) Math.ceil(totalBooks / (double) ITEMS_PER_PAGE);
            pagina.setPageCount(pageCount);
            //pagina.setCurrentPageIndex(pageCount);

            pagina.setPageFactory(pageIndex -> createPage(pageIndex));
            progressIndicator.setVisible(false);
        });

        // Listener to track current page iNDEX

        pagina.currentPageIndexProperty().addListener((observable, oldValue, newValue) -> currentPageIndex = newValue.intValue());
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
                    cardController.setPreviousStage(primaryStage, this.currentPageIndex);
                    cardLayout.getChildren().add(cardBox);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        new Thread(task).start();
    }

    private VBox createPage(int pageIndex) {
        if (demo.containsKey(pageIndex)) {
            return demo.get(pageIndex);
        }

        VBox pageBox = new VBox();
        GridPane newGridPane = new GridPane(); // Tạo GridPane mới cho mỗi trang
        newGridPane.getChildren().clear();
        newGridPane.setHgap(10);
        newGridPane.setVgap(10);
        newGridPane.setPadding(new Insets(10, 10, 10, 10)); // Thiết lập padding cho GridPane

        int start = pageIndex * ITEMS_PER_PAGE;
        //int end = Math.min(start + ITEMS_PER_PAGE, books.size());

        Task<ObservableList<Book>> task = BookService.getInstance().featchBookForPage(start, ITEMS_PER_PAGE);
        task.setOnSucceeded(event -> {
            progressIndicator.setVisible(false);
            ObservableList<Book> bookPerPage = task.getValue();
            for (int i = 0; i < bookPerPage.size() ; i++) {
                Book book = bookPerPage.get(i);
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/FXMLs/BookCard.fxml"));
                    VBox cardBox = fxmlLoader.load();
                    BookCardController bookCardController = fxmlLoader.getController();
                    bookCardController.setData(book);
                    //bookCardController.setPreviousStage((Stage) cardBox.getScene().getWindow());
                    bookCardController.setPreviousStage(primaryStage, currentPageIndex);
                    int column = i % COLUMNS;
                    int row = i / COLUMNS;
                    newGridPane.add(cardBox, column, row);
                    GridPane.setMargin(cardBox, new Insets(10));
                    //bookCardController.setPreviousStage((Stage) cardLayout.getScene().getWindow());
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
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        pageBox.getChildren().add(scrollPane);
        demo.put(pageIndex, pageBox);
        return pageBox;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setUserData(this);
    }

    public void setCurrentPage (int pageIndex) {
        pagina.setCurrentPageIndex(pageIndex);
    }

}

