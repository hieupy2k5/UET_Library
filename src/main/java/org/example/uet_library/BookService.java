package org.example.uet_library;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.json.JSONArray;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.example.uet_library.Book;


public class BookService {
    private static BookService instance;
    private final BookAPI bookAPI = new BookAPI();

    private BookService() {
    }

    public static BookService getInstance() {
        if (instance == null) {
            instance = new BookService();
        }
        return instance;
    }

    public Task<JSONArray> searchBooks(String query, String filter) {
        return new Task<>() {
            @Override
            protected JSONArray call() throws Exception {
                return bookAPI.fetchBooks(query, filter);
            }
        };
    }

    public Task<Void> addBook(Book book) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                Database connection = new Database();
                String queryInsert = "INSERT INTO books(ISBN, Title, Author, year_published, image_url, quantity, category, QRCODE, book_link, description) values(?,?,?,?,?,?,?,?,?,?)";
                byte[] qr = QRGenerateAPI.getInstance().generateQRCode(book.getInfoBookLink());
                try (Connection conDB = connection.getConnection();) {
                    PreparedStatement ps = conDB.prepareStatement(queryInsert);
                    ps.setString(1, book.getIsbn());
                    ps.setString(2, book.getTitle());
                    ps.setString(3, book.getAuthor());
                    ps.setInt(4, book.getYear());
                    ps.setString(5, book.getImageUrl());
                    ps.setInt(6, book.getQuantity());
                    ps.setString(7, book.getType());
                    ps.setBytes(8, qr);
                    ps.setString(9, book.getInfoBookLink());
                    ps.setString(10, book.getDescription());
                    ps.execute();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

    public Task<Void> deleteBook(String ISBN) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                String queryDelete = "DELETE FROM books WHERE ISBN= ?";
                Database connection = new Database();
                try (Connection conDB = connection.getConnection()) {
                    PreparedStatement preparedStatement = conDB.prepareStatement(queryDelete);
                    preparedStatement.setString(1, ISBN);
                    preparedStatement.execute();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

    public boolean isExitsBook(String isbn) {
        String query = "SELECT COUNT(*) FROM books WHERE ISBN = ?";
        Database connection = new Database();
        try (Connection conDB = connection.getConnection()) {
            PreparedStatement preparedStatement = conDB.prepareStatement(query);
            preparedStatement.setString(1, isbn);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) == 1;
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return false;
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
                        Book book = new Book(title, author, isbn, imageUrl, year, type, quantity);
                        //book.setqrCode(resultSet.getBytes("QRCODE"));
                        bookList.add(book);
                    }

                } catch (SQLException e) {
                    // Log the specific SQL exception for better debugging
                    System.err.println("Error fetching books from database: " + e.getMessage());
                    throw new Exception("Database query failed", e); // Re-throw with cause for chaining
                }

                return bookList;
            }
        };
    }

    public Task<ObservableList<Book>> fetchBookFromDB(String ISBN) {
        return new Task<>() {
            @Override
            protected ObservableList<Book> call() throws Exception {
                ObservableList<Book> bookList = FXCollections.observableArrayList();
                Database connection = new Database();
                try (Connection conDB = connection.getConnection()) {

                    String query = "SELECT * FROM books WHERE ISBN LIKE ?";
                    PreparedStatement preparedStatement = conDB.prepareStatement(query);
                    preparedStatement.setString(1, "%" + ISBN + "%");
                    ResultSet resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()) {
                        String title = resultSet.getString("Title");
                        String author = resultSet.getString("Author");
                        int year = resultSet.getInt("year_published"); // Use alias for clarity
                        String isbn = resultSet.getString("ISBN");
                        String imageUrl = resultSet.getString("image_url");
                        int quantity = resultSet.getInt("quantity");
                        String type = resultSet.getString("category");
                        Book book = new Book(title, author, isbn, imageUrl, year, type, quantity);
                        book.setqrCode(resultSet.getBytes("QRCODE"));
                        bookList.add(book);
                    }

                } catch (SQLException e) {
                    // Log the specific SQL exception for better debugging
                    System.err.println("Error fetching books from database: " + e.getMessage());
                    throw new Exception("Database query failed", e); // Re-throw with cause for chaining
                }

                return bookList;
            }
        };
    }

    public Task<ObservableList<Borrow>> fetchBorrowFromDB() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return new Task<>() {
            @Override
            protected ObservableList<Borrow> call() throws Exception {
                ObservableList<Borrow> borrowList = FXCollections.observableArrayList();
                Database connection = new Database();
                int userID = SessionManager.getInstance().getUserId();
                try (Connection conDB = connection.getConnection()) {

                    String query = "SELECT borrow.*, books.title, books.author, books.category " +
                            "FROM borrow " +
                            "JOIN books ON borrow.book_id = books.ISBN " +
                            "WHERE borrow.user_id = ?";
                    PreparedStatement preparedStatement = conDB.prepareStatement(query);
                    preparedStatement.setInt(1, userID);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()) {
                        String isbn = resultSet.getString("book_id");
                        int quantity = resultSet.getInt("quantity");
                        String title = resultSet.getString("title");
                        String author = resultSet.getString("author");
                        String category = resultSet.getString("category");
                        Timestamp borrowTimestamp = resultSet.getTimestamp("borrow_date");
                        Timestamp returnTimestamp = resultSet.getTimestamp("return_date");

                        LocalDateTime borrow_date =
                                (borrowTimestamp != null) ? borrowTimestamp.toLocalDateTime() : null;
                        LocalDateTime return_date =
                                (returnTimestamp != null) ? returnTimestamp.toLocalDateTime() : null;

                        String status = resultSet.getString("status");
                        Borrow borrow = new Borrow(isbn, title, author, category, quantity,
                                borrow_date, return_date, status);
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

    public Task<Void> editBook(Book book) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                Database database = new Database();
                String query = "UPDATE books SET title = ?, author = ?, quantity = ?, year_published = ?, category = ? WHERE ISBN LIKE ?";
                try (Connection conDB = database.getConnection()) {
                    PreparedStatement preparedStatement = conDB.prepareStatement(query);
                    preparedStatement.setString(1, book.getTitle());
                    preparedStatement.setString(2, book.getAuthor());
                    preparedStatement.setInt(3, book.getQuantity());
                    preparedStatement.setString(4, book.getYear() + "");
                    preparedStatement.setString(5, book.getType());
                    preparedStatement.setString(6, book.getIsbn());
                    preparedStatement.execute();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

    /**
     * Return true => Borrow successfully, else => Something has gone wrong (not our faults ;) )
     */
    public boolean borrowBook(int userId, String bookId, int borrowedQuantity) {
        Database dbConnection = new Database();
        try (Connection conn = dbConnection.getConnection()) {
            String checkQuery = "SELECT quantity FROM books WHERE ISBN = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, bookId);
            ResultSet rs = checkStmt.executeQuery();

            // Sufficient # of books => Allow users to borrow
            if (rs.next() && rs.getInt("quantity") >= borrowedQuantity) {
                // Update # of books in db after borrowing
                String updateQuery = "UPDATE books SET quantity = books.quantity - ? WHERE ISBN = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setInt(1, borrowedQuantity);
                updateStmt.setString(2, bookId);
                updateStmt.executeUpdate();

                // Record the borrowing action
                String insertQuery = "INSERT INTO borrow (user_id, book_id, quantity, borrow_date, status) VALUES (?, ?, ?, NOW(), 'borrowed')";
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                insertStmt.setInt(1, userId);
                insertStmt.setString(2, bookId);
                insertStmt.setInt(3, borrowedQuantity);
                insertStmt.executeUpdate();

                return true;
            } else { // Insufficient number of books => Tell users to get lost
                System.out.println("Not enough books available.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Return books (For responsible readers who do not steal books)
     */
    public boolean returnBook(int userId, String bookId, LocalDateTime borrowDate) {
        Database dbConnection = new Database();
        try (Connection conn = dbConnection.getConnection()) {
            // Add the return books to library
            String updateQuery = "UPDATE books SET quantity = quantity + (SELECT quantity FROM borrow WHERE user_id = ? AND book_id = ? AND status = 'borrowed' and borrow_date = ?) WHERE ISBN = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
            updateStmt.setInt(1, userId);
            updateStmt.setString(2, bookId);
            updateStmt.setString(3, borrowDate.toString());
            updateStmt.setString(4, bookId);
            updateStmt.executeUpdate();

            // Update each selected borrow entry with the return date
            String returnQuery = "UPDATE borrow SET status = 'returned', return_date = NOW() WHERE book_id = ? and borrow_date = ?";
            PreparedStatement returnStmt = conn.prepareStatement(returnQuery);
            returnStmt.setString(1, bookId);
            returnStmt.setString(2, borrowDate.toString());
            returnStmt.executeUpdate();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
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
    public Task<ObservableList<Book>> featchBookForPage(int start, int itemsPerPage) {
        return new Task<>() {
            @Override
            protected ObservableList<Book> call() throws Exception {
                ObservableList<Book> bookList = FXCollections.observableArrayList();
                Database connection = new Database();
                try (Connection conDB = connection.getConnection()) {
                    // Use PreparedStatement for protection against SQL injection
                    String query = "SELECT * FROM books LIMIT ? OFFSET ?";
                    PreparedStatement preparedStatement = conDB.prepareStatement(query);
                    preparedStatement.setInt(1, itemsPerPage);
                    preparedStatement.setInt(2, start);
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

    public Task<ObservableList<Book>> featchBookForPage(Book bookCurrent) {
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
}