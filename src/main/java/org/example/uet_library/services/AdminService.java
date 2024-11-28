package org.example.uet_library.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.example.uet_library.apis.QRGenerateAPI;
import org.example.uet_library.database.Database;
import org.example.uet_library.enums.RequestResult;
import org.example.uet_library.models.Book;
import org.example.uet_library.models.Request;
import org.example.uet_library.models.User;
import org.example.uet_library.utilities.SessionManager;

public class AdminService {

    private static AdminService instance;

    private AdminService() {
    }

    public static AdminService getInstance() {
        if (instance == null) {
            instance = new AdminService();
        }
        return instance;
    }

    /**
     * Adds a new book to the database with its details and generates a QR code for it.
     * @param book the book object containing all the details to be added.
     * @return a Task that performs the database operation asynchronously.
     */
    public Task<Void> addBook(Book book) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                Database connection = new Database();
                String queryInsert = "INSERT INTO books (ISBN, Title, Author, year_published, image_url, quantity, category, QRCODE, book_link, description) values(?,?,?,?,?,?,?,?,?,?)";
                byte[] qr = QRGenerateAPI.getInstance().generateQRCode(book.getInfoBookLink());
                try (Connection conDB = connection.getConnection();) {
                    PreparedStatement ps = conDB.prepareStatement(queryInsert);
                    ps.setString(1, book.getIsbn());
                    ps.setString(2, book.getTitle());
                    ps.setString(3, book.getAuthor());
                    ps.setInt(4, book.getYear());
                    ps.setString(5, book.getImageUrl());
                    ps.setInt(6, book.getQuantity());
                    ps.setString(7, book.getCategory());
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

    /**
     * Deletes a book from the database based on its ISBN.
     * @param ISBN the ISBN of the book to be deleted.
     * @return a Task that returns true if the book was successfully deleted, otherwise false.
     */
    public Task<Boolean> deleteBook(String ISBN) {
        return new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                Database db = new Database();
                try (Connection conDB = db.getConnection()) {
                    String deleteBook = "DELETE FROM books WHERE ISBN = ?";
                    PreparedStatement deleteBookStmt = conDB.prepareStatement(deleteBook);
                    deleteBookStmt.setString(1, ISBN);
                    deleteBookStmt.executeUpdate();

                    return true;
                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        };
    }

    /**
     * Checks if a book with the given ISBN exists in the database.
     * @param isbn the ISBN of the book to check.
     * @return true if the book exists, otherwise false.
     */
    public boolean isBookExisting(String isbn) {
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

    /**
     * Fetches a list of books from the database that match the provided ISBN or Title (partial match).
     * @param ISBN the search term used to match the ISBN or Title.
     * @return a Task that returns an ObservableList of Book objects matching the search criteria.
     */
    public Task<ObservableList<Book>> fetchBookFromDB(String ISBN) {
        return new Task<>() {
            @Override
            protected ObservableList<Book> call() throws Exception {
                ObservableList<Book> bookList = FXCollections.observableArrayList();
                Database connection = new Database();
                try (Connection conDB = connection.getConnection()) {

                    String query = "SELECT * FROM books WHERE ISBN LIKE ? OR Title LIKE ?";
                    PreparedStatement preparedStatement = conDB.prepareStatement(query);
                    preparedStatement.setString(1, "%" + ISBN + "%");
                    preparedStatement.setString(2, "%" + ISBN + "%");
                    ResultSet resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()) {
                        String title = resultSet.getString("Title");
                        String author = resultSet.getString("Author");
                        int year = resultSet.getInt("year_published");
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
                    System.err.println(
                        "Error fetching books in fetchBookFromDB(String ISBN) (BookService.java): "
                            + e.getMessage());
                    throw new Exception("Database query failed",
                        e); // Re-throw with cause for chaining
                }

                return bookList;
            }
        };
    }

    /**
     * Fetches all users from the database.
     * @return a Task that returns an ObservableList of User objects.
     */
    public Task<ObservableList<User>> fetchUserFromDB() {
        return new Task<>() {
            @Override
            protected ObservableList<User> call() throws Exception {
                ObservableList<User> userList = FXCollections.observableArrayList();
                Database connection = new Database();
                try (Connection conDB = connection.getConnection()) {
                    String query = "SELECT * FROM users";
                    PreparedStatement preparedStatement = conDB.prepareStatement(query);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()) {
                        int userID = resultSet.getInt("id");
                        String username = resultSet.getString("username");
                        String email = resultSet.getString("email");
                        String firstName = resultSet.getString("first_name");
                        String lastName = resultSet.getString("last_name");
                        String fullName = firstName + " " + lastName;

                        User user = new User(userID, username, firstName, lastName, email);
                        userList.add(user);
                    }

                } catch (SQLException e) {

                    System.err.println("Error fetching borrow from database: " + e.getMessage());
                    throw new Exception("Database query failed",
                        e); // Re-throw with cause for chaining
                }

                return userList;
            }
        };
    }

    /**
     * Fetches all pending user requests from the database and includes details about the user and book.
     * @return a Task that returns an ObservableList of Request objects.
     */
    public Task<ObservableList<Request>> fetchUserRequestFromDB() {
        return new Task<>() {
            @Override
            protected ObservableList<Request> call() throws Exception {
                ObservableList<Request> myRequestList = FXCollections.observableArrayList();
                int userID = SessionManager.getInstance().getUserId();
                Database connection = new Database();
                try (Connection conDB = connection.getConnection()) {
                    String query =
                        "SELECT requests.*, users.username, users.id, books.ISBN, books.title, books.author, books.image_url "
                            +
                            "FROM requests " +
                            "JOIN books ON requests.book_id = books.ISBN " +
                            "JOIN users ON requests.user_id = users.id " +
                            "WHERE requests.status = 'pending'";
                    PreparedStatement preparedStatement = conDB.prepareStatement(query);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()) {
                        int request_id = resultSet.getInt("id");
                        int user_id = resultSet.getInt("user_id");
                        String book_id = resultSet.getString("book_id");
                        String username = resultSet.getString("username");
                        String title = resultSet.getString("title");
                        String author = resultSet.getString("author");
                        String status = resultSet.getString("status");
                        String image = resultSet.getString("image_url");

                        Request request = new Request(request_id, user_id, book_id, username, title,
                            author,
                            status, image);
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

    /**
     * Updates the details of an existing book in the database.
     * @param book the Book object containing updated information.
     * @return a Task that performs the update operation asynchronously.
     */
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
                    preparedStatement.setString(5, book.getCategory());
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
     * Accepts a user request for a book, updates the request status, and decreases the book quantity.
     * @param userId the ID of the user making the request.
     * @param bookId the ISBN of the book being requested.
     * @return a RequestResult indicating the result of the operation (ACCEPTED, INVALID_INFO, or ERROR).
     */
    public RequestResult acceptRequest(int userId, String bookId) {
        Database dbConnection = new Database();
        try (Connection conn = dbConnection.getConnection()) {
            // Change the request status to accepted
            String updateQuery = "UPDATE requests SET status = 'accepted' WHERE user_id = ? AND book_id = ? AND status = 'pending'";
            PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
            updateStmt.setInt(1, userId);
            updateStmt.setString(2, bookId);
            int cnt = updateStmt.executeUpdate();
            if (cnt == 0) {
                return RequestResult.INVALID_INFO;
            }

            // Decrement book amount from stock
            String decrementQuery = "UPDATE books SET quantity = books.quantity - 1 WHERE ISBN = ?";
            PreparedStatement decrementStmt = conn.prepareStatement(decrementQuery);
            decrementStmt.setString(1, bookId);
            decrementStmt.executeUpdate();

            return RequestResult.ACCEPTED;
        } catch (SQLException e) {
            e.printStackTrace();
            return RequestResult.ERROR;
        }
    }

    /**
     * Denies a user request for a book and updates the request status to 'denied.'
     * @param userId the ID of the user making the request.
     * @param bookId the ISBN of the book being requested.
     * @return a RequestResult indicating the result of the operation (DENIED, INVALID_INFO, or ERROR).
     */
    public RequestResult denyRequest(int userId, String bookId) {
        Database dbConnection = new Database();
        try (Connection conn = dbConnection.getConnection()) {
            // Change the request status to denied
            String updateQuery = "UPDATE requests SET status = 'denied' WHERE user_id = ? AND book_id = ? AND status = 'pending'";
            PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
            updateStmt.setInt(1, userId);
            updateStmt.setString(2, bookId);
            int cnt = updateStmt.executeUpdate();
            if (cnt == 0) {
                return RequestResult.INVALID_INFO;
            }

            return RequestResult.DENIED;
        } catch (SQLException e) {
            e.printStackTrace();
            return RequestResult.ERROR;
        }
    }

    /**
     * Deletes a user from the database based on their user ID.
     * @param userId the ID of the user to be deleted.
     * @return true if the user was successfully deleted, otherwise false.
     */
    public boolean deleteUser(int userId) {
        Database dbConnection = new Database();
        try (Connection conn = dbConnection.getConnection()) {
            // Delete user
            String deleteQuery = "DELETE FROM users WHERE id = ?";
            PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);
            deleteStmt.setInt(1, userId);
            int cnt = deleteStmt.executeUpdate();

            return cnt > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves the quantity of a book available for a specific request.
     * @param requestId the ID of the request.
     * @return the quantity of the book if found, otherwise null.
     */
    public Integer bookQuantityForRequest(int requestId) {
        Database dbConnection = new Database();
        try (Connection conn = dbConnection.getConnection()) {
            String query = "SELECT b.quantity " +
                "FROM books b " +
                "INNER JOIN requests r ON b.ISBN = r.book_id " +
                "WHERE r.id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, requestId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("quantity");
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Loads the total number of users from the database.
     * @return a Task that returns the total number of users as an Integer.
     */
    public Task<Integer> loadNumberOfUser() {
        return new Task<>() {
            @Override
            protected Integer call() throws Exception {
                Database db = new Database();
                try (Connection connection = db.getConnection()) {
                    String query = "SELECT COUNT(*) FROM users";
                    try (PreparedStatement ps = connection.prepareStatement(query);
                        ResultSet resultSet = ps.executeQuery()) {
                        if (resultSet.next()) {
                            return resultSet.getInt("COUNT(*)");
                        }
                    } catch (SQLException ex) {
                        System.err.println("Fail to load data");
                    }
                }
                return 0;
            }
        };
    }

    /**
     * Retrieves the total number of distinct books that have been borrowed from the database.
     * @return a Task that returns the total number of borrowed books as an Integer.
     */
    public Task<Integer> fetchNumberOfBookBorrowed() {
        return new Task<>() {

            @Override
            protected Integer call() throws Exception {
                try {
                    Database db = new Database();
                    Connection connection = db.getConnection();
                    String query = "SELECT COUNT(DISTINCT(book_id)) AS totalBorrowed FROM borrow";
                    PreparedStatement preparedStatement = connection.prepareStatement(query);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                        return resultSet.getInt("totalBorrowed");
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                return 0;
            }
        };
    }

    /**
     * Retrieves the top 6 most borrowed books along with their titles and image URLs.
     * @return a Task that returns an ObservableList of Book objects representing the top borrowed books.
     */
    public Task<ObservableList<Book>> top6BookMostBorrowed() {
        return new Task<>() {
            @Override
            protected ObservableList<Book> call() throws Exception {
                ObservableList<Book> bookList = FXCollections.observableArrayList();
                Database connection = new Database();
                try (Connection conDB = connection.getConnection()) {
                    String query = "SELECT books.title, image_url, COUNT(ISBN) AS TONG\n" +
                        "from books\n" +
                        "INNER JOIN borrow\n" +
                        "ON books.ISBN = borrow.book_id\n" +
                        "GROUP BY ISBN\n" +
                        "ORDER BY TONG DESC\n" +
                        "LIMIT 6;";
                    PreparedStatement preparedStatement = conDB.prepareStatement(query);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()) {
                        String title = resultSet.getString("title");
                        String imageUrl = resultSet.getString("image_url");
                        Book book = new Book();
                        book.setTitle(title);
                        book.setImageUrl(imageUrl);
                        bookList.add(book);
                    }
                } catch (SQLException e) {
                    System.err.println("fail to load data");
                }
                return bookList;
            }
        };
    }

    /**
     * Fetches the top 5 users who have borrowed the most books.
     * @return a Task that returns an ObservableList of User objects, each including their borrowed book count.
     */
    public Task<ObservableList<User>> fetchTop5Borrower() {
        return new Task<>() {
            @Override
            protected ObservableList<User> call() throws Exception {
                ObservableList<User> userList = FXCollections.observableArrayList();
                Database connection = new Database();
                try (Connection conDB = connection.getConnection()) {
                    String query = "SELECT U.id,U.username,U.email,SUM(quantity) AS TONG\n" +
                        "FROM users U\n" +
                        "INNER JOIN borrow B\n" +
                        "ON U.id = B.user_id\n" +
                        "GROUP BY user_id\n" +
                        "ORDER BY TONG DESC\n" +
                        "LIMIT 5;\n";
                    Statement statement = conDB.createStatement();
                    ResultSet resultSet = statement.executeQuery(query);
                    while (resultSet.next()) {
                        String id = resultSet.getString("id");
                        String username = resultSet.getString("username");
                        String email = resultSet.getString("email");
                        int sumOfBookBorrowed = resultSet.getInt("TONG");
                        User user = new User(username, "", "", email);
                        user.setNumberOfBookBorrowed(sumOfBookBorrowed);
                        userList.add(user);
                    }
                }
                return userList;
            }
        };
    }
}
