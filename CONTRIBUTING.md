# Contributing to KB-Ai

Thank you for considering a contribution.

KB-Ai is currently in the planning and specification phase. The architecture and product requirements are documented, but the application and native engine have not been implemented.

## Before starting

1. Read the [Product Specification](docs/PRODUCT_SPEC.md).
2. Read the [Architecture](docs/ARCHITECTURE.md) and relevant [ADRs](docs/adr/).
3. Check the [Roadmap](docs/ROADMAP.md) and existing GitHub issues.
4. For a new feature, discuss its user value, privacy impact, platform requirements, and maintenance cost before implementation.

Please avoid adding dependencies or splitting modules without a clear technical reason.

## Contribution expectations

- Keep changes focused and explain the problem they solve.
- Preserve the separation between Android platform code and the Rust input core.
- Never log, upload, or add real user-entered text to test fixtures.
- Do not add dictionaries, language models, themes, images, fonts, or other assets without recording their source and license.
- Do not add network access, analytics, telemetry, accounts, or cloud processing without an explicit product and privacy review.
- Do not claim a feature works until it has been tested in a real Android input field.
- Update the relevant documentation when a design or security decision changes.

## Code quality

When implementation begins:

- Format Rust with rustfmt.
- Run Rust tests and Clippy for changed crates.
- Run Android unit tests, lint, and the relevant build variants.
- Test the IME in multiple editor types, including password and numeric fields.
- Test Unicode behavior with combining marks and multi-code-point emoji.
- Include regression tests for every fixed input or lifecycle bug.
- Keep native interfaces small, typed, and versioned.

The exact commands will be added after the Gradle and Cargo project structure has been created and verified. Do not copy commands from an unrelated project and assume they work here.

## Pull requests

- Open an issue first for substantial features or architectural changes.
- Use a clear title and describe the behavior change, implementation boundaries, and test evidence.
- Include screenshots or short recordings for UI changes when they do not expose private information.
- List any limitations and follow-up work honestly.
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

## Reporting security issues

Do not disclose vulnerabilities in public issues. Follow the private reporting guidance in [SECURITY.md](SECURITY.md).
