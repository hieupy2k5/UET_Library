package org.example.uet_library.utilities;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import org.example.uet_library.enums.BookCheckResult;
import org.example.uet_library.models.Book;
import org.example.uet_library.services.BookService;
import org.example.uet_library.services.UserService;

public class SharedData {

    private static SharedData instance;
    private ObservableMap<Book, Integer> selectedBooksMap = FXCollections.observableHashMap();

    private SharedData() {
    }

    public static SharedData getInstance() {
        if (instance == null) {
            instance = new SharedData();
        }
        return instance;
    }

    public ObservableMap<Book, Integer> getSelectedBooksMap() {
        return selectedBooksMap;
    }

    /**
     * Add selected book to cart.
     * @param book is the selected book.
     */
    public void addToCart(Book book) {
        BookCheckResult bookCheckResult = BookService.getInstance().isBookBorrowedOrRequested(book.getIsbn());
        if (bookCheckResult == BookCheckResult.ALREADY_REQUESTED) {
            AlertHelper.showAlert(AlertType.ERROR, "Book already in request list",
                "Please check \"My Requests\" tab to see your request for this book.");
            return;
        }
        if (bookCheckResult == BookCheckResult.ALREADY_BORROWED) {
            AlertHelper.showAlert(AlertType.ERROR,
                "You are already borrowing this book",
                "To borrow this book again, please return it first.");
            return;
        }
        int quantityInStock = book.getQuantity();
        if (quantityInStock > 0) {
            if (!selectedBooksMap.containsKey(book)) {
                selectedBooksMap.put(book, 1);
                Platform.runLater(() -> AlertHelper.showAlert(Alert.AlertType.INFORMATION,
                    "Book added to cart successfully",
                    String.format("You have added %s to your cart.", book.getTitle())));
            } else {
                Platform.runLater(
                    () -> AlertHelper.showAlert(Alert.AlertType.WARNING, "Book already in cart",
                        "The selected book is already in your cart."));
            }
        } else {
            AlertHelper.showAlert(AlertType.ERROR, "Insufficient amount of books",
                "We have ran out of stock for this book. Please try again later!");
        }
    }
}
