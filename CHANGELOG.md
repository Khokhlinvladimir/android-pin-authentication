# Changelog

## 2.0.0-alpha01

- Add an injectable `PinAuthController` with immutable `StateFlow` state and typed results.
- Add public credential and attempt-store contracts for JVM testing and custom persistence.
- Add a state-driven `PinAuthScreen` Compose entry point that does not use the legacy singleton.
- Keep the existing `PinCodeScreen` API available for backwards compatibility.
- Add controller unit tests and real-device coverage for the new Compose entry point.

## 1.0.6

- Replace the fast SHA-256 PIN verifier with a versioned PBKDF2 verifier and per-PIN random salt.
- Migrate legacy SHA-256 verifiers after a successful PIN check.
- Enforce a persistent lock after the configured attempt limit is exhausted.
- Delete the stored PIN during the deletion scenario.
- Move expensive PIN storage and verification work off the Compose UI thread.
- Remove the process-global keyboard listener and prevent duplicate scenario callbacks.
- Validate PIN length and maximum-attempt configuration.
- Fix biometric prompt lifecycle handling and the unnecessary runtime permission request.
- Add unit and Android instrumentation regression tests.
