# Materialistic TODO

## High Priority
- [x] Migrate to RxJava 3 <!-- id: 5 -->
- [x] Fix OPML structure <!-- id: 6 -->
- [x] Ensure all compilation blockers are resolved <!-- id: 7 -->
- [x] Resolve Dependabot PR failures (#17, #18, #20, #21) <!-- id: 8 -->
- [x] Upgrade to Android SDK 35 (Vanilla Ice Cream) and fix compilation blockers <!-- id: 13 -->

## Feature Parity & Cleanup
- [x] Investigate Waydroid installation issues <!-- id: 9 -->
- [/] Review PRs for code quality and memory leaks <!-- id: 10 -->
    - [x] Fix memory leaks in adapters <!-- id: 11 -->
    - [x] Improve injection sites <!-- id: 12 -->

## Future Work
- [ ] Consider upgrading `minSdk` to 28 for architectural benefits <!-- id: 14 -->
    - Pros: native BiometricPrompt, Display Cutout support, fewer SDK checks
    - Cons: reduces reach by ~5%
- [ ] Implement Algolia ETag persistence with LruCache <!-- id: 15 -->
- [ ] Adjust NetworkModule caching strategy <!-- id: 16 -->
