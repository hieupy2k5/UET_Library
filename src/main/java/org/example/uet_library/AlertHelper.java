package org.example.uet_library;

import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * For reusing Alerts. You guys (Viet and Hieu) can add more functions to suit your needs.
 */
public class AlertHelper {
    public static void showAlert(Alert.AlertType type, String boldText, String lightText) {
        Alert alert = new Alert(type);
        String header = switch (type) {
            case INFORMATION -> "Info";
            case WARNING -> "WARNING";
            case ERROR -> "ERROR";
            default -> "";
        };
        alert.setTitle(header);
        alert.setHeaderText(null);

        Label boldLabel = new Label(boldText);
        boldLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");

        Label lightLabel = new Label(lightText);
        lightLabel.setStyle("-fx-font-weight: normal; -fx-font-size: 13px;");

        VBox content = new VBox(5, boldLabel, lightLabel);
        content.setAlignment(Pos.CENTER_LEFT);

        alert.getDialogPane().setContent(content);

        alert.showAndWait();
    }

}
