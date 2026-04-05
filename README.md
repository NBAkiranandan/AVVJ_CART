# AVVJ_CART 🛒

### Online Shopping Cart Simulation (Java Swing + MySQL)

AVVJ_CART is a desktop-based online shopping cart simulation developed using **Java Swing** for the graphical user interface and **MySQL** for database management. The project demonstrates the core functionality of an e-commerce system including user authentication, product management, and cart operations.

## 🚀 Features

* User Login and Sign Up system
* Forgot Password functionality
* Admin Dashboard for management
* Supplier Dashboard for product handling
* User Dashboard for browsing products
* Add and manage items in shopping cart
* MySQL database integration using JDBC
* Simple and user-friendly GUI

## 🛠️ Technologies Used

* **Java (Core Java)**
* **Java Swing** – GUI Development
* **MySQL** – Database
* **JDBC** – Database Connectivity
* **NetBeans / Java IDE**

## 📂 Project Structure

```
src/
 ├── icons/                # Application icons
 └── user/
      ├── AdminDashboard.java
      ├── BillGenerator.java
      ├── DBConnection.java
      ├── EmailUtility.java
      ├── ForgotPassword.java
      ├── Login.java
      ├── OTPManager.java
      ├── SignUp.java
      ├── SupplierDashboard.java
      ├── UserDashboard.java
      ├── Theme.java
      └── TestConnection.java
```

## 🗄️ Database

The database schema is provided in:

```
database.sql
```

Import this file into **MySQL** before running the application.

## ▶️ How to Run

1. Clone the repository
2. Import the project into **NetBeans / any Java IDE**
3. Create the database in **MySQL**
4. Import `database.sql`
5. Update database credentials in `DBConnection.java`
6. Run the project

## 🎯 Purpose of the Project

This project was developed to understand:

* Java Swing GUI development
* Database connectivity using JDBC
* Implementation of a basic shopping cart system
* Role-based dashboards (Admin, Supplier, User)

## 👨‍💻 Author

NB Akiranandan
B.Tech Computer Science Engineering
