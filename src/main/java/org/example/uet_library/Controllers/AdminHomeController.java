package org.example.uet_library.Controllers;

import javafx.application.Platform;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.example.uet_library.Book;
import org.example.uet_library.BookService;
import org.example.uet_library.Database;
import org.example.uet_library.User;

import java.sql.*;

public class AdminHomeController {
    private ObservableList<Book> bookTop6 = FXCollections.observableArrayList();
    private ObservableList<User> top5Users = FXCollections.observableArrayList();

    @FXML
    private VBox tableOfTopBorrower = new VBox();

    @FXML
    private ImageView ImageTop2;

    @FXML
    private ImageView ImageTop3;

    @FXML
    private ImageView ImageTop4;

    @FXML
    private ImageView ImageTop5;

    @FXML
    private ImageView ImageTop6;

    @FXML
    private ImageView imageTop1;

    @FXML
    private Label top1Text;

    @FXML
    private Label top2Text;

    @FXML
    private Label top3Text;

    @FXML
    private Label top4Text;

    @FXML
    private Label top5Text;

    @FXML
    private Label top6Text;


        @FXML
        private Text issued_book;

        @FXML
        private Text numberOfBookField;

        @FXML
        private Text numberOfBookField1;

        @FXML
        private PieChart chartBookIssue;

        @FXML
        private Text numeberOfUser;

        private int numberOfBook = 0;

        private int numberOfBookBorrowed = 0;

        public void initialize() {
            this.loadTopBorrower();
            loadTopBorrowedBook();
        }

        private void loadTotalBook() {
            Task<Integer> task = BookService.getInstance().fetchTotalBook();

            task.setOnSucceeded(e -> {
                this.numberOfBook = task.getValue();
                loadIssuedBook();
                numberOfBookField.setText(this.numberOfBook + "");
            });
            task.setOnFailed(e -> {
                System.out.println(task.getException());
            });
            new Thread(task).start();
        }


    private void loadNumberOfUser() {
        Task<Integer> task = BookService.getInstance().loadNumberOfUser();
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            numeberOfUser.setText(task.getValue().toString());
        }));
        task.setOnFailed(e -> Platform.runLater(() -> {
            System.out.println(task.getException());

        }));
        new Thread(task).start();
    }

    private void loadIssuedBook() {
        Task<Integer> task = BookService.getInstance().fetchNumberOfBookBorrowed();

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            this.numberOfBookBorrowed = task.getValue().intValue();
            loadNumberOfUser();
            loadChart();
            issued_book.setText(this.numberOfBookBorrowed + "");
        }));
        task.setOnFailed(e -> Platform.runLater(() -> {
            System.out.println(task.getException());
        }));
        new Thread(task).start();
    }

    private void loadChart() {
        ObservableList<PieChart.Data> pieChartData =
                FXCollections.observableArrayList(
                        new PieChart.Data("Borrowed Books", this.numberOfBookBorrowed),
                        new PieChart.Data("Books Not Yet Borrowed", this.numberOfBook - this.numberOfBookBorrowed)
                );
        pieChartData.forEach(data ->
                data.nameProperty().bind(
                        Bindings.concat(data.getName(), " amount: ", (int)data.getPieValue())));

        this.chartBookIssue.getData().addAll(pieChartData);
    }


    public void loadTopBorrowedBook() {
            Task<ObservableList<Book>> task = BookService.getInstance().top6BookMostBorrowed();

            task.setOnSucceeded(e -> Platform.runLater(() -> {
                bookTop6 = task.getValue();
                for(int i = 0; i < bookTop6.size(); i++) {
                    Book book = bookTop6.get(i);
                    String title = book.getTitle();
                    Image image = new Image(book.getImageUrl());
                    switch (i) {
                        case 0:
                            imageTop1.setImage(image);
                            top1Text.setText(title);
                            break;
                        case 1:
                            ImageTop2.setImage(image);
                            top2Text.setText(title);
                            break;
                        case 2:
                            ImageTop3.setImage(image);
                            top3Text.setText(title);
                            break;
                        case 3:
                            ImageTop4.setImage(image);
                            top4Text.setText(title);
                            break;
                        case 4:
                            ImageTop5.setImage(image);
                            top5Text.setText(title);
                            break;
                        case 5:
                            ImageTop6.setImage(image);
                            top6Text.setText(title);
                            break;
                        default:
                            break;
                    }
                }
                loadTotalBook();
            }));
            task.setOnFailed(e -> Platform.runLater(() -> {
                System.out.println(task.getException());
            }));
            new Thread(task).start();
    }

    public void loadTopBorrower() {
        tableOfTopBorrower.setSpacing(15);
        tableOfTopBorrower.setStyle("-fx-border-color: #2A2A2A");
        tableOfTopBorrower.setStyle("-fx-background-color: #B1DCB8");
        HBox header = createRow("Username", "Email", "Number of books borrowed");
        header.setStyle("-fx-background-color: #A4ABEE");
        tableOfTopBorrower.getChildren().add(header);

        Task<ObservableList<User>> task = BookService.getInstance().fetchTop5Borrower();
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            this.top5Users = task.getValue();
            tableOfTopBorrower.setPrefHeight(10*top5Users.size());
            for (int i = 0; i < this.top5Users.size(); i++) {
                User user = this.top5Users.get(i);
                HBox row = createRow(user.getUsername(), user.getEmail(), String.valueOf(user.getNumberOfBookBorrowed()));
                if (i % 2 == 0) {
                    row.setStyle("-fx-background-color: #E8C4BD;");
                } else {
                    row.setStyle("-fx-background-color: #D4E1F4;");
                }
                tableOfTopBorrower.getChildren().add(row);
            }
        }));

        task.setOnFailed(e->{
            System.err.println("Fail to create table with Vbox");
        });
        new Thread(task).start();
    }

    public HBox createRow(String col1, String col2, String col3) {
            HBox row = new HBox();
            row.setSpacing(10);

            Label column1 = new Label(col1);
            Label column2 = new Label(col2);
            Label column3 = new Label(col3);

            column1.setPrefWidth(100);
            column2.setPrefWidth(150);
            column3.setPrefWidth(200);

            column1.setStyle("-fx-alignment: CENTER;");
            column2.setStyle("-fx-alignment: CENTER;");
            column3.setStyle("-fx-alignment: CENTER;");

            row.getChildren().addAll(column1, column2, column3);
            return row;
    }

}