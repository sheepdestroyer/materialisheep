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

package io.github.sheepdestroyer.materialisheep.data;

/**
 * An interface for managing local items.
 *
 * @param <T> the type of item
 */
public interface LocalItemManager<T> {
    /**
     * Gets the number of items.
     *
     * @return the number of items, or 0 if there are none
     */
    int getSize();

    /**
     * Gets the item at the given position.
     *
     * @param position the position of the item
     * @return the item at the given position, or `null` if there is no item at that position
     */
    T getItem(int position);

    /**
     * Initiates an asynchronous query for local items.
     *
     * @param observer the listener that will be informed of changes
     * @param filter   the query filter, if any
     */
    void attach(Observer observer, String filter);

    /**
     * Cleans up any extra state created by {@link #attach(Observer, String)}.
     */
    void detach();

    /**
     * A callback interface for local item change events.
     */
    interface Observer {
        /**
         * Called when local items change (i.e., are added, removed, or edited).
         */
        void onChanged();
    }
}
