# Table of contents

* [Overview](#overview)
* [Getting started](#getting-started)
* [Screens and features](#screens-and-features)
  * [LogIn](#login)
  * [Register](#register)
  * [Side bar](#side-bar)
  * [User Features](#user-features)
    * [Home screen](#user-home-screen)
    * [Show book](#user-show-book)
    * [Favorites](#user-favorites)
    * [Borrow and Return Book](#user-borrow-and-return-book)
  * [Admin Features](#admin-features)
    * [Home screen](#admin-home-screen)
    * [Show book](#admin-show-book)
    * [Add Book](#admin-add-book)
    * [User Manager](#admin-user-manager)
    * [User Requests](#admin-user-requests)
  * [Setting](#setting)

---

# Overview

This is an online library application to support the real library. You can search and apply to borrow books, and when your request is approved, you can go directly to the library to borrow them. If unavailable, you can add them to your favorites to borrow later. You can also rate books with comments for the author and other readers.

---

# Getting started

To use the app, you need to:
- Download or clone the source code from this GitHub repository.
- Install JDK22 or later (JDK23 recommended).
- Install MySQL and add the MySQL Connector/J to the project.
- Create a database (you can export SQL code from [DBDiagram](https://dbdiagram.io/d/OOP-6704a091fb079c7ebdabcbcb)).
- Install the required dependencies and run.

---

# Screens and features

## LogIn

This screen appears when you open the app. You can log in with your username and password.

![Log In](user-attachments/LogIn.png)

---

## Register

If you don't have an account, go to the register screen by clicking "register" at the bottom right. You can register as either an admin or a user.

![Register](user-attachments/Register.png)

---

## Side bar

<div style="display: flex; justify-content: space-between; align-items: center;">

  <div style="text-align: center; width: 45%;">
    <img src="user-attachments/UserNavigation.png" alt="User Navigation" style="height: 500px; width: auto; border: 1px solid black;">
    <p><strong>User</strong></p>
  </div>

  <div style="text-align: center; width: 45%;">
    <img src="user-attachments/AdminNavigation.png" alt="Admin Navigation" style="height: 500px; width: auto; border: 1px solid black;">
    <p><strong>Admin</strong></p>
  </div>

</div>

---

## User Features

### Home screen
![User Home](user-attachments/UserHomeView.png)

### Show book
![User Borrow Book](user-attachments/UserBorrowBookView.png)

### Favorites
![User Favorites](user-attachments/UserFavoritesView.png)

### Borrow and Return Book
#### User Requests
![User Requests](user-attachments/UserRequestsView.png)

#### User Return Book
![User Return Book](user-attachments/UserReturnBookView.png)

---

## Admin Features

### Home screen
![Admin Home](user-attachments/AdminHomeView.png)

### Show book
![Admin Show Book](user-attachments/AdminShowBookView.png)

### Add Book
![Admin Add Book](user-attachments/AdminAddBook.png)

### User Manager
![Admin User Manager](user-attachments/AdminUserManager.png)

### User Requests
![Admin User Requests](user-attachments/AdminUserRequest.png)

---

## Setting

![User Settings](user-attachments/Setting.png)
