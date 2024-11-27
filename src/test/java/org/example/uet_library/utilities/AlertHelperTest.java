package org.example.uet_library.utilities;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import org.example.uet_library.TestHelper;

public class AlertHelperTest {

    @BeforeAll
    public static void setUpClass() {
        TestHelper.initToolkit(); // Ensure JavaFX is initialized
    }

    @Test
    public void testShowAlert_Error() {
        TestHelper.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            AlertHelper.showAlert(Alert.AlertType.ERROR, "Bold Text", "Light Text");

            assertEquals("ERROR", alert.getTitle());
            assertNull(alert.getHeaderText()); // Header should be null
            assertNotNull(alert.getDialogPane().getContent()); // Ensure content is present
        });
    }

    @Test
    public void testShowAlert_Information() {
        TestHelper.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            AlertHelper.showAlert(Alert.AlertType.INFORMATION, "Bold Info", "Light Info");

            assertEquals("Info", alert.getTitle());
            assertNull(alert.getHeaderText()); // Header should be null
            assertNotNull(alert.getDialogPane().getContent()); // Ensure content is present
        });
    }
}
