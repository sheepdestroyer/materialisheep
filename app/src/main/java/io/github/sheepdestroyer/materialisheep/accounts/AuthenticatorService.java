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

package io.github.sheepdestroyer.materialisheep.accounts;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;

/**
 * A service that provides the account authenticator.
 */
public class AuthenticatorService extends Service {
    private AccountAuthenticator mAuthenticator;

    /**
     * Called by the system when the service is first created.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        mAuthenticator = new AccountAuthenticator(this);
    }

    /**
     * Return the communication channel to the service.
     *
     * @param intent The Intent that was used to bind to this service.
     * @return Return an IBinder through which clients can call on to the
     *         service.
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
