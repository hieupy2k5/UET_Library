package org.example.uet_library.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class UsersController {

    @FXML
    public Label usersLabel;

    @FXML
    public void initialize() {
        usersLabel.setText("Users View");
    }
}
