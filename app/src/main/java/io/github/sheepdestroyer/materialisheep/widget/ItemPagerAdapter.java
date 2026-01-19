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

package io.github.sheepdestroyer.materialisheep.widget;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.ViewGroup;

import io.github.sheepdestroyer.materialisheep.AppUtils;
import io.github.sheepdestroyer.materialisheep.ItemFragment;
import io.github.sheepdestroyer.materialisheep.LazyLoadFragment;
import io.github.sheepdestroyer.materialisheep.Preferences;
import io.github.sheepdestroyer.materialisheep.R;
import io.github.sheepdestroyer.materialisheep.Scrollable;
import io.github.sheepdestroyer.materialisheep.WebFragment;
import io.github.sheepdestroyer.materialisheep.annotation.Synthetic;
import io.github.sheepdestroyer.materialisheep.data.Item;
import io.github.sheepdestroyer.materialisheep.data.WebItem;

public class ItemPagerAdapter extends androidx.viewpager2.adapter.FragmentStateAdapter {
    private final Context mContext;
    private final WebItem mItem;
    private final boolean mShowArticle;
    private final int mCacheMode;
    private final int mDefaultItem;

    /**
     * Constructor for ItemPagerAdapter.
     *
     * @param fragmentActivity The context activity
     * @param builder          The builder containing configuration
     */
    public ItemPagerAdapter(androidx.fragment.app.FragmentActivity fragmentActivity, @NonNull Builder builder) {
        super(fragmentActivity);
        mContext = fragmentActivity;
        mItem = builder.item;
        mShowArticle = builder.showArticle;
        mCacheMode = builder.cacheMode;
        mDefaultItem = Math.min(getItemCount() - 1,
                builder.defaultViewMode == Preferences.StoryViewMode.Comment ? 0 : 1);
    }

    /**
     * Returns the unique ID for the item at the given position.
     *
     * @param position Position of the item
     * @return The unique ID
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Creates the fragment for the given position.
     * 
     * @param position The position of the fragment
     * @return The fragment instance
     */
    @Override
    public Fragment createFragment(int position) {
        Bundle args = new Bundle();
        args.putBoolean(LazyLoadFragment.EXTRA_EAGER_LOAD, mDefaultItem == position);
        if (position == 0) {
            args.putParcelable(ItemFragment.EXTRA_ITEM, mItem);
            args.putInt(ItemFragment.EXTRA_CACHE_MODE, mCacheMode);
            Fragment fragment = new ItemFragment();
            fragment.setArguments(args);
            return fragment;
        } else {
            args.putParcelable(WebFragment.EXTRA_ITEM, mItem);
            Fragment fragment = new WebFragment();
            fragment.setArguments(args);
            return fragment;
        }
    }

    /**
     * Returns the total number of items in the adapter.
     *
     * @return The item count (1 if only comments/article, 2 if both)
     */
    @Override
    public int getItemCount() {
        return mItem.isStoryType() && !mShowArticle ? 1 : 2;
    }

    /**
     * Returns the title for the page at the given position.
     * 
     * @param position The position of the page
     * @return The title as a CharSequence
     */
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            if (mItem instanceof Item) {
                int count = ((Item) mItem).getKidCount();
                return mContext.getResources()
                        .getQuantityString(R.plurals.comments_count, count, count);
            }
            return mContext.getString(R.string.title_activity_item);
        }
        return mContext.getString(mItem.isStoryType() ? R.string.article : R.string.full_text);
    }

    public static class Builder {
        WebItem item;
        boolean showArticle;
        int cacheMode;
        Preferences.StoryViewMode defaultViewMode;

        public Builder setItem(@NonNull WebItem item) {
            this.item = item;
            return this;
        }

        public Builder setShowArticle(boolean showArticle) {
            this.showArticle = showArticle;
            return this;
        }

        public Builder setCacheMode(int cacheMode) {
            this.cacheMode = cacheMode;
            return this;
        }

        public Builder setDefaultViewMode(Preferences.StoryViewMode viewMode) {
            this.defaultViewMode = viewMode;
            return this;
        }
    }

    /**
     * Helper to generate the fragment tag used by ViewPager2's
     * FragmentStateAdapter.
     * 
     * @param itemId The item ID (usually position).
     * @return The tag string.
     */
    public static String getFragmentTag(long itemId) {
        return "f" + itemId;
    }

    /**
     * Helper to retrieve the fragment at the specified position.
     *
     * @param fragmentManager The FragmentManager.
     * @param position        The position of the fragment.
     * @return The fragment, or null.
     */
    public Fragment findFragment(FragmentManager fragmentManager, int position) {
        return fragmentManager.findFragmentByTag(getFragmentTag(getItemId(position)));
    }
}
