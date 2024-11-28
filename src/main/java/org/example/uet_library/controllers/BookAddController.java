package org.example.uet_library.controllers;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.example.uet_library.services.AdminService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

import org.example.uet_library.models.Book;
/**
 * The {@code BookAddController} class is responsible for handling the user interface
 * and logic for adding books to the library's database. This controller interacts
 * with the UI defined in the FXML file and provides methods for validating user input,
 * displaying book details, and saving new books.
 * <p>
 * This class uses asynchronous tasks to communicate with the database, ensuring
 * the UI remains responsive while operations are performed in the background.
 */
public class BookAddController {
    private MenuController menuController = BookAPISearchController.menuController;
    private Book newBook;
    private ObservableList<Book> bookListToBack;
    private int pageIndex = 0;

    private BookAPISearchController bookAPISearchController;


    /**
     * Sets the reference to the {@code BookAPISearchController}.
     *
     * @param bookAPISearchController the {@code BookAPISearchController} to set.
     */
    public void setBookAPISearch(BookAPISearchController bookAPISearchController) {
        this.bookAPISearchController = bookAPISearchController;
    }


    /**
     * Sets the details of a new book to be added and updates the UI fields accordingly.
     *
     * @param newBook the {@code Book} object containing the book details.
     */
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
            if (newBook.getCategory() == null || newBook.getCategory().equals("")) {
                typeBook.setText("No type");
            } else {
                typeBook.setText(newBook.getCategory());
            }
            yearBook.setText(newBook.getYear()+"");
            if (newBook.getImageUrl() == null || newBook.getImageUrl().isEmpty()) {
                Image image = new Image(getClass().getResource("/Images/imageNotFound.jpg").toExternalForm());
                imageOfBook.setImage(image);
                imageOfBook.setFitHeight(150);
                imageOfBook.setFitWidth(150f);
            } else {
                Image imageAdd = new Image(newBook.getImageUrl(),150,150, true, true);
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

    /**
     * Displays a warning alert with the specified message.
     *
     * @param notion the warning message to display.
     */
    public void alterNotion(String notion) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(notion);
        alert.setContentText(notion);
        alert.showAndWait();
    }

    /**
     * Saves the new book to the database after validating the input fields.
     * Prompts the user for confirmation before proceeding.
     *
     * @param event the action event triggered by clicking the save button.
     * @throws IOException              if an I/O error occurs.
     * @throws SQLException             if a database error occurs.
     * @throws ClassNotFoundException   if the database driver is not found.
     */
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
                if(AdminService.getInstance().isBookExisting(newBook.getIsbn())){
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

    /**
     * Asynchronously saves the book to the database.
     *
     * @param book the {@code Book} to save.
     * @throws IOException if an error occurs during the operation.
     */
    public void saveBookTask(Book book) throws IOException {
        Task<Void> task = AdminService.getInstance().addBook(book);

        task.setOnSucceeded(event->{
        });

        task.setOnFailed(event -> {
            System.err.println("Failed to add book: " + task.getException().getMessage());
        });

        task.setOnRunning(event -> {
            try {
                BookAPISearchController.setBack();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        new Thread(task).start();

    }


    /**
     * Validates the input fields for adding a book.
     *
     * @return {@code true} if all fields are valid, otherwise {@code false}.
     */
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

    /**
     * Handles the back action triggered by the "Back" button.
     *
     * @param event the action event triggered by the button click.
     * @throws IOException if an error occurs during navigation.
     */
    @FXML
    public void BackOnAction(ActionEvent event) throws IOException {
        if (this.bookAPISearchController != null) {
            BookAPISearchController.setBack();
        }
    }

}
