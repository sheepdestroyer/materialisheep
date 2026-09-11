# Changelog

## [UNRELEASED]

### Changed
- **Release Automation**: Updated `.github/workflows/release.yml` to package and publish `materialisheep-<tag>.apk` directly to GitHub Releases with atomic asset creation.

## [0.2] - 2026-09-11

### Added
- **Unit Test Coverage Expansion**:
  - `AndroidUtilsTest`: Comprehensive unit test suite for `AndroidUtils.TextUtils.equals` (null checks, length differences, CharSequence/String variants) and `AndroidUtils.TextUtils.isEmpty`.
  - `AppUtilsTest`: Added coverage for resource dimensions (`getDimension`, `getDimensionInDp`), HN URL validation (`isHackerNewsUrl`), item/user URI creation (`createItemUri`, `createUserUri`), intent data ID extraction (`getDataUriId`), themed attribute resolution (`getThemedResId`), navigation orchestration (`navigate`), display height (`getDisplayHeight`), sharing intent resolution (`share`), chooser intents (`makeSendIntentChooser`), connectivity verification (`isOnWiFi`), activity restart (`restart`), FAB visibility toggles (`toggleFab`), status bar color & dimming (`setStatusBarColor`, `setStatusBarDim`), WebView zoom controls (`toggleWebViewZoom`), and custom layout inflater instantiation (`createLayoutInflater`).
  - `PreferencesTest`: Added coverage for username management (`getUsername`, `setUsername`), ad-block toggling (`adBlockEnabled`), and comment draft management (`saveDraft`, `getDraft`, `deleteDraft`, `clearDrafts`).
  - `CacheableWebViewTest`: Positive archive loading, invalid extension/prefix rejection, sibling directory escape isolation, and disabled file/content access checks.
  - `FileDownloaderTest`: Error handling, 404 response handling, SHA-256 cache file verification, and network failure cleanup.
  - `SinglePageItemRecyclerViewAdapterTest`: RecyclerView item state restoration and `ParcelCompat` verification.
  - `NavFloatingActionButtonTest`: Touch drag offsets and vibration null guards.
  - `AlgoliaClientTest` & `HackerNewsClientTest`: Null-safe error messages, async error handling, and item caching.
  - `HackerNewsItemTest`, `FavoriteTest`, `SessionManagerTest`, `AdBlockerTest`, `SubmitActivityTest`.

### Changed
- **Upgraded Build Toolchain**:
  - Gradle: 8.12 → 9.2.1.
  - Android Gradle Plugin (AGP): 8.9.1 → 9.2.1.
  - Kotlin: 2.3.0 → 2.4.0.
  - KSP: 2.3.9.
  - `minSdkVersion`: 24 → 31 (for modern `RemoteViews` collection builder APIs).
  - Target/Compile SDK: 36.
- **Performance Optimizations**:
  - `StoryRecyclerViewAdapter`: Replaced $O(N)$ linear scans with $O(1)$ ID lookup maps (`mIdToItem`, `mIdToPosition`).
  - `ThreadPreviewRecyclerViewAdapter`: Replaced linear list contains checks with `HashSet`.
  - `SubmitActivity`: Pre-compiled URL validation regex pattern.
- **Modernization & Deprecations**:
  - `WidgetHelper`: Migrated to `FLAG_UPDATE_CURRENT | FLAG_MUTABLE` on collection item pending intent templates for Android 12+ compatibility.
  - `ThemedActivity`: Migrated from deprecated `ActivityManager.TaskDescription` constructors to modern `TaskDescription.Builder`.
  - `SinglePageItemRecyclerViewAdapter`: Migrated `Parcel.readArrayList` to `ParcelCompat.readArrayList`.
  - `PreferencesActivity`: Migrated from deprecated `Fragment.instantiate` to direct instantiation.
  - `NavFloatingActionButton`: Migrated legacy `Vibrator.vibrate` calls to `VibrationEffect`.
  - `HackerNewsItem`: Removed obsolete `@Keep private long[] parts;` array and dead code annotations.
  - `ItemActivity`: Removed obsolete `@SuppressWarnings("deprecation")` annotation following migration to `BundleCompat` and `IntentCompat`.

### Fixed
- **Security & Robustness**:
  - `AppUtils`: Hardened `getDataUriId` against null intents, null `altParamId`, and non-hierarchical/opaque URIs (`data.isHierarchical()`).
  - Mitigated Local File Inclusion (LFI) in `CacheableWebView` by disabling file/content access and enforcing strict canonical directory parent checks.
  - Mitigated path traversal in `FileDownloader` by hashing URLs to SHA-256 hex strings and sandboxing files in `cacheDir`.
  - Prevented temporary file collision in `FileDownloader` by appending unique UUIDs.
  - Removed exposed GitHub personal access tokens from `FeedbackClient` and `app/build.gradle`.
  - Disabled global cleartext HTTP traffic in `network_security_config.xml`.
- **Concurrency & Lifecycle**:
  - `SyncDelegate`: Ensured atomic single-finish contract with `AtomicBoolean`, synchronized all `SyncProgress` progress mutators, and declared `title` as `volatile`.
  - `ItemSyncJobService`: Implemented atomic map removal on `onStopJob`.
  - `SubmitActivity`: Ensured offscreen `WebView` chrome client dereference and clean destruction in `onDestroy`.
  - `LazyLoadFragment`: Reset `mLoaded` in `onDestroyView()` to prevent blank fragments when navigating back.
  - `SinglePageItemRecyclerViewAdapter`: Fixed negative modulo remainder calculation crash in `getThreadColor()`, and dereferenced recycled `mColors`.
  - `UserServicesClient`: Closed response bodies in `finally` blocks and on redirect errors.
  - `AlgoliaClient`: Guarded error messages against null and validated numeric `objectID` strings.
  - `AppUtils`: Added `Intent.FLAG_ACTIVITY_NEW_TASK` for launches from non-Activity contexts.

## [4.0] - 2026-XX-XX
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
- **Target SDK 36 Compatibility (PR #51):**
  - Suppressed various deprecation warnings (NetworkInfo, Fragment APIs, Parcellable, etc.) to ensure a clean build with `targetSdk` 36 (Issue #46).
  - Added TODOs for future refactoring and migration of suppressed APIs.
- **Modernized Fragment and View Pager Implementations (PR #56):**
  - Migrated from deprecated `ViewPager` to `ViewPager2` using `FragmentStateAdapter`.
  - Replaced `onActivityCreated` with `onViewCreated`.
  - Replaced `setRetainInstance` with `LazyLoadViewModel` for state retention.
  - Implemented `MenuProvider` API for handling menus, replacing `setHasOptionsMenu`.

### Fixed
- Resolved `NetworkOnMainThreadException` crash in `ReadabilityClient` by offloading database caching to background thread.
- Fixed memory leaks and resource management in `ReadabilityClient` and `WebFragment` by implementing proper `destroy()` lifecycle and using `CompositeDisposable`.




### Removed
- Removed the obsolete Dagger 1 dependency (`com.squareup.dagger:dagger:1.2.5`).
- Removed all Dagger 1-related classes and interfaces, including `InjectableActivity` and `Injectable`.
- Removed temporary `CustomCrashHandler` used for debugging.

