package org.example.uet_library;
public class Book {
    private String title;
    private String author;
    private String isbn;
    private String imageLink;
    private int year;
    private int quantity;
    private String type;
    private byte[] qrCode;
    private String infoBookLink;

    public String getInfoBookLink() {
        return infoBookLink;
    }

    public void setInfoBookLink(String link) {
        this.infoBookLink = link;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public Book(String title, String author, String isbn, String imageLink, int year, String type, String infoBookLink) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.imageLink = imageLink;
        this.year = year;
        this.type = type;
        this.quantity = 0;
        this.qrCode = null;
        this.infoBookLink = infoBookLink;
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
        return "Book:\n" +
                "Title: " + title + "\n" +
                "Author: " + author + "\n" +
                "ISBN: " + isbn + "\n" +
                "Image Link: " + imageLink + "\n" +
                "Year: " + year + "\n" +
                "Quantity: " + quantity + "\n" +
                "Type: " + type;
    }

    public byte[] getqrCode() {
        return this.qrCode;
    }
    public void setqrCode(byte[] qrCode) {
        this.qrCode = qrCode;
    }
}

