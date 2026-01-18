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

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;

import io.github.sheepdestroyer.materialisheep.data.ItemManager;

/**
 * Activity to display new stories
 */
public class NewActivity extends BaseStoriesActivity {
    public static final String EXTRA_REFRESH = NewActivity.class.getName() + ".EXTRA_REFRESH";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MaterialisticApplication) getApplication()).applicationComponent.inject(this);
    }

    /**
     * This is called for activities that set launchMode to "singleTop" in
     * their package, or if a client used the
     * {@link Intent#FLAG_ACTIVITY_SINGLE_TOP}
     * flag when calling {@link #startActivity}.
     *
     * @param intent The new intent that was started for the activity.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getBooleanExtra(EXTRA_REFRESH, false)) {
            // triggered by new submission from user, refresh list
            ListFragment listFragment = ((ListFragment) getSupportFragmentManager()
                    .findFragmentByTag(LIST_FRAGMENT_TAG));
            if (listFragment != null) {
                listFragment.filter(getFetchMode());
            }
        }
    }

    /**
     * Gets the default title for the activity.
     *
     * @return The default title.
     */
    @Override
    protected String getDefaultTitle() {
        return getString(R.string.title_activity_new);
    }

    /**
     * Gets the fetch mode for the stories.
     *
     * @return The fetch mode.
     */
    @NonNull
    @Override
    protected String getFetchMode() {
        return ItemManager.NEW_FETCH_MODE;
    }

    /**
     * Gets the item cache mode for the activity.
     *
     * @return The item cache mode.
     */
    @Override
    protected int getItemCacheMode() {
        return ItemManager.MODE_NETWORK;
    }
}
