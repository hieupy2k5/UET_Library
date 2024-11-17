import static org.junit.jupiter.api.Assertions.assertEquals;

import org.example.uet_library.SessionManager;
import org.junit.jupiter.api.*;

class SessionManagerTest {

    @BeforeAll
    static void initAll() {
        System.out.println("Initialize resources before all tests.");
    }

    @BeforeEach
    void init() {
        System.out.println("Run before each test.");
    }

    @Test
    void testGetUserId() {
        SessionManager sessionManager = SessionManager.getInstance();
        sessionManager.setUserId(5);

        assertEquals(5, sessionManager.getUserId(), "User ID should match the set value.");
    }

    @AfterEach
    void tearDown() {
        System.out.println("Run after each test.");
    }

    @AfterAll
    static void tearDownAll() {
        System.out.println("Clean up resources after all tests.");
    }
}
