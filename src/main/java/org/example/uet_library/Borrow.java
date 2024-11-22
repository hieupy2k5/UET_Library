package org.example.uet_library;



public class Borrow {

    private int id;
    private String user_id;
    private String book_id;
    private int quantity;
    private String borrow_date;
    private String return_date;
    private String status;
    private String title;
    private String author;
    private String category;
    private String image_url;
    private boolean isRate = false;

    public Borrow(int id, String book_id, String title, String author, String category, int quantity, String borrow_date,
        String return_date, String status, String image_url) {
        this.id = id;
        this.book_id = book_id;
        this.title = title;
        this.author = author;
        this.category = category;
        this.quantity = quantity;
        this.borrow_date = borrow_date;
        this.return_date = return_date;
        this.status = status;
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

    public int getQuantity() {
        return quantity;
    }

    public String getIsbn() {
        return book_id;
    }

    public String getImageUrl() { return image_url; }

    public String getBorrowDate() {
        return borrow_date;
    }

    public String getReturnDate() {
        return return_date;
    }

    public String getStatus() {
        return status;
    }

    public boolean isRate() {
        return isRate;
    }

    public void setRate(boolean isRate) {
        this.isRate = isRate;
    }
}
