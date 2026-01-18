/*
 * Copyright (c) 2016 Ha Duy Trung
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

import androidx.sqlite.db.SupportSQLiteOpenHelper;
import android.content.Context;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.github.sheepdestroyer.materialisheep.accounts.UserServices;
import io.github.sheepdestroyer.materialisheep.accounts.UserServicesClient;
import io.github.sheepdestroyer.materialisheep.data.AlgoliaClient;
import io.github.sheepdestroyer.materialisheep.data.AlgoliaPopularClient;
import io.github.sheepdestroyer.materialisheep.data.FeedbackClient;
import io.github.sheepdestroyer.materialisheep.data.HackerNewsClient;
import io.github.sheepdestroyer.materialisheep.data.ItemManager;
import io.github.sheepdestroyer.materialisheep.data.LocalCache;
import io.github.sheepdestroyer.materialisheep.data.MaterialisticDatabase;
import io.github.sheepdestroyer.materialisheep.data.ReadabilityClient;
import io.github.sheepdestroyer.materialisheep.data.SyncScheduler;
import io.github.sheepdestroyer.materialisheep.data.UserManager;
import io.github.sheepdestroyer.materialisheep.data.SavedStoriesDao;
import io.github.sheepdestroyer.materialisheep.data.android.Cache;
import okhttp3.Call;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Dagger module for data-related dependencies.
 */
@Module(includes = NetworkModule.class)
public class DataModule {
    public static final String HN = "hn";
    public static final String ALGOLIA = "algolia";
    public static final String POPULAR = "popular";
    public static final String MAIN_THREAD = "main";
    public static final String IO_THREAD = "io";

    /**
     * Provides a singleton instance of {@link HackerNewsClient}.
     *
     * @param client The {@link HackerNewsClient} instance.
     * @return The singleton instance of {@link HackerNewsClient}.
     */
    @Provides
    @Singleton
    @Named(HN)
    public ItemManager provideHackerNewsClient(HackerNewsClient client) {
        return client;
    }

    /**
     * Provides a singleton instance of {@link AlgoliaClient}.
     *
     * @param client The {@link AlgoliaClient} instance.
     * @return The singleton instance of {@link AlgoliaClient}.
     */
    @Provides
    @Singleton
    @Named(ALGOLIA)
    public ItemManager provideAlgoliaClient(AlgoliaClient client) {
        return client;
    }

    /**
     * Provides a singleton instance of {@link AlgoliaPopularClient}.
     *
     * @param client The {@link AlgoliaPopularClient} instance.
     * @return The singleton instance of {@link AlgoliaPopularClient}.
     */
    @Provides
    @Singleton
    @Named(POPULAR)
    public ItemManager provideAlgoliaPopularClient(AlgoliaPopularClient client) {
        return client;
    }

    /**
     * Provides a singleton instance of {@link UserManager}.
     *
     * @param client The {@link HackerNewsClient} instance.
     * @return The singleton instance of {@link UserManager}.
     */
    @Provides
    @Singleton
    public UserManager provideUserManager(HackerNewsClient client) {
        return client;
    }

    /**
     * Provides a singleton instance of {@link FeedbackClient}.
     *
     * @param client The {@link FeedbackClient.Impl} instance.
     * @return The singleton instance of {@link FeedbackClient}.
     */
    @Provides
    @Singleton
    public FeedbackClient provideFeedbackClient(FeedbackClient.Impl client) {
        return client;
    }

    /**
     * Provides a singleton instance of {@link ReadabilityClient}.
     *
     * @param client The {@link ReadabilityClient.Impl} instance.
     * @return The singleton instance of {@link ReadabilityClient}.
     */
    @Provides
    @Singleton
    public ReadabilityClient provideReadabilityClient(ReadabilityClient.Impl client) {
        return client;
    }

    /**
     * Provides a singleton instance of {@link UserServices}.
     *
     * @param callFactory The {@link Call.Factory} instance.
     * @param ioScheduler The IO scheduler.
     * @return The singleton instance of {@link UserServices}.
     */
    @Provides
    @Singleton
    public UserServices provideUserServices(Call.Factory callFactory,
            @Named(IO_THREAD) Scheduler ioScheduler) {
        return new UserServicesClient(callFactory, ioScheduler);
    }

    /**
     * Provides a singleton instance of the IO scheduler.
     *
     * @return The IO scheduler.
     */
    @Provides
    @Singleton
    @Named(IO_THREAD)
    public Scheduler provideIoScheduler() {
        return Schedulers.io();
    }

    /**
     * Provides a singleton instance of the main thread scheduler.
     *
     * @return The main thread scheduler.
     */
    @Provides
    @Singleton
    @Named(MAIN_THREAD)
    public Scheduler provideMainThreadScheduler() {
        return AndroidSchedulers.mainThread();
    }

    /**
     * Provides a singleton instance of {@link SyncScheduler}.
     *
     * @return The singleton instance of {@link SyncScheduler}.
     */
    @Provides
    @Singleton
    public SyncScheduler provideSyncScheduler() {
        return new SyncScheduler();
    }

    /**
     * Provides a singleton instance of {@link LocalCache}.
     *
     * @param cache The {@link Cache} instance.
     * @return The singleton instance of {@link LocalCache}.
     */
    @Provides
    @Singleton
    public LocalCache provideLocalCache(Cache cache) {
        return cache;
    }

    /**
     * Provides a singleton instance of {@link MaterialisticDatabase}.
     *
     * @param context The application context.
     * @return The singleton instance of {@link MaterialisticDatabase}.
     */
    @Provides
    @Singleton
    public MaterialisticDatabase provideDatabase(Context context) {
        return MaterialisticDatabase.getInstance(context);
    }

    /**
     * Provides a singleton instance of
     * {@link SavedStoriesDao}.
     *
     * @param database The {@link MaterialisticDatabase} instance.
     * @return The singleton instance of
     *         {@link SavedStoriesDao}.
     */
    @Provides
    @Singleton
    public SavedStoriesDao provideSavedStoriesDao(MaterialisticDatabase database) {
        return database.getSavedStoriesDao();
    }

    /**
     * Provides a singleton instance of
     * {@link MaterialisticDatabase.ReadStoriesDao}.
     *
     * @param database The {@link MaterialisticDatabase} instance.
     * @return The singleton instance of
     *         {@link MaterialisticDatabase.ReadStoriesDao}.
     */
    @Provides
    public MaterialisticDatabase.ReadStoriesDao provideReadStoriesDao(MaterialisticDatabase database) {
        return database.getReadStoriesDao();
    }

    /**
     * Provides a singleton instance of {@link MaterialisticDatabase.ReadableDao}.
     *
     * @param database The {@link MaterialisticDatabase} instance.
     * @return The singleton instance of {@link MaterialisticDatabase.ReadableDao}.
     */
    @Provides
    public MaterialisticDatabase.ReadableDao provideReadableDao(MaterialisticDatabase database) {
        return database.getReadableDao();
    }

    /**
     * Provides a singleton instance of {@link SupportSQLiteOpenHelper}.
     *
     * @param database The {@link MaterialisticDatabase} instance.
     * @return The singleton instance of {@link SupportSQLiteOpenHelper}.
     */
    @Provides
    public SupportSQLiteOpenHelper provideOpenHelper(MaterialisticDatabase database) {
        return database.getOpenHelper();
    }
}
