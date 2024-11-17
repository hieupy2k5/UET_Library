package org.example.uet_library;

public class Admin extends Account {

    public Admin(int id, String username, String password, String first_name, String last_name,
        String email) {
        super(id, username, password, first_name, last_name, email);
    }

    public Admin(String username, String first_name, String last_name,
        String email) {
        super(username, first_name, last_name, email);
    }
}
