# Image to PDF Converter – Simple & Fast

A professional Android application for converting images to PDF documents with advanced features and a clean, modern UI built with Jetpack Compose.

## 📱 Overview

This Android app allows users to easily convert multiple images into high-quality PDF documents. It features a clean, professional interface following Material 3 design guidelines and implements a Pro feature tier with Google Play Billing integration.

### App Details
- **Package Name:** `com.zaheer.imagetopdf`
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 34 (Android 14)
- **Architecture:** MVVM (Model-View-ViewModel)
- **UI Framework:** Jetpack Compose with Material 3

## ✨ Features

### Free Version Features
- Select multiple images from gallery (up to 10 images)
- Preview and reorder selected images
- Rotate images in 90° increments
- Remove unwanted images
- Configurable PDF settings:
  - Page size selection (A4, Letter, Legal)
  - Adjustable margins
  - Quality options (Low, Medium)
- Save PDFs to device storage
- Share generated PDFs
- Free version includes watermark

### Pro Features (In-App Purchase)
- ✅ **Unlimited Images** - Add unlimited images per PDF
- ✅ **No Watermark** - Generate clean PDFs without watermarks
- ✅ **High Resolution Export** - Export with highest quality settings
- ✅ **Batch PDF Creation** - Create multiple PDFs efficiently
- ✅ **One-time Purchase** - Lifetime access with single payment

## 🛠️ Technology Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose
- **Material Design:** Material 3
- **Navigation:** Navigation Compose
- **Async:** Kotlin Coroutines + StateFlow
- **Image Loading:** Coil
- **Billing:** Google Play Billing Library v7.0.0
- **PDF Generation:** Android PdfDocument API
- **Build System:** Gradle (Groovy DSL)

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- JDK 8 or higher
- Android SDK with API 34
- Gradle 8.2+

### Building the Project

1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Build and run: `./gradlew assembleDebug`

## 📝 How to Use

1. **Select Images:** Tap '+' to open gallery and select images
2. **Preview & Edit:** Rotate and reorder images as needed
3. **Configure Settings:** Adjust page size, margins, and quality
4. **Generate PDF:** Create and save your PDF
5. **Share:** Send PDF via any app

## 💳 In-App Billing

- **Product ID:** `imagetopdf_pro_unlock`
- **Type:** One-time in-app purchase (INAPP)
- Pro status persisted in SharedPreferences

## 🔒 Permissions

- `INTERNET` - For Google Play Billing
- `READ_MEDIA_IMAGES` - For accessing gallery images

## 👤 Author

Developed by Zaheer Abbas

---

**Note:** Before publishing to Google Play Store, generate actual launcher icons and configure the product in Google Play Console.
