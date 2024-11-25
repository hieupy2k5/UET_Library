package org.example.uet_library.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ResourceBundle;

public class HomeController implements Initializable {

    @FXML
    public RadioButton radioButton_Document;

    @FXML
    public RadioButton radioButton_Author;

    @FXML
    public RadioButton radioButton_Publisher;

    @FXML
    public RadioButton radioButton_Category;

    @FXML
    public javafx.scene.control.TextField TextField;

    @FXML
    public Button button_Search;

    @FXML
    public Label label_Search;

    @FXML
    public Label label_Error;

    @FXML
    public ImageView imageView_Search;

    public ToggleGroup group;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        group = new ToggleGroup();
        radioButton_Author.setToggleGroup(group);
        radioButton_Publisher.setToggleGroup(group);
        radioButton_Category.setToggleGroup(group);
        radioButton_Document.setToggleGroup(group);

        radioButton_Document.setSelected(true);
        label_Error.setVisible(false);

        group.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                RadioButton selectedRadioButton = (RadioButton) newValue;
                String selectedText = selectedRadioButton.getText();
                TextField.setPromptText("Enter " + selectedText + " name");
            }
        });
    }

    @FXML
    public void handleSearch() {
        String searchText = TextField.getText();

        if (searchText == null || searchText.trim().isEmpty()) {
            label_Error.setVisible(true);
        } else {
            label_Error.setVisible(false);
            RadioButton selectedRadioButton = (RadioButton) group.getSelectedToggle();
            String searchCategory = selectedRadioButton.getText();
            System.out.println("Searching for " + searchText + " in category " + searchCategory);
        }
    }
}
