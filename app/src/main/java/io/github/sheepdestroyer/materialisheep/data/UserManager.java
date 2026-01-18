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

import android.content.Context;
import android.os.Parcelable;
import androidx.annotation.NonNull;

/**
 * An interface for managing users.
 */
public interface UserManager {
    /**
     * Gets a user by their username.
     *
     * @param username the username of the user to get
     * @param listener the listener to be notified of the response
     */
    void getUser(String username, final ResponseListener<User> listener);

    /**
     * An interface that represents a user.
     */
    interface User extends Parcelable {
        /**
         * Gets the user's ID.
         *
         * @return the user's ID
         */
        String getId();

        /**
         * Gets the user's "about" text.
         *
         * @return the user's "about" text
         */
        String getAbout();

        /**
         * Gets the user's karma.
         *
         * @return the user's karma
         */
        long getKarma();

        /**
         * Gets the user's creation date.
         *
         * @param context the application context
         * @return the user's creation date
         */
        String getCreated(Context context);

        /**
         * Gets an array of the user's submitted items.
         *
         * @return an array of the user's submitted items
         */
        @NonNull Item[] getItems();
    }
}
