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

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import static io.github.sheepdestroyer.materialisheep.DataModule.HN;
import io.github.sheepdestroyer.materialisheep.ActivityModule;
import io.github.sheepdestroyer.materialisheep.DataModule;
import io.github.sheepdestroyer.materialisheep.annotation.Synthetic;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;

/**
 * An {@link ItemManager} that uses the Algolia REST API forHN Search.
 */
public class AlgoliaClient implements ItemManager {

    /**
     * A flag that indicates whether to sort search results by time.
     */
    public static boolean sSortByTime = true;
    /**
     * The host of the Algolia API.
     */
    public static final String HOST = "hn.algolia.com";
    private static final String BASE_API_URL = "https://" + HOST + "/api/v1/";

    static final String MIN_CREATED_AT = "created_at_i>";
    protected final RestService mRestService;
    private final ItemManager mHackerNewsClient;
    private final Scheduler mMainThreadScheduler;

    @Inject
    public AlgoliaClient(RestServiceFactory factory, @Named(HN) ItemManager hackerNewsClient,
            @Named(DataModule.MAIN_THREAD) Scheduler mainThreadScheduler) {
        mRestService = factory.rxEnabled(true).create(BASE_API_URL, RestService.class);
        mHackerNewsClient = hackerNewsClient;
        mMainThreadScheduler = mainThreadScheduler;
    }

    /**
     * Fetches stories from the Algolia API.
     *
     * @param filter    the filter to apply (e.g., query string or range)
     * @param cacheMode the {@link CacheMode} to use
     * @param listener  the {@link ResponseListener} to notify of the results
     */
    @Override
    @android.annotation.SuppressLint("CheckResult")
    public void getStories(String filter, @CacheMode int cacheMode,
            final ResponseListener<Item[]> listener) {
        if (listener == null) {
            return;
        }
        searchRx(filter)
                .map(this::toItems)
                .observeOn(mMainThreadScheduler)
                .subscribe(listener::onResponse,
                        t -> listener.onError(t != null ? t.getMessage() : ""));
    }

    @Override
    public void getItem(String itemId, @CacheMode int cacheMode, ResponseListener<Item> listener) {
        mHackerNewsClient.getItem(itemId, cacheMode, listener);
    }

    /**
     * Fetches stories from the Algolia API synchronously.
     *
     * @param filter    the filter to apply
     * @param cacheMode the {@link CacheMode} to use
     * @return an array of {@link Item}s
     */
    @Override
    public Item[] getStories(String filter, @CacheMode int cacheMode) {
        try {
            return toItems(search(filter).execute().body());
        } catch (IOException e) {
            return new Item[0];
        }
    }

    @Override
    public Item getItem(String itemId, @CacheMode int cacheMode) {
        return mHackerNewsClient.getItem(itemId, cacheMode);
    }

    /**
     * Searches for stories that match the given filter.
     *
     * @param filter the filter to apply
     * @return an {@link Observable} that emits the search results
     */
    protected Observable<AlgoliaHits> searchRx(String filter) {
        return sSortByTime ? mRestService.searchByDateRx(filter, null) : mRestService.searchRx(filter, null);
    }

    /**
     * Searches for stories that match the given filter.
     *
     * @param filter the filter to apply
     * @return a {@link Call} that can be used to execute the search
     */
    protected Call<AlgoliaHits> search(String filter) {
        return sSortByTime ? mRestService.searchByDate(filter, null) : mRestService.search(filter, null);
    }

    @NonNull
    private Item[] toItems(AlgoliaHits algoliaHits) {
        if (algoliaHits == null) {
            return new Item[0];
        }
        Hit[] hits = algoliaHits.hits;
        Item[] stories = new Item[hits == null ? 0 : hits.length];
        for (int i = 0; i < stories.length; i++) {
            // noinspection ConstantConditions
            HackerNewsItem item = new HackerNewsItem(
                    Long.parseLong(hits[i].objectID));
            item.rank = i + 1;
            stories[i] = item;
        }
        return stories;
    }

    interface RestService {
        String HEADER_IF_NONE_MATCH = "If-None-Match";

        /**
         * Asynchronously searches for stories by date.
         *
         * @param query the search query
         * @param etag  the ETag for a conditional request, can be null
         * @return an Observable of search results
         */
        @GET("search_by_date?hitsPerPage=100&tags=story&attributesToRetrieve=objectID&attributesToHighlight=none")
        Observable<AlgoliaHits> searchByDateRx(@Query("query") String query,
                @Header(HEADER_IF_NONE_MATCH) @Nullable String etag);

        /**
         * Asynchronously searches for stories.
         *
         * @param query the search query
         * @param etag  the ETag for a conditional request, can be null
         * @return an Observable of search results
         */
        @GET("search?hitsPerPage=100&tags=story&attributesToRetrieve=objectID&attributesToHighlight=none")
        Observable<AlgoliaHits> searchRx(@Query("query") String query,
                @Header(HEADER_IF_NONE_MATCH) @Nullable String etag);

        /**
         * Asynchronously searches for stories created after a minimum timestamp.
         *
         * @param numericFilters the numeric filter for creation timestamp (e.g.
         *                       "created_at_i>12345")
         * @param etag           the ETag for a conditional request, can be null
         * @return an Observable of search results
         */
        @GET("search?hitsPerPage=100&tags=story&attributesToRetrieve=objectID&attributesToHighlight=none")
        Observable<AlgoliaHits> searchByMinTimestampRx(@Query("numericFilters") String numericFilters,
                @Header(HEADER_IF_NONE_MATCH) @Nullable String etag);

        /**
         * Synchronously searches for stories by date.
         *
         * @param query the search query
         * @param etag  the ETag for a conditional request, can be null
         * @return a {@link Call} of search results
         */
        @GET("search_by_date?hitsPerPage=100&tags=story&attributesToRetrieve=objectID&attributesToHighlight=none")
        Call<AlgoliaHits> searchByDate(@Query("query") String query,
                @Header(HEADER_IF_NONE_MATCH) @Nullable String etag);

        /**
         * Synchronously searches for stories.
         *
         * @param query the search query
         * @param etag  the ETag for a conditional request, can be null
         * @return a {@link Call} of search results
         */
        @GET("search?hitsPerPage=100&tags=story&attributesToRetrieve=objectID&attributesToHighlight=none")
        Call<AlgoliaHits> search(@Query("query") String query,
                @Header(HEADER_IF_NONE_MATCH) @Nullable String etag);

        /**
         * Synchronously searches for stories created after a minimum timestamp.
         *
         * @param numericFilters the numeric filter for creation timestamp (e.g.
         *                       "created_at_i>12345")
         * @param etag           the ETag for a conditional request, can be null
         * @return a {@link Call} of search results
         */
        @GET("search?hitsPerPage=100&tags=story&attributesToRetrieve=objectID&attributesToHighlight=none")
        Call<AlgoliaHits> searchByMinTimestamp(@Query("numericFilters") String numericFilters,
                @Header(HEADER_IF_NONE_MATCH) @Nullable String etag);
    }

    static class AlgoliaHits {
        @Keep
        @Synthetic
        Hit[] hits;
    }

    static class Hit {
        @Keep
        @Synthetic
        String objectID;
    }
}
