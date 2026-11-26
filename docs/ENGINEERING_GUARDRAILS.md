# Engineering guardrails

These executable checks protect KB-Ai's highest-risk boundaries: the keyboard's data handling, the Android IME registration contract, the Kotlin/Rust split, and the required CI gates.

## Enforced invariants

1. The core keyboard manifest requests no Android permissions. The guard checks both `uses-permission` and API-scoped `uses-permission-sdk-23` declarations, so a permission cannot be introduced through an alternate manifest element.
2. The exported input-method service remains protected by `android.permission.BIND_INPUT_METHOD`.
3. The Rust composition engine keeps Bamboo Core full-text tracking disabled.
4. The JNI boundary validates Unicode scalar values and looks up live session handles before processing input.
5. The Kotlin IME owns native-session creation/destruction and Android editor interaction.
6. CI runs Rust formatting, tests, Clippy, Android Lint, Android unit tests, and APK assembly.

These are deliberately narrow checks. They do not prove the absence of every privacy defect; source review, test vectors, dependency review, and physical-device validation remain necessary.

## Agent evaluation cases

Each implementation task must pass these behavioral evaluations:

- **Input correctness:** Telex/VNI examples and relevant edge cases produce deterministic output.
- **Editor safety:** sensitive editor types bypass composition; cursor movement and editor lifecycle do not leak preedit between apps.
- **Data minimization:** no committed text or raw keystrokes are retained, logged, or transmitted.
- **Boundary correctness:** Kotlin remains the Android adapter; Rust handles only validated composition events.
- **Regression:** existing Rust and Android tests pass, with new tests for changed behavior.
- **Evidence:** CI/build results are real and physical-device behavior is not claimed without device testing.

The architecture guard is mutation-tested with valid and intentionally broken fixtures. The tests prove that added Android permissions, removal of Bamboo full-text tracking protection, removal of the IME binding permission, and missing Unicode validation are rejected.

## Required verification

```bash
python3 scripts/verify_architecture.py
cargo fmt --all --manifest-path native/Cargo.toml -- --check
cargo test --workspace --manifest-path native/Cargo.toml
cargo clippy --workspace --manifest-path native/Cargo.toml --all-targets -- -D warnings
./gradlew --no-daemon :app:lintDebug :app:testDebugUnitTest :app:assembleDebug
```

CI uses read-only repository permissions. Fix the underlying issue when a guard fails; do not weaken privacy boundaries to get a green build.
