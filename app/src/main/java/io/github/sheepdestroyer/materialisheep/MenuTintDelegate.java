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

package io.github.sheepdestroyer.materialisheep;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import android.view.Menu;
import android.view.MenuItem;

/**
 * A delegate that helps tint menu items for activities and fragments.
 */
public class MenuTintDelegate {
    private int mTextColorPrimary;

    /**
     * A callback that should be triggered after the activity has been created.
     *
     * @param context The activity context.
     */
    public void onActivityCreated(Context context) {
        mTextColorPrimary = ContextCompat.getColor(context,
                AppUtils.getThemedResId(context, android.R.attr.textColorPrimary));
    }

    /**
     * A callback that should be triggered after the menu has been inflated.
     *
     * @param menu The inflated menu.
     */
    public void onOptionsMenuCreated(Menu menu) {
        for (int i = 0; i < menu.size(); i++) {
            Drawable drawable = menu.getItem(i).getIcon();
            if (drawable == null) {
                continue;
            }
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(drawable, mTextColorPrimary);
        }
    }

    /**
     * Sets the icon for a menu item and tints it.
     *
     * @param item The menu item.
     * @param icon The icon resource.
     */
    @SuppressWarnings("unused")
    public void setIcon(MenuItem item, @DrawableRes int icon) {
        item.setIcon(icon);
        Drawable drawable = item.getIcon();
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, mTextColorPrimary);
    }
}
