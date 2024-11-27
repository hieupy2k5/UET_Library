package org.example.uet_library.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private String dbName = "";
    public Connection connection;

    public Database() {
    }

    public Database(String dbName) {
        this.dbName = dbName;
    }

    public Connection getConnection() {
        Config config = Config.getInstance();
        if (dbName.isEmpty()) dbName = config.get("DATABASE_NAME");
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
