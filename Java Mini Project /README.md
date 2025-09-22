

# **💼 Digital Wallet System – Java Mini Project**
## 📜 Project Overview 
This Digital Wallet System is a GUI-based desktop application developed in Java using Swing. It allows users to manage virtual wallet activities such as deposits, withdrawals, transfers, transaction history, and PDF mini-statements. The system supports two roles: user and admin.

## ⚙️ Technologies Used
Java (Swing) – for GUI development

MySQL – for database management

JDBC – to connect Java and MySQL

iText PDF Library – to generate PDF mini statements

CardLayout – for UI panel transitions

## 👤 User Types 
##  1. Regular User 
Can register and login

Perform deposit, withdraw, send money, and request money

View current balance and transaction history

Generate PDF mini statements

### 2. Admin 
Login using admin credentials

View all users, transactions, and user activity logs

Switch between admin and user panel

## 🔐 Features 
** Feature	Description
Authentication	Login and registration for users with password match and validation
Role-Based Access	Admin and user functionalities are separated
Deposit/Withdraw	Allows users to add or remove money from the wallet
Send/Request Money	Transfers funds to/from other users
Transaction History	Displays last 20 transactions with date and type
Activity Logging	Every major action (login, logout, deposit etc.) is logged
PDF Mini Statement	Generates and saves a formatted PDF file of recent transactions
Admin Controls	Admins can view all users, transactions, and activity logs **

## 🗂️ Folder Structure (if applicable) 

project-root/
│
├── MINI_PROJECT_JAVA_CODE.java.txt    # Main source code
├── walletdb.sql (suggested)           # SQL script to create required DB and tables
└── README.md                          # This file

## 🧱 Database Structure 
You’ll need to create a MySQL database named walletdb with tables like:


CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50),
    password VARCHAR(50),
    user_type VARCHAR(10),
    balance DOUBLE
);

CREATE TABLE transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    type VARCHAR(20),
    amount DOUBLE,
    date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_activity_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    activity_type VARCHAR(50),
    amount DOUBLE,
    activity_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
🏃‍♂️ How to Run
Ensure MySQL Server is running.

Import or create the walletdb database using the schema above.

Update DB credentials in the code (connectToDB() method).

Compile and run WalletAppGUI.java using any Java IDE or CLI.

Start using the wallet app!

## ✍️ Contributors 
 
Tushar Nagare && Bharat Kolhe   

## 📄 License 
This is a mini-project developed for academic purposes. Modify and reuse freely.# ATM_using_java
