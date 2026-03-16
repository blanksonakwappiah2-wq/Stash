CREATE DATABASE IF NOT EXISTS quickbite;
USE quickbite;

-- Tables will be auto-created by JPA, but here's the schema for reference

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    password VARCHAR(255),
    role VARCHAR(50),
    address VARCHAR(255),
    phone VARCHAR(20)
);

CREATE TABLE restaurants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    address VARCHAR(255),
    contact VARCHAR(255),
    owner_id BIGINT,
    rating DOUBLE DEFAULT 0,
    FOREIGN KEY (owner_id) REFERENCES users(id)
);

CREATE TABLE menu_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    description TEXT,
    price DOUBLE,
    image_url VARCHAR(255),
    category VARCHAR(100),
    restaurant_id BIGINT,
    FOREIGN KEY (restaurant_id) REFERENCES restaurants(id)
);

CREATE TABLE delivery_options (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    method VARCHAR(50), -- e.g., 'standard', 'express', 'drone'
    base_fee DOUBLE, -- base delivery fee
    per_km_fee DOUBLE, -- additional fee per km
    estimated_time INT -- estimated delivery time in minutes
);

CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT,
    restaurant_id BIGINT,
    status VARCHAR(50),
    total_amount DOUBLE,
    delivery_fee DOUBLE,
    distance DOUBLE,
    delivery_address VARCHAR(255),
    delivery_option_id BIGINT,
    order_time DATETIME,
    delivery_time DATETIME,
    delivery_agent_id BIGINT,
    FOREIGN KEY (customer_id) REFERENCES users(id),
    FOREIGN KEY (restaurant_id) REFERENCES restaurants(id),
    FOREIGN KEY (delivery_option_id) REFERENCES delivery_options(id),
    FOREIGN KEY (delivery_agent_id) REFERENCES users(id)
);

CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT,
    menu_item_id BIGINT,
    quantity INT,
    price DOUBLE,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)
);

CREATE TABLE reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT,
    restaurant_id BIGINT,
    rating INT,
    comment TEXT,
    review_time DATETIME,
    FOREIGN KEY (customer_id) REFERENCES users(id),
    FOREIGN KEY (restaurant_id) REFERENCES restaurants(id)
);

-- Sample data for delivery options
INSERT INTO delivery_options (method, base_fee, per_km_fee, estimated_time) VALUES
('standard', 2.99, 0.50, 45),
('express', 4.99, 0.75, 25),
('drone', 6.99, 1.00, 15);