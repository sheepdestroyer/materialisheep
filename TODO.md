

# Materialistic TODO

## Feature Parity & Cleanup
- [x] Review PRs for code quality and memory leaks <!-- id: 10 -->
- [x] Fix Gradle Deprecation Warnings (Issue #46) <!-- id: 21 -->

## Deprecation Refactoring (Long-term)
- [x] **Phase 1: Fragment API Modernization** <!-- id: 17 -->
    - [x] Replace `setHasOptionsMenu`/`onOptionsItemSelected` with `MenuProvider`
    - [x] Replace `onActivityCreated` with `onViewCreated`
    - [x] Replace `setRetainInstance` with `ViewModel`
    - [x] Migrate `FragmentStatePagerAdapter` to `ViewPager2`
- [ ] **Phase 2: System & Device API Migration** <!-- id: 18 -->
    - [ ] Migrate `NetworkInfo` to `ConnectivityManager.NetworkCallback`
    - [x] Update `Vibrator` usage to `VibrationEffect`
    - [ ] Adopt `WindowMetrics` and `WindowInsetsController`
    - [ ] Implement Edge-to-Edge (replace `setStatusBarColor`)
- [ ] **Phase 3: Widget & View Cleanup** <!-- id: 19 -->
    - [x] Update `RemoteViews` adapter API
    - [ ] Fix `setLayoutFrozen` (RecyclerView) and `BottomSheetCallback`
    - [x] Update Preferences to AndroidX Preferences
- [ ] **Phase 4: Architecture Components** <!-- id: 20 -->
    - [ ] Replace `LocalBroadcastManager` with `SharedFlow`/`LiveData`
    - [x] Replace `readArrayList` with type-safe deserialization (`ParcelCompat`)

## Future Work
- [x] Upgraded `minSdk` to 31 for modern RemoteViews and Android 12+ capabilities <!-- id: 14 -->
- [ ] Implement Algolia ETag persistence with LruCache <!-- id: 15 -->
- [ ] Adjust NetworkModule caching strategy <!-- id: 16 -->
