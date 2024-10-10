module org.example.uet_library {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.uet_library to javafx.fxml;
    exports org.example.uet_library;
}