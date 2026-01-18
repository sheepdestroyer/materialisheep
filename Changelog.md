# Changelog

## [UNRELEASED]

## [3.0] - 2026-06-XX
### Added
- Added GitHub Actions workflows for Continuous Integration and Release builds.
- The `CI.md` file documents the new CI/CD pipeline.
- `AGENTS.md` provides instructions for setting up the development environment.
- `TODO.md` tracks the remaining build issues.
- Created a new `MaterialisticApplication` class to initialize Dagger 2.
- Created a new `ApplicationComponent` and `ApplicationModule` for Dagger 2.

### Changed
- **Upgraded build environment to support Java 21.**
  - Gradle: 7.5.1 → 8.12.
  - Android Gradle Plugin: 7.4.2 → 8.9.1.
  - Kotlin: 1.8.20 → 2.3.0.
  - CI/CD workflows: Updated to use Java 21.
- **Migrated the dependency injection framework from Dagger 1 to Dagger 2.51.1.**
  - Replaced all Dagger 1 annotations and modules with their Dagger 2 equivalents.
  - Refactored all activities and fragments to use the new Dagger 2 component for injection.
- Replaced Mercury Web Parser with local Readability.js implementation.
- Fixed `R.attr` resource resolution issues by migrating to `androidx.appcompat.R.attr`.
- Updated various AndroidX and Google Material dependencies to their latest versions.
- Set `compileSdk` and `targetSdk` to 36.

### Removed
- Removed the obsolete Dagger 1 dependency (`com.squareup.dagger:dagger:1.2.5`).
- Removed all Dagger 1-related classes and interfaces, including `InjectableActivity` and `Injectable`.
