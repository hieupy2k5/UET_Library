package org.example.uet_library.models;

public class Request {

    private int id;
    private int user_id;
    private String book_id;
    private String username;
    private String title;
    private String author;
    private String status;
    private String image_url;

    public Request(int id, String book_id, String title, String author, String status, String image_url) {
        this.id = id;
        this.book_id = book_id;
        this.title = title;
        this.author = author;
        this.status = status;
        this.image_url = image_url;
    }

    public Request(int id, int user_id, String book_id, String username, String title,
        String author, String status, String image_url) {
        this.id = id;
        this.user_id = user_id;
        this.book_id = book_id;
        this.username = username;
        this.title = title;
        this.author = author;
        this.status = status;
        this.image_url = image_url;
    }

    public int getId() {
        return id;
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

    public String getImageUrl() {
        return image_url;
    }

    public String getAuthor() {
        return author;
    }

    public String getStatus() {
        return status;
    }
}