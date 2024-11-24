package org.example.uet_library.models;

public class Favor {
    private int id;
    private String user_id;
    private String book_id;
    private String title;
    private String author;
    private String category;
    private String image_url;

    public Favor(int id, String book_id, String title, String author, String category, String image_url) {
        this.id = id;
        this.book_id = book_id;
        this.title = title;
        this.author = author;
        this.category = category;
        this.image_url = image_url;
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
        return book_id;
    }

    public String getImageUrl() { return image_url; }

}
