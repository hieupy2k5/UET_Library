package org.example.uet_library.models;

public class Request implements TableItem {
    private int id;
    private int userId;
    private String bookId;
    private String username;
    private String title;
    private String author;
    private String status;
    private String imageUrl;

    public Request(int id, String bookId, String title, String author, String status, String imageUrl) {
        this.id = id;
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.status = status;
        this.imageUrl = imageUrl;
    }

    public Request(int id, int userId, String bookId, String username, String title,
        String author, String status, String imageUrl) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.username = username;
        this.title = title;
        this.author = author;
        this.status = status;
        this.imageUrl = imageUrl;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getBookId() {
        return bookId;
    }

    public String getUsername() {
        return username;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getAuthor() {
        return author;
    }

    public String getStatus() {
        return status;
    }
}
