module org.example.uet_library {
    requires javafx.controls;
    requires javafx.fxml;
    requires okhttp3;
    requires java.sql;

    opens org.example.uet_library to javafx.fxml;
    exports org.example.uet_library;
}