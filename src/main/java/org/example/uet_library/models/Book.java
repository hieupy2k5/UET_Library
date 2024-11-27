package org.example.uet_library.models;

public class Book implements TableItem {

    private String title;
    private String author;
    private String isbn;
    private String imageUrl;
    private int year;
    private int quantity;
    private String category;
    private byte[] qrCode;
    private String infoBookLink;
    private String description;


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInfoBookLink() {
        return infoBookLink;
    }

    public void setInfoBookLink(String link) {
        this.infoBookLink = link;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public Book(String title, String author, String isbn, String imageUrl, int year,
        String category,
        String infoBookLink, String description) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.imageUrl = imageUrl;
        this.year = year;
        this.category = category;
        this.quantity = 0;
        this.qrCode = null;
        this.infoBookLink = infoBookLink;
        this.description = description;
    }

    public Book(String title, String author, String isbn, String imageUrl, int year,
        String category,
        int quantity) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.imageUrl = imageUrl;
        this.year = year;
        this.category = category;
        this.quantity = quantity;
    }

    public Book() {

    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
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
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getYear() {
        return this.year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    @Override
    public String toString() {
        return "Book:\n" +
            "Title: " + title + "\n" +
            "Author: " + author + "\n" +
            "ISBN: " + isbn + "\n" +
            "Image Link: " + imageUrl + "\n" +
            "Year: " + year + "\n" +
            "Quantity: " + quantity + "\n" +
            "Type: " + category;
    }

    public byte[] getqrCode() {
        return this.qrCode;
    }

    public void setqrCode(byte[] qrCode) {
        this.qrCode = qrCode;
    }
}

