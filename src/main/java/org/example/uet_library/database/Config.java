package org.example.uet_library.database;

import io.github.cdimascio.dotenv.Dotenv;
import java.util.HashMap;
import java.util.Map;

public class Config {

    private static final Config instance = new Config();
    private final Dotenv dotenv = Dotenv.load();
    private final Map<String, String> overrides = new HashMap<>();

    public static Config getInstance() {
        return instance;
    }

    /**
     * We try to get the according field from .env file. If we have overwritten a field, it will be
     * stored in our Map and we will retrieve from that instead.
     *
     * @param key is the field.
     * @return the value of the key in either .env file or in Map.
     */
    public String get(String key) {
        return overrides.containsKey(key) ? overrides.get(key) : dotenv.get(key);
    }

    public void set(String key, String value) {
        overrides.put(key, value);
    }

    public void reset() {
        overrides.clear();
    }
}
