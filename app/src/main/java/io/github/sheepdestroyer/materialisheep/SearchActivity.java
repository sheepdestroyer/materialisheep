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

package io.github.sheepdestroyer.materialisheep;

import android.app.SearchManager;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import io.github.sheepdestroyer.materialisheep.data.AlgoliaClient;
import io.github.sheepdestroyer.materialisheep.data.HackerNewsClient;
import io.github.sheepdestroyer.materialisheep.data.SearchRecentSuggestionsProvider;

/**
 * An activity that displays search results.
 */
public class SearchActivity extends BaseListActivity {

    private static final int MAX_RECENT_SUGGESTIONS = 10;
    private String mQuery;

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState(Bundle)}.
     *                           Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getIntent().hasExtra(SearchManager.QUERY)) {
            mQuery = getIntent().getStringExtra(SearchManager.QUERY);
        }
        super.onCreate(savedInstanceState);
        if (!TextUtils.isEmpty(mQuery)) {
            getSupportActionBar().setSubtitle(mQuery);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    SearchRecentSuggestionsProvider.PROVIDER_AUTHORITY,
                    SearchRecentSuggestionsProvider.MODE) {
                @Override
                public void saveRecentQuery(String queryString, String line2) {
                    truncateHistory(getContentResolver(), MAX_RECENT_SUGGESTIONS - 1);
                    super.saveRecentQuery(queryString, line2);
                }
            };
            suggestions.saveRecentQuery(mQuery, null);
        }
    }

    /**
     * Initialize the contents of the Activity's standard options menu.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed;
     *         if you return false it will not be shown.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sort, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Prepare the Screen's standard options menu to be displayed.
     *
     * @param menu The options menu as last shown or first initialized by
     *             onCreateOptionsMenu().
     * @return You must return true for the menu to be displayed;
     *         if you return false it will not be shown.
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(AlgoliaClient.sSortByTime ? R.id.menu_sort_recent : R.id.menu_sort_popular)
                .setChecked(true);
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     *         proceed, true to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getGroupId() == R.id.menu_sort_group) {
            item.setChecked(true);
            sort(item.getItemId() == R.id.menu_sort_recent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Gets the default title for the activity.
     *
     * @return The default title.
     */
    @Override
    protected String getDefaultTitle() {
        return getString(R.string.title_activity_search);
    }

    /**
     * Instantiates the list fragment for the activity.
     *
     * @return The list fragment.
     */
    @Override
    protected Fragment instantiateListFragment() {
        Bundle args = new Bundle();
        args.putString(ListFragment.EXTRA_FILTER, mQuery);
        if (TextUtils.isEmpty(mQuery)) {
            args.putString(ListFragment.EXTRA_ITEM_MANAGER, HackerNewsClient.class.getName());
        } else {
            args.putString(ListFragment.EXTRA_ITEM_MANAGER, AlgoliaClient.class.getName());
        }
        return Fragment.instantiate(this, ListFragment.class.getName(), args);
    }

    private void sort(boolean byTime) {
        if (AlgoliaClient.sSortByTime == byTime) {
            return;
        }
        AlgoliaClient.sSortByTime = byTime;
        Preferences.setSortByRecent(this, byTime);
        ListFragment listFragment = (ListFragment) getSupportFragmentManager()
                .findFragmentByTag(LIST_FRAGMENT_TAG);
        if (listFragment != null) {
            listFragment.filter(mQuery);
        }
    }
}
