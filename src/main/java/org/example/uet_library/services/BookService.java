package org.example.uet_library.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.example.uet_library.database.Database;
import org.example.uet_library.enums.BookCheckResult;
import org.example.uet_library.models.Book;
import org.example.uet_library.utilities.SessionManager;

/**
 * Service class that handles all operations related to books, such as fetching books from the database,
 * managing favorites, recommendations, and other related tasks. This class follows the Singleton pattern.
 */
public class BookService {

    private static BookService instance;

    /**
     * Private constructor to enforce Singleton pattern.
     */
    private BookService() {
    }

    /**
     * Gets the single instance of the BookService.
     * @return the singleton instance of BookService.
     */
    public static BookService getInstance() {
        if (instance == null) {
            instance = new BookService();
        }
        return instance;
    }

    /**
     * Fetches all books from the database.
     * @return a Task that produces an ObservableList of all books.
     * @throws Exception if there is a database error.
     */
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
                        int year = resultSet.getInt("year_published");
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

    /**
     * Fetches the top 5 recently added books from the database, ordered by their addition date.
     * @return a Task that produces an ObservableList of the top 5 recently added books.
     * @throws Exception if there is a database error.
     */
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
                        int year = resultSet.getInt("year_published");
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

    /**
     * Fetches the total number of books in the database.
     * @return a Task that produces the total number of books as an Integer.
     * @throws Exception if there is a database error.
     */
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

    /**
     * Fetches recommended books based on the given book's title, author, and category.
     * @param bookCurrent the book for which recommendations are fetched.
     * @return a Task that produces an ObservableList of recommended books.
     * @throws Exception if there is a database error.
     */
    public Task<ObservableList<Book>> fetchRecommendations(Book bookCurrent) {
        return new Task<>() {
            @Override
            public ObservableList<Book> call() throws Exception {
                ObservableList<Book> recommendedList = FXCollections.observableArrayList();
                Database connection = new Database();
                try (Connection conDB = connection.getConnection()) {
                    String query = "SELECT * FROM books WHERE ISBN != ? AND " +
                        "(category = ? OR author = ? " +
                        "OR (Title LIKE ? OR Title LIKE ? OR Title LIKE ?)) " +
                        "ORDER BY " +
                        "(author = ?) DESC, " +
                        "(category = ?) DESC, " +
                        "(Title LIKE ? OR Title LIKE ? OR Title LIKE ?) DESC " +
                        "LIMIT 4";
                    PreparedStatement preparedStatement = conDB.prepareStatement(query);

                    String[] titleKeywords = bookCurrent.getTitle().split(" ");
                    String keyword1 =
                        "%" + (titleKeywords.length > 0 ? titleKeywords[0] : "") + "%";
                    String keyword2 =
                        "%" + (titleKeywords.length > 1 ? titleKeywords[1] : "") + "%";
                    String keyword3 =
                        "%" + (titleKeywords.length > 2 ? titleKeywords[2] : "") + "%";

                    // Set the parameters
                    preparedStatement.setString(1, bookCurrent.getIsbn());
                    preparedStatement.setString(2, bookCurrent.getCategory());
                    preparedStatement.setString(3, bookCurrent.getAuthor());
                    preparedStatement.setString(4, keyword1);
                    preparedStatement.setString(5, keyword2);
                    preparedStatement.setString(6, keyword3);
                    preparedStatement.setString(7, bookCurrent.getAuthor());
                    preparedStatement.setString(8, bookCurrent.getCategory());
                    preparedStatement.setString(9, keyword1);
                    preparedStatement.setString(10, keyword2);
                    preparedStatement.setString(11, keyword3);
                    ResultSet resultSet = preparedStatement.executeQuery();

//                    System.out.println("Current book:");
//                    System.out.println("ISBN: " + bookCurrent.getIsbn());
//                    System.out.println("Title: " + bookCurrent.getTitle());
//                    System.out.println("Author: " + bookCurrent.getAuthor());
//                    System.out.println("\n");
//                    System.out.println("Fetched books:");
                    while (resultSet.next()) {
                        String title = resultSet.getString("Title");
                        String author = resultSet.getString("Author");
                        int year = resultSet.getInt("year_published");
                        String isbn = resultSet.getString("ISBN");
                        String imageUrl = resultSet.getString("image_url");
                        int quantity = resultSet.getInt("quantity");
                        String type = resultSet.getString("category");
                        String description = resultSet.getString("description");
                        Book book = new Book(title, author, isbn, imageUrl, year, type, quantity);
                        book.setDescription(description);
                        book.setqrCode(resultSet.getBytes("qrcode"));
                        recommendedList.add(book);

//                        System.out.println("ISBN: " + resultSet.getString("ISBN"));
//                        System.out.println("Title: " + resultSet.getString("title"));
//                        System.out.println("Author: " + resultSet.getString("Author") + "\n");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return recommendedList;
            }

        };
    }

    /**
     * Searches for books in the database by title or author using a keyword.
     * @param keyword the keyword used for searching.
     * @return a Task that produces an ObservableList of books matching the keyword.
     * @throws Exception if there is a database error.
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

    /**
     * Removes a book from the favorites list based on the favorite ID.
     * @param favorId the ID of the favorite entry to be removed.
     * @return true if the book was removed successfully, false otherwise.
     */
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

    /**
     * Removes a book from the favorites list based on the book's ID and the current user's ID.
     * @param book the book to be removed from favorites.
     * @return true if the book was removed successfully, false otherwise.
     */
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

    /**
     * Fetches the list of favorite book IDs for a given user.
     * @param userID the ID of the user whose favorite books are being fetched.
     * @return a Set of book IDs that are marked as favorites by the user.
     */
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

    /**
     * Checks if a book is already borrowed or requested by the current user.
     * @param bookId the ID of the book being checked.
     * @return a BookCheckResult indicating whether the book is borrowed, requested, or can be requested.
     */
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
            queryStmt.setString(4, bookId);
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

    /**
     * Checks if a book is borrowed by any user.
     * @param bookId the ID of the book being checked.
     * @return a BookCheckResult indicating whether the book is borrowed or can be deleted.
     */
    public BookCheckResult isBookBorrowedByAnyone(String bookId) {
        Database db = new Database();
        try (Connection conn = db.getConnection()) {
            String query = "SELECT * FROM borrow WHERE book_id = ? AND status = 'borrowed'";
            PreparedStatement queryStmt = conn.prepareStatement(query);
            queryStmt.setString(1, bookId);
            queryStmt.executeQuery();
            ResultSet rs = queryStmt.executeQuery();
            while (rs.next()) {
                return BookCheckResult.ALREADY_BORROWED;
            }
            return BookCheckResult.CAN_BE_DELETED;
        } catch (SQLException e) {
            e.printStackTrace();
            return BookCheckResult.ERROR;
        }
    }
}