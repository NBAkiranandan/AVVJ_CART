CREATE DATABASE IF NOT EXISTS avvj_cart;
USE avvj_cart;

-- Reset Tables
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS cart;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS wallet;
DROP TABLE IF EXISTS users;

------------------------------------------------
-- USERS TABLE
------------------------------------------------
CREATE TABLE users(
id INT AUTO_INCREMENT PRIMARY KEY,
username VARCHAR(50) UNIQUE NOT NULL,
email VARCHAR(100) UNIQUE NOT NULL,
password VARCHAR(255) NOT NULL,
mobile VARCHAR(15),
age INT,
address TEXT,
security_question VARCHAR(255),
security_answer VARCHAR(255),
role ENUM('user','admin','supplier') DEFAULT 'user',
approved TINYINT(1) DEFAULT 1
);

------------------------------------------------
-- WALLET (NO ADD MONEY FEATURE)
------------------------------------------------
CREATE TABLE wallet(
user_id INT PRIMARY KEY,
balance DOUBLE DEFAULT 100000,
FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
);

------------------------------------------------
-- PRODUCTS
------------------------------------------------
CREATE TABLE products(
id INT AUTO_INCREMENT PRIMARY KEY,
name VARCHAR(255),
description TEXT,
price DOUBLE,
stock INT,
image_path VARCHAR(255),
category VARCHAR(100),
supplier_id INT,
FOREIGN KEY(supplier_id) REFERENCES users(id) ON DELETE SET NULL
);

------------------------------------------------
-- CART
------------------------------------------------
CREATE TABLE cart(
id INT AUTO_INCREMENT PRIMARY KEY,
user_id INT,
product_id INT,
quantity INT DEFAULT 1,
price DOUBLE,
FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE,
FOREIGN KEY(product_id) REFERENCES products(id) ON DELETE CASCADE
);

------------------------------------------------
-- ORDERS
------------------------------------------------
CREATE TABLE orders(
id INT AUTO_INCREMENT PRIMARY KEY,
user_id INT,
total_amount DOUBLE,
status VARCHAR(40) DEFAULT 'Confirmed',
order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
);

------------------------------------------------
-- ORDER ITEMS
------------------------------------------------
CREATE TABLE order_items(
id INT AUTO_INCREMENT PRIMARY KEY,
order_id INT,
product_id INT,
quantity INT,
price DOUBLE,
FOREIGN KEY(order_id) REFERENCES orders(id) ON DELETE CASCADE,
FOREIGN KEY(product_id) REFERENCES products(id) ON DELETE CASCADE
);

------------------------------------------------
-- DEFAULT USERS
------------------------------------------------
INSERT INTO users(username,email,password,role,approved)
VALUES
('admin','admin@avvj.com','admin123','admin',1),
('supplier','supplier@avvj.com','supp123','supplier',1),
('akira','akira@test.com','akira123','user',1);

------------------------------------------------
-- INITIAL WALLET
------------------------------------------------
INSERT INTO wallet VALUES
(1,100000),
(2,100000),
(3,100000);

------------------------------------------------
-- PRODUCTS WITH IMAGES
------------------------------------------------
INSERT INTO products(name,description,price,stock,image_path,category,supplier_id)
VALUES

('iPhone 15',
'Latest Apple smartphone with titanium design',
79999,
50,
'/icons/phone.png',
'Electronics',
2),

('Samsung Galaxy S24 Ultra',
'AI powered Samsung flagship phone',
124999,
40,
'/icons/phone.png',
'Electronics',
2),

('MacBook Air M3',
'Apple lightweight laptop with M3 chip',
114900,
20,
'/icons/laptop.png',
'Electronics',
2),

('Gaming Laptop',
'High performance gaming laptop RTX graphics',
95000,
15,
'/icons/laptop.png',
'Electronics',
2),

('Nike Running Shoes',
'Comfortable lightweight sports shoes',
4999,
100,
'/icons/shoes.png',
'Footwear',
2),

('Adidas Sneakers',
'Stylish everyday sneakers',
3999,
120,
'/icons/shoes.png',
'Footwear',
2);