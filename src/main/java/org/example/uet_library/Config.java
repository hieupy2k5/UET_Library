package org.example.uet_library;

import io.github.cdimascio.dotenv.Dotenv;

public class Config {

    private static final Config instance = new Config();
    private final Dotenv dotenv = Dotenv.load();

    public static Config getInstance() {
        return instance;
    }

    public String get(String key) {
        return dotenv.get(key);
    }
}
