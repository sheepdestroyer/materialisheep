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

package io.github.sheepdestroyer.materialisheep.accounts;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.StringRes;

import java.io.IOException;

/**
 * An interface for user-related services.
 */
public interface UserServices {
    /**
     * A callback for user-related service calls.
     */
    abstract class Callback {
        /**
         * Called when the service call is complete.
         *
         * @param successful True if the call was successful, false otherwise.
         */
        public void onDone(boolean successful) {}

        /**
         * Called when the service call fails.
         *
         * @param throwable The throwable that caused the failure.
         */
        public void onError(Throwable throwable) {}
    }

    /**
     * An exception that occurs during a user-related service call.
     */
    class Exception extends IOException {
        public final @StringRes int message;
        public Uri data;

        public Exception(int message) {
            this.message = message;
        }

        Exception(String message) {
            super(message);
            this.message = 0;
        }
    }

    /**
     * Logs in a user.
     *
     * @param username      The username.
     * @param password      The password.
     * @param createAccount True to create a new account, false to log in.
     * @param callback      The callback to be invoked when the call is complete.
     */
    void login(String username, String password, boolean createAccount, Callback callback);

    /**
     * Votes up an item.
     *
     * @param context  The context.
     * @param itemId   The ID of the item to vote up.
     * @param callback The callback to be invoked when the call is complete.
     * @return True if the vote was successful, false otherwise.
     */
    boolean voteUp(Context context, String itemId, Callback callback);

    /**
     * Replies to an item.
     *
     * @param context  The context.
     * @param parentId The ID of the parent item.
     * @param text     The reply text.
     * @param callback The callback to be invoked when the call is complete.
     */
    void reply(Context context, String parentId, String text, Callback callback);

    /**
     * Submits a new story.
     *
     * @param context The context.
     * @param title   The title of the story.
     * @param content The content of the story.
     * @param isUrl   True if the content is a URL, false otherwise.
     * @param callback The callback to be invoked when the call is complete.
     */
    void submit(Context context, String title, String content, boolean isUrl, Callback callback);
}
