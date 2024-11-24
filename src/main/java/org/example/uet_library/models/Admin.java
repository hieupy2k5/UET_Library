package org.example.uet_library.models;

public class Admin extends Account {

    public Admin(int id, String username, String password, String firstName, String last_name,
        String email) {
        super(id, username, password, firstName, last_name, email);
    }

    public Admin(String username, String firstName, String lastName,
        String email) {
        super(username, firstName, lastName, email);
    }
}
