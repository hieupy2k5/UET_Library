package org.example.uet_library;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.control.Alert;

public class SharedData {
    private static SharedData instance;
    private ObservableMap<Book, Integer> selectedBooksMap = FXCollections.observableHashMap();

    private SharedData() {}

    public static SharedData getInstance() {
        if (instance == null) {
            instance = new SharedData();
        }
        return instance;
    }

    public ObservableMap<Book, Integer> getSelectedBooksMap() {
        return selectedBooksMap;
    }

    public void addToCart(Book book) {
        if (!selectedBooksMap.containsKey(book)) {
            selectedBooksMap.put(book, 1);
            Platform.runLater(() -> AlertHelper.showAlert(Alert.AlertType.INFORMATION, "Book added to cart successfully",
                    String.format("You have added %s to your cart.", book.getTitle())));
        } else {
            Platform.runLater(() -> AlertHelper.showAlert(Alert.AlertType.WARNING, "Book already in cart",
                    "The selected book is already in your cart."));
        }
    }
}
