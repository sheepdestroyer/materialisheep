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

import android.accounts.AccountManager;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.github.sheepdestroyer.materialisheep.appwidget.WidgetService;
import io.github.sheepdestroyer.materialisheep.data.ItemSyncJobService;
import io.github.sheepdestroyer.materialisheep.data.ItemSyncService;

@Module(includes = DataModule.class)
/**
 * A Dagger module that provides dependencies for various activities and services in the application.
 * This module is responsible for injecting context-related objects and managers.
 */
public class ActivityModule {

    /**
     * Provides the AccountManager.
     *
     * @param context The application context.
     * @return The AccountManager instance.
     */
    @Provides @Singleton
    public AccountManager provideAccountManager(Context context) {
        return AccountManager.get(context);
    }
}
