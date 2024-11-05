package org.example.uet_library.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.example.uet_library.Book;
import org.example.uet_library.BookService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class BookAPISearch {
    public static MenuController menuController;
    private Book selectedBook;

    public void setMenuController(MenuController menuController) {
        this.menuController = menuController;
    }
    @FXML
    private Button AddBookOnAction;

    @FXML
    private TableColumn<Book, String> ISBN;

    @FXML
    private TableColumn<Book, String> author;

    @FXML
    private TableColumn<Book, String> image;

    @FXML
    private TextField queryBook;

    @FXML
    private TableView<Book> tableOfBook;

    @FXML
    private TableColumn<Book, String> tittle;

    @FXML
    private TableColumn<Book, String> type;

    @FXML
    private TableColumn<Book,Integer> year;

    @FXML
    private ChoiceBox<String> filterSearch;

    @FXML
    public void searchBookOnAction(ActionEvent event) {
        if (queryBook.getText().equals("")) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Please enter a search term");
            alert.showAndWait();
        } else if (filterSearch.getValue() == null || filterSearch.getValue().equals("")) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("You want to search by isbn, author or title ? please select your type");
            alert.showAndWait();
        } else {
            searchBook(queryBook.getText(), filterSearch.getValue());
        }
    }

    public void searchBook(String query, String filter) {
        Task<JSONArray> task = BookService.getInstance().searchBooks(query, filter);

        task.setOnSucceeded(event -> {
            JSONArray jsonArray = task.getValue();
            updateTableView(jsonArray);
        });

        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            System.out.println(exception.getMessage());
        });

        new Thread(task).start();
    }

    public void updateTableView(JSONArray bookArrJson) {
        ObservableList<Book> bookList = FXCollections.observableArrayList();
        for(int i = 0; i < bookArrJson.length(); i++) {
            JSONObject bookJson = bookArrJson.getJSONObject(i).getJSONObject("volumeInfo");
            if(bookJson != null) {
                JSONObject volumeInfo = bookJson.optJSONObject("volumeInfo");
                String title = bookJson.optString("title", "No title");
                JSONArray authors = bookJson.optJSONArray("authors");
                String author = (authors != null) ? authors.getString(0) : "No author";
                String isbn = null;
                if(bookJson.optJSONArray("industryIdentifiers") != null) {
                    isbn = bookJson.optJSONArray("industryIdentifiers").getJSONObject(0).getString("identifier");
                }
                JSONArray categories = bookJson.optJSONArray("categories");
                String type = null;
                if (categories != null && categories.length() > 0) {
                    type = categories.getString(0);
                }

                JSONObject imageLinks = bookJson.optJSONObject("imageLinks");
                String imageUrl = (imageLinks != null) ? imageLinks.optString("thumbnail", "") : "";

                int year = 0;
                if(bookJson.has("publishedDate")) {
                    String pushlishedDate = bookJson.optString("publishedDate");
                    if (pushlishedDate.length() >= 4) {
                        year = Integer.parseInt(pushlishedDate.substring(0, 4));
                    }
                }
                String url = "";
                if(bookJson.has("infoLink")) {
                    url = bookJson.optString("infoLink","");
                }
                bookList.add(new Book(title, author, isbn,imageUrl, year, type, url));
            }
        }
        tableOfBook.setItems(bookList);
    }

    @FXML
    public void initialize() {

        if(filterSearch != null) {
            filterSearch.getItems().addAll("Title", "Author","ISBN");
            System.out.println(filterSearch.getItems());
        }


        tittle.setCellValueFactory(new PropertyValueFactory<>("title"));
        author.setCellValueFactory(new PropertyValueFactory<>("author"));
        ISBN.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        year.setCellValueFactory(new PropertyValueFactory<>("year"));
        type.setCellValueFactory(new PropertyValueFactory<>("type"));


        image.setCellValueFactory(new PropertyValueFactory<>("imageLink"));
        image.setCellFactory(column ->  new TableCell<Book, String>(){
            private final ImageView imageView = new ImageView();
            @Override
            protected void updateItem(String imageLink, boolean empty) {
                super.updateItem(imageLink, empty);
                if(empty || imageLink == null || imageLink.isEmpty()) {
                    setGraphic(null);
                } else {
                    Image imageAdd = new Image(imageLink,70,70, true, true);
                    imageView.setImage(imageAdd);
                    setGraphic(imageView);
                }
            }
        });
        tableOfBook.setOnMouseClicked(event -> {
            selectedBook = tableOfBook.getSelectionModel().getSelectedItem();
        });
        filterSearch.valueProperty().addListener((observable, oldValue, newValue) -> {
            tableOfBook.getItems().clear();
        });
    }

    @FXML
    public void AddBookOnAction(ActionEvent actionEvent) throws IOException {

        if (selectedBook == null || tableOfBook.getSelectionModel().getSelectedItem() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Please select a search term");
            alert.showAndWait();
        }
        else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXMLs/BookAdd.fxml"));
                Parent root = loader.load();

                // Fetch controller of MenuController
                BookAddController controller = loader.getController();
                controller.setNewBook(selectedBook); // Transfer Book Information

                // Add root into Pane
                if (menuController != null) {
                    menuController.setContent(root);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
