/*
 * Copyright (c) 2018 Ha Duy Trung
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

package io.github.hidroh.materialistic.data


import androidx.annotation.WorkerThread

/**
 * An interface for a local cache of data.
 */
@WorkerThread
interface LocalCache {
  /**
   * Gets the readable content for a given item ID.
   *
   * @param itemId the ID of the item
   * @return the readable content, or `null` if it is not cached
   */
  fun getReadability(itemId: String?): String?

  /**
   * Puts the readable content for a given item ID into the cache.
   *
   * @param itemId  the ID of the item
   * @param content the readable content
   */
  fun putReadability(itemId: String?, content: String?)

  /**
   * Checks if an item has been viewed.
   *
   * @param itemId the ID of the item
   * @return `true` if the item has been viewed, `false` otherwise
   */
  fun isViewed(itemId: String?): Boolean

  /**
   * Marks an item as viewed.
   *
   * @param itemId the ID of the item
   */
  fun setViewed(itemId: String?)

  /**
   * Checks if an item is a favorite.
   *
   * @param itemId the ID of the item
   * @return `true` if the item is a favorite, `false` otherwise
   */
  fun isFavorite(itemId: String?): Boolean
}
