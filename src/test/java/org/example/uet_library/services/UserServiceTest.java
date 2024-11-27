package org.example.uet_library.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.example.uet_library.database.Database;
import org.example.uet_library.enums.BookCheckResult;
import org.junit.jupiter.api.Test;

public class UserServiceTest extends ServiceTest {

    @Test
    void testRequestBookSuccess() {
        UserService userService = UserService.getInstance();
        int userId = 1;
        String isbn = "312335709";

        boolean result = userService.requestBook(userId, isbn, 1);

        assertTrue(result, "The book should be requested successfully.");
    }

    @Test
    void testRequestBookFailureDueToInvalidBookId() {
        UserService userService = UserService.getInstance();
        int userId = 1;
        String isbn = "someStupidID";
        int quantity = 1;

        boolean result = userService.requestBook(userId, isbn, quantity);

        assertFalse(result, "The requesting should fail due to invalid book id.");
    }

    @Test
    void testRequestBookFailureDueToInsufficientQuantity() {
        UserService userService = UserService.getInstance();
        int userId = 1;
        String isbn = "12345";
        int quantity = 1000;

        boolean result = userService.requestBook(userId, isbn, quantity);

        assertFalse(result, "The requesting should fail due to insufficient quantity.");
    }

    @Test
    void testReturnBookSuccess() throws SQLException {
        Database db = new Database();
        Connection conn = db.getConnection();
        String insertRequest = """
            INSERT INTO borrow VALUES (1, '312335709', 1, 1, NOW(), null, 'borrowed');
            """;
        PreparedStatement stmt = conn.prepareStatement(insertRequest);
        stmt.executeUpdate();

        boolean result = UserService.getInstance().returnBook(1, "312335709");

        assertTrue(result, "The book should be returned successfully.");
    }

    @Test
    void testReturnBookFailureDueToInvalidBookId() throws SQLException {
        Database db = new Database();
        Connection conn = db.getConnection();
        String insertRequest = """
            INSERT INTO borrow VALUES (1, '312335709', 1, 1, NOW(), null, 'borrowed');
            """;
        PreparedStatement stmt = conn.prepareStatement(insertRequest);
        stmt.executeUpdate();

        boolean result = UserService.getInstance().returnBook(1, "1234569");

        assertFalse(result, "The book should not be returned due to invalid book id.");
    }

    @Test
    void testRequestAgainSuccess() {
        boolean result = UserService.getInstance().requestAgain(1);

        assertTrue(result, "The book should be returned successfully.");
    }

    @Test
    void testIsBookBorrowedOrRequestedCanBeRequested() {
        BookCheckResult result = UserService.getInstance().isBookBorrowedOrRequested("312335709");

        assertEquals(BookCheckResult.CAN_BE_REQUESTED, result);
    }

    @Test
    void testIsBookBorrowedOrRequestedAlreadyBorrowed() throws SQLException {
        Database db = new Database();
        Connection conn = db.getConnection();

        String query = "INSERT INTO borrow VALUES (1, '312335709', 1, 1, NOW(), null, 'borrowed')";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.executeUpdate();

        BookCheckResult result = UserService.getInstance().isBookBorrowedOrRequested("312335709");

        assertEquals(BookCheckResult.ALREADY_BORROWED, result);
    }

    @Test
    void testIsBookBorrowedOrRequestedAlreadyRequested() throws SQLException {
        Database db = new Database();
        Connection conn = db.getConnection();

        String query = "INSERT INTO requests VALUES (1, 1, '312335709', 1, 'pending')";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.executeUpdate();

        BookCheckResult result = UserService.getInstance().isBookBorrowedOrRequested("312335709");

        assertEquals(BookCheckResult.ALREADY_REQUESTED, result);
    }

    @Override
    void setUpDB() throws SQLException {
        Database db = new Database();
        Connection conn = db.getConnection();

        String booksQuery = """
            INSERT INTO books VALUES ('312335709', '', '', 2000, 11, '', null, null, null, NOW(), '');
            """;
        String usersQuery = """
            INSERT INTO users VALUES (1,'test','test','test','test','test');
            """;

        PreparedStatement booksStmt = conn.prepareStatement(booksQuery);
        booksStmt.executeUpdate();

        PreparedStatement usersStmt = conn.prepareStatement(usersQuery);
        usersStmt.executeUpdate();
    }
}