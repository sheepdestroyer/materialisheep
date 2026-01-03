# TODO

This document outlines the remaining work required to get the project building successfully.

## Status
All compilation blockers have been resolved. The project builds successfully.

### Completed
- Fixed `R` class generation by using `androidx.appcompat.R.attr`.
- Fixed Kotlin and Java compilation errors.
- Provisioned JDK 21.

## Future Work

### Tech Debt & Modernization
- **Migrate to RxJava 3**:
  - **Reason**: Current codebase uses RxJava 1.3.8 (EOL). Reviewers strongly recommended upgrading to RxJava 3.
  - **Context**: This was deferred from the Dagger 2 migration PR (#13) to avoid excessive scope creep and complexity, as RxJava 3 introduces significant breaking changes (e.g., `Flowable`, null-safety) that should be handled in isolation.
