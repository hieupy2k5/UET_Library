package org.example.uet_library;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.json.JSONArray;

import javax.print.DocFlavor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


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
                String queryInsert = "INSERT INTO book(ISBN, Title, Author, yearpublished, imageUrl, quantity, category, QRCODE, bookLink) values(?,?,?,?,?,?,?,?,?)";
                byte[] qr = QRGenerateAPI.getInstance().generateQRCode(book.getInfoBookLink());
                try (Connection conDB = connection.getConnection();) {
                    PreparedStatement ps = conDB.prepareStatement(queryInsert);
                    ps.setString(1, book.getIsbn());
                    ps.setString(2, book.getTitle());
                    ps.setString(3, book.getAuthor());
                    ps.setInt(4,book.getYear());
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
                String queryDelete = "DELETE FROM book WHERE ISBN= ?";
                Database connection = new Database();
                try (Connection conDB =  connection.getConnection()) {
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
        String query = "SELECT COUNT(*) FROM book WHERE ISBN = ?";
        Database connection = new Database();
        try (Connection conDB = connection.getConnection()){
            PreparedStatement preparedStatement = conDB.prepareStatement(query);
            preparedStatement.setString(1, isbn);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) == 1;
            }
        }catch (SQLException e) {
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
                    // Use PreparedStatement for protection against SQL injection
                    String query = "SELECT * FROM book";
                    PreparedStatement preparedStatement = conDB.prepareStatement(query);

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
                    // Use PreparedStatement for protection against SQL injection
                    String query = "SELECT * FROM book wheel WHERE ISBN LIKE ?";
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
                    // Log the specific SQL exception for better debugging
                    System.err.println("Error fetching books from database: " + e.getMessage());
                    throw new Exception("Database query failed", e); // Re-throw with cause for chaining
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
                String query = "UPDATE book SET title = ?, author = ?, quantity = ?, yearpublished = ?, category = ? WHERE isbn LIKE ?";
                try(Connection conDB = database.getConnection()) {
                    PreparedStatement preparedStatement = conDB.prepareStatement(query);
                    preparedStatement.setString(1, book.getTitle());
                    preparedStatement.setString(2, book.getAuthor());
                    preparedStatement.setInt(3, book.getQuantity());
                    preparedStatement.setString(4, book.getYear()+"");
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

    public Task<ObservableList<Book>> top5BookRecentlyAdded() {
        return new Task<>() {
            @Override
            protected ObservableList<Book> call() throws Exception {
                ObservableList<Book> bookList = FXCollections.observableArrayList();
                Database connection = new Database();
                try (Connection conDB = connection.getConnection()) {
                    // Use PreparedStatement for protection against SQL injection
                    String query = "SELECT * FROM book ORDER BY added_date DESC LIMIT 5";
                    PreparedStatement preparedStatement = conDB.prepareStatement(query);

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
}
