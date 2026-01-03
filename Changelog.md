# Changelog

## [UNRELEASED]

## [3.0] - 2026-06-XX
### Changed
- Migrated dependency injection from Dagger 1 to Dagger 2.51.1.
- Replaced Mercury Web Parser with local Readability.js implementation.
- Upgraded build system: Gradle 8.9, AGP 8.x, Java 21, Kotlin 2.0.0.
- Upgraded `compileSdk` and `targetSdk` to 33.
- Fixed `R.attr` resource resolution issues by migrating to `androidx.appcompat.R.attr`.

### Added
- Added GitHub Actions workflows for Continuous Integration and Release builds.
- Added `CI.md` to document the new CI/CD pipeline.
- Added `AGENTS.md` with instructions for setting up the development environment.
- Added `TODO.md` to track the remaining build issues.
- Created a new `MaterialisticApplication` class to initialize Dagger 2.
- Created a new `ApplicationComponent` and `ApplicationModule` for Dagger 2.

### Changed
- **Upgraded the entire build environment to support Java 21.**
  - Upgraded Gradle from 7.5.1 to 8.6.
  - Upgraded Android Gradle Plugin from 7.4.2 to 8.4.0.
  - Upgraded Kotlin from 1.8.20 to 2.0.0.
  - Updated CI/CD workflows to use Java 21.
- **Migrated the dependency injection framework from Dagger 1 to Dagger 2.**
  - Replaced all Dagger 1 annotations and modules with their Dagger 2 equivalents.
  - Refactored all activities and fragments to use the new Dagger 2 component for injection.
- Updated various AndroidX and Google Material dependencies to their latest versions.
- Set `compileSdk` and `targetSdk` to 33.

### Removed
- Removed the obsolete Dagger 1 dependency (`com.squareup.dagger:dagger:1.2.5`).
- Removed all Dagger 1 related classes and interfaces, including `InjectableActivity` and `Injectable`.
