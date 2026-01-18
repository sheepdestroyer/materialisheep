/*
 * Copyright (c) 2023 Ha Duy Trung
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may
 * obtain a copy of the License at
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

import android.app.Application;
import android.graphics.Typeface;
import androidx.appcompat.app.AppCompatDelegate;
import android.os.StrictMode;
import io.github.sheepdestroyer.materialisheep.data.AlgoliaClient;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MaterialisticApplication extends android.app.Application {
    public static Typeface TYPE_FACE = null;
    public ApplicationComponent applicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        applicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
        AppCompatDelegate.setDefaultNightMode(Preferences.Theme.getAutoDayNightMode(this));
        AlgoliaClient.sSortByTime = Preferences.isSortByRecent(this);
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyFlashScreen()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
        }
        Preferences.migrate(this);
        TYPE_FACE = FontCache.getInstance().get(this, Preferences.Theme.getTypeface(this));
        AppUtils.registerAccountsUpdatedListener(this);
        AdBlocker.init(this, Schedulers.io());
    }
}
