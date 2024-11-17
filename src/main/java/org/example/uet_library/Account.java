package org.example.uet_library;

public abstract class Account {
    private int id;
    private String username;
    private String password;
    private String first_name;
    private String last_name;
    private String email;

    public Account(int id, String username, String password, String first_name, String last_name, String email) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.first_name = first_name;
        this.last_name = last_name;
        this.email = email;
    }

    public Account(String username, String first_name, String last_name, String email) {
        this.username = username;
        this.first_name = first_name;
        this.last_name = last_name;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFirst_name() {
        return first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public String getUsername() {
         return username;
    }

    public String getPassword() {
        return password;
    }
}
