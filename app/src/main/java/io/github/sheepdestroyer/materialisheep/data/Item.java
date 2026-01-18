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

import android.content.Context;

/**
 * Represents an item that can be displayed as story/comment
 */
public interface Item extends WebItem {

    /**
     * Populates this item with information from another item.
     *
     * @param info the source item
     */
    void populate(Item info);

    /**
     * Gets raw item type, used to be parsed by {@link #getType()}
     * @return string type or null
     * @see Type
     */
    String getRawType();

    /**
     * Gets the raw URL of the item.
     *
     * @return the raw URL, or `null` if the item does not have a URL
     * @see #getUrl()
     */
    String getRawUrl();

    /**
     * Gets an array of the item's "kid" IDs.
     *
     * @return an array of kid IDs, or `null` if the item has no kids
     * @see #getKidCount()
     * @see #getKidItems()
     */
    long[] getKids();

    /**
     * Gets the username of the item's author.
     *
     * @return the author's username, or `null` if the item has no author
     * @see WebItem#getDisplayedAuthor(Context, boolean, int)
     */
    String getBy();

    /**
     * Gets the creation date of the item in Unix time.
     *
     * @return the creation date in seconds since the epoch
     * @see WebItem#getDisplayedAuthor(Context, boolean, int)
     */
    long getTime();

    /**
     * Gets the title of the item.
     *
     * @return the title, or `null` if the item does not have a title
     * @see #getDisplayedTitle()
     */
    String getTitle();

    /**
     * Gets the item's text.
     *
     * @return the item's text, or `null` if the item does not have text
     * @see #getDisplayedTitle()
     */
    String getText();

    /**
     * Gets the number of the item's kids.
     *
     * @return the number of kids
     * @see #getKids()
     * @see #getKidItems()
     */
    int getKidCount();

    /**
     * Gets the previous number of kids, before {@link #populate(Item)} is called.
     *
     * @return the previous number of kids
     * @see #setLastKidCount(int)
     */
    int getLastKidCount();

    /**
     * Sets the previous number of kids, before {@link #populate(Item)} is called.
     *
     * @param lastKidCount the previous number of kids
     */
    void setLastKidCount(int lastKidCount);

    /**
     * Checks if the item has new kids after {@link #populate(Item)}.
     *
     * @return `true` if the item has new kids, `false` otherwise
     */
    boolean hasNewKids();

    /**
     * Gets an array of the item's kids, with corresponding IDs in {@link #getKids()}.
     *
     * @return an array of kids, or `null` if the item has no kids
     * @see #getKids()
     * @see #getKidCount()
     */
    Item[] getKidItems();

    /**
     * Gets the item's current revision. A revision can be used to determine if the item's
     * state is stale and needs to be updated.
     *
     * @return the current revision
     * @see #setLocalRevision(int)
     * @see #populate(Item)
     * @see #setFavorite(boolean)
     */
    int getLocalRevision();

    /**
     * Updates the item's current revision to a new one.
     *
     * @param localRevision the new item revision
     * @see #getLocalRevision()
     */
    void setLocalRevision(int localRevision);

    /**
     * Gets the item's descendants, if any.
     *
     * @return the item's descendants, or -1 if none
     */
    int getDescendants();

    /**
     * Indicates whether this item has been viewed.
     *
     * @return `true` if the item has been viewed, `false` if not, `null` if unknown
     */
    boolean isViewed();

    /**
     * Sets the item's view status.
     *
     * @param isViewed `true` if the item has been viewed, `false` otherwise
     */
    void setIsViewed(boolean isViewed);

    /**
     * Gets the item's level, i.e., how many ascendants it has.
     *
     * @return the item's level
     */
    int getLevel();

    /**
     * Gets the parent ID, if any.
     *
     * @return the parent ID, or 0 if none
     */
    String getParent();

    /**
     * Gets the parent item, if any.
     *
     * @return the parent item, or `null`
     */
    Item getParentItem();

    /**
     * Checks if the item has been deleted.
     *
     * @return `true` if the item has been deleted, `false` otherwise
     */
    boolean isDeleted();

    /**
     * Checks if the item is dead.
     *
     * @return `true` if the item is dead, `false` otherwise
     */
    boolean isDead();

    /**
     * Gets the item's score.
     *
     * @return the item's score
     */
    int getScore();

    /**
     * Increments the item's score.
     */
    void incrementScore();

    /**
     * Checks if the item has been voted on via a user action.
     *
     * @return `true` if the item has been voted on, `false` otherwise
     * @see #incrementScore()
     */
    boolean isVoted();

    /**
     * Checks if the item has a pending vote via a user action.
     *
     * @return `true` if the item has a pending vote, `false` otherwise
     * @see #incrementScore()
     */
    boolean isPendingVoted();

    /**
     * Clears the pending voted status.
     *
     * @see #isPendingVoted()
     * @see #incrementScore()
     */
    void clearPendingVoted();

    /**
     * Checks if the item is collapsed.
     *
     * @return `true` if the item is collapsed, `false` otherwise
     */
    boolean isCollapsed();

    /**
     * Sets the item's collapsed state.
     *
     * @param collapsed `true` to collapse the item, `false` otherwise
     */
    void setCollapsed(boolean collapsed);

    /**
     * Gets the item's rank among its siblings.
     *
     * @return the item's rank
     */
    int getRank();

    /**
     * Checks if the item's content is expanded.
     *
     * @return `true` if the content is expanded, `false` otherwise
     */
    boolean isContentExpanded();

    /**
     * Sets the item's content expanded state.
     *
     * @param expanded `true` to expand the content, `false` otherwise
     */
    void setContentExpanded(boolean expanded);

    /**
     * Gets the ID of the neighboring item in the given direction.
     *
     * @param direction the direction of the neighbor
     * @return the ID of the neighboring item, or 0 if there is no neighbor in that direction
     */
    long getNeighbour(int direction);

    /**
     * Gets the displayed text of the item.
     *
     * @return the displayed text
     */
    CharSequence getDisplayedText();
}
