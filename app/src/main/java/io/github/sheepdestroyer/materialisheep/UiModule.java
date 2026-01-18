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

package io.github.sheepdestroyer.materialisheep;

import android.annotation.SuppressLint;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.github.sheepdestroyer.materialisheep.appwidget.WidgetConfigActivity;
import io.github.sheepdestroyer.materialisheep.widget.FavoriteRecyclerViewAdapter;
import io.github.sheepdestroyer.materialisheep.widget.MultiPageItemRecyclerViewAdapter;
import io.github.sheepdestroyer.materialisheep.widget.PopupMenu;
import io.github.sheepdestroyer.materialisheep.widget.SinglePageItemRecyclerViewAdapter;
import io.github.sheepdestroyer.materialisheep.widget.StoryRecyclerViewAdapter;
import io.github.sheepdestroyer.materialisheep.widget.SubmissionRecyclerViewAdapter;
import io.github.sheepdestroyer.materialisheep.widget.ThreadPreviewRecyclerViewAdapter;

@Module
/**
 * A Dagger module that provides UI-related dependencies.
 */
class UiModule {
    /**
     * Provides a singleton instance of {@link PopupMenu}.
     *
     * @return The singleton instance of {@link PopupMenu}.
     */
    @Provides
    public PopupMenu providePopupMenu() {
        return new PopupMenu.Impl();
    }

    /**
     * Provides a singleton instance of {@link CustomTabsDelegate}.
     *
     * @return The singleton instance of {@link CustomTabsDelegate}.
     */
    @Provides @Singleton
    public CustomTabsDelegate provideCustomTabsDelegate() {
        return new CustomTabsDelegate();
    }

    /**
     * Provides a singleton instance of {@link KeyDelegate}.
     *
     * @return The singleton instance of {@link KeyDelegate}.
     */
    @Provides @Singleton
    public KeyDelegate provideKeyDelegate() {
        return new KeyDelegate();
    }

    /**
     * Provides a singleton instance of {@link ActionViewResolver}.
     *
     * @return The singleton instance of {@link ActionViewResolver}.
     */
    @Provides @Singleton
    public ActionViewResolver provideActionViewResolver() {
        return new ActionViewResolver();
    }

    /**
     * Provides a singleton instance of {@link AlertDialogBuilder}.
     *
     * @return The singleton instance of {@link AlertDialogBuilder}.
     */
    @Provides
    public AlertDialogBuilder provideAlertDialogBuilder() {
        return new AlertDialogBuilder.Impl();
    }

    /**
     * Provides a singleton instance of {@link ResourcesProvider}.
     *
     * @param context The application context.
     * @return The singleton instance of {@link ResourcesProvider}.
     */
    @SuppressLint("Recycle")
    @Provides @Singleton
    public ResourcesProvider provideResourcesProvider(Context context) {
        return resId -> context.getResources().obtainTypedArray(resId);
    }
}
