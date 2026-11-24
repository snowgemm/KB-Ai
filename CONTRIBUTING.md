# Contributing to KB-Ai

Thank you for considering a contribution.

KB-Ai currently has a Phase 0 Android/Rust integration prototype. It is not yet a production keyboard and does not yet implement QWERTY, Telex, VNI, or prediction.

## Before starting

1. Read the [Product Specification](docs/PRODUCT_SPEC.md).
2. Read the [Architecture](docs/ARCHITECTURE.md) and relevant [ADRs](docs/adr/).
3. Check the [Roadmap](docs/ROADMAP.md) and existing GitHub issues.
4. For a new feature, discuss user value, privacy impact, platform requirements, and maintenance cost before implementation.

Avoid adding dependencies or splitting modules without a clear technical reason.

## Toolchain

Use the versions recorded in the README and `rust-toolchain.toml`. Android SDK Platform 37, Build Tools 37.0.0, NDK 30.0.16248370, JDK 17, Rust 1.100.0, and cargo-ndk 4.1.2 are required for the current prototype.

## Local validation

```sh
cargo fmt --all --manifest-path native/Cargo.toml -- --check
cargo test --workspace --manifest-path native/Cargo.toml
cargo clippy --workspace --manifest-path native/Cargo.toml --all-targets -- -D warnings
./gradlew --no-daemon :app:testDebugUnitTest :app:assembleDebug
```

On Windows, use `gradlew.bat` for the last command. These commands are the expected checks; report their actual results rather than assuming they passed.

## Contribution expectations

- Keep changes focused and explain the problem they solve.
- Preserve the separation between Android platform code and the Rust input core.
- Never log, upload, or add real user-entered text to test fixtures.
- Do not add dictionaries, language models, themes, images, fonts, or other assets without recording their source and license.
- Do not add network access, analytics, telemetry, accounts, or cloud processing without an explicit product and privacy review.
- Do not claim a feature works until it has been tested in a real Android input field.
- Update the relevant documentation when a design or security decision changes.

## Code quality

- Format Rust with rustfmt.
- Run Rust tests and Clippy for changed crates.
- Run Android unit tests, lint, and relevant build variants.
- Test the IME in multiple editor types, including password and numeric fields.
- Test Unicode behavior with combining marks and multi-code-point emoji.
- Include regression tests for every fixed input or lifecycle bug.
- Keep native interfaces small, typed, and versioned.

## Pull requests

- Open an issue first for substantial features or architectural changes.
- Use a clear title and describe behavior changes, implementation boundaries, and test evidence.
- Include sanitized screenshots or recordings for UI changes when useful.
- List limitations and follow-up work honestly.
- Keep unrelated formatting or refactoring out of the change.

## Commit messages

Use Conventional Commits where practical:

- feat: add a user-facing capability
- fix: correct an observable defect
- docs: update documentation
- test: add or improve tests
- refactor: restructure code without changing behavior
- build: change build tooling or dependencies
- chore: maintenance work

## Security reports

Do not disclose vulnerabilities in public issues. Follow the private reporting guidance in [SECURITY.md](SECURITY.md).
