package org.example.uet_library;
public class Book {
    private String title;
    private String author;
    private String isbn;
    private String imageLink;
    private int year;
    private int quantity;
    private String type;

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public Book(String title, String author, String isbn, String imageLink, int year, String type) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.imageLink = imageLink;
        this.year = year;
        this.type = type;
        this.quantity = 0;
    }

    public Book(String title, String author, String isbn, String imageLink, int year, String type, int quantity) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.imageLink = imageLink;
        this.year = year;
        this.type = type;
        this.quantity = quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getImageUrl() {
        return imageLink;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public int getYear() {
        return this.year;
    }

    @Override
    public String toString() {
        return "Book [title=" + title + ", author=" + author + ", isbn=" + isbn;
    }
}
