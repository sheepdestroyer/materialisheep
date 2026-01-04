# CI/CD Workflows

This document explains the CI/CD workflows for this project, which are managed using GitHub Actions.

## Workflows

There are two main workflows:

1.  **CI (Continuous Integration):** This workflow runs on every pull request to the `master` branch.
2.  **Release:** This workflow runs whenever a new tag is pushed to the repository.

### CI Workflow

The CI workflow is defined in the file `.github/workflows/ci.yml`. It performs the following steps:

1.  **Sets up the environment:** It sets up an Ubuntu environment with Java 21 and the Android SDK (API level 36).
2.  **Caches dependencies:** It caches the Gradle dependencies to speed up future builds.
3.  **Runs tests:** It runs the unit tests using the command `./gradlew test`.

This workflow ensures that all pull requests are tested before they are merged into the `master` branch.

### Release Workflow

The Release workflow is defined in the file `.github/workflows/release.yml`. It performs the following steps:

1.  **Sets up the environment:** It sets up an Ubuntu environment with Java 21 and the Android SDK (API level 33).
2.  **Caches dependencies:** It caches the Gradle dependencies to speed up future builds.
3.  **Builds a release APK:** It builds a release APK using the command `./gradlew assembleRelease`.
4.  **Uploads the APK:** It uploads the generated APK as a release artifact.

This workflow automates the process of building a release APK whenever a new version is tagged.

## Validating the Workflows

To validate the workflows, you can do the following:

*   **CI Workflow:** Create a new pull request to the `master` branch. The CI workflow will be automatically triggered, and you can see the results in the "Actions" tab of the repository.
*   **Release Workflow:** Push a new tag to the repository (e.g., `git tag v1.0.0 && git push origin v1.0.0`). The Release workflow will be automatically triggered, and you can find the generated APK in the "Releases" section of the repository.

