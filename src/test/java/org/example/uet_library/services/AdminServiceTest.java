package org.example.uet_library.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.example.uet_library.database.Database;
import org.example.uet_library.enums.RequestResult;
import org.example.uet_library.models.Request;
import org.junit.jupiter.api.Test;

public class AdminServiceTest extends ServiceTest {

    @Test
    void testBookExists() {
        AdminService adminService = AdminService.getInstance();
        boolean result = adminService.isBookExisting("312335709");

        assertTrue(result, "The book should exist.");
    }

    @Test
    void testBookDoesNotExist() {
        AdminService adminService = AdminService.getInstance();
        boolean result = adminService.isBookExisting("NonExistentBook");

        assertFalse(result, "The book should not exist.");
    }

    @Test
    void testDeleteUserSuccess() {
        AdminService adminService = AdminService.getInstance();
        boolean result = adminService.deleteUser(1);

        assertTrue(result, "The user should be deleted successfully.");
    }

    @Test
    void testDeleteUserFailureDueToInvalidUserId() {
        AdminService adminService = AdminService.getInstance();
        boolean result = adminService.deleteUser(-1);

        assertFalse(result, "The user does not exist.");
    }

    @Test
    void testAcceptRequestSuccess() {
        AdminService adminService = AdminService.getInstance();
        RequestResult result = adminService.acceptRequest(1, "312335709");

        assertEquals(RequestResult.ACCEPTED, result);
    }

    @Test
    void testAcceptRequestFailureDueToInvalidUserId() {
        AdminService adminService = AdminService.getInstance();
        RequestResult result = adminService.acceptRequest(-1, "312335709");

        assertEquals(RequestResult.INVALID_INFO, result);
    }

    @Test
    void testAcceptRequestFailureDueToInvalidBookId() {
        AdminService adminService = AdminService.getInstance();
        RequestResult result = adminService.acceptRequest(1, "SomeInvalidBookId");

        assertEquals(RequestResult.INVALID_INFO, result);
    }

    @Test
    void testDenyRequestSuccess() {
        AdminService adminService = AdminService.getInstance();
        RequestResult result = adminService.denyRequest(1, "312335709");

        assertEquals(RequestResult.DENIED, result);
    }

    @Test
    void testDenyRequestFailureDueToInvalidUserId() {
        AdminService adminService = AdminService.getInstance();
        RequestResult result = adminService.denyRequest(-1, "312335709");

        assertEquals(RequestResult.INVALID_INFO, result);
    }

    @Test
    void testDenyRequestFailureDueToInvalidBookId() {
        AdminService adminService = AdminService.getInstance();
        RequestResult result = adminService.denyRequest(1, "SomeInvalidBookId");

        assertEquals(RequestResult.INVALID_INFO, result);
    }

    @Test
    void testGetBookQuantityForRequest() {
        AdminService adminService = AdminService.getInstance();
        Integer result = adminService.bookQuantityForRequest(1);

        assertEquals(11, result);
    }

    @Test
    void testGetBookQuantityForRequestFailureDueToInvalidRequestId() {
        AdminService adminService = AdminService.getInstance();
        Integer result = adminService.bookQuantityForRequest(-1);

        assertNull(result);
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
        String requestQuery = """
            INSERT INTO requests VALUES (1, 1, '312335709', 1, 'pending');
            """;

        PreparedStatement booksStmt = conn.prepareStatement(booksQuery);
        booksStmt.executeUpdate();

        PreparedStatement usersStmt = conn.prepareStatement(usersQuery);
        usersStmt.executeUpdate();

        PreparedStatement requestStmt = conn.prepareStatement(requestQuery);
        requestStmt.executeUpdate();
    }
}
