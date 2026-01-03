# Materialistic for Hacker News

Materialistic is a Hacker News client for Android that is built with a clean, modern architecture and a focus on Material Design principles. It uses the official [HackerNews/API], [Algolia Hacker News Search API], and [Mercury Web Parser API] to provide a fast, reliable, and feature-rich experience.

## Project Overview

Materialistic follows a modular architecture that separates concerns and promotes maintainability. The core components of the application are:

*   **Data Layer:** The data layer is responsible for fetching and caching data from the Hacker News API and other sources. It uses [Retrofit] for making network requests and [Room] for local data persistence.
*   **Domain Layer:** The domain layer contains the business logic of the application. It is responsible for transforming data from the data layer into a format that is easy for the presentation layer to consume.
*   **Presentation Layer:** The presentation layer is responsible for displaying data to the user and handling user interactions. It uses the Model-View-ViewModel (MVVM) design pattern to separate the UI from the business logic.

## Core Components

The codebase is organized into the following packages:

*   `data`: Contains the data models, network clients, and local data sources for the application.
*   `accounts`: Contains classes for managing user accounts.
*   `appwidget`: Contains classes for implementing home screen widgets.
*   `ktx`: Contains Kotlin extension functions that are used throughout the application.
*   `activities`: Contains the activities that make up the application's UI.
*   `fragments`: Contains the fragments that are used to build the activities' UIs.
*   `viewmodels`: Contains the ViewModels that are used by the activities and fragments.

## Setup

### Requirements

*   JDK 21
*   Latest Android SDK tools
*   Latest Android platform tools
*   AndroidX

### Dependencies

*   [Official Hacker News API][HackerNews/API]
*   [Algolia Hacker News Search API]
*   [Mercury Web Parser API]
*   [Android Jetpack]
*   [Retrofit]
*   [OkHttp]
*   [Dagger]
*   [RxJava] & [RxAndroid]
*   [PDF.js]

### Build

1.  Clone the repository:
    ```
    git clone https://github.com/hidroh/materialistic.git
    ```
2.  Create a `keystore.properties` file in the root of the project and add the following properties:
    ```
    MERCURY_TOKEN=<your_mercury_web_parser_api_key>
    ```
    You can get a Mercury Web Parser API key [here][mercury].
3.  Build the project:
    ```
    ./gradlew assembleDebug
    ```
4.  To build with LeakCanary enabled, run:
    ```
    ./gradlew assembleDebug -Pleak
    ```

## Code Style

This project follows the official [Kotlin style guide](https://developer.android.com/kotlin/style-guide). Please make sure your contributions adhere to these guidelines.

## Articles

*   [Supporting multiple themes in your Android app (Part 1)][article-theme1]
*   [Supporting multiple themes in your Android app (Part 2)][article-theme2] [![][Android Weekly 144 Badge]][Android Weekly 144]
*   [Building custom preferences with preference-v7][article-preference]
*   [Hacking up an ad blocker for Android][article-adblocker]
*   [Bottom sheet everything][article-bottom-sheet] [![][AndroidDev Digest 99 Badge]][AndroidDev Digest 99] [![][Android Weekly 227 Badge]][Android Weekly 227]

## Screenshots

<img src="assets/screenshot-1.png" width="200px" />
<img src="assets/screenshot-2.png" width="200px" />
<img src="assets/screenshot-3.png" width="200px" />
<img src="assets/screenshot-4.png" width="600px" />

## Contributing

Contributions are always welcome. Please make sure you read [Contributing notes](CONTRIBUTING.md) first.

## License

    Copyright 2015 Ha Duy Trung

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

[Hacker News]: https://news.ycombinator.com/
[HackerNews/API]: https://github.com/HackerNews/API
[Play Store]: https://play.google.com/store/apps/details?id=io.github.hidroh.materialistic&referrer=utm_source%3Dgithub
[Play Store Badge]: https://play.google.com/intl/en_us/badges/images/badge_new.png
[Algolia Hacker News Search API]: https://github.com/algolia/hn-search
[Mercury Web Parser API]: https://mercury.postlight.com/web-parser/
[Android Jetpack]: https://developer.android.com/jetpack/
[Room]: https://developer.android.com/topic/libraries/architecture/room
[Retrofit]: https://github.com/square/retrofit
[OkHttp]: https://github.com/square/okhttp
[Dagger]: https://github.com/square/dagger
[RxJava]: https://github.com/ReactiveX/RxJava
[RxAndroid]: https://github.com/ReactiveX/RxAndroid
[mercury]: https://mercury.postlight.com/web-parser/
[article-theme1]: http://www.hidroh.com/2015/02/16/support-multiple-themes-android-app/
[article-theme2]: http://www.hidroh.com/2015/02/25/support-multiple-themes-android-app-part-2/
[article-preference]: http://www.hidroh.com/2015/11/30/building-custom-preferences-v7/
[article-adblocker]: http://www.hidroh.com/2016/05/19/hacking-up-ad-blocker-android/
[article-bottom-sheet]: http://www.hidroh.com/2016/06/17/bottom-sheet-everything/
[Android Weekly 144 Badge]: https://img.shields.io/badge/android--weekly-144-blue.svg
[Android Weekly 227 Badge]: https://img.shields.io/badge/android--weekly-227-blue.svg
[Android Weekly 144]: http://androidweekly.net/issues/issue-144
[Android Weekly 227]: http://androidweekly.net/issues/issue-227
[AndroidDev Digest 99 Badge]: https://img.shields.io/badge/androiddev--digest-99-blue.svg
[AndroidDev Digest 99]: https://www.androiddevdigest.com/digest-99/
[PDF.js]: https://mozilla.github.io/pdf.js/
