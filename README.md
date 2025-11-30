# SecureVault

SecureVault is a secure, offline, self-contained Android password manager application.

## Features
- **Offline-First**: No internet permission required. All data stays on your device.
- **Secure Storage**: Credentials are encrypted using AES-256-GCM backed by the Android Keystore.
- **Encrypted Export/Import**: Backup your data to an encrypted file using a custom password (PBKDF2 + AES-GCM).
- **Modern UI**: Built with Jetpack Compose and Material 3.

## Build Instructions
1. Open **Android Studio**.
2. Select **Open** and choose the `SecureVault` directory.
3. Wait for Gradle to sync.
4. Connect your Android device or start an emulator.
5. Click the **Run** button (Green Play icon).

## Security Details
- **Local Storage**: Data is stored in a Room database. Sensitive fields (username, password, notes) are encrypted *before* insertion using a key stored in the Android Keystore, which is hardware-backed on supported devices.
- **Export**: Data is exported to a JSON file. The JSON content is encrypted using a key derived from a user-provided password using PBKDF2WithHmacSHA256 (65,536 iterations) and AES-GCM.
