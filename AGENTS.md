# AGENTS.md

This document provides instructions for agents and automated systems to work with this repository.

## Development Environment Setup

The project has been upgraded to a modern Android build environment. Strict adherence to these versions is required to ensure a successful build.

### 1. Java Development Kit (JDK)

- **Version:** Java 21 is mandatory.
- **Installation:**
  ```bash
  sudo apt-get update && sudo apt-get install -y openjdk-21-jdk
  ```
- **Verification:** Ensure Java 21 is the default version:
  ```bash
  java -version
  ```

### 2. Android SDK

- **`compileSdk`:** 36
- **`targetSdk`:** 36
- **`minSdk`:** 21
- **`buildToolsVersion`:** The Android Gradle Plugin will automatically download the required version. Ensure the `ANDROID_HOME` environment variable is correctly set to your SDK location.

### 3. Gradle

- **Gradle Version:** 8.12
- **Android Gradle Plugin (AGP) Version:** 8.9.1
- **Kotlin Version:** 2.1.0
- The Gradle wrapper (`./gradlew`) is included in the repository and should be used for all build commands. It will automatically download the correct Gradle version.

## Building the Project

- **Clean the project:**
  ```bash
  ./gradlew clean
  ```
- **Build a debug APK:**
  ```bash
  ./gradlew assembleDebug
  ```
- **Run unit tests:**
  ```bash
  ./gradlew test
  ```

## Troubleshooting

### Build Failures due to `Permission denied`

During the build process, you may encounter `Permission denied` errors related to the `.gradle` or `.kotlin` directories in the project root. This is often due to file ownership issues within the sandbox environment.

**Solution:**

Before running a build, ensure the current user owns these directories by running the following commands from the repository root:

```bash
sudo chown -R $(whoami) .gradle/
sudo chown -R $(whoami) .kotlin/
```

If the build fails with an error related to deleting the `app/build` directory, you may need to remove it manually with `sudo`:

```bash
sudo rm -rf app/build
```

### Build Failures due to `R` class not found

If the build fails with "cannot find symbol" errors related to the `R` class (e.g., `R.attr.colorPrimary`), it signifies that the Android resource processor failed. This is almost always a symptom of another underlying issue.

**Do not attempt to fix `R` class errors directly.** Instead, look for other compilation errors in the build log. Fixing those will typically resolve the `R` class generation problem. If there are no other errors, the cause is likely a misconfiguration in the `app/build.gradle` file's dependencies.
