package org.example.uet_library.Controllers;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.example.uet_library.models.Borrow;
import org.example.uet_library.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class RatingDialogController {
    @FXML
    private Button butStar1;

    @FXML
    private Button butStar2;

    @FXML
    private Button butStar3;

    @FXML
    private Button butStar4;

    @FXML
    private Button butStar5;

    @FXML
    private Button cancelButton;

    @FXML
    private TextArea commentField;

    @FXML
    private ImageView star1;

    @FXML
    private ImageView star2;

    @FXML
    private ImageView star3;

    @FXML
    private ImageView star4;

    @FXML
    private ImageView star5;

    @FXML
    private Button submitButton;

    @FXML
    private ImageView ImageBook;

    private int rating = 0;
    private String comment;
    public static String userName;


    private final Image starEmpty = new Image(getClass().getResource("/Images/star.png").toExternalForm());
    private final Image starFilled = new Image(getClass().getResource("/Images/star_color.png").toExternalForm());

    private ReturnDocumentController returnDocumentController;

    public void setReturnDocumentController(ReturnDocumentController returnDocumentController) {
        this.returnDocumentController = returnDocumentController;
    }
    private Borrow borrow;

    private String type;
    @FXML
    private void initialize() {
        resetStar();

        butStar1.setOnMouseClicked(event -> setRating(1));
        butStar2.setOnMouseClicked(event -> setRating(2));
        butStar3.setOnMouseClicked(event -> setRating(3));
        butStar4.setOnMouseClicked(event -> setRating(4));
        butStar5.setOnMouseClicked(event -> setRating(5));
    }

    private void setRating(int newRating) {
        if (this.rating == newRating) {
            return;
        }

        this.rating = newRating;

        star1.setImage(rating >= 1 ? starFilled : starEmpty);
        star2.setImage(rating >= 2 ? starFilled : starEmpty);
        star3.setImage(rating >= 3 ? starFilled : starEmpty);
        star4.setImage(rating >= 4 ? starFilled : starEmpty);
        star5.setImage(rating >= 5 ? starFilled : starEmpty);
    }

    private void resetStar() {
        star1.setImage(starEmpty);
        star2.setImage(starEmpty);
        star3.setImage(starEmpty);
        star4.setImage(starEmpty);
        star5.setImage(starEmpty);
    }


    public void setData(Borrow borrowedBook) {
        this.borrow = borrowedBook;
        Image image = null;
        String imageUrl = borrow.getImageUrl();;
        if (imageUrl == null || imageUrl.isEmpty()) {
            image = new Image(getClass().getResource("/Images/imageNotFound.jpg").toExternalForm());
        } else {
            image = new Image(imageUrl);
        }
        ImageBook.setImage(image);
        ImageBook.setFitHeight(image.getHeight());
        ImageBook.setFitWidth(image.getWidth());
    }

    @FXML
    private void cancelOnAction(ActionEvent event) {
        Stage currentStage = (Stage) cancelButton.getScene().getWindow();
        currentStage.close();
    }

    public Task<Void> saveComment() {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                Database db = new Database();
                try(Connection con = db.getConnection()) {
                    String sql = "INSERT INTO Ratings (Comment, User_name, Rating, ISBN) VALUES (?, ?, ?, ?)";
                    PreparedStatement ps = con.prepareStatement(sql);
                    String comment = commentField.getText();
                    ps.setString(1, comment);
                    ps.setString(2, userName);
                    ps.setInt(3, rating);
                    ps.setString(4, borrow.getIsbn());
                    ps.executeUpdate();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

    @FXML
    public void SaveButtonOnAction(ActionEvent event) {
        if (commentField == null || commentField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText(null);
            alert.setContentText("YOU HAVEN'T WRITTEN A COMMENT YET");
            alert.showAndWait();
        } else {
            Task<Void> task;
        if (type.equals("Rate")) {
            task = this.saveComment();
        } else {
            task = this.reFeedBack();
        }
        task.setOnSucceeded(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText("Your rating has been saved");
            alert.showAndWait();
            this.borrow.setIsRated(true);
            returnDocumentController.fetchBookRating();
            Stage stage = (Stage) submitButton.getScene().getWindow();
            stage.close();
        });
        task.setOnFailed(e -> {

        });
        new Thread(task).start();}
    }

    public void setTypeFeedBack(String type) {
        this.type = type;
    }

    private Task<Void> reFeedBack() {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                Database db = new Database();
                try(Connection con = db.getConnection()) {
                    String sql = "Update Ratings SET comment = ?, rating = ? WHERE User_name = ? AND isbn = ?";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setString(1, commentField.getText());
                    ps.setInt(2, rating);
                    ps.setString(3, userName);
                    ps.setString(4, borrow.getIsbn());
                    ps.executeUpdate();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

    }
}
