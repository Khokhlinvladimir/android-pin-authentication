# Changelog

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
