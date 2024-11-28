package org.example.uet_library.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.example.uet_library.models.Book;

import java.io.IOException;

/**
 * Controller class for managing individual book cards in the library system.
 * This class is responsible for displaying book information on a card, including an image, title, and a random background color.
 * It also handles user interactions such as clicking on the book card to view more details.
 */
public class BookCardController {

    @FXML
    private Label bookTitle;

    @FXML
    private ImageView ImageBook;

    @FXML
    private VBox bookCard;

    @FXML
    private ImageView rating_star;

    private Book book;

    private UserHomeController userHomeController;
    private ShowMoreResultController showMoreResultController;

    /**
     * Array of colors used to generate random background colors for the book title.
     */
    private String[] colors = {"#B9E5FF", "#BDB2FE", "#FB9AA8", "#FF5056"};

    /**
     * Sets the parent controller for user home interactions.
     *
     * @param userHomeController the {@link UserHomeController} instance used to handle navigation or data updates.
     */
    public void setUserHomeController(UserHomeController userHomeController) {
        this.userHomeController = userHomeController;
    }

    /**
     * Populates the book card with the provided book data.
     * This includes setting the book title, loading the book image, and applying a random background color to the title.
     *
     * @param book the {@link Book} instance containing the book details to display.
     */
    public void setData(Book book) {
        this.book = book;
        String url = book.getImageUrl();

        Image image;
        if (url == null || url.isEmpty()) {
            // Load default image if no image URL is provided
            image = new Image(getClass().getResource("/Images/imageNotFound.jpg").toExternalForm());
        } else {
            image = new Image(url, true);
        }

        ImageBook.setImage(image);
        bookTitle.setText(book.getTitle());

        // Set a random background color for the book title
        String randomColor = colors[(int) (Math.random() * colors.length)];
        bookTitle.setStyle("-fx-background-color: " + randomColor + ";"
            + "-fx-background-radius: 20;"
            + "-fx-effect: dropShadow(three-pass-box, rgba(0,0,0,0),10,0,0,10);");
    }

    /**
     * Handles the event triggered when the book card button is clicked.
     * If the book and user home controller are set, it opens the detailed view of the selected book.
     *
     * @param event the {@link ActionEvent} triggered by clicking the book card button.
     * @throws IOException if an error occurs during navigation to the book details screen.
     */
    @FXML
    public void bookButtonClicked(ActionEvent event) throws IOException {
        try {
            if (userHomeController != null && book != null) {
                userHomeController.openBookDetails(book);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
