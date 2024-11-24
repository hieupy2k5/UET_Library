package org.example.uet_library.models;

public class Favor implements TableItem {
    private int id;
    private String bookId;
    private String title;
    private String author;
    private String category;
    private String imageUrl;

    public Favor(int id, String bookId, String title, String author, String category, String imageUrl) {
        this.id = id;
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.category = category;
        this.imageUrl = imageUrl;
    }

    public int getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getIsbn() {
        return bookId;
    }

    public String getImageUrl() { return imageUrl; }

}
