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

package io.github.sheepdestroyer.materialisheep.appwidget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import io.github.sheepdestroyer.materialisheep.ThemedActivity;
import androidx.fragment.app.Fragment;
import io.github.sheepdestroyer.materialisheep.MaterialisticApplication;
import androidx.preference.PreferenceFragmentCompat;
import android.text.TextUtils;
import android.view.Window;

import io.github.sheepdestroyer.materialisheep.R;

/**
 * An activity that allows the user to configure a new widget.
 */
public class WidgetConfigActivity extends ThemedActivity {
    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MaterialisticApplication) getApplication()).applicationComponent.inject(this);
        setResult(RESULT_CANCELED);
        if (getIntent().getExtras() == null ||
                (mAppWidgetId = getIntent().getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                        AppWidgetManager.INVALID_APPWIDGET_ID)) == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_widget_config);
        if (savedInstanceState == null) {
            Bundle args = new Bundle();
            args.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.widget_preferences,
                            Fragment.instantiate(this, WidgetConfigurationFragment.class.getName(), args),
                            WidgetConfigurationFragment.class.getName())
                    .commit();
        }
        //noinspection ConstantConditions
        findViewById(R.id.button_ok).setOnClickListener(v -> configure());
    }

    @Override
    protected boolean isDialogTheme() {
        return true;
    }

    /**
     * Finishes the widget configuration and returns the result.
     */
    private void configure() {
        new WidgetHelper(this).configure(mAppWidgetId);
        setResult(RESULT_OK, new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId));
        finish();
    }

    /**
     * A fragment that hosts the widget configuration preferences.
     */
    public static class WidgetConfigurationFragment extends PreferenceFragmentCompat {

        private SharedPreferences.OnSharedPreferenceChangeListener mListener =
                (sharedPreferences, key) -> {
                    if (TextUtils.equals(key, getString(R.string.pref_widget_query))) {
                        setFilterQuery();
                    }
                };

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesName(WidgetHelper.getConfigName(
                    getArguments().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID)));
            getPreferenceManager().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(mListener);
            setFilterQuery();
        }

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            addPreferencesFromResource(R.xml.preferences_widget);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            getPreferenceManager().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(mListener);
        }

        /**
         * Sets the summary for the filter query preference to its current value.
         */
        private void setFilterQuery() {
            String key = getString(R.string.pref_widget_query);
            getPreferenceManager().findPreference(key)
                    .setSummary(getPreferenceManager().getSharedPreferences()
                            .getString(key, null));
        }
    }
}
