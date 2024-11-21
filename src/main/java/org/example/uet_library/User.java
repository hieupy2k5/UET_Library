package org.example.uet_library;

public class User extends Account {
    private int numberOfBookBorrowed;

    public User(int id, String username, String password, String first_name, String last_name,
        String email) {
        super(id, username, password, first_name, last_name, email);
    }

    public User(int id, String username, String first_name, String last_name,
        String email) {
        super(id, username, first_name, last_name, email);
    }

    public User(String username, String firstName, String lastName, String email) {
        super(username, firstName, lastName, email);
    }

    public void setNumberOfBookBorrowed(int numberOfBookBorrowed) {
        this.numberOfBookBorrowed = numberOfBookBorrowed;
    }

    public int getNumberOfBookBorrowed() {
        return numberOfBookBorrowed;
    }
}
