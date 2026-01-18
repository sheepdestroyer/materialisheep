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

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;

import io.github.sheepdestroyer.materialisheep.data.AlgoliaPopularClient;

/**
 * An activity that displays a list of popular stories.
 */
public class PopularActivity extends BaseListActivity {
    private static final String STATE_RANGE = "state:range";

    private String mRange;

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle
     *                           contains the data it most
     *                           recently supplied in
     *                           {@link #onSaveInstanceState(Bundle)}.
     *                           Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MaterialisticApplication) getApplication()).applicationComponent.inject(this);
        if (savedInstanceState != null) {
            setRange(savedInstanceState.getString(STATE_RANGE));
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
        getMenuInflater().inflate(R.menu.menu_popular, menu);
        return super.onCreateOptionsMenu(menu);
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
        if (item.getItemId() == R.id.menu_range_day) {
            filter(AlgoliaPopularClient.LAST_24H);
            return true;
        } else if (item.getItemId() == R.id.menu_range_week) {
            filter(AlgoliaPopularClient.PAST_WEEK);
            return true;
        } else if (item.getItemId() == R.id.menu_range_month) {
            filter(AlgoliaPopularClient.PAST_MONTH);
            return true;
        } else if (item.getItemId() == R.id.menu_range_year) {
            filter(AlgoliaPopularClient.PAST_YEAR);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Called to retrieve per-instance state from an activity before being killed
     * so that the state can be restored in {@link #onCreate(Bundle)} or
     * {@link #onRestoreInstanceState(Bundle)}.
     *
     * @param outState Bundle in which to place your saved state.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_RANGE, mRange);
    }

    /**
     * Gets the default title for the activity.
     *
     * @return The default title.
     */
    @Override
    protected String getDefaultTitle() {
        return getString(R.string.title_activity_popular);
    }

    /**
     * Instantiates the list fragment for the activity.
     *
     * @return The list fragment.
     */
    @Override
    protected Fragment instantiateListFragment() {
        Bundle args = new Bundle();
        setRange(Preferences.getPopularRange(this));
        args.putString(ListFragment.EXTRA_FILTER, mRange);
        args.putString(ListFragment.EXTRA_ITEM_MANAGER, AlgoliaPopularClient.class.getName());
        return Fragment.instantiate(this, ListFragment.class.getName(), args);
    }

    /**
     * Checks if the activity is searchable.
     *
     * @return True if the activity is searchable, false otherwise.
     */
    @Override
    protected boolean isSearchable() {
        return false;
    }

    private void filter(@AlgoliaPopularClient.Range String range) {
        setRange(range);
        Preferences.setPopularRange(this, range);
        ListFragment listFragment = (ListFragment) getSupportFragmentManager()
                .findFragmentByTag(LIST_FRAGMENT_TAG);
        if (listFragment != null) {
            listFragment.filter(range);
        }
    }

    private void setRange(String range) {
        mRange = range;
        final int stringRes;
        switch (range) {
            case AlgoliaPopularClient.LAST_24H:
            default:
                stringRes = R.string.popular_range_last_24h;
                break;
            case AlgoliaPopularClient.PAST_WEEK:
                stringRes = R.string.popular_range_past_week;
                break;
            case AlgoliaPopularClient.PAST_MONTH:
                stringRes = R.string.popular_range_past_month;
                break;
            case AlgoliaPopularClient.PAST_YEAR:
                stringRes = R.string.popular_range_past_year;
                break;
        }
        getSupportActionBar().setSubtitle(stringRes);
    }

}
