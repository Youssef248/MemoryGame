# Memory Game App

## Overview

This Android application is a memory game where players match pairs of cards. The game offers different difficulty levels, including easy, medium, and hard, with varying board sizes. Users can choose predefined difficulty levels or create custom game boards.

## Features

- Multiple difficulty levels: Easy, Medium, and Hard.
- Customizable game boards with the ability to choose images for each card.
- Firebase authentication for anonymous sign-in.
- Firebase Firestore integration for storing and retrieving custom game data.
- User-friendly UI with a responsive grid layout for cards.

## Project Structure

The project is structured as follows:

- `app` directory: Contains the main Android application code.
  - `com.example.memorygame`: Root package for the app.
    - `models`: Package containing the data models, such as `MemoryGame` and `BoardSize`.
    - `utils`: Package containing utility classes and constants.
    - `MainActivity.kt`: Main activity handling the game setup and UI.
    - `CreateActivity.kt`: Activity for creating custom game boards.
- `README.md`: This file, providing an overview of the application.

## Dependencies

The application uses the following external libraries:

- Picasso: For image loading and caching.
- Firebase: For authentication and Firestore database.

## Testing

The project includes JUnit tests for the `BoardSize` class to ensure that the game board dimensions and calculations are correct.

## How to Run

1. Clone the repository to your local machine.
2. Open the project in Android Studio.
3. Build and run the application on an Android emulator or physical device.

## Additional Notes

- Firebase Authentication: The app uses Firebase Anonymous Authentication for user sign-in.
- Firestore Database: Custom game data is stored and retrieved from the Firestore database.



