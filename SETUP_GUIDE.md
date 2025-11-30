# Setting Up Android Development Environment

## Option 1: Cloud Build (No Local Setup Required) - EASIEST
If you don't want to install Android Studio, you can use **GitHub Actions** to build the app for you.

### Steps:
1.  **Create a GitHub Repository**:
    *   Go to [github.com/new](https://github.com/new).
    *   Name it `SecureVault`.
    *   **Important**: Keep it **Private** if you want to keep your code secure, though the code itself doesn't contain your passwords (only the logic).

2.  **Push Code to GitHub**:
    *   Open a terminal in the `SecureVault` folder.
    *   Run these commands:
        ```bash
        git init
        git add .
        git commit -m "Initial commit"
        git branch -M main
        git remote add origin https://github.com/YOUR_USERNAME/SecureVault.git
        git push -u origin main
        ```
    *   *(Replace `YOUR_USERNAME` with your actual GitHub username)*

3.  **Download APK**:
    *   Go to your repository on GitHub.
    *   Click on the **Actions** tab.
    *   You should see a workflow named "Build Android APK" running (yellow circle) or completed (green check).
    *   Click on the workflow run.
    *   Scroll down to the **Artifacts** section.
    *   Click **app-debug** to download the zip file.
    *   Extract the zip to get `app-debug.apk`.

4.  **Install on Phone**:
    *   Send the `app-debug.apk` to your phone (via USB, email, Drive, etc.).
    *   Tap to install. (You may need to allow "Install from unknown sources").

---

## Option 2: Local Build (Requires Android Studio)
Follow these steps if you want to build and test locally on your computer.

1.  **Install Android Studio**: Download from [developer.android.com/studio](https://developer.android.com/studio).
2.  **Open Project**: Open the `SecureVault` folder.
3.  **Run**: Connect your phone and click the Green Play button.
