

# Materialistic TODO

## Feature Parity & Cleanup
- [/] Review PRs for code quality and memory leaks <!-- id: 10 -->
- [x] Fix Gradle Deprecation Warnings (Issue #46) <!-- id: 21 -->

## Deprecation Refactoring (Long-term)
- [ ] **Phase 1: Fragment API Modernization** <!-- id: 17 -->
    - Replace `setHasOptionsMenu`/`onOptionsItemSelected` with `MenuProvider`
    - Replace `onActivityCreated` with `onViewCreated`
    - Replace `setRetainInstance` with `ViewModel`
    - Migrate `FragmentStatePagerAdapter` to `ViewPager2`
- [ ] **Phase 2: System & Device API Migration** <!-- id: 18 -->
    - Migrate `NetworkInfo` to `ConnectivityManager.NetworkCallback`
    - Update `Vibrator` usage to `VibrationEffect`
    - Adopt `WindowMetrics` and `WindowInsetsController`
    - Implement Edge-to-Edge (replace `setStatusBarColor`)
- [ ] **Phase 3: Widget & View Cleanup** <!-- id: 19 -->
    - Update `RemoteViews` adapter API
    - Fix `setLayoutFrozen` (RecyclerView) and `BottomSheetCallback`
    - Update Preferences to AndroidX Preferences
- [ ] **Phase 4: Architecture Components** <!-- id: 20 -->
    - Replace `LocalBroadcastManager` with `SharedFlow`/`LiveData`
    - Replace `readArrayList` with type-safe deserialization

## Future Work
- [ ] Consider upgrading `minSdk` to 28 for architectural benefits <!-- id: 14 -->
    - Pros: native BiometricPrompt, Display Cutout support, fewer SDK checks
    - Cons: reduces reach by ~5%
- [ ] Implement Algolia ETag persistence with LruCache <!-- id: 15 -->
- [ ] Adjust NetworkModule caching strategy <!-- id: 16 -->
