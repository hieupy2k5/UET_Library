module org.example.uet_library {
    requires javafx.controls;
    requires javafx.fxml;
    requires okhttp3;
    requires java.sql;
    requires java.desktop;
    requires io.github.cdimascio.dotenv.java;
    requires bcrypt;
    requires org.json;

    opens org.example.uet_library to javafx.fxml;
    exports org.example.uet_library;
    exports org.example.uet_library.Controllers;
    opens org.example.uet_library.Controllers to javafx.fxml;
}