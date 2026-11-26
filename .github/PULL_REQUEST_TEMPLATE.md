## Summary

Describe the change and the user problem it solves.

## Scope

- [ ] Product behavior
- [ ] Android / IME integration
- [ ] Rust input engine
- [ ] UI / UX
- [ ] Security / privacy
- [ ] Documentation
- [ ] Build / CI

## Validation

- [ ] Architecture guard and mutation tests pass.
- [ ] Relevant Rust tests and Clippy pass.
- [ ] Android Lint and unit tests pass.
- [ ] Build succeeds for affected native and Android targets.
- [ ] Physical-device testing completed where required.
- [ ] Limitations and untested paths are listed with exact evidence.

## Privacy and security

- [ ] No typed content, password, clipboard content, or private text was added to logs, fixtures, or telemetry.
- [ ] No new permission, network request, telemetry, or persistent user-data path was added without review.
- [ ] Sensitive editor types still bypass Vietnamese composition.
- [ ] Kotlin owns InputConnection and Android lifecycle; Rust receives only validated input events.
- [ ] Native session handles and destruction paths remain safe.
- [ ] New dependencies and assets have been reviewed for maintenance and license.

## Independent review

- [ ] The complete diff was reviewed independently of the implementing agent.
- [ ] Existing tests were not weakened or removed to make the change pass.
- [ ] No unrelated changes or unexplained generated files remain.
- [ ] Any architectural change has an ADR.

## Documentation

- [ ] Relevant product, architecture, security, and test documents are updated.

## Screenshots / evidence

Add sanitized screenshots, recordings, test output, or benchmark results when relevant. Do not include private user content.
