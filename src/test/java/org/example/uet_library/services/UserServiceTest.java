package org.example.uet_library.services;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.example.uet_library.database.Config;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserServiceTest {

    @BeforeEach
    void setUp() {
        Config config = Config.getInstance();
        config.set("DATABASE_NAME", "library_test");
    }

    @AfterEach
    void tearDown() {
        Config config = Config.getInstance();
        config.reset();
    }

    @Test
    void testRequestBookSuccess() {
        UserService userService = UserService.getInstance();
        int userId = 1;
        String isbn = "0312335709";

        boolean result = userService.requestBook(userId, isbn, 1);

        assertTrue(result, "The book should be requested successfully.");
    }

    @Test
    void testBorrowBookFailureDueToInvalidBookId() {
        UserService userService = UserService.getInstance();
        int userId = 13;
        String isbn = "someStupidID";
        int quantity = 1;

        boolean result = userService.requestBook(userId, isbn, quantity);

        assertFalse(result, "The requesting should fail due to invalid book id.");
    }

    @Test
    void testBorrowBookFailureDueToInsufficientQuantity() {
        UserService userService = UserService.getInstance();
        int userId = 1;
        String isbn = "12345";
        int quantity = 1000;

        boolean result = userService.requestBook(userId, isbn, quantity);

        assertFalse(result, "The requesting should fail due to insufficient quantity.");
    }
}
