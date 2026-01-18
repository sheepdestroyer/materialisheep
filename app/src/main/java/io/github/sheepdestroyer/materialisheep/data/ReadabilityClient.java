/*
 * Copyright (c) 2015 Ha Duy Trung
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may not obtain a copy of the License at
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

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Named;

import io.github.sheepdestroyer.materialisheep.DataModule;
import okio.Okio;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * A client for fetching readable content from a URL.
 */
public interface ReadabilityClient {
    /**
     * A callback interface for receiving the readable content.
     */
    interface Callback {
        /**
         * Called when the readable content is available.
         *
         * @param content the readable content, or `null` if the content could not be
         *                fetched
         */
        void onResponse(String content);
    }

    /**
     * Parses the content of a URL and returns the readable content.
     *
     * @param itemId   the ID of the item
     * @param url      the URL to parse
     * @param callback the callback to be invoked when the content is available
     */
    void parse(String itemId, String url, Callback callback);

    /**
     * Parses the content of a URL and caches the readable content.
     *
     * @param itemId the ID of the item
     * @param url    the URL to parse
     */
    @WorkerThread
    void parse(String itemId, String url);

    /**
     * Destroys this client and releases resources.
     */
    void destroy();

    /**
     * An implementation of {@link ReadabilityClient} that uses Mozilla's
     * Readability.js.
     */
    class Impl implements ReadabilityClient {
        private static final String TAG = "ReadabilityClient";
        private final LocalCache mCache;
        private final Context mContext;
        private final Scheduler mIoScheduler;
        private final Scheduler mMainThreadScheduler;
        private String mReadabilityJs;
        private final CompositeDisposable mDisposables = new CompositeDisposable();

        @Inject
        public Impl(Context context, LocalCache cache,
                @Named(DataModule.IO_THREAD) Scheduler ioScheduler,
                @Named(DataModule.MAIN_THREAD) Scheduler mainThreadScheduler) {
            mContext = context;
            mCache = cache;
            mIoScheduler = ioScheduler;
            mMainThreadScheduler = mainThreadScheduler;
            try (InputStream inputStream = mContext.getAssets().open("Readability.js")) {
                mReadabilityJs = Okio.buffer(Okio.source(inputStream)).readUtf8();
            } catch (IOException e) {
                Log.e(TAG, "Failed to load Readability.js from assets", e);
                // mReadabilityJs will be null, and fromNetwork will emit null
            }
        }

        @Override
        public void parse(String itemId, String url, Callback callback) {
            mDisposables.add(Observable.defer(() -> fromCache(itemId))
                    .subscribeOn(mIoScheduler)
                    .switchIfEmpty(fromNetwork(itemId, url))
                    .observeOn(mMainThreadScheduler)
                    .firstElement()
                    .subscribe(callback::onResponse, throwable -> {
                        android.util.Log.e(TAG, "Failed to parse " + url, throwable);
                        callback.onResponse(null);
                    }, () -> callback.onResponse(null)));
        }

        @WorkerThread
        @Override
        public void parse(String itemId, String url) {
            mDisposables.add(Observable.defer(() -> fromCache(itemId))
                    .subscribeOn(Schedulers.trampoline())
                    .switchIfEmpty(fromNetwork(itemId, url))
                    .observeOn(Schedulers.trampoline())
                    .ignoreElements()
                    .subscribe(() -> {
                    }, throwable -> Log.w(TAG, "Failed to pre-parse " + url,
                            throwable)));
        }

        @NonNull
        @android.annotation.SuppressLint("SetJavaScriptEnabled")
        private Observable<String> fromNetwork(String itemId, String url) {
            return Observable.<String>create(emitter -> new Handler(Looper.getMainLooper()).post(() -> {
                WebView webView = new WebView(mContext);
                emitter.setCancellable(() -> AndroidSchedulers.mainThread()
                        .createWorker().schedule(webView::destroy));
                final AtomicBoolean isFinished = new AtomicBoolean(false);
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        if (mReadabilityJs == null) {
                            if (isFinished.compareAndSet(false, true)) {
                                emitter.onComplete();
                            }
                            return;
                        }
                        String script = mReadabilityJs + "; new Readability(document).parse();";
                        view.evaluateJavascript(script, value -> {
                            if (isFinished.compareAndSet(false, true)) {
                                String content = null;
                                if (value != null && !value.equalsIgnoreCase("null")) {
                                    try {
                                        JSONObject json = new JSONObject(value);
                                        content = json.getString("content");
                                        final String savedContent = content;
                                        mDisposables.add(Completable
                                                .fromAction(() -> mCache.putReadability(itemId, savedContent))
                                                .subscribeOn(mIoScheduler)
                                                .subscribe(() -> {
                                                }, error -> Log.e(TAG, "Failed to cache content",
                                                        error)));
                                    } catch (JSONException e) {
                                        Log.w(TAG, "Failed to parse Readability output", e);
                                        // content will be null
                                    }
                                }
                                if (content != null) {
                                    emitter.onNext(content);
                                }
                                emitter.onComplete();
                            }
                        });
                    }

                    @SuppressWarnings("deprecation")
                    @Override
                    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                        super.onReceivedError(view, errorCode, description, failingUrl);
                        if (url.equals(failingUrl) && isFinished.compareAndSet(false, true)) {
                            emitter.onComplete();
                        }
                    }

                    @Override
                    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                        super.onReceivedError(view, request, error);
                        if (request.isForMainFrame() && isFinished.compareAndSet(false, true)) {
                            emitter.onComplete();
                        }
                    }
                });
                WebSettings settings = webView.getSettings();
                settings.setJavaScriptEnabled(true);
                settings.setBlockNetworkImage(true);
                settings.setLoadsImagesAutomatically(false);
                settings.setAllowFileAccess(false);
                settings.setAllowContentAccess(false);
                settings.setGeolocationEnabled(false);
                webView.loadUrl(url);
            })).timeout(30, TimeUnit.SECONDS);
        }

        @Override
        public void destroy() {
            mDisposables.clear();
        }

        private Observable<String> fromCache(String itemId) {
            String content = mCache.getReadability(itemId);
            return content != null ? Observable.just(content) : Observable.empty();
        }
    }
}
