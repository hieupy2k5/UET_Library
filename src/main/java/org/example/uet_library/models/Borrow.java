package org.example.uet_library.models;

public class Borrow implements TableItem {

    private int id;
    private String bookId;
    private int quantity;
    private String borrowDate;
    private String returnDate;
    private String status;
    private String title;
    private String author;
    private String category;
    private String imageUrl;
    private boolean isRated = false;

    public Borrow(int id, String bookId, String title, String author, String category, int quantity, String borrowDate,
        String returnDate, String status, String imageUrl) {
        this.id = id;
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.category = category;
        this.quantity = quantity;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
        this.status = status;
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

    public int getQuantity() {
        return quantity;
    }

    public String getIsbn() {
        return bookId;
    }

    public String getImageUrl() { return imageUrl; }

    public String getBorrowDate() {
        return borrowDate;
    }

    public String getReturnDate() {
        return returnDate;
    }

    public String getStatus() {
        return status;
    }

    public boolean isRated() {
        return isRated;
    }

    public void setIsRated(boolean isRated) {
        this.isRated = isRated;
    }
}
