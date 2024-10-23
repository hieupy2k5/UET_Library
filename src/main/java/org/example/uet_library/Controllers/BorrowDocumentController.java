package org.example.uet_library.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class BorrowDocumentController {

    @FXML
    public Label borrowLabel;

    @FXML
    public void initialize() {
        borrowLabel.setText("Borrow Document View");
    }
}
