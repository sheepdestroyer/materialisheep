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

import androidx.annotation.IntDef;
import androidx.annotation.StringDef;
import androidx.annotation.WorkerThread;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An interface for managing {@link Item} data.
 */
public interface ItemManager {

    /**
     * An annotation for defining the fetch mode for stories.
     */
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            TOP_FETCH_MODE,
            NEW_FETCH_MODE,
            ASK_FETCH_MODE,
            SHOW_FETCH_MODE,
            JOBS_FETCH_MODE,
            BEST_FETCH_MODE
    })
    @interface FetchMode {}
    String TOP_FETCH_MODE = "top";
    String NEW_FETCH_MODE = "new";
    String ASK_FETCH_MODE = "ask";
    String SHOW_FETCH_MODE = "show";
    String JOBS_FETCH_MODE = "jobs";
    String BEST_FETCH_MODE = "best";

    /**
     * An annotation for defining the cache mode for data fetching.
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            MODE_DEFAULT,
            MODE_CACHE,
            MODE_NETWORK
    })
    @interface CacheMode {}
    /**
     * The default cache mode.
     */
    int MODE_DEFAULT = 0;
    /**
     * The cache mode that forces the use of cached data.
     */
    int MODE_CACHE =1;
    /**
     * The cache mode that forces a network request.
     */
    int MODE_NETWORK = 2;

    /**
     * Gets an array of stories.
     *
     * @param filter    the filter to apply to the stories
     * @param cacheMode the cache mode to use
     * @param listener  the listener to be notified of the response
     */
    void getStories(String filter, @CacheMode int cacheMode, final ResponseListener<Item[]> listener);

    /**
     * Gets an individual item by its ID.
     *
     * @param itemId    the ID of the item to get
     * @param cacheMode the cache mode to use
     * @param listener  the listener to be notified of the response
     */
    void getItem(String itemId, @CacheMode int cacheMode, ResponseListener<Item> listener);

    /**
     * Gets an array of stories.
     *
     * @param filter    the filter to apply to the stories
     * @param cacheMode the cache mode to use
     * @return an array of stories
     */
    @WorkerThread
    Item[] getStories(String filter, @CacheMode int cacheMode);

    /**
     * Gets an individual item by its ID.
     *
     * @param itemId    the ID of the item to get
     * @param cacheMode the cache mode to use
     * @return the item, or `null` if the item could not be found
     */
    @WorkerThread
    Item getItem(String itemId, @CacheMode int cacheMode);
}
