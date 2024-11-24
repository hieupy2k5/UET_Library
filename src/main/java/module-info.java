module org.example.uet_library {
    requires javafx.controls;
    requires javafx.fxml;
    requires okhttp3;
    requires java.sql;
    requires java.desktop;
    requires io.github.cdimascio.dotenv.java;
    requires bcrypt;
    requires org.json;
    requires mysql.connector.j;
    requires java.net.http;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;

    exports org.example.uet_library.Controllers;
    opens org.example.uet_library.Controllers to javafx.fxml;
    exports org.example.uet_library.services;
    opens org.example.uet_library.services to javafx.fxml;
    exports org.example.uet_library.apis;
    opens org.example.uet_library.apis to javafx.fxml;
    exports org.example.uet_library.database;
    opens org.example.uet_library.database to javafx.fxml;
    exports org.example.uet_library.utilities;
    opens org.example.uet_library.utilities to javafx.fxml;
    exports org.example.uet_library.models;
    opens org.example.uet_library.models to javafx.fxml;
    exports org.example.uet_library;
    opens org.example.uet_library to javafx.fxml;
}