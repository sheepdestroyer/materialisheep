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

package io.github.hidroh.materialistic;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import androidx.annotation.WorkerThread;
import android.text.TextUtils;
import android.webkit.WebResourceResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import okhttp3.HttpUrl;
import okio.BufferedSource;
import okio.Okio;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;

/**
 * A simple ad blocker that blocks network requests to hosts listed in the ad
 * hosts file.
 */
public class AdBlocker {
    private static final String AD_HOSTS_FILE = "pgl.yoyo.org.txt";
    private static final Set<String> AD_HOSTS = java.util.Collections.synchronizedSet(new HashSet<>());

    /**
     * Initializes the ad blocker by loading the ad hosts from the assets file.
     *
     * @param context   The application context.
     * @param scheduler The RxJava scheduler to perform the operation on.
     */
    @SuppressLint("CheckResult")
    public static void init(Context context, Scheduler scheduler) {
        Observable.fromCallable(() -> loadFromAssets(context))
                .subscribeOn(scheduler)
                .subscribe(result -> {
                },
                        t -> android.util.Log.e(AdBlocker.class.getSimpleName(), "Error loading ad hosts", t));
    }

    /**
     * Checks if a given URL is an ad.
     *
     * @param url The URL to check.
     * @return True if the URL is an ad, false otherwise.
     */
    public static boolean isAd(String url) {
        HttpUrl httpUrl = HttpUrl.parse(url);
        return isAdHost(httpUrl != null ? httpUrl.host() : "");
    }

    /**
     * Creates an empty WebResourceResponse to block a network request.
     *
     * @return An empty WebResourceResponse.
     */
    public static WebResourceResponse createEmptyResource() {
        return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream("".getBytes()));
    }

    @WorkerThread
    private static Boolean loadFromAssets(Context context) throws IOException {
        try (InputStream stream = context.getAssets().open(AD_HOSTS_FILE);
                BufferedSource buffer = Okio.buffer(Okio.source(stream))) {
            String line;
            while ((line = buffer.readUtf8Line()) != null) {
                AD_HOSTS.add(line);
            }
        }
        return true;
    }

    /**
     * Recursively walking up sub domain chain until we exhaust or find a match,
     * effectively doing a longest substring matching here
     */
    private static boolean isAdHost(String host) {
        if (TextUtils.isEmpty(host)) {
            return false;
        }
        int index = host.indexOf(".");
        return index >= 0 && (AD_HOSTS.contains(host) ||
                index + 1 < host.length() && isAdHost(host.substring(index + 1)));
    }
}
