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
import androidx.annotation.NonNull;

import io.github.sheepdestroyer.materialisheep.data.ItemManager;

/**
 * Activity to display ask stories
 */
public class AskActivity extends BaseStoriesActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MaterialisticApplication) getApplication()).applicationComponent.inject(this);
    }

    /**
     * Gets the default title for the activity.
     *
     * @return The default title.
     */
    @Override
    protected String getDefaultTitle() {
        return getString(R.string.title_activity_ask);
    }

    /**
     * Gets the fetch mode for the items to be displayed.
     *
     * @return The fetch mode.
     */
    @NonNull
    @Override
    protected @ItemManager.FetchMode String getFetchMode() {
        return ItemManager.ASK_FETCH_MODE;
    }

}
