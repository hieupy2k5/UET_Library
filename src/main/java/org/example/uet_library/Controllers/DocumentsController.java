package org.example.uet_library.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DocumentsController {

    @FXML
    public Label documentsLabel;

    @FXML
    public void initialize() {
        documentsLabel.setText("Documents View");
    }
}
