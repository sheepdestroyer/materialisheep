/*
 * Copyright (c) 2018 Ha Duy Trung
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

package io.github.sheepdestroyer.materialisheep.ktx

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import io.github.sheepdestroyer.materialisheep.AppUtils
import java.io.Closeable
import java.io.File

/**
 * Closes this [Closeable] quietly.
 */
fun Closeable?.closeQuietly() {
    try {
        this?.close()
    } catch (ignored: java.io.IOException) {
    }
}

/**
 * Gets a content URI for this file.
 *
 * @param context   the context
 * @param authority the authority of the file provider
 * @return a content URI for this file
 */
fun File.getUri(context: Context, authority: String) =
    FileProvider.getUriForFile(context, authority, this)!!

/**
 * Creates a chooser intent for sending this URI.
 *
 * @param context the context
 * @return a chooser intent for sending this URI
 */
fun Uri.toSendIntentChooser(context: Context) =
    AppUtils.makeSendIntentChooser(context, this)!!

/**
 * Sets the notification channel for this builder.
 *
 * @param context   the context
 * @param channelId the ID of the notification channel
 * @param name      the name of the notification channel
 * @return this builder
 */
fun NotificationCompat.Builder.setChannel(context: Context,
    channelId: String,
    name: CharSequence): NotificationCompat.Builder {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        .createNotificationChannel(NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_LOW))
    this.setChannelId(channelId)
  }
  return this
}
