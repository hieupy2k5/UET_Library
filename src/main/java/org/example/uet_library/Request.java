package org.example.uet_library;

public class Request {
    private int user_id;
    private String book_id;
    private String username;
    private String title;
    private String author;
    private String status;

    public Request(String title, String author, String status) {
        this.title = title;
        this.author = author;
        this.status = status;
    }

    public Request(int user_id, String book_id,String username, String title, String author, String status) {
        this.user_id = user_id;
        this.book_id = book_id;
        this.username = username;
        this.title = title;
        this.author = author;
        this.status = status;
    }

    public int getUser_id() {
        return user_id;
    }

    public String getBook_id() {
        return book_id;
    }

    public String getUsername() {
        return username;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getStatus() {
        return status;
    }
}
