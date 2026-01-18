package io.github.sheepdestroyer.materialisheep.data

import android.database.Cursor
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomWarnings
import io.github.sheepdestroyer.materialisheep.data.MaterialisticDatabase.SavedStory

/**
 * A DAO for accessing saved stories.
 */
@Dao
interface SavedStoriesDao {
    @Query("SELECT * FROM saved ORDER BY time DESC")
    fun selectAll(): LiveData<List<SavedStory>>

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Query("SELECT * FROM saved ORDER BY time DESC")
    fun selectAllToCursor(): Cursor

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Query("SELECT * FROM saved WHERE title LIKE '%' || :query || '%' ORDER BY time DESC")
    fun searchToCursor(query: String): Cursor

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg savedStories: SavedStory)

    @Query("DELETE FROM saved")
    fun deleteAll(): Int

    @Query("DELETE FROM saved WHERE itemid = :itemId")
    fun deleteByItemId(itemId: String): Int

    @Query("DELETE FROM saved WHERE title LIKE '%' || :query || '%'")
    fun deleteByTitle(query: String): Int

    @Query("SELECT * FROM saved WHERE itemid = :itemId")
    fun selectByItemId(itemId: String): SavedStory?
}
