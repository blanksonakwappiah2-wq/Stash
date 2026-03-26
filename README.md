---
title: QuickBite App
emoji: 🍴
colorFrom: orange
colorTo: red
sdk: docker
app_port: 10000
pinned: false
---

# QuickBite Food Ordering & Delivery System

This project is a full-stack Java application for food ordering and delivery.

## Modules

- **backend**: Spring Boot REST API
- **desktop**: Console-based Java application (simulated desktop app)
- **android**: Android mobile app (requires separate setup with Android Studio)

## Setup

1. Install Java 17, Maven, MySQL.
2. Create MySQL database: Run `database/quickbite.sql`
3. Update `backend/src/main/resources/application.properties` with your MySQL credentials and Google Maps API key.
4. Build and run backend: `cd backend && mvn spring-boot:run`
5. For desktop: `cd desktop && mvn javafx:run`
6. For Android: Import the android folder into Android Studio and build.

## Google Maps Integration

The application uses Google Maps Distance Matrix API to calculate delivery distances and fees.

1. Get a Google Maps API key from [Google Cloud Console](https://console.cloud.google.com/).
2. Enable the Distance Matrix API for your project.
3. Replace `YOUR_GOOGLE_MAPS_API_KEY` in `backend/src/main/resources/application.properties` with your actual API key.

## Features

- User management (customers, restaurants, admins, delivery agents)
- Restaurant menu management
- Order placement and tracking with distance-based delivery fees using Google Maps
- Delivery status updates

## API Endpoints

- Users: /api/users
- Orders: /api/orders

## Sample Workflow

1. Create users via POST /api/users
2. Create restaurants and menu items
3. Place order via POST /api/orders
4. Update order status via PUT /api/orders/{id}/status
5. Assign delivery agent via PUT /api/orders/{id}/assign

For desktop app, extend Main.java to include GUIs for login, browsing, ordering, etc.
For Android, create activities for similar features.