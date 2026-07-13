# Developer Setup Guide

This guide will help you set up your development environment for the `fake-progress-lib` project.

## Prerequisites

- **Android Studio**: Download and install the latest version of [Android Studio](https://developer.android.com/studio).
- **JDK**: The project uses the JDK bundled with Android Studio (JetBrains Runtime).
- **Xcode (macOS Only)**: Required for building and running iOS targets. Install via the Mac App Store.

## Environment Variables

To build the project from the terminal or IDE, you need to set up the following environment variables.

### 1. JAVA_HOME

Set `JAVA_HOME` to point to the JDK bundled with Android Studio.

- **Windows**:
    - Variable: `JAVA_HOME`
    - Value: `C:\Users\<YourUser>\AppData\Local\Programs\Android Studio\jbr` (or your specific installation path).
    - Update `Path`: Add `%JAVA_HOME%\bin` to your system/user `Path` variable.
- **macOS/Linux**:
    - Variable: `JAVA_HOME`
    - Value: Usually inside the Android Studio application folder (e.g., `/Applications/Android Studio.app/Contents/jbr/Contents/Home`).
    - Update `PATH`: Add `$JAVA_HOME/bin` to your `PATH`.

### 2. ANDROID_HOME

Set `ANDROID_HOME` to point to your Android SDK location.

- **Windows**:
    - Variable: `ANDROID_HOME`
    - Value: `C:\Users\<YourUser>\AppData\Local\Android\Sdk`.
    - Update `Path`: Add `%ANDROID_HOME%\platform-tools` and `%ANDROID_HOME%\emulator` to your system/user `Path` variable.
- **macOS/Linux**:
    - Variable: `ANDROID_HOME`
    - Value: Usually `~/Library/Android/sdk`.
    - Update `PATH`: Add `$ANDROID_HOME/platform-tools` and `$ANDROID_HOME/emulator` to your `PATH`.

## Project Configuration

### local.properties

The Android Gradle plugin requires a `local.properties` file in the project root to locate the SDK.

1. Create a file named `local.properties` in the root directory of the project.
2. Add the following line, replacing the path with your actual Android SDK path:
   ```properties
   sdk.dir=C:/Users/<YourUser>/AppData/Local/Android/Sdk
   ```
   *Note: Use forward slashes `/` even on Windows for Gradle compatibility.*

## Launching the Project

To start developing or exploring the project:

1. **Open the Project**: Launch **Android Studio** (recommended) or **IntelliJ IDEA**.
2. **Import**: Select **Open** and navigate to the project root directory.
3. **Gradle Sync**: Wait for the IDE to finish the initial Gradle sync. This might take a few minutes as it downloads dependencies and sets up the multiplatform targets.
4. **Project Structure**: The core logic is located in the `:library` module within `commonMain`.

*Note: Since this is a library project, there is no standalone application to "run". You can verify your changes by running the tests as described in the next section.*

## Verification

After setting up, restart your terminal or IDE to apply the environment variable changes.

### Running Tests

You can verify the setup by running the tests for all targets:

```bash
./gradlew allTests
```

To run tests for a specific target, you can use:

- **Android**: `./gradlew :library:testAndroid`
- **JVM**: `./gradlew :library:jvmTest`
- **iOS (Simulator)**: `./gradlew :library:iosSimulatorArm64Test` (Requires macOS and Xcode)
- **Linux**: `./gradlew :library:linuxX64Test` (Requires Linux host)

If everything is configured correctly, the build should start and the tests should pass.
