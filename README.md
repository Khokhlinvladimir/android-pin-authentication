# PIN Authentication Library on Jetpack Compose

[![](https://jitpack.io/v/Khokhlinvladimir/android-pin-authentication.svg)](https://jitpack.io/#Khokhlinvladimir/android-pin-authentication)

[Версия на русском языке](https://github.com/Khokhlinvladimir/android-pin-authentication/blob/main/README_RU.md)

## Description

Library for user authentication using PIN code. This library provides convenient and secure ways to verify users' identities using a simple numeric PIN. It can be used by developers when creating applications that require an additional layer of security or user authentication.

<img src="https://github.com/Khokhlinvladimir/android-pin-authentication/blob/main/screens/preview_russian.gif" alt="" width="200px"></a>    <img src="https://github.com/Khokhlinvladimir/android-pin-authentication/blob/main/screens/preview_english_01.png" alt="" width="200px"></a>    <img src="https://github.com/Khokhlinvladimir/android-pin-authentication/blob/main/screens/preview_english_02.png" alt="" width="200px"></a>    <img src="https://github.com/Khokhlinvladimir/android-pin-authentication/blob/main/screens/preview_english_03.png" alt="" width="200px"></a>

## Main features

1. Simple and intuitive PIN authentication.
2. Reliable storage and protection of user PIN codes using encryption.
3. Ability to customize the required length and complexity of the PIN code.
4. Limit the number of attempts to enter a PIN code to prevent brute force attacks.
5. Advanced configuration options and event notifications.

# Instructions for using the library for PIN code authentication

This tutorial will help you become familiar with using the PIN authentication library in your application. The library provides a simple and reliable way to protect access to your application using a PIN code. Let's figure out how it works:

## Step 1: Installing the library

Installing a PIN authentication library is the first step to securely protecting your application.

1. Add the dependency to your build.gradle file:

```gradle
dependencies {
    implementation 'com.github.Khokhlinvladimir:android-pin-authentication:v1.0.6'
}
```
With this simple step, you will enable a powerful authentication tool in your application, making it reliable and secure.

2. Specifying the repository in the settings.gradle file:

```gradle
dependencyResolutionManagement {
     repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
     repositories {
         mavenCentral()
         maven { url 'https://jitpack.io' }
     }
}
```
Don't forget to add this setting to your settings.gradle file to ensure the library will be available for download.
## Step 2: Initialize the library

In your activity or fragment, initialize the library like this:

```kotlin
val pinCodeStateManager = PinCodeStateManager.getInstance()
```

## Step 3: Configure Security Settings

Set security options such as maximum number of PIN attempts, PIN length, and biometric authentication activation:

```kotlin
pinCodeStateManager.setMaxPinAttempts(maxAttempts = 4, application = application)
pinCodeStateManager.setPinLength(pinLength = 4, application = application)
pinCodeStateManager.setBiometricEnabled(enabled = true, application = application)
pinCodeStateManager.autoLaunchBiometricEnabled(enabled = true,application = application)
```

## Step 4: Create, validate, change and delete PIN code

The library supports four main scenarios:

### 1. Create a PIN code

```kotlin
pinCodeStateManager.setScenario(PinCodeScenario.CREATION)
```

Allows the user to create a new PIN.

### 2. PIN code validation

```kotlin
pinCodeStateManager.setScenario(PinCodeScenario.VALIDATION)
```

The user can log into the application by entering their existing PIN. The library will check the entered PIN code for compliance and, if validated successfully, will provide access to the application.

### 3. Change PIN code

```kotlin
pinCodeStateManager.setScenario(PinCodeScenario.CHANGE)
```

Allows the user to change their current PIN to a new one.

### 4. Removing the PIN code

```kotlin
pinCodeStateManager.setScenario(PinCodeScenario.DELETION)
```

The user can delete their current PIN.

## Step 5: Event Handling

Handle events such as successful PIN creation, validation, change, and deletion, as well as input attempts exhaustion:

```kotlin
pinCodeStateManager.onCreationSuccess {
     // Handle successful PIN setting
}

pinCodeStateManager.onValidationSuccess {
     // Handle successful PIN validation
}

pinCodeStateManager.onLoginAttemptsExpended {
     // Handle exhaustion of PIN entry attempts
}

// and so on for other events
```

## Step 6: Integration with the application interface

Integrate the library with your application's interface

```kotlin
Surface(
     modifier = Modifier.fillMaxSize(),
     color = MaterialTheme.colorScheme.background
) {
     // Display a screen for entering or validating a PIN code
     PinCodeScreen()
}
```

These are the basic steps to use the library for PIN authentication in your Android application. Customize the library and handle events according to your needs to create a secure and seamless user experience.

## Experimental controller API (2.0.0-alpha01)

The new API is state-driven, does not depend on the process-global `PinCodeStateManager`, and can be tested with custom stores. The legacy API above remains available during migration.

```kotlin
val controller = remember {
    PinAuthController.create(
        context,
        PinAuthConfig(
            pinLength = 4,
            maxAttempts = 5,
            biometricEnabled = false,
            autoLaunchBiometric = false
        )
    )
}

PinAuthScreen(
    controller = controller,
    scenario = PinAuthScenario.VALIDATION,
    onResult = { result ->
        when (result) {
            PinAuthResult.Validated -> openProtectedContent()
            PinAuthResult.AttemptsExhausted -> startAccountRecovery()
            PinAuthResult.ResetRequested -> startAccountRecovery()
            else -> Unit
        }
    }
)
```

`PinAuthController.state` exposes immutable `StateFlow<PinAuthUiState>`. Terminal events are delivered through `results`, while `dispatch` accepts typed digit, backspace, and reset actions. Biometric support for this entry point will be added in a later alpha.

## Security and migration notes

- Version 1.0.6 stores a versioned PBKDF2 verifier with a random per-PIN salt. Existing SHA-256 records are migrated automatically after the first successful validation.
- PIN verification and storage run off the Compose UI thread.
- When the configured attempt limit is exhausted, PIN input stays locked across process restarts. The host application must provide recovery through `onResetPassword` and `clearConfiguration`.
- PIN length must be between 4 and 12 digits. The maximum attempt count must be between 1 and 100.
- A local PIN is an application lock, not a replacement for server-side authentication. Applications storing sensitive data should additionally protect cryptographic keys with Android Keystore and exclude PIN-related preferences from backup.

## Technical specifications:

Minimum SDK version (minSdk): 24

Target SDK version (targetSdk): 34

Gradle version: 8.5.2

OpenJDK Version: 17.0.1

## License

This library is distributed under the MIT license. Details can be found in the LICENSE file.

## Author

The library is developed and maintained by Khokhlin Vladimir. You can contact me via telegram [@vkhokhlin](https://t.me/vkhokhlin).

## Assistance

If you have suggestions for improving the library or find a bug, please create an issue or send a pull request to the GitHub repository.
