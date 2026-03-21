# QuickBite Android App

A native Android application for the QuickBite food ordering and delivery system.

## Features

### Customer Features
- User registration and login
- Browse restaurants by category
- View restaurant details and menu items
- Place orders with delivery options
- Track order status in real-time
- View order history

### Restaurant Owner Features
- Dashboard to manage restaurant
- View and manage menu items
- Receive and process orders
- Update order status

### Delivery Agent Features
- View assigned deliveries
- Update delivery status
- Navigate to delivery locations

## Tech Stack

- **Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel)
- **Networking**: Retrofit 2
- **JSON Parsing**: Gson
- **Async Operations**: Kotlin Coroutines
- **UI Components**: Material Design Components
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)

## Project Structure

```
android/
├── src/main/
│   ├── java/com/quickbite/android/
│   │   ├── data/
│   │   │   ├── model/          # Data classes
│   │   │   ├── api/            # Retrofit services
│   │   │   └── repository/     # Data repositories
│   │   ├── ui/
│   │   │   ├── LoginActivity.kt
│   │   │   ├── RegisterActivity.kt
│   │   │   ├── customer/       # Customer screens
│   │   │   ├── owner/          # Owner screens
│   │   │   └── agent/          # Agent screens
│   │   └── QuickBiteApplication.kt
│   ├── res/
│   │   ├── layout/             # XML layouts
│   │   ├── values/             # Colors, strings, themes
│   │   └── drawable/           # Images and icons
│   └── AndroidManifest.xml
├── build.gradle
└── README.md
```

## Setup Instructions

### Prerequisites
1. Android Studio Hedgehog (2023.1.1) or newer
2. JDK 17
3. Android SDK 34

### Build Steps

1. **Open in Android Studio**
   ```
   File -> Open -> Select the android/ folder
   ```

2. **Sync Gradle**
   - Android Studio will automatically sync
   - Wait for dependencies to download

3. **Configure Backend URL**
   - Edit `QuickBiteApplication.kt`
   - Change `BASE_URL` to your backend server:
     ```kotlin
     const val BASE_URL = "http://YOUR_SERVER_IP:8080/api/"
     ```
   - For emulator: use `10.0.2.2` for localhost
   - For physical device: use your computer's IP address

4. **Run the App**
   - Select an emulator or connect a device
   - Click Run (Shift+F10)

## API Endpoints Used

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/users/login` | POST | User login |
| `/users/register` | POST | User registration |
| `/restaurants` | GET | Get all restaurants |
| `/orders` | POST | Place new order |
| `/orders/{id}` | GET | Get order details |
| `/orders/{id}/status` | PUT | Update order status |

## Default Test Accounts

| Role | Email | Password |
|------|-------|----------|
| Customer | customer@quickbite.com | Customer123 |
| Owner | owner@quickbite.com | Owner123 |
| Agent | agent@quickbite.com | Agent123 |
| Manager | manager@quickbite.com | Manager123 |

## Screenshots

The app includes the following screens:
1. Login Screen
2. Registration Screen
3. Restaurant List (Customer)
4. Restaurant Detail & Menu
5. Order Placement
6. Order Tracking
7. Owner Dashboard
8. Delivery Agent Dashboard

## Building Release APK

```bash
./gradlew assembleRelease
```

The APK will be generated at:
`build/outputs/apk/release/app-release.apk`

## Troubleshooting

### Network Issues
- Ensure backend server is running
- Check BASE_URL configuration
- Verify internet permission in manifest
- For emulator: use `10.0.2.2` instead of `localhost`

### Build Errors
- Clean and rebuild: `./gradlew clean build`
- Invalidate caches: File -> Invalidate Caches / Restart
- Update Android Studio and Gradle

## Future Enhancements

- [ ] Push notifications for order updates
- [ ] Google Maps integration for delivery tracking
- [ ] Payment gateway integration
- [ ] Dark mode support
- [ ] Offline mode with Room database
- [ ] Image caching for menu items
- [ ] Search and filter restaurants
- [ ] User reviews and ratings

## License

This project is part of the QuickBite Food Ordering System.
