package org.example.uet_library.Controllers;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.example.uet_library.BookService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

import org.example.uet_library.Book;
public class BookAddController {
    private MenuController menuController = BookAPISearch.menuController;
    private Book newBook;
    private ObservableList<Book> bookListToBack;
    private int pageIndex = 0;

    public void setMenuControllerForAddManager(MenuController menuController) {
        this.menuController = menuController;
    }

    private BookAPISearch bookAPISearch;

    public void setBookAPISearch(BookAPISearch bookAPISearch) {
        this.bookAPISearch = bookAPISearch;
    }


    public void setNewBook(Book newBook) {
        this.newBook = newBook;
        if (newBook != null) {
            if(newBook.getAuthor() == null || newBook.getAuthor().isEmpty()) {
                authoradd.setText("No author");
            } else {
                authoradd.setText(newBook.getAuthor());
            }
            isbnBook.setText(newBook.getIsbn());
            titleadd.setText(newBook.getTitle());
            if (newBook.getType() == null || newBook.getType().equals("")) {
                typeBook.setText("No type");
            } else {
                typeBook.setText(newBook.getType());
            }
            yearBook.setText(newBook.getYear()+"");
            if (newBook.getImageLink() == null || newBook.getImageLink().isEmpty()) {
                Image image = new Image(getClass().getResource("/Images/imageNotFound.jpg").toExternalForm());
                imageOfBook.setImage(image);
                imageOfBook.setFitHeight(150);
                imageOfBook.setFitWidth(150f);
            } else {
                Image imageAdd = new Image(newBook.getImageLink(),150,150, true, true);
                imageOfBook.setImage(imageAdd);
            }

        } else {
            authoradd.setText("");
            isbnBook.setText("");
            titleadd.setText("");
            typeBook.setText("");
            yearBook.setText("");
        }
    }
    @FXML
    private Button SaveBookToDB;

    @FXML
    private TextField authoradd;

    @FXML
    private Button backToBookAdd;

    @FXML
    private TextField isbnBook;

    @FXML
    private TextField quantityAdd;

    @FXML
    private TextField titleadd;

    @FXML
    private TextField typeBook;

    @FXML
    private TextField yearBook;

    @FXML
    private ImageView imageOfBook;
    public void initialize() {

    }

    public void alterNotion(String notion) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(notion);
        alert.setContentText(notion);
        alert.showAndWait();
    }

    public void backToBookAdd(ActionEvent event) throws IOException {
        ChangeSceneMachine.getInstance().changeScene("BookInfo.fxml",event,800,600);
    }

    public void saveBook(ActionEvent event) throws IOException, SQLException, ClassNotFoundException {
        if(this.checkTextField()) {
            String title = titleadd.getText();
            String author = authoradd.getText();
            String isbn = isbnBook.getText();
            String type = typeBook.getText();
            Integer year = Integer.parseInt(yearBook.getText());
            String url = newBook.getImageUrl();
            Integer quantity = Integer.parseInt(quantityAdd.getText());
            String bookLink = newBook.getInfoBookLink();
            String description = newBook.getDescription();
            newBook = new Book(title, author, isbn, url,year, type, bookLink,description);
            newBook.setQuantity(quantity);
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Are you sure you want to save this book?");
            alert.setContentText("Please choice OK or Cancel");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                if(BookService.getInstance().isExitsBook(newBook.getIsbn())){
                    Alert a =  new Alert(Alert.AlertType.ERROR);
                    a.setTitle("Error");
                    a.setHeaderText(null);
                    a.setContentText("Book already exists");
                    a.showAndWait();
                } else {
                    Alert alter = new Alert(Alert.AlertType.INFORMATION);
                    alter.setTitle("Information");
                    alter.setHeaderText(null);
                    alter.setContentText("Book added successfully");
                    alter.showAndWait();
                    this.saveBookTask(newBook);
                }
            }} else {
            return;
        }
    }

    public void saveBookTask(Book book) throws IOException {
        Task<Void> task = BookService.getInstance().addBook(book);

        task.setOnSucceeded(event->{
        });

        task.setOnFailed(event -> {
            System.err.println("Failed to add book: " + task.getException().getMessage());
        });

        task.setOnRunning(event -> {
            try {
                BookAPISearch.setBack();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        new Thread(task).start();

    }

    private boolean checkTextField() {
        if (titleadd.getText() == null || titleadd.getText().isEmpty()) {
            this.alterNotion("Please enter a valid title");
            return false;
        }
        else if(authoradd.getText() == null || authoradd.getText().isEmpty()) {
            this.alterNotion("Please enter a valid author");
            return false;
        }
        else if (typeBook.getText() == null || typeBook.getText().isEmpty()) {
            this.alterNotion("Please enter a valid type");
            return false;
        }
        else if(isbnBook.getText() == null || isbnBook.getText().isEmpty()) {
            this.alterNotion("Please enter a valid ISBN");
            return false;
        } else if (yearBook.getText() == null || yearBook.getText().isEmpty()) {
            this.alterNotion("Please enter a valid year");
            return false;
        } else if(quantityAdd.getText() == null || quantityAdd.getText().isEmpty()) {
            this.alterNotion("Please enter a valid quantity");
            return false;
        }
        return true;
    }

    @FXML
    public void BackOnAction(ActionEvent event) throws IOException {
        if (this.bookAPISearch != null) {
            BookAPISearch.setBack();
        }
    }

}
