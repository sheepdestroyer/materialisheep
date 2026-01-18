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
import android.os.Handler;
import android.os.SystemClock;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.text.format.DateUtils;

import io.github.sheepdestroyer.materialisheep.annotation.Synthetic;
import io.github.sheepdestroyer.materialisheep.data.HackerNewsClient;
import io.github.sheepdestroyer.materialisheep.data.ItemManager;

/**
 * An abstract base activity for displaying a list of stories. This activity
 * handles common
 * functionality for story lists, such as refreshing the list and displaying the
 * last updated time.
 */
public abstract class BaseStoriesActivity extends BaseListActivity
        implements ListFragment.RefreshCallback {

    private static final String STATE_LAST_UPDATED = "state:lastUpdated";
    @Synthetic
    Long mLastUpdated;
    private final Runnable mLastUpdateTask = new Runnable() {
        @Override
        public void run() {
            if (mLastUpdated == null) {
                return;
            }
            // noinspection ConstantConditions
            if (getSupportActionBar() == null) {
                return;
            }
            if (AppUtils.hasConnection(BaseStoriesActivity.this)) {
                getSupportActionBar().setSubtitle(getString(R.string.last_updated,
                        DateUtils.getRelativeTimeSpanString(mLastUpdated,
                                System.currentTimeMillis(),
                                DateUtils.MINUTE_IN_MILLIS,
                                DateUtils.FORMAT_ABBREV_ALL)));
                mHandler.postAtTime(this, SystemClock.uptimeMillis() + DateUtils.MINUTE_IN_MILLIS);
            } else {
                getSupportActionBar().setSubtitle(R.string.offline);
            }
        }
    };
    @Synthetic
    final Handler mHandler = new Handler(android.os.Looper.getMainLooper());

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
        if (savedInstanceState != null) {
            mLastUpdated = savedInstanceState.getLong(STATE_LAST_UPDATED);
        }
    }

    /**
     * Called after {@link #onRestoreInstanceState(Bundle)}, {@link #onRestart()},
     * or
     * {@link #onPause()}, for your activity to start interacting with the user.
     */
    @Override
    protected void onResume() {
        super.onResume();
        mHandler.removeCallbacks(mLastUpdateTask);
        mHandler.post(mLastUpdateTask);
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into
     * the background, but has not (yet) been killed.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mLastUpdateTask);
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
        if (mLastUpdated != null) {
            outState.putLong(STATE_LAST_UPDATED, mLastUpdated);
        }
    }

    /**
     * Called when the list has been refreshed.
     */
    @Override
    public void onRefreshed() {
        onItemSelected(null);
        mLastUpdated = System.currentTimeMillis();
        mHandler.removeCallbacks(mLastUpdateTask);
        mHandler.post(mLastUpdateTask);
    }

    /**
     * Gets the fetch mode for the stories.
     *
     * @return The fetch mode.
     */
    @NonNull
    @ItemManager.FetchMode
    protected abstract String getFetchMode();

    /**
     * Instantiates the list fragment for the activity.
     *
     * @return The list fragment.
     */
    @Override
    protected Fragment instantiateListFragment() {
        Bundle args = new Bundle();
        args.putString(ListFragment.EXTRA_ITEM_MANAGER, HackerNewsClient.class.getName());
        args.putString(ListFragment.EXTRA_FILTER, getFetchMode());
        return Fragment.instantiate(this, ListFragment.class.getName(), args);
    }

}
