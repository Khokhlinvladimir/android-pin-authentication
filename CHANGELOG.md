# Changelog

## 2.0.0-alpha04

- Add `PinMaskStyle.showSuccessCheck` to hide the success check while preserving the merge animation.

## 2.0.0-alpha03

- Add `PinAuthCustomization`, `PinKeypadStyle`, and `PinMaskStyle` for reusable visual configuration.
- Add composable slots for fully custom digit content and PIN masking without replacing input logic.
- Support the same customization object in both controller and legacy Compose APIs.
- Keep entered PIN values private: custom masks receive counts and feedback state only.

## 2.0.0-alpha02

- Add a configurable premium motion system through `PinAuthMotionSpec`.
- Animate PIN entry dots, deletion, processing, error shake, lockout, and success state.
- Add spring press feedback, bounded ripple, disabled transitions, and optional haptics to the keypad.
- Add animated title, notification, scenario, and host-screen transitions.
- Add a subtle animated security background to the state-driven API.
- Replace slow runtime PBKDF2 checks with Android Keystore-backed HMAC verification and migrate older verifiers after a successful check.
- Restore biometric validation and the confirmation dialog for forgotten-PIN recovery in the controller UI.
- Preserve the existing `PinAuthScreen`, `PinAuthContent`, `Keyboard`, and legacy screen signatures.

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
