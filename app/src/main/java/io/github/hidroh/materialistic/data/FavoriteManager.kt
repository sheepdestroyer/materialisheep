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

package io.github.hidroh.materialistic.data

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.database.CursorWrapper
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.annotation.AnyThread
import androidx.annotation.WorkerThread
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.github.hidroh.materialistic.DataModule
import io.github.hidroh.materialistic.FavoriteActivity
import io.github.hidroh.materialistic.R
import io.github.hidroh.materialistic.ktx.closeQuietly
import io.github.hidroh.materialistic.ktx.getUri
import io.github.hidroh.materialistic.ktx.setChannel
import io.github.hidroh.materialistic.ktx.toSendIntentChooser
import okio.Okio
import okio.buffer
import okio.sink
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Data repository for {@link Favorite}
 */
@Singleton
class FavoriteManager @Inject constructor(
    private val cache: LocalCache,
    @Named(DataModule.IO_THREAD) private val ioScheduler: Scheduler,
    private val dao: MaterialisticDatabase.SavedStoriesDao) : LocalItemManager<Favorite> {

  companion object {
    /**
     * The notification channel ID for export notifications.
     */
    private const val CHANNEL_EXPORT = "export"
    private const val URI_PATH_ADD = "add"
    private const val URI_PATH_REMOVE = "remove"
    private const val URI_PATH_CLEAR = "clear"
    private const val PATH_SAVED = "saved"
    private const val FILENAME_EXPORT = "materialistic-export.txt"
    private const val FILE_AUTHORITY = "io.github.hidroh.materialistic.fileprovider"

    /**
     * Checks if a URI represents an added favorite.
     *
     * @param uri the URI to check
     * @return `true` if the URI represents an added favorite, `false` otherwise
     */
    fun isAdded(uri: Uri) = uri.toString().startsWith(buildAdded().toString())

    /**
     * Checks if a URI represents a removed favorite.
     *
     * @param uri the URI to check
     * @return `true` if the URI represents a removed favorite, `false` otherwise
     */
    fun isRemoved(uri: Uri) = uri.toString().startsWith(buildRemoved().toString())

    /**
     * Checks if a URI represents cleared favorites.
     *
     * @param uri the URI to check
     * @return `true` if the URI represents cleared favorites, `false` otherwise
     */
    fun isCleared(uri: Uri) = uri.toString().startsWith(buildCleared().toString())

    private fun buildAdded(): Uri.Builder =
        MaterialisticDatabase.getBaseSavedUri().buildUpon().appendPath(URI_PATH_ADD)

    private fun buildCleared(): Uri.Builder =
        MaterialisticDatabase.getBaseSavedUri().buildUpon().appendPath(URI_PATH_CLEAR)

    private fun buildRemoved(): Uri.Builder =
        MaterialisticDatabase.getBaseSavedUri().buildUpon().appendPath(URI_PATH_REMOVE)
  }

  private val notificationId = System.currentTimeMillis().toInt()
  private val syncScheduler = SyncScheduler()
  private var cursor: Cursor? = null
  private var loader: FavoriteRoomLoader? = null

  override fun getSize() = cursor?.count ?: 0

  override fun getItem(position: Int) = if (cursor?.moveToPosition(position) == true) {
      cursor!!.favorite
    } else {
      null
    }

  override fun attach(observer: LocalItemManager.Observer, filter: String?) {
    loader = FavoriteRoomLoader(filter, observer)
    loader!!.load()
  }

  override fun detach() {
    if (cursor != null) {
      cursor = null
    }
    loader = null
  }

  /**
   * Exports all favorites matched given query to a file.
   *
   * @param context an instance of [Context]
   * @param query   a query to filter stories to be retrieved
   */
  @SuppressLint("CheckResult")
  fun export(context: Context, query: String?) {
    val appContext = context.applicationContext
    notifyExportStart(appContext)
    Observable.defer { Observable.just(query ?: "") }
        .map { query(it) }
        .filter { it.moveToFirst() }
        .map {
          try {
            toFile(appContext, Cursor(it))?.let { uri -> listOf(uri) } ?: emptyList()
          } catch (e: IOException) {
            emptyList<Uri>()
          } finally {
            it.close()
          }
        }
        .onErrorReturn { emptyList() }
        .defaultIfEmpty(emptyList())
        .subscribeOn(ioScheduler)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { notifyExportDone(appContext, it.firstOrNull()) }
  }

  /**
   * Adds a story as a favorite.
   *
   * @param context an instance of [Context]
   * @param story   the story to be added as a favorite
   */
  @SuppressLint("CheckResult")
  fun add(context: Context, story: WebItem) {
    Observable.defer { Observable.just(story) }
        .doOnNext { insert(it) }
        .map { it.id }
        .map { buildAdded().appendPath(story.id).build() }
        .subscribeOn(ioScheduler)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { MaterialisticDatabase.getInstance(context).setLiveValue(it) }
    syncScheduler.scheduleSync(context, story.id)
  }

  /**
   * Clears all stories that match a given query from favorites.
   *
   * @param context an instance of [Context]
   * @param query   a query to filter stories to be cleared
   */
  @SuppressLint("CheckResult")
  fun clear(context: Context, query: String?) {
    Observable.defer { Observable.just(query ?: "") }
        .map { deleteMultiple(it) }
        .subscribeOn(ioScheduler)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { MaterialisticDatabase.getInstance(context).setLiveValue(buildCleared().build()) }
  }

  /**
   * Removes a story with a given ID from favorites.
   *
   * @param context an instance of [Context]
   * @param itemId  the ID of the story to be removed from favorites
   */
  @SuppressLint("CheckResult")
  fun remove(context: Context, itemId: String?) {
    if (itemId == null) return
    Observable.defer { Observable.just(itemId) }
        .doOnNext { delete(it) }
        .map { buildRemoved().appendPath(it).build() }
        .subscribeOn(ioScheduler)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { MaterialisticDatabase.getInstance(context).setLiveValue(it) }
  }

  /**
   * Removes multiple stories with given IDs from favorites.
   *
   * @param context an instance of [Context]
   * @param itemIds a collection of story IDs to be removed from favorites
   */
  @SuppressLint("CheckResult")
  fun remove(context: Context, itemIds: Collection<String>?) {
    if (itemIds.orEmpty().isEmpty()) return
    Observable.defer { Observable.fromIterable(itemIds.orEmpty()) }
        .subscribeOn(ioScheduler)
        .doOnNext { delete(it) }
        .map { buildRemoved().appendPath(it).build() }
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { MaterialisticDatabase.getInstance(context).setLiveValue(it) }
  }

  /**
   * Checks if a story is a favorite.
   *
   * @param itemId the ID of the story to check
   * @return an [Observable] that emits `true` if the story is a favorite, `false` otherwise
   */
  @WorkerThread
  fun check(itemId: String?) = Observable.just(if (itemId.isNullOrEmpty()) {
    false
  } else {
    cache.isFavorite(itemId)
  })!!

  @WorkerThread
  private fun toFile(context: Context, cursor: Cursor): Uri? {
    if (cursor.count == 0) return null
    val dir = File(context.filesDir, PATH_SAVED)
    if (!dir.exists() && !dir.mkdir()) return null
    val file = File(dir, FILENAME_EXPORT)
    if (!file.exists() && !file.createNewFile()) return null
    val bufferedSink = file.sink().buffer()
    with(bufferedSink) {
      do {
        val item = cursor.favorite
        writeUtf8(item.displayedTitle)
        writeByte('\n'.code)
        writeUtf8(item.url)
        writeByte('\n'.code)
        writeUtf8(HackerNewsClient.WEB_ITEM_PATH.format(item.id))
        if (!cursor.isLast) {
          writeByte('\n'.code)
          writeByte('\n'.code)
        }
      } while (cursor.moveToNext())
      flush()
      closeQuietly()
    }
    return file.getUri(context, FILE_AUTHORITY)
  }

  @SuppressLint("MissingPermission")
  private fun notifyExportStart(context: Context) {
    NotificationManagerCompat.from(context)
      .notify(
        notificationId, createNotificationBuilder(context)
          .setCategory(NotificationCompat.CATEGORY_PROGRESS)
          .setProgress(0, 0, true)
          .setContentIntent(
            PendingIntent.getActivity(
              context, 0,
              Intent(context, FavoriteActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
              PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
          )
          .build()
      )
  }

  @SuppressLint("MissingPermission")
  private fun notifyExportDone(context: Context, uri: Uri?) {
    val manager = NotificationManagerCompat.from(context)
    with(manager) {
      cancel(notificationId)
      if (uri == null) return
      context.grantUriPermission(context.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
      notify(
        notificationId, createNotificationBuilder(context)
          .setPriority(NotificationCompat.PRIORITY_HIGH)
          .setVibrate(longArrayOf(0L))
          .setContentText(context.getString(R.string.export_notification))
          .setContentIntent(
            PendingIntent.getActivity(
              context, 0,
              uri.toSendIntentChooser(context).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
              PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
          )
          .build()
      )
    }
  }

  private fun createNotificationBuilder(context: Context) =
      NotificationCompat.Builder(context, CHANNEL_EXPORT)
          .setChannel(context, CHANNEL_EXPORT, context.getString(R.string.export_saved_stories))
          .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
          .setSmallIcon(R.drawable.ic_notification)
          .setContentTitle(context.getString(R.string.export_saved_stories))
          .setAutoCancel(true)

  @WorkerThread
  private fun query(filter: String?): android.database.Cursor = if (filter.isNullOrEmpty()) {
    dao.selectAllToCursor()
  } else {
    dao.searchToCursor(filter)
  }

  @WorkerThread
  private fun insert(story: WebItem) {
    dao.insert(MaterialisticDatabase.SavedStory.from(story))
    loader?.load()
  }

  @WorkerThread
  private fun delete(itemId: String?) {
    dao.deleteByItemId(itemId)
    loader?.load()
  }

  @WorkerThread
  private fun deleteMultiple(query: String?): Int {
    val deleted = if (query.isNullOrEmpty()) dao.deleteAll() else dao.deleteByTitle(query)
    loader?.load()
    return deleted
  }

  /**
   * A cursor wrapper to retrieve associated {@link Favorite}
   */
  private class Cursor(cursor: android.database.Cursor) : CursorWrapper(cursor) {
    val favorite: Favorite
      get() = Favorite(
          getString(getColumnIndexOrThrow(MaterialisticDatabase.FavoriteEntry.COLUMN_NAME_ITEM_ID)),
          getString(getColumnIndexOrThrow(MaterialisticDatabase.FavoriteEntry.COLUMN_NAME_URL)),
          getString(getColumnIndex(MaterialisticDatabase.FavoriteEntry.COLUMN_NAME_TITLE)),
          getString(getColumnIndex(MaterialisticDatabase.FavoriteEntry.COLUMN_NAME_TIME)).toLong())
  }

  inner class FavoriteRoomLoader(private val filter: String?,
                                 private val observer: LocalItemManager.Observer) {
    @AnyThread
    @SuppressLint("CheckResult")
    fun load() {
      Observable.defer { Observable.just(filter ?: "") }
          .map { query(it) }
          .subscribeOn(ioScheduler)
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe {
            cursor = Cursor(it)
            observer.onChanged()
          }
    }
  }
}
