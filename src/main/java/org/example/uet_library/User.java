package org.example.uet_library;

public class User extends Account {

    public User(int id, String username, String password, String first_name, String last_name,
        String email) {
        super(id, username, password, first_name, last_name, email);
    }

    public User(int id, String username, String first_name, String last_name,
        String email) {
        super(id, username, first_name, last_name, email);
    }
}
