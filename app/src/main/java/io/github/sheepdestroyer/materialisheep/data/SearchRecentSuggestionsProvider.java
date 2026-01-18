/*
 * Copyright (c) 2015 Ha Duy Trung
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.sheepdestroyer.materialisheep.data;

/**
 * A {@link android.content.SearchRecentSuggestionsProvider} that provides recent search queries.
 */
public class SearchRecentSuggestionsProvider extends android.content.SearchRecentSuggestionsProvider {
    /**
     * The authority of the search recent suggestions provider.
     */
    public static final String PROVIDER_AUTHORITY = "io.github.sheepdestroyer.materialisheep.recentprovider";
    /**
     * The mode of the search recent suggestions provider.
     */
    public static final int MODE = DATABASE_MODE_QUERIES;

    /**
     * Constructs a new {@code SearchRecentSuggestionsProvider}.
     */
    public SearchRecentSuggestionsProvider() {
        setupSuggestions(PROVIDER_AUTHORITY, MODE);
    }
}
