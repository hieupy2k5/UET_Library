package org.example.uet_library.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.example.uet_library.database.Database;
import org.example.uet_library.enums.BookCheckResult;
import org.example.uet_library.models.Book;
import org.example.uet_library.models.Borrow;
import org.example.uet_library.models.Favor;
import org.example.uet_library.models.Request;
import org.example.uet_library.models.User;
import org.example.uet_library.utilities.SessionManager;


public class BookService {

    private static BookService instance;

    private BookService() {
    }

    public static BookService getInstance() {
        if (instance == null) {
            instance = new BookService();
        }
        return instance;
    }


    public Task<ObservableList<Book>> fetchBookFromDB() {
        return new Task<>() {
            @Override
            protected ObservableList<Book> call() throws Exception {
                ObservableList<Book> bookList = FXCollections.observableArrayList();
                Database connection = new Database();
                try (Connection conDB = connection.getConnection()) {

                    String query = "SELECT * FROM books";
                    PreparedStatement preparedStatement = conDB.prepareStatement(query);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()) {
                        String title = resultSet.getString("title");
                        String author = resultSet.getString("author");
                        int year = resultSet.getInt("year_published"); // Use alias for clarity
                        String isbn = resultSet.getString("isbn");
                        String imageUrl = resultSet.getString("image_url");
                        int quantity = resultSet.getInt("quantity");
                        String type = resultSet.getString("category");
                        String description = resultSet.getString("description");
                        Book book = new Book(title, author, isbn, imageUrl, year, type, quantity);
                        book.setDescription(description);
                        book.setqrCode(resultSet.getBytes("QRCODE"));
                        bookList.add(book);
                    }

                } catch (SQLException e) {
                    // Log the specific SQL exception for better debugging
                    System.err.println(
                        "Error fetching books in fetchBookFromDB() (BookService.java): "
                            + e.getMessage());
                    throw new Exception("Database query failed",
                        e); // Re-throw with cause for chaining
                }

                return bookList;
            }
        };
    }

    public Task<ObservableList<Book>> top5BookRecentlyAdded() {
        return new Task<>() {
            @Override
            protected ObservableList<Book> call() throws Exception {
                ObservableList<Book> bookList = FXCollections.observableArrayList();
                Database connection = new Database();
                try (Connection conDB = connection.getConnection()) {
                    // Use PreparedStatement for protection against SQL injection
                    String query = "SELECT * FROM books ORDER BY added_at DESC LIMIT 5";
                    PreparedStatement preparedStatement = conDB.prepareStatement(query);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()) {
                        String title = resultSet.getString("Title");
                        String author = resultSet.getString("Author");
                        int year = resultSet.getInt("year_published"); // Use alias for clarity
                        String isbn = resultSet.getString("ISBN");
                        String imageUrl = resultSet.getString("image_url");
                        int quantity = resultSet.getInt("quantity");
                        String type = resultSet.getString("category");
                        String description = resultSet.getString("description");
                        Book book = new Book(title, author, isbn, imageUrl, year, type, quantity);
                        book.setqrCode(resultSet.getBytes("qrcode"));
                        book.setDescription(description);
                        bookList.add(book);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return bookList;
            }
        };
    }

    public Task<Integer> fetchTotalBook() {
        return new Task<>() {
            @Override
            protected Integer call() throws Exception {
                Database connection = new Database();
                try (Connection conDB = connection.getConnection()) {
                    String query = "SELECT COUNT(*) FROM books";
                    PreparedStatement preparedStatement = conDB.prepareStatement(query);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()) {
                        return resultSet.getInt("COUNT(*)");
                    }

                    return -1;
                }
            }
        };
    }

    public Task<ObservableList<Book>> fetchBookForPage(Book bookCurrent) {
        return new Task<>() {
            @Override
            public ObservableList<Book> call() throws Exception {
                ObservableList<Book> bookList = FXCollections.observableArrayList();
                Database connection = new Database();
                try (Connection conDB = connection.getConnection()) {
                    // Use PreparedStatement for protection against SQL injection
                    String query = "SELECT * FROM books WHERE category = ? OR author = ? OR title LIKE ?";
                    PreparedStatement preparedStatement = conDB.prepareStatement(query);
                    preparedStatement.setString(1, "%" + bookCurrent + "%");
                    preparedStatement.setString(2, "%" + bookCurrent.getAuthor() + "%");
                    preparedStatement.setString(3, "%" + bookCurrent.getTitle() + "%");
                    ResultSet resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()) {
                        String title = resultSet.getString("Title");
                        String author = resultSet.getString("Author");
                        int year = resultSet.getInt("year_published"); // Use alias for clarity
                        String isbn = resultSet.getString("ISBN");
                        String imageUrl = resultSet.getString("image_url");
                        int quantity = resultSet.getInt("quantity");
                        String type = resultSet.getString("category");
                        String description = resultSet.getString("description");
                        Book book = new Book(title, author, isbn, imageUrl, year, type, quantity);
                        book.setDescription(description);
                        book.setqrCode(resultSet.getBytes("qrcode"));
                        bookList.add(book);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return bookList;
            }
        };
    }

    /**
     * Use for popUp search
     *
     * @param keyword is bookTitle or Author.
     * @return ObservableList result.
     */
    public Task<ObservableList<Book>> fetchBookByTitleOrAuthor(String keyword) {
        return new Task<>() {
            @Override
            protected ObservableList<Book> call() throws Exception {
                ObservableList<Book> bookList = FXCollections.observableArrayList();
                Database connection = new Database();
                try (Connection conDB = connection.getConnection()) {
                    String query = "SELECT * FROM books WHERE title LIKE ? OR author LIKE ? LIMIT 3";
                    PreparedStatement preparedStatement = conDB.prepareStatement(query);

                    preparedStatement.setString(1, "%" + keyword + "%");
                    preparedStatement.setString(2, "%" + keyword + "%");

                    ResultSet resultSet = preparedStatement.executeQuery();

                    while (resultSet.next()) {
                        String title = resultSet.getString("title");
                        String author = resultSet.getString("author");
                        int year = resultSet.getInt("year_published");
                        String isbn = resultSet.getString("ISBN");
                        String imageUrl = resultSet.getString("image_url");
                        int quantity = resultSet.getInt("quantity");
                        String type = resultSet.getString("category");
                        String description = resultSet.getString("description");

                        Book book = new Book(title, author, isbn, imageUrl, year, type, quantity);
                        book.setDescription(description);
                        book.setqrCode(resultSet.getBytes("qrcode"));
                        bookList.add(book);
                    }
                } catch (SQLException e) {
                    System.err.println(
                        "Error fetching books by title or author from database: " + e.getMessage());
                    throw new Exception("Database query failed", e);
                }
                return bookList;
            }
        };
    }


    public boolean removeBookFromFavoritesByID(int favorId) {
        Database dbConnection = new Database();
        try (Connection conn = dbConnection.getConnection()) {
            String deleteQuery = "DELETE FROM favors WHERE id = ?";
            PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);
            deleteStmt.setInt(1, favorId);

            int rowsAffected = deleteStmt.executeUpdate();

            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeBookFromFavoritesByBookIDAndUserID(Book book) {
        Database dbConnection = new Database();
        try (Connection conn = dbConnection.getConnection()) {
            String deleteQuery = "DELETE FROM favors WHERE user_id = ? AND book_id = ?";
            PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);

            int userId = SessionManager.getInstance().getUserId();
            deleteStmt.setInt(1, userId);
            deleteStmt.setString(2, book.getIsbn());

            int rowsAffected = deleteStmt.executeUpdate();

            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Set<String> fetchFavoriteBooksByUserID(int userID) {
        Set<String> favoriteBooks = new HashSet<>();
        String query = "SELECT book_id FROM favors WHERE user_id = ?";
        Database dbConnection = new Database();

        try (Connection connection = dbConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, userID);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    favoriteBooks.add(resultSet.getString("book_id"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching favorite books: " + e.getMessage());
            e.printStackTrace();
        }

        return favoriteBooks;
    }
}