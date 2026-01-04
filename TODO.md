# TODO

This document outlines the remaining work required to get the project building successfully.

## Status
All compilation blockers have been resolved. The project builds successfully.

### Completed
- Fixed `R` class generation by using `androidx.appcompat.R.attr`.
- Fixed Kotlin and Java compilation errors.
- Provisioned JDK 21.
- **Migrate to RxJava 3**: Successfully migrated from EOL RxJava 1.3.8 to RxJava 3.1.8.

## Future Work

### Tech Debt & Modernization
- **Implement Algolia ETag Support**:
  - **Reason**: `TODO` found in codebase. Improves network efficiency.
  - **Context**: Started in `feature/algolia-etag`.
