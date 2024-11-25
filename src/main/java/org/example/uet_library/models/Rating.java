package org.example.uet_library.models;

public class Rating {
    private String ISBN;
    private int rating;
    private String userName;

    private String comment;
    private String commentDate;

    public Rating(String ISBN, int rating, String userName, String comment, String commentDate) {
        this.ISBN = ISBN;
        this.rating = rating;
        this.userName = userName;
        this.comment = comment;
        this.commentDate = commentDate;
    }

    public void setCommentDate(String commentDate) {
        this.commentDate = commentDate;
    }

    public String getCommentDate() {
        return commentDate;
    }

    public String getISBN() {
        return ISBN;
    }

    public void setISBN(String ISBN) {
        this.ISBN = ISBN;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return this.getComment();
    }
}
