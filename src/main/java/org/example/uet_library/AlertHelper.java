package org.example.uet_library;

import javafx.scene.control.Alert;

/**
 * For reusing Alerts. You guys (Viet and Hieu) can add more functions to suit your needs.
 */
public class AlertHelper {
    public static void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
