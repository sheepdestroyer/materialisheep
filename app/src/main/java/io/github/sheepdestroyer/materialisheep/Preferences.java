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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import io.github.sheepdestroyer.materialisheep.annotation.PublicApi;
import io.github.sheepdestroyer.materialisheep.annotation.Synthetic;
import io.github.sheepdestroyer.materialisheep.data.AlgoliaPopularClient;
import io.github.sheepdestroyer.materialisheep.preference.ThemePreference;

@SuppressWarnings("WeakerAccess")
@PublicApi
/**
 * A utility class for accessing and modifying application preferences.
 */
public class Preferences {
    private static final String DRAFT_PREFIX = "draft_%1$s";
    private static final String PREFERENCES_DRAFT = "_drafts";
    @VisibleForTesting
    static Boolean sReleaseNotesSeen = null;

    public enum SwipeAction {
        None,
        Vote,
        Save,
        Refresh,
        Share
    }

    public enum StoryViewMode {
        Comment,
        Article,
        Readability
    }

    private static final BoolToStringPref[] PREF_MIGRATION = new BoolToStringPref[] {
            new BoolToStringPref(R.string.pref_item_click, false,
                    R.string.pref_story_display, R.string.pref_story_display_value_comments),
            new BoolToStringPref(R.string.pref_item_search_recent, true,
                    R.string.pref_search_sort, R.string.pref_search_sort_value_default)
    };

    /**
     * Synchronizes the summary of all preferences.
     *
     * @param preferenceManager The preference manager.
     */
    public static void sync(PreferenceManager preferenceManager) {
        Map<String, ?> map = preferenceManager.getSharedPreferences().getAll();
        for (String key : map.keySet()) {
            sync(preferenceManager, key);
        }
    }

    private static void sync(PreferenceManager preferenceManager, String key) {
        Preference pref = preferenceManager.findPreference(key);
        if (pref instanceof ListPreference) {
            ListPreference listPref = (ListPreference) pref;
            pref.setSummary(listPref.getEntry());
        }
    }

    /**
     * Migrates boolean preferences to string preferences.
     *
     * @param context The application context.
     */
    public static void migrate(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        for (BoolToStringPref pref : PREF_MIGRATION) {
            if (pref.isChanged(context, sp)) {
                editor.putString(context.getString(pref.newKey), context.getString(pref.newValue));
            }

            if (pref.hasOldValue(context, sp)) {
                editor.remove(context.getString(pref.oldKey));
            }
        }

        editor.apply();
    }

    /**
     * Checks if the list item view is a card view.
     *
     * @param context The application context.
     * @return True if the list item view is a card view, false otherwise.
     */
    public static boolean isListItemCardView(Context context) {
        return get(context, R.string.pref_list_item_view, false);
    }

    /**
     * Checks if the search results are sorted by recent.
     *
     * @param context The application context.
     * @return True if the search results are sorted by recent, false otherwise.
     */
    public static boolean isSortByRecent(Context context) {
        return get(context, R.string.pref_search_sort, R.string.pref_search_sort_value_recent)
                .equals(context.getString(R.string.pref_search_sort_value_recent));
    }

    /**
     * Sets the search sort order.
     *
     * @param context  The application context.
     * @param byRecent True to sort by recent, false otherwise.
     */
    public static void setSortByRecent(Context context, boolean byRecent) {
        set(context, R.string.pref_search_sort, context.getString(
                byRecent ? R.string.pref_search_sort_value_recent : R.string.pref_search_sort_value_default));
    }

    /**
     * Gets the default story view mode.
     *
     * @param context The application context.
     * @return The default story view mode.
     */
    public static StoryViewMode getDefaultStoryView(Context context) {
        String pref = get(context, R.string.pref_story_display,
                R.string.pref_story_display_value_article);
        if (TextUtils.equals(context.getString(R.string.pref_story_display_value_comments), pref)) {
            return StoryViewMode.Comment;
        }
        if (TextUtils.equals(context.getString(R.string.pref_story_display_value_readability), pref)) {
            return StoryViewMode.Readability;
        }
        return StoryViewMode.Article;
    }

    /**
     * Checks if the external browser is enabled.
     *
     * @param context The application context.
     * @return True if the external browser is enabled, false otherwise.
     */
    public static boolean externalBrowserEnabled(Context context) {
        return get(context, R.string.pref_external, false);
    }

    /**
     * Checks if color coding is enabled.
     *
     * @param context The application context.
     * @return True if color coding is enabled, false otherwise.
     */
    public static boolean colorCodeEnabled(Context context) {
        return get(context, R.string.pref_color_code, true);
    }

    /**
     * Gets the color code opacity.
     *
     * @param context The application context.
     * @return The color code opacity.
     */
    public static int colorCodeOpacity(Context context) {
        return getInt(context, R.string.pref_color_code_opacity, 100);
    }

    /**
     * Checks if smooth scrolling is enabled.
     *
     * @param context The application context.
     * @return True if smooth scrolling is enabled, false otherwise.
     */
    public static boolean smoothScrollEnabled(Context context) {
        return get(context, R.string.pref_smooth_scroll, true);
    }

    /**
     * Checks if the thread indicator is enabled.
     *
     * @param context The application context.
     * @return True if the thread indicator is enabled, false otherwise.
     */
    public static boolean threadIndicatorEnabled(Context context) {
        return get(context, R.string.pref_thread_indicator, true);
    }

    /**
     * Checks if highlighting updated items is enabled.
     *
     * @param context The application context.
     * @return True if highlighting updated items is enabled, false otherwise.
     */
    public static boolean highlightUpdatedEnabled(Context context) {
        return get(context, R.string.pref_highlight_updated, true);
    }

    /**
     * Checks if items are automatically marked as viewed.
     *
     * @param context The application context.
     * @return True if items are automatically marked as viewed, false otherwise.
     */
    public static boolean autoMarkAsViewed(Context context) {
        return get(context, R.string.pref_auto_viewed, false);
    }

    /**
     * Checks if navigation is enabled.
     *
     * @param context The application context.
     * @return True if navigation is enabled, false otherwise.
     */
    public static boolean navigationEnabled(Context context) {
        return get(context, R.string.pref_navigation, false);
    }

    /**
     * Checks if navigation vibration is enabled.
     *
     * @param context The application context.
     * @return True if navigation vibration is enabled, false otherwise.
     */
    public static boolean navigationVibrationEnabled(Context context) {
        return get(context, R.string.pref_navigation_vibrate, true);
    }

    /**
     * Checks if custom tabs are enabled.
     *
     * @param context The application context.
     * @return True if custom tabs are enabled, false otherwise.
     */
    public static boolean customTabsEnabled(Context context) {
        return get(context, R.string.pref_custom_tab, true);
    }

    /**
     * Checks if the comment display is single page.
     *
     * @param context       The application context.
     * @param displayOption The display option.
     * @return True if the comment display is single page, false otherwise.
     */
    public static boolean isSinglePage(Context context, String displayOption) {
        return !TextUtils.equals(displayOption,
                context.getString(R.string.pref_comment_display_value_multiple));
    }

    /**
     * Checks if comments are auto-expanded.
     *
     * @param context       The application context.
     * @param displayOption The display option.
     * @return True if comments are auto-expanded, false otherwise.
     */
    public static boolean isAutoExpand(Context context, String displayOption) {
        return TextUtils.equals(displayOption,
                context.getString(R.string.pref_comment_display_value_single));
    }

    /**
     * Gets the comment display option.
     *
     * @param context The application context.
     * @return The comment display option.
     */
    public static String getCommentDisplayOption(Context context) {
        return get(context, R.string.pref_comment_display,
                R.string.pref_comment_display_value_single);
    }

    /**
     * Sets the popular range.
     *
     * @param context The application context.
     * @param range   The popular range.
     */
    public static void setPopularRange(Context context, @AlgoliaPopularClient.Range @NonNull String range) {
        set(context, R.string.pref_popular_range, range);
    }

    /**
     * Gets the popular range.
     *
     * @param context The application context.
     * @return The popular range.
     */
    @NonNull
    public static String getPopularRange(Context context) {
        return get(context, R.string.pref_popular_range, AlgoliaPopularClient.LAST_24H);
    }

    /**
     * Gets the maximum number of lines for a comment.
     *
     * @param context The application context.
     * @return The maximum number of lines for a comment.
     */
    public static int getCommentMaxLines(Context context) {
        String maxLinesString = get(context, R.string.pref_max_lines, null);
        int maxLines = maxLinesString == null ? -1 : Integer.parseInt(maxLinesString);
        if (maxLines < 0) {
            maxLines = Integer.MAX_VALUE;
        }
        return maxLines;
    }

    /**
     * Gets the line height for comments.
     *
     * @param context The application context.
     * @return The line height for comments.
     */
    public static float getLineHeight(Context context) {
        return getFloatFromString(context, R.string.pref_line_height, 1.0f);
    }

    /**
     * Gets the line height for readability mode.
     *
     * @param context The application context.
     * @return The line height for readability mode.
     */
    public static float getReadabilityLineHeight(Context context) {
        return getFloatFromString(context, R.string.pref_readability_line_height, 1.0f);
    }

    /**
     * Checks if lazy loading is enabled.
     *
     * @param context The application context.
     * @return True if lazy loading is enabled, false otherwise.
     */
    public static boolean shouldLazyLoad(Context context) {
        return get(context, R.string.pref_lazy_load, true);
    }

    /**
     * Gets the username of the current user.
     *
     * @param context The application context.
     * @return The username of the current user.
     */
    public static String getUsername(Context context) {
        return get(context, R.string.pref_username, null);
    }

    /**
     * Sets the username of the current user.
     *
     * @param context  The application context.
     * @param username The username of the current user.
     */
    public static void setUsername(Context context, String username) {
        set(context, R.string.pref_username, username);
    }

    /**
     * Gets the launch screen preference.
     *
     * @param context The application context.
     * @return The launch screen preference.
     */
    @NonNull
    public static String getLaunchScreen(Context context) {
        return get(context, R.string.pref_launch_screen, R.string.pref_launch_screen_value_top);
    }

    /**
     * Checks if the launch screen is the last used screen.
     *
     * @param context The application context.
     * @return True if the launch screen is the last used screen, false otherwise.
     */
    public static boolean isLaunchScreenLast(Context context) {
        return TextUtils.equals(context.getString(R.string.pref_launch_screen_value_last),
                getLaunchScreen(context));
    }

    /**
     * Checks if ad blocking is enabled.
     *
     * @param context The application context.
     * @return True if ad blocking is enabled, false otherwise.
     */
    public static boolean adBlockEnabled(Context context) {
        return get(context, R.string.pref_ad_block, true);
    }

    /**
     * Saves a draft of a comment.
     *
     * @param context  The application context.
     * @param parentId The ID of the parent item.
     * @param draft    The draft content.
     */
    public static void saveDraft(Context context, String parentId, String draft) {
        context.getSharedPreferences(context.getPackageName() + PREFERENCES_DRAFT, Context.MODE_PRIVATE)
                .edit()
                .putString(String.format(Locale.US, DRAFT_PREFIX, parentId), draft)
                .apply();
    }

    /**
     * Gets a draft of a comment.
     *
     * @param context  The application context.
     * @param parentId The ID of the parent item.
     * @return The draft content.
     */
    public static String getDraft(Context context, String parentId) {
        return context
                .getSharedPreferences(context.getPackageName() + PREFERENCES_DRAFT, Context.MODE_PRIVATE)
                .getString(String.format(Locale.US, DRAFT_PREFIX, parentId), null);
    }

    /**
     * Deletes a draft of a comment.
     *
     * @param context  The application context.
     * @param parentId The ID of the parent item.
     */
    public static void deleteDraft(Context context, String parentId) {
        context.getSharedPreferences(context.getPackageName() + PREFERENCES_DRAFT, Context.MODE_PRIVATE)
                .edit()
                .remove(String.format(Locale.US, DRAFT_PREFIX, parentId))
                .apply();
    }

    /**
     * Clears all drafts.
     *
     * @param context The application context.
     */
    public static void clearDrafts(Context context) {
        context.getSharedPreferences(context.getPackageName() + PREFERENCES_DRAFT, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }

    /**
     * Checks if the release notes have been seen.
     *
     * @param context The application context.
     * @return True if the release notes have been seen, false otherwise.
     */
    public static boolean isReleaseNotesSeen(Context context) {
        if (sReleaseNotesSeen == null) {
            PackageInfo info = null;
            try {
                info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                // no op
            }
            // considered seen if first time install or last seen release is up to date
            if (info != null && info.firstInstallTime == info.lastUpdateTime) {
                setReleaseNotesSeen(context);
            } else {
                sReleaseNotesSeen = getInt(context, R.string.pref_latest_release, 0) >= BuildConfig.LATEST_RELEASE;
            }
        }
        return sReleaseNotesSeen;
    }

    /**
     * Sets the release notes as seen.
     *
     * @param context The application context.
     */
    public static void setReleaseNotesSeen(Context context) {
        sReleaseNotesSeen = true;
        setInt(context, R.string.pref_latest_release, BuildConfig.LATEST_RELEASE);
    }

    /**
     * Checks if multi-window mode is enabled.
     *
     * @param context The application context.
     * @return True if multi-window mode is enabled, false otherwise.
     */
    public static boolean multiWindowEnabled(Context context) {
        return !TextUtils.equals(context.getString(R.string.pref_multi_window_value_none),
                get(context, R.string.pref_multi_window, R.string.pref_multi_window_value_none));
    }

    /**
     * Gets the swipe preferences for the list.
     *
     * @param context The application context.
     * @return The swipe preferences for the list.
     */
    public static SwipeAction[] getListSwipePreferences(Context context) {
        String left = get(context, R.string.pref_list_swipe_left, R.string.swipe_save),
                right = get(context, R.string.pref_list_swipe_right, R.string.swipe_vote);
        return new SwipeAction[] { parseSwipeAction(left), parseSwipeAction(right) };
    }

    /**
     * Resets all preferences to their default values.
     *
     * @param context The application context.
     */
    public static void reset(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .clear()
                .apply();
    }

    private static SwipeAction parseSwipeAction(String value) {
        try {
            return SwipeAction.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException e) {
            return SwipeAction.None;
        }
    }

    @Synthetic
    static boolean get(Context context, @StringRes int key, boolean defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(key), defaultValue);
    }

    private static int getInt(Context context, @StringRes int key, int defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(context.getString(key), defaultValue);
    }

    private static float getFloatFromString(Context context, @StringRes int key, float defaultValue) {
        String floatValue = get(context, key, null);
        try {
            return Float.parseFloat(floatValue);
        } catch (NumberFormatException | NullPointerException e) {
            return defaultValue;
        }
    }

    @Synthetic
    static String get(Context context, @StringRes int key, String defaultValue) {
        return get(context, context.getString(key), defaultValue);
    }

    private static String get(Context context, @StringRes int key, @StringRes int defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(key), context.getString(defaultValue));
    }

    private static String get(Context context, String key, String defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(key, defaultValue);
    }

    private static void setInt(Context context, @StringRes int key, int value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(context.getString(key), value)
                .apply();
    }

    private static void set(Context context, @StringRes int key, String value) {
        set(context, context.getString(key), value);
    }

    private static void set(Context context, String key, String value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(key, value)
                .apply();
    }

    @Synthetic
    static void set(Context context, @StringRes int key, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(context.getString(key), value)
                .apply();
    }

    public static class BoolToStringPref {
        @Synthetic
        final int oldKey;
        private final boolean oldDefault;
        @Synthetic
        final int newKey;
        @Synthetic
        final int newValue;

        @Synthetic
        BoolToStringPref(@StringRes int oldKey, boolean oldDefault,
                @StringRes int newKey, @StringRes int newValue) {
            this.oldKey = oldKey;
            this.oldDefault = oldDefault;
            this.newKey = newKey;
            this.newValue = newValue;
        }

        @Synthetic
        boolean isChanged(Context context, SharedPreferences sp) {
            return hasOldValue(context, sp) &&
                    sp.getBoolean(context.getString(oldKey), oldDefault) != oldDefault;
        }

        @Synthetic
        boolean hasOldValue(Context context, SharedPreferences sp) {
            return sp.contains(context.getString(oldKey));
        }
    }

    @PublicApi
    public static class Theme {

        public static void apply(Context context, boolean dialogTheme, boolean isTranslucent) {
            ThemePreference.ThemeSpec themeSpec = getTheme(context, isTranslucent);
            context.setTheme(themeSpec.theme);
            if (themeSpec.themeOverrides >= 0) {
                context.getTheme().applyStyle(themeSpec.themeOverrides, true);
            }
            if (dialogTheme) {
                context.setTheme(AppUtils.getThemedResId(context, androidx.appcompat.R.attr.alertDialogTheme));
            }
        }

        static @Nullable String getTypeface(Context context) {
            return get(context, R.string.pref_font, null);
        }

        static @Nullable String getReadabilityTypeface(Context context) {
            String typefaceName = get(context, R.string.pref_readability_font, null);
            if (TextUtils.isEmpty(typefaceName)) {
                return getTypeface(context);
            }
            return typefaceName;
        }

        public static @StyleRes int resolveTextSize(String choice) {
            switch (Integer.parseInt(choice)) {
                case -1:
                    return R.style.AppTextSize_XSmall;
                case 0:
                default:
                    return R.style.AppTextSize;
                case 1:
                    return R.style.AppTextSize_Medium;
                case 2:
                    return R.style.AppTextSize_Large;
                case 3:
                    return R.style.AppTextSize_XLarge;
            }
        }

        public static @StyleRes int resolvePreferredTextSize(Context context) {
            return resolveTextSize(getPreferredTextSize(context));
        }

        static @StyleRes int resolvePreferredReadabilityTextSize(Context context) {
            return resolveTextSize(getPreferredReadabilityTextSize(context));
        }

        public static int getAutoDayNightMode(Context context) {
            return getTheme(context, false) instanceof ThemePreference.DayNightSpec &&
                    get(context, R.string.pref_daynight_auto, false) ? AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                            : AppCompatDelegate.MODE_NIGHT_NO;
        }

        public static void disableAutoDayNight(Context context) {
            set(context, R.string.pref_daynight_auto, false);
        }

        private static @NonNull String getPreferredReadabilityTextSize(Context context) {
            String choice = get(context, R.string.pref_readability_text_size, null);
            if (TextUtils.isEmpty(choice)) {
                return getPreferredTextSize(context);
            }
            return choice;
        }

        private static @NonNull String getPreferredTextSize(Context context) {
            return get(context, R.string.pref_text_size, String.valueOf(0));
        }

        private static ThemePreference.ThemeSpec getTheme(Context context, boolean isTransulcent) {
            return ThemePreference.getTheme(get(context, R.string.pref_theme, null), isTransulcent);
        }
    }

    @PublicApi
    public static class Offline {

        public static boolean isEnabled(Context context) {
            return get(context, R.string.pref_saved_item_sync, false);
        }

        public static boolean isCommentsEnabled(Context context) {
            return isEnabled(context) &&
                    get(context, R.string.pref_offline_comments, true);
        }

        public static boolean isArticleEnabled(Context context) {
            return isEnabled(context) &&
                    get(context, R.string.pref_offline_article, true);
        }

        public static boolean isReadabilityEnabled(Context context) {
            return isEnabled(context) &&
                    get(context, R.string.pref_offline_readability, true);
        }

        public static boolean currentConnectionEnabled(Context context) {
            return !isWifiOnly(context) || AppUtils.isOnWiFi(context);
        }

        public static boolean isNotificationEnabled(Context context) {
            return get(context, R.string.pref_offline_notification, false);
        }

        public static boolean isWifiOnly(Context context) {
            String wifiValue = context.getString(R.string.offline_data_wifi);
            return TextUtils.equals(wifiValue, get(context, R.string.pref_offline_data, wifiValue));
        }
    }

    public static class Observable {
        private static Set<String> CONTEXT_KEYS;
        private final Map<String, Integer> mSubscribedKeys = new HashMap<>();
        private final SharedPreferences.OnSharedPreferenceChangeListener mListener = (sharedPreferences, key) -> {
            if (mSubscribedKeys.containsKey(key)) {
                notifyChanged(mSubscribedKeys.get(key), CONTEXT_KEYS.contains(key));
            }
        };
        private Observer mObserver;

        public void subscribe(Context context, @NonNull Observer observer, @NonNull int... preferenceKeys) {
            ensureContextKeys(context);
            setSubscription(context, preferenceKeys);
            mObserver = observer;
            PreferenceManager.getDefaultSharedPreferences(context)
                    .registerOnSharedPreferenceChangeListener(mListener);
        }

        public void unsubscribe(Context context) {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .unregisterOnSharedPreferenceChangeListener(mListener);
        }

        private void setSubscription(Context context, int[] preferenceKeys) {
            mSubscribedKeys.clear();
            for (int key : preferenceKeys) {
                mSubscribedKeys.put(context.getString(key), key);
            }
        }

        private void notifyChanged(int key, boolean contextChanged) {
            if (mObserver != null) {
                mObserver.onPreferenceChanged(key, contextChanged);
            }
        }

        @SuppressLint("UseSparseArrays")
        private void ensureContextKeys(Context context) {
            if (CONTEXT_KEYS != null) {
                return;
            }
            CONTEXT_KEYS = new HashSet<>();
            CONTEXT_KEYS.add(context.getString(R.string.pref_theme));
            CONTEXT_KEYS.add(context.getString(R.string.pref_text_size));
            CONTEXT_KEYS.add(context.getString(R.string.pref_font));
            CONTEXT_KEYS.add(context.getString(R.string.pref_daynight_auto));
        }
    }

    public interface Observer {
        void onPreferenceChanged(@StringRes int key, boolean contextChanged);
    }
}
