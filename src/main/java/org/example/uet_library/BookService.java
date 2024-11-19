package org.example.uet_library;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import org.json.JSONArray;


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

    public Task<Boolean> deleteBook(String ISBN) {
        return new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                String queryDelete = "DELETE FROM books WHERE ISBN= ?";
                Database connection = new Database();
                try (Connection conDB = connection.getConnection()) {
                    PreparedStatement preparedStatement = conDB.prepareStatement(queryDelete);
                    preparedStatement.setString(1, ISBN);
                    preparedStatement.execute();
                } catch (SQLException e) {
                    return false;
                }
                return true;
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
                        book.setqrCode(resultSet.getBytes("QRCODE"));
                        bookList.add(book);
                    }

                } catch (SQLException e) {
                    // Log the specific SQL exception for better debugging
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

                    String query = "SELECT * FROM books WHERE ISBN LIKE ? OR Title LIKE ?";
                    PreparedStatement preparedStatement = conDB.prepareStatement(query);
                    preparedStatement.setString(1, "%" + ISBN + "%");
                    preparedStatement.setString(2, "%" + ISBN + "%");
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
                    throw new Exception("Database query failed",
                        e); // Re-throw with cause for chaining
                }

                return bookList;
            }
        };
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
                        String isbn = resultSet.getString("book_id");
                        int quantity = resultSet.getInt("quantity");
                        String title = resultSet.getString("title");
                        String author = resultSet.getString("author");
                        String category = resultSet.getString("category");
                        String image = resultSet.getString("image_url");
                        Timestamp borrowTimestamp = resultSet.getTimestamp("borrow_date");
                        Timestamp returnTimestamp = resultSet.getTimestamp("return_date");

                        LocalDateTime borrow_date =
                            (borrowTimestamp != null) ? borrowTimestamp.toLocalDateTime() : null;
                        LocalDateTime return_date =
                            (returnTimestamp != null) ? returnTimestamp.toLocalDateTime() : null;

                        String status = resultSet.getString("status");
                        Borrow borrow = new Borrow(isbn, title, author, category, quantity,
                            borrow_date, return_date, status, image);
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

                        User user = new User(username, firstName, lastName, email);
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

    public Task<ObservableList<Request>> fetchMyRequestFromDB() {
        return new Task<>() {
            @Override
            protected ObservableList<Request> call() throws Exception {
                ObservableList<Request> myRequestList = FXCollections.observableArrayList();
                int userID = SessionManager.getInstance().getUserId();
                Database connection = new Database();
                try (Connection conDB = connection.getConnection()) {
                    String query =
                        "SELECT requests.*, books.title, books.author, books.image_url "
                            +
                            "FROM requests " +
                            "JOIN books ON requests.book_id = books.ISBN " +
                            "WHERE requests.user_id = ?";
                    PreparedStatement preparedStatement = conDB.prepareStatement(query);
                    preparedStatement.setInt(1, userID);

                    ResultSet resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()) {
                        String title = resultSet.getString("title");
                        String author = resultSet.getString("author");
                        String status = resultSet.getString("status");

                        Request request = new Request(title, author, status);
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
                        int user_id = resultSet.getInt("user_id");
                        String book_id = resultSet.getString("book_id");
                        String username = resultSet.getString("username");
                        String title = resultSet.getString("title");
                        String author = resultSet.getString("author");
                        String status = resultSet.getString("status");

                        Request request = new Request(user_id, book_id, username, title, author,
                            status);
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

    public boolean requestBook(int userId, String bookId, int requestedQuantity) {
        Database dbConnection = new Database();
        try (Connection conn = dbConnection.getConnection()) {
            String checkQuery = "SELECT quantity FROM books WHERE ISBN = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, bookId);
            ResultSet rs = checkStmt.executeQuery();

            // Sufficient # of books => Allow users to request
            if (rs.next() && rs.getInt("quantity") >= requestedQuantity) {
                // Update # of books in db after requesting
                String updateQuery = "UPDATE books SET quantity = books.quantity - ? WHERE ISBN = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setInt(1, requestedQuantity);
                updateStmt.setString(2, bookId);
                updateStmt.executeUpdate();

                // Record the requesting action
                String insertQuery = "INSERT INTO requests (user_id, book_id, quantity, status) VALUES (?, ?, ?, 'pending')";
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                insertStmt.setInt(1, userId);
                insertStmt.setString(2, bookId);
                insertStmt.setInt(3, requestedQuantity);
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
     * For borrowing books.
     *
     * @param userId           is the id of the borrower.
     * @param bookId           is the book they want to borrow.
     * @param borrowedQuantity the quantity of books they want to borrow.
     * @return whether the book is borrowed successfully.
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
     * For returning books.
     *
     * @param userId     is the id of the current user.
     * @param bookId     is the book that the user wants to return.
     * @param borrowDate is the borrow date of that book.
     * @return whether the book is successfully returned.
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

    public boolean adminAcceptRequest(int userId, String bookId) {
        Database dbConnection = new Database();
        try (Connection conn = dbConnection.getConnection()) {
            // Change the request status to accepted
            String updateQuery = "UPDATE requests SET status = 'accepted' WHERE user_id = ? AND book_id = ? AND status = 'pending'";
            PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
            updateStmt.setInt(1, userId);
            updateStmt.setString(2, bookId);
            updateStmt.executeUpdate();

            // Borrow the book
            String insertQuery = "INSERT INTO borrow (user_id, book_id, quantity, borrow_date, status) VALUES (?, ?, ?, NOW(), 'borrowed')";
            PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
            insertStmt.setInt(1, userId);
            insertStmt.setString(2, bookId);
            insertStmt.setInt(3, 1);
            insertStmt.executeUpdate();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean adminDeclineRequest(int userId, String bookId) {
        Database dbConnection = new Database();
        try (Connection conn = dbConnection.getConnection()) {
            // Change the request status to declined
            String updateQuery = "UPDATE requests SET status = 'declined' WHERE user_id = ? AND book_id = ? AND status = 'pending'";
            PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
            updateStmt.setInt(1, userId);
            updateStmt.setString(2, bookId);
            updateStmt.executeUpdate();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public void userBorrowBook(String bookId) {
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

    public Task<ObservableList<Book>> fetchBookForPage(int start, int itemsPerPage) {
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
                        book.setImageLink(imageUrl);
                        bookList.add(book);
                    }
                } catch (SQLException e) {
                    System.err.println("fail to load data");
                }
                return bookList;
            }
        };
    }

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
                        User user = new User(username, "","",email);
                        user.setNumberOfBookBorrowed(sumOfBookBorrowed);
                        userList.add(user);
                    }
                }
                return userList;
            }
        };
    }
}