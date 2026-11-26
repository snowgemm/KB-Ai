# Agent instructions

## Required workflow
1. Read `README.md`, `CONTRIBUTING.md`, `docs/PRODUCT_SPEC.md`, `docs/ARCHITECTURE.md`, `docs/SECURITY_PRIVACY.md`, relevant ADRs, and `docs/TEST_PLAN.md` before editing.
2. Inspect the current Kotlin, JNI, Rust, Gradle, NDK, and CI implementation. Verify external APIs and toolchain versions against current official documentation.
3. Define the expected input/editor behavior, affected layers, privacy impact, and test plan before non-trivial changes.
4. Keep changes within the smallest responsible layer. Kotlin owns Android lifecycle, keyboard UI, selection, and InputConnection; Rust owns only active Vietnamese composition.
5. Add deterministic Telex/VNI, editor-policy, lifecycle, or JNI tests for changed behavior. Never weaken existing test vectors to make an implementation pass.
6. Run the architecture/privacy guard, Rust fmt/tests/Clippy, Android Lint, Android unit tests, and APK build.
7. Re-read the full diff and surrounding code. Report exact test evidence and explicitly identify physical-device checks that remain outstanding.

## Enforced architecture and privacy boundaries
- Core typing is offline. Do not add Internet permission, network requests, analytics, clipboard monitoring, or typed-content telemetry without a reviewed ADR and explicit approval.
- Never log, persist, upload, or retain committed editor text or raw keystrokes.
- Keep Bamboo Core full-text tracking disabled.
- Password, email, URL, numeric, and phone editors must bypass Vietnamese composition according to the documented editor policy.
- Kotlin owns InputConnection and all Android framework interaction. Rust receives only validated key code points and session handles.
- Validate native handles and Unicode scalar values. Destroy native sessions at editor lifecycle boundaries.
- Keep the synchronous key path bounded; do not add disk, database, network, or unbounded work to it.

## Change safety
- Do not introduce a new dependency or native API without evaluating maintenance, license, security, binary size, and runtime cost.
- Do not claim the keyboard is stable from unit tests or CI alone. Physical-device IME behavior remains a separate acceptance gate.
- Keep action permissions minimal and third-party GitHub Actions pinned to full commit SHAs.
