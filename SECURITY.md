# Security Policy

KB-Ai is a keyboard project. An input method may process sensitive text, so security and privacy defects are treated as high priority.

## Supported versions

There are currently no released application versions. Security review applies to the repository and to all future release candidates.

Once releases exist, this section will list supported versions and their security-fix policy.

## Reporting a vulnerability

Please do not publish exploit details, private text, passwords, tokens, or sensitive screenshots in a public issue.

Use GitHub's **Private vulnerability reporting** for this repository when it is enabled. If private reporting is unavailable, contact the repository maintainer through the GitHub profile:

https://github.com/snowgemm

Include only the information needed to understand and reproduce the issue. Do not include real user-entered content or credentials.

## Security priorities

- No collection or transmission of typed content.
- No typed-content logging, crash-report attachments, or analytics.
- Password and other sensitive fields must not feed suggestions or personalization.
- Clipboard access must be user-initiated; clipboard history is off by default.
- Native calls must validate inputs and convert failures into controlled errors.
- Rust panics and Java exceptions must not cross the JNI boundary unchecked.
- Third-party assets and datasets must have verified provenance and compatible licenses.

See [Security and Privacy Design](docs/SECURITY_PRIVACY.md).
