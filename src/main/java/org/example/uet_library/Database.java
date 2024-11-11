package org.example.uet_library;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    public Connection connection;

    public Connection getConnection() {
        Config config = Config.getInstance();
        String dbName = config.get("DATABASE_NAME");
        String dbUsername = config.get("DATABASE_USERNAME");
        String dbPassword = config.get("DATABASE_PASSWORD");
        String dbHost = config.get("DATABASE_HOST");
        Integer dbPort = Integer.parseInt(config.get("DATABASE_PORT"));

        String connectionString = String.format("jdbc:mysql://%s:%d/%s?user=%s&password=%s", dbHost, dbPort, dbName, dbUsername, dbPassword);

        try {
            connection = DriverManager.getConnection(connectionString);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return connection;
    }
}
