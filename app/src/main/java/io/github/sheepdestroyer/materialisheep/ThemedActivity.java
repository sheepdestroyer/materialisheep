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

import android.app.ActivityManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import android.text.TextUtils;
import android.view.Menu;

/**
 * An abstract base activity that supports different themes.
 */
public abstract class ThemedActivity extends AppCompatActivity {
    private final MenuTintDelegate mMenuTintDelegate = new MenuTintDelegate();
    private final Preferences.Observable mThemeObservable = new Preferences.Observable();
    private boolean mResumed = true;
    private boolean mPendingThemeChanged;

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle
     *                           contains the data it most
     *                           recently supplied in
     *                           {@link #onSaveInstanceState(Bundle)}.
     *                           Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Preferences.Theme.apply(this, isDialogTheme(), isTranslucent());
        super.onCreate(savedInstanceState);
        setTaskTitle(getTitle());
        mMenuTintDelegate.onActivityCreated(this);
    }

    /**
     * Called after {@link #onCreate(Bundle)} has completed.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle
     *                           contains the data it most
     *                           recently supplied in
     *                           {@link #onSaveInstanceState(Bundle)}.
     *                           Otherwise it is null.
     */
    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mThemeObservable.subscribe(this, (key, contextChanged) -> onThemeChanged(key),
                R.string.pref_theme, R.string.pref_daynight_auto);
    }

    /**
     * Initialize the contents of the Activity's standard options menu.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed;
     *         if you return false it will not be shown.
     */
    @CallSuper
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenuTintDelegate.onOptionsMenuCreated(menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Called after {@link #onRestoreInstanceState(Bundle)}, {@link #onRestart()},
     * or
     * {@link #onPause()}, for your activity to start interacting with the user.
     */
    @Override
    protected void onResume() {
        super.onResume();
        mResumed = true;
        if (mPendingThemeChanged) {
            AppUtils.restart(this, false);
        }
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into
     * the background, but has not (yet) been killed.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mResumed = false;
    }

    /**
     * Called before the activity is destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mThemeObservable.unsubscribe(this);
    }

    /**
     * Set the title of the activity.
     *
     * @param title The title to set.
     */
    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        setTaskTitle(title);
    }

    /**
     * Checks if the activity should be displayed as a dialog.
     *
     * @return True if the activity should be displayed as a dialog, false
     *         otherwise.
     */
    protected boolean isDialogTheme() {
        return false;
    }

    /**
     * Checks if the activity should be translucent.
     *
     * @return True if the activity should be translucent, false otherwise.
     */
    protected boolean isTranslucent() {
        return false;
    }

    private void onThemeChanged(int key) {
        if (key == R.string.pref_daynight_auto) {
            AppCompatDelegate.setDefaultNightMode(Preferences.Theme.getAutoDayNightMode(this));
        }
        if (mResumed) {
            AppUtils.restart(this, true);
        } else {
            mPendingThemeChanged = true;
        }
    }

    void setTaskTitle(CharSequence title) {
        if (!TextUtils.isEmpty(title)) {
            setTaskDescription(new ActivityManager.TaskDescription(title.toString(),
                    BitmapFactory.decodeResource(getResources(), R.drawable.ic_app),
                    ContextCompat.getColor(this,
                            AppUtils.getThemedResId(this, androidx.appcompat.R.attr.colorPrimary))));
        }
    }
}
