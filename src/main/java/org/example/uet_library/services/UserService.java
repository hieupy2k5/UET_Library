package org.example.uet_library.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.example.uet_library.database.Database;
import org.example.uet_library.enums.BookCheckResult;
import org.example.uet_library.models.Book;
import org.example.uet_library.models.Borrow;
import org.example.uet_library.models.Favor;
import org.example.uet_library.models.Request;
import org.example.uet_library.utilities.SessionManager;

public class UserService {

    private static UserService instance;

    private UserService() {
    }

    public static UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }

        return instance;
    }



    /**
     * Fetch borrow records from database
     *
     * @return a list of borrow records
     */
    public Task<ObservableList<Borrow>> fetchBorrowFromDB() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return new Task<>() {
            @Override
            protected ObservableList<Borrow> call() throws Exception {
                ObservableList<Borrow> borrowList = FXCollections.observableArrayList();
                Database connection = new Database();
                int userID = SessionManager.getInstance().getUserId();
                try (Connection conDB = connection.getConnection()) {
                    String query =
                        "SELECT borrow.*, books.title, books.author, books.category, books.image_url "
                            +
                            "FROM borrow " +
                            "JOIN books ON borrow.book_id = books.ISBN " +
                            "WHERE borrow.user_id = ?";
                    PreparedStatement preparedStatement = conDB.prepareStatement(query);
                    preparedStatement.setInt(1, userID);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()) {
                        int borrowID = resultSet.getInt("id");
                        String isbn = resultSet.getString("book_id");
                        int quantity = resultSet.getInt("quantity");
                        String title = resultSet.getString("title");
                        String author = resultSet.getString("author");
                        String category = resultSet.getString("category");
                        String image = resultSet.getString("image_url");
                        Date borrowTimestamp = resultSet.getTimestamp("borrow_date");
                        Date returnTimestamp = resultSet.getTimestamp("return_date");

                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                        String formattedBorrowDate = sdf.format(borrowTimestamp);
                        String formattedReturnDate =
                            (returnTimestamp != null) ? sdf.format(returnTimestamp) : "N/A";

                        String status = resultSet.getString("status");
                        Borrow borrow = new Borrow(borrowID, isbn, title, author, category,
                            quantity,
                            formattedBorrowDate, formattedReturnDate, status, image);
                        borrowList.add(borrow);
                    }

                } catch (SQLException e) {

                    System.err.println("Error fetching borrow from database: " + e.getMessage());
                    throw new Exception("Database query failed",
                        e); // Re-throw with cause for chaining
                }

                return borrowList;
            }
        };
    }


    public Task<ObservableList<Request>> fetchMyRequestFromDB() {
        return new Task<>() {
            @Override
            protected ObservableList<Request> call() throws Exception {
                ObservableList<Request> myRequestList = FXCollections.observableArrayList();
                int userID = SessionManager.getInstance().getUserId();
                Database connection = new Database();
                try (Connection conDB = connection.getConnection()) {
                    String query =
                        "SELECT requests.*, books.ISBN, books.title, books.author, books.image_url "
                            +
                            "FROM requests " +
                            "JOIN books ON requests.book_id = books.ISBN " +
                            "WHERE requests.user_id = ?";
                    PreparedStatement preparedStatement = conDB.prepareStatement(query);
                    preparedStatement.setInt(1, userID);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()) {
                        int request_id = resultSet.getInt("id");
                        String book_id = resultSet.getString("book_id");
                        String title = resultSet.getString("title");
                        String author = resultSet.getString("author");
                        String status = resultSet.getString("status");
                        String image = resultSet.getString("image_url");

                        Request request = new Request(request_id, book_id, title, author, status,
                            image);
                        myRequestList.add(request);
                    }

                } catch (SQLException e) {

                    System.err.println("Error fetching borrow from database: " + e.getMessage());
                    throw new Exception("Database query failed",
                        e); // Re-throw with cause for chaining
                }

                return myRequestList;
            }
        };
    }



    public boolean requestBook(int userId, String bookId, int requestedQuantity) {
        Database dbConnection = new Database();
        try (Connection conn = dbConnection.getConnection()) {
            String checkQuery = "SELECT quantity FROM books WHERE ISBN = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, bookId);
            ResultSet rs = checkStmt.executeQuery();

            // Sufficient # of books => Allow users to request
            if (rs.next() && rs.getInt("quantity") >= requestedQuantity) {
                // Record the requesting action
                String insertQuery = "INSERT INTO requests (user_id, book_id, quantity, status) VALUES (?, ?, ?, 'pending')";
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                insertStmt.setInt(1, userId);
                insertStmt.setString(2, bookId);
                insertStmt.setInt(3, requestedQuantity);
                insertStmt.executeUpdate();

                return true;
            } else {
                System.out.println("Error requesting book.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean returnBook(int borrowId, String bookId) {
        Database dbConnection = new Database();
        try (Connection conn = dbConnection.getConnection()) {
            // Add the return books to library
            String updateQuery = "UPDATE books SET quantity = quantity + (SELECT quantity FROM borrow WHERE id = ?) WHERE ISBN = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
            updateStmt.setInt(1, borrowId);
            updateStmt.setString(2, bookId);
            updateStmt.executeUpdate();

            // Update each selected borrow entry with the return date
            String returnQuery = "UPDATE borrow SET status = 'returned', return_date = CONVERT_TZ(NOW(), 'UTC', '+07:00') WHERE id = ?";
            PreparedStatement returnStmt = conn.prepareStatement(returnQuery);
            returnStmt.setInt(1, borrowId);
            returnStmt.executeUpdate();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean borrowBook(int requestId, String bookId) {
        Database dbConnection = new Database();
        try (Connection conn = dbConnection.getConnection()) {
            // Insert new borrow record
            int userId = SessionManager.getInstance().getUserId();
            String insertQuery = "INSERT INTO borrow (user_id, book_id, quantity, borrow_date, status) VALUES (?, ?, ?, CONVERT_TZ(NOW(), 'UTC', '+07:00'), 'borrowed')";
            PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
            insertStmt.setInt(1, userId);
            insertStmt.setString(2, bookId);
            insertStmt.setInt(3, 1);
            insertStmt.executeUpdate();

            // Delete accepted request after borrowing
            String deleteQuery = "DELETE FROM requests WHERE id = ?";
            PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);
            deleteStmt.setInt(1, requestId);
            deleteStmt.executeUpdate();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean requestAgain(int requestId) {
        Database dbConnection = new Database();
        try (Connection conn = dbConnection.getConnection()) {
            String updateQuery = "UPDATE requests SET status = 'pending' WHERE id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
            updateStmt.setInt(1, requestId);
            updateStmt.executeUpdate();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }


    public BookCheckResult isBookBorrowedOrRequested(String bookId) {
        Database db = new Database();
        try (Connection conn = db.getConnection()) {
            int userId = SessionManager.getInstance().getUserId();
            String query = """
                SELECT 'requested' AS status FROM requests WHERE book_id = ? AND user_id = ?
                UNION
                SELECT 'borrowed' AS status FROM borrow WHERE book_id = ? AND user_id = ? AND status = 'borrowed'
                """;
            PreparedStatement queryStmt = conn.prepareStatement(query);
            queryStmt.setString(1, bookId);
            queryStmt.setInt(2, userId);
            queryStmt.setString(3, bookId);
            queryStmt.setInt(4, userId);
            queryStmt.executeQuery();

            ResultSet rs = queryStmt.executeQuery();
            while (rs.next()) {
                if (rs.getString("status").equals("requested")) {
                    return BookCheckResult.ALREADY_REQUESTED;
                } else if (rs.getString("status").equals("borrowed")) {
                    return BookCheckResult.ALREADY_BORROWED;
                }
            }

            return BookCheckResult.CAN_BE_REQUESTED;
        } catch (SQLException e) {
            e.printStackTrace();
            return BookCheckResult.ERROR;
        }
    }

    public Task<ObservableList<Favor>> fetchFavorFromDB() {
        return new Task<>() {
            @Override
            protected ObservableList<Favor> call() throws Exception {
                ObservableList<Favor> favorList = FXCollections.observableArrayList();
                Database connection = new Database();
                int userID = SessionManager.getInstance().getUserId();
                try (Connection conDB = connection.getConnection()) {
                    String query =
                        "SELECT favors.*, books.title, books.author, books.category, books.image_url "
                            +
                            "FROM favors " +
                            "JOIN books ON favors.book_id = books.ISBN " +
                            "WHERE favors.user_id = ?";
                    PreparedStatement preparedStatement = conDB.prepareStatement(query);
                    preparedStatement.setInt(1, userID);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()) {
                        int favorID = resultSet.getInt("id");
                        String isbn = resultSet.getString("book_id");
                        String title = resultSet.getString("title");
                        String author = resultSet.getString("author");
                        String category = resultSet.getString("category");
                        String image = resultSet.getString("image_url");

                        Favor favor = new Favor(favorID, isbn, title, author, category, image);
                        favorList.add(favor);
                    }
                } catch (SQLException e) {
                    System.err.println("Error fetching favor from database: " + e.getMessage());
                    throw new Exception("Database query failed", e);
                }

                return favorList;
            }
        };
    }

    public boolean addBookToFavorites(Book book) {
        Database dbConnection = new Database();
        try (Connection conn = dbConnection.getConnection()) {
            String insertQuery =
                "INSERT INTO favors (user_id, book_id, title, author, image_url) " +
                    "VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(insertQuery);
            stmt.setInt(1, SessionManager.getInstance().getUserId());
            stmt.setString(2, book.getIsbn());
            stmt.setString(3, book.getTitle());
            stmt.setString(4, book.getAuthor());
            stmt.setString(5, book.getImageUrl());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
