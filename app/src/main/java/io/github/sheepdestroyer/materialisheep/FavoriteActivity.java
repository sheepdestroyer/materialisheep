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
import androidx.lifecycle.Observer;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;

import io.github.sheepdestroyer.materialisheep.data.FavoriteManager;
import io.github.sheepdestroyer.materialisheep.data.ItemManager;
import io.github.sheepdestroyer.materialisheep.data.MaterialisticDatabase;
import io.github.sheepdestroyer.materialisheep.data.WebItem;

/**
 * Activity for displaying a list of favorite stories.
 */
public class FavoriteActivity extends BaseListActivity {

    static final String EMPTY_QUERY = MaterialisticDatabase.class.getName();
    private static final String STATE_FILTER = "state:filter";
    private final Observer<Uri> mObserver = uri -> {
        if (uri == null) {
            return;
        }
        if (FavoriteManager.Companion.isRemoved(uri)) {
            WebItem selected = getSelectedItem();
            if (selected != null &&
                    TextUtils.equals(selected.getId(), uri.getLastPathSegment())) {
                onItemSelected(null);
            }
        } else if (FavoriteManager.Companion.isCleared(uri)) {
            onItemSelected(null);
        }
    };
    private String mFilter;

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
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mFilter = savedInstanceState.getString(STATE_FILTER);
            getSupportActionBar().setSubtitle(mFilter);
        }
        MaterialisticDatabase.getInstance(this).getLiveData().observe(this, mObserver);
    }

    /**
     * This is called for activities that set launchMode to "singleTop" in
     * their package, or if a client used the {@link Intent#FLAG_ACTIVITY_SINGLE_TOP}
     * flag when calling {@link #startActivity}.
     *
     * @param intent The new intent that was started for the activity.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (!intent.hasExtra(SearchManager.QUERY)) {
            return;
        }
        onItemSelected(null);
        mFilter = intent.getStringExtra(SearchManager.QUERY);
        if (TextUtils.equals(mFilter, EMPTY_QUERY)) {
            mFilter = null;
        }
        getSupportActionBar().setSubtitle(mFilter);
        FavoriteFragment fragment = (FavoriteFragment) getSupportFragmentManager()
                .findFragmentByTag(LIST_FRAGMENT_TAG);
        if (fragment != null) {
            fragment.filter(mFilter);
        }
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
        outState.putString(STATE_FILTER, mFilter);
    }

    /**
     * Gets the default title for the activity.
     *
     * @return The default title.
     */
    @Override
    protected String getDefaultTitle() {
        return getString(R.string.title_activity_favorite);
    }

    /**
     * Instantiates the list fragment for the activity.
     *
     * @return The list fragment.
     */
    @Override
    protected Fragment instantiateListFragment() {
        Bundle args = new Bundle();
        args.putString(FavoriteFragment.EXTRA_FILTER, mFilter);
        return Fragment.instantiate(this, FavoriteFragment.class.getName(), args);
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

    /**
     * Gets the item cache mode for the activity.
     *
     * @return The item cache mode.
     */
    @Override
    protected int getItemCacheMode() {
        return ItemManager.MODE_CACHE;
    }
}
