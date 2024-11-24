package org.example.uet_library.models;

public class User extends Account {
    private int numberOfBookBorrowed;

    public User(int id, String username, String password, String firstName, String lastName,
        String email) {
        super(id, username, password, firstName, lastName, email);
    }

    public User(int id, String username, String firstName, String lastName,
        String email) {
        super(id, username, firstName, lastName, email);
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
