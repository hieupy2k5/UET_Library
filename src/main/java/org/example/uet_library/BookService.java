package org.example.uet_library;

import com.mysql.cj.Session;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.json.JSONArray;


/**
 * Every operation on books are here.
 */
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
                String queryInsert = "INSERT INTO books(ISBN, title, author, year_published, image_url, quantity, category, qrcode, book_link) values(?,?,?,?,?,?,?,?,?)";
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

                    System.err.println("Error fetching books from database: " + e.getMessage());
                    throw new Exception("Database query failed",
                        e); // Re-throw with cause for chaining
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
                        int year = resultSet.getInt("yearpublished"); // Use alias for clarity
                        String isbn = resultSet.getString("ISBN");
                        String imageUrl = resultSet.getString("ImageUrl");
                        int quantity = resultSet.getInt("quantity");
                        String type = resultSet.getString("category");
                        Book book = new Book(title, author, isbn, imageUrl, year, type, quantity);
                        book.setqrCode(resultSet.getBytes("QRCODE"));
                        bookList.add(book);
                    }

                } catch (SQLException e) {

                    System.err.println("Error fetching books from database: " + e.getMessage());
                    throw new Exception("Database query failed",
                        e);
                }

                return bookList;
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

    // Return true => Borrow successfully, else => Something has gone wrong (not our faults ;) )
    public boolean borrowBook(int userId, int bookId, int quantity) {
        Database dbConnection = new Database();
        try (Connection conn = dbConnection.getConnection()) {
            String checkQuery = "SELECT quantity FROM book WHERE ISBN = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setInt(1, bookId);
            ResultSet rs = checkStmt.executeQuery();

            // Sufficient # of books => Allow users to borrow
            if (rs.next() && rs.getInt("quantity") >= quantity) {
                // Update # of books in db after borrowing
                String updateQuery = "UPDATE book SET quantity = book.quantity - ? WHERE ISBN = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setInt(1, quantity);
                updateStmt.setInt(2, bookId);
                updateStmt.executeUpdate();

                // Record the borrowing action
                String insertQuery = "INSERT INTO borrow (user_id, book_id, borrow_date, status) VALUES (?, ?, NOW(), 'borrowed')";
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                insertStmt.setInt(1, userId);
                insertStmt.setInt(2, bookId);
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

    // Return books (For responsible readers who do not steal books)
    public boolean returnBook(int userId, int bookId, int quantity) {
        Database dbConnection = new Database();
        try (Connection conn = dbConnection.getConnection()) {
            // Increment # of books in db
            String updateQuery = "UPDATE book SET quantity = quantity + ? WHERE ISBN = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
            updateStmt.setInt(1, quantity);
            updateStmt.setInt(2, bookId);
            updateStmt.executeUpdate();

            // Update the borrow record
            String returnQuery = "UPDATE borrow SET status = 'returned', return_date = NOW() WHERE user_id = ? AND book_id = ? AND status = 'borrowed' LIMIT ?";
            // Why auto-formatting doesn't work with the above line??? It is too longgggggg ToT
            PreparedStatement returnStmt = conn.prepareStatement(returnQuery);
            returnStmt.setInt(1, userId);
            returnStmt.setInt(2, bookId);
            returnStmt.setInt(3, quantity); // Only update the specified # of return books
            returnStmt.executeUpdate();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


}
