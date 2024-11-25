package org.example.uet_library.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Pagination;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.Text;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import org.example.uet_library.database.Database;
import org.example.uet_library.models.Book;
import org.example.uet_library.services.BookRating;
import org.example.uet_library.services.BookService;
import org.example.uet_library.utilities.AlertHelper;
import org.example.uet_library.utilities.SessionManager;
import org.example.uet_library.utilities.SharedData;

public class ShowBookInformation {
    private final Image STAR_NOT_FILL = new Image(getClass().getResource("/Images/Favor1.png").toExternalForm(),40,40,true, true);
    private final Image STAR_FILL = new Image(getClass().getResource("/Images/Favor2.png").toExternalForm(),40,40,true, true);

    private boolean checkFetchback = false;

    @FXML
    private Text available;

    @FXML
    private Text unavailable;

    @FXML
    Button showAllButton;

    @FXML
    private Pagination pagina;

    @FXML
    private Text Author_label;

    @FXML
    private Text BookTitle;

    @FXML
    private ImageView imageBook;

    @FXML
    private Text description;

    @FXML
    private HBox cardLayout;

    @FXML
    private ImageView qrCode;

    @FXML
    private VBox commentBook;

    @FXML
    private ImageView Favor;

    private ObservableList<Book> recommendBooks;

    private Book bookCurrent;

    private UserHomeController userHomeController;
    private ShowMoreResultController showMoreResultController;

    public void setShowMoreResultController(ShowMoreResultController showMoreResultController) {
        this.showMoreResultController = showMoreResultController;
    }

    private ObservableList<BookRating> feedback = FXCollections.observableArrayList();

    public void setUserHomeController(UserHomeController userHomeController) {
        this.userHomeController = userHomeController;
        this.showAllButton.setVisible(false);
    }

    private boolean showAllFeedBack = false;

    @FXML
    void BackOnAction(ActionEvent event) throws IOException {
        if (userHomeController != null) {
            userHomeController.goBack();
        } else if (this.showMoreResultController != null) {
            this.showMoreResultController.goToSearchPage();
        }
    }

    public Task<ObservableList<BookRating>> getFeedBack() {
        return new Task<ObservableList<BookRating>>() {
            @Override
            protected ObservableList<BookRating> call() throws Exception {
                Database db = new Database();
                ObservableList<BookRating> feedback = FXCollections.observableArrayList();
                try (Connection conn = db.getConnection()) {
                    String query = "SELECT * FROM Ratings where ISBN = ? order by comment_at desc";
                    PreparedStatement ps = conn.prepareStatement(query);
                    ps.setString(1, bookCurrent.getIsbn());
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        String username = rs.getString("User_name");
                        Integer rating = rs.getInt("Rating");
                        String comment = rs.getString("Comment");
                        Timestamp commentAt = rs.getTimestamp("comment_at");
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String formattedCommentAt = sdf.format(commentAt);
                        BookRating bookRating = new BookRating("", rating, username, comment, formattedCommentAt);
                        bookRating.setComment(comment);
                        feedback.add(bookRating);
                    }
                    return feedback;
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }
    public void setData(Book book) {
        this.bookCurrent = book;
        description.setText(book.getDescription());
        description.setTextAlignment(TextAlignment.JUSTIFY);
        Author_label.setText(book.getAuthor());
        BookTitle.setText(book.getTitle());
        String url = book.getImageUrl();
        Image image;
        if (url == null || url.equals("")) {
            image = new Image(getClass().getResource("/Images/imageNotFound.jpg").toExternalForm(), true);
        } else {
            image = new Image(url, true);
        }
        imageBook.setImage(image);
        if (book.getQuantity() == 0) {
            available.setVisible(false);
            unavailable.setVisible(true);
        } else {
            available.setVisible(true);
            unavailable.setVisible(false);
        }
        byte[] qrCode = new byte[1];
        qrCode = book.getqrCode();
        ByteArrayInputStream bis = new ByteArrayInputStream(qrCode);
        this.qrCode.setImage(new Image(bis));
        checkIfFavorite();
        fetchBookForRecommendBook();
        setCommentBook();
    }

    public void fetchBookForRecommendBook() {
        Task<ObservableList<Book>> task = BookService.getInstance().fetchBookForPage(this.bookCurrent);
        task.setOnSucceeded(event -> {
            recommendBooks = task.getValue();
            for(Book book : recommendBooks) {

                try {
                    FXMLLoader fxmlloader = new FXMLLoader(getClass().getResource("/FXMLs/Card.fxml"));
                    HBox cardBox = fxmlloader.load();

                    CardController card = fxmlloader.getController();
                    card.setUserHomeController(this.userHomeController);
                    card.setShowMoreResultController(this.showMoreResultController);
                    card.setData(book);
                    cardLayout.getChildren().add(cardBox);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        task.setOnFailed(event -> {
            System.out.println("Failed");
        });

        new Thread(task).start();
    }

    @FXML
    public void handleButtonAdd() {
        SharedData.getInstance().addToCart(bookCurrent);
    }

    private void setCommentBook() {
        Task<ObservableList<BookRating>> task = getFeedBack();

        task.setOnSucceeded(event -> {
            this.checkFetchback = false;
            this.feedback = task.getValue();
            if (feedback == null || feedback.isEmpty()) {
                VBox commentBox = new VBox();
                commentBox.getChildren().add(new Text("No comments for this book."));
                this.commentBook.getChildren().add(commentBox);
                this.showAllButton.setVisible(false);
            } else {
                displayFeedBack(false);
                showAllButton.setVisible(feedback.size() > 5);
            }
        });

        task.setOnFailed(event -> {
            System.out.println("Failed to load comments.");
        });

        new Thread(task).start();
    }

    private void displayFeedBack(boolean showAll) {
        commentBook.getChildren().clear();
        int count = showAll ? feedback.size() : Math.min(feedback.size(), 5);

        for (int i = 0; i < count; i++) {
            BookRating bookRating = feedback.get(i);

            VBox commentBox = new VBox();
            commentBox.setSpacing(10);
            commentBox.setPrefWidth(850);

            commentBox.setPadding(new Insets(10, 50, 10, 50));
            HBox userInfoBox = new HBox();
            userInfoBox.setSpacing(10);
            userInfoBox.setStyle("-fx-border-color: #000; -fx-border-width: 1px;");

            String userNameText = bookRating.getUserName();
            if (userNameText == null || userNameText.equals("") || userNameText.equals(" ")) {
                userNameText = "Unknown";
            }

            Text userName = new Text(userNameText);
            userName.setStyle("-fx-font-weight: bold;");
            Text commentTime = new Text(bookRating.getCommentDate());
            commentTime.setStyle("-fx-font-style: italic; -fx-font-size: 12;");
            userInfoBox.getChildren().addAll(userName, new Text("-"), commentTime);

            Text commentContent = new Text(bookRating.getComment());
            commentContent.setWrappingWidth(400);
            commentContent.setStyle("-fx-font-size: 14;");

            Text separator = new Text("------------------------------------------------------------");
            separator.setStyle("-fx-fill: gray; -fx-font-size: 12;");

            HBox starRatingBox = new HBox();
            starRatingBox.setSpacing(10);

            int rating = bookRating.getRating();
            for (int j = 0; j < 5; j++) {
                if(j < rating) {
                    starRatingBox.getChildren().add(new ImageView(STAR_FILL));
                } else {
                    starRatingBox.getChildren().add(new ImageView(STAR_NOT_FILL));
                }
            }

            commentBox.getChildren().addAll(userInfoBox,starRatingBox,commentContent, separator);
            this.commentBook.getChildren().add(commentBox);
        }
    }

    @FXML
    public void handleShowAllFeedback() {
        showAllFeedBack = !showAllFeedBack;
        displayFeedBack(showAllFeedBack);
        showAllButton.setText(showAllFeedBack ? "Show Less" : "Show All");
    }

    private boolean checkIfFavorite() {
        try {
            Database db = new Database();
            try (Connection conn = db.getConnection()) {
                String checkQuery = "SELECT COUNT(*) FROM favors WHERE user_id = ? AND book_id = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                checkStmt.setInt(1, SessionManager.getInstance().getUserId());
                checkStmt.setString(2, bookCurrent.getIsbn());

                ResultSet rs = checkStmt.executeQuery();
                rs.next();
                boolean isFavorite = rs.getInt(1) > 0;

                if (isFavorite) {
                    Favor.setImage(STAR_FILL);
                } else {
                    Favor.setImage(STAR_NOT_FILL);
                }

                return isFavorite;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    @FXML
    public void handleHeartIcon() {
        try {
            Database db = new Database();
            try (Connection conn = db.getConnection()) {
                if (checkIfFavorite()) {
                    String deleteQuery = "DELETE FROM favors WHERE user_id = ? AND book_id = ?";
                    PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);
                    deleteStmt.setInt(1, SessionManager.getInstance().getUserId());
                    deleteStmt.setString(2, bookCurrent.getIsbn());
                    deleteStmt.executeUpdate();

                    AlertHelper.showAlert(Alert.AlertType.INFORMATION, "Successfully Removed",
                            "You have removed \"" + bookCurrent.getTitle()
                                    + "\" from your favorites.");
                } else {
                    BookService.getInstance().addBookToFavorites(bookCurrent);

                    AlertHelper.showAlert(Alert.AlertType.INFORMATION, "Successfully Added",
                            "The book \"" + bookCurrent.getTitle()
                                    + "\" has been added to your favorites.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goPreviousBook(ActionEvent event) throws IOException {
        if(userHomeController!=null) {
            userHomeController.goPreviousBook();
        } else if (this.showMoreResultController != null) {
            showMoreResultController.goToPreviousBook();
        }
    }

}
