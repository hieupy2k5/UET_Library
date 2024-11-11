package org.example.uet_library;

import java.time.LocalDateTime;

public class Borrow {

    private int id;
    private String user_id;
    private String book_id;
    private int quantity;
    private LocalDateTime borrow_date;
    private LocalDateTime return_date;
    private String status;
    private String title;
    private String author;
    private String category;

    public Borrow(String user_id, String book_id, int quantity, LocalDateTime borrow_date,
        LocalDateTime return_date, String status) {
        this.user_id = user_id;
        this.book_id = book_id;
        this.quantity = quantity;
        this.borrow_date = borrow_date;
        this.return_date = return_date;
        this.status = status;
    }

    public Borrow(String book_id, String title, String author, String category, int quantity, LocalDateTime borrow_date,
        LocalDateTime return_date, String status) {
        this.book_id = book_id;
        this.title = title;
        this.author = author;
        this.category = category;
        this.quantity = quantity;
        this.borrow_date = borrow_date;
        this.return_date = return_date;
        this.status = status;
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
        return book_id;
    }

    public LocalDateTime getBorrowDate() {
        return borrow_date;
    }

    public LocalDateTime getReturnDate() {
        return return_date;
    }

    public String getStatus() {
        return status;
    }
}
