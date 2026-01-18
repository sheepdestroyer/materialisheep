package io.github.sheepdestroyer.materialisheep.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import java.util.List;

@Database(entities = {
        MaterialisticDatabase.SavedStory.class,
        MaterialisticDatabase.ReadStory.class,
        MaterialisticDatabase.Readable.class
}, version = 4, exportSchema = false)
/**
 * A Room database for storing saved stories, read stories, and readable
 * content.
 */
public abstract class MaterialisticDatabase extends RoomDatabase {

    private static final String BASE_URI = "content://io.github.sheepdestroyer.materialisheep";

    private static MaterialisticDatabase sInstance;
    private final MutableLiveData<Uri> mLiveData = new MutableLiveData<>();

    /**
     * Gets the singleton instance of the database.
     *
     * @param context the application context
     * @return the singleton instance of the database
     */
    public static synchronized MaterialisticDatabase getInstance(Context context) {
        if (sInstance == null) {
            sInstance = setupBuilder(Room.databaseBuilder(context.getApplicationContext(),
                    MaterialisticDatabase.class,
                    DbConstants.DB_NAME))
                    .build();
        }
        return sInstance;
    }

    @VisibleForTesting
    protected static Builder<MaterialisticDatabase> setupBuilder(Builder<MaterialisticDatabase> builder) {
        return builder.addMigrations(new Migration(3, 4) {
            @Override
            public void migrate(@NonNull SupportSQLiteDatabase database) {
                database.execSQL(DbConstants.SQL_CREATE_SAVED_TABLE);
                database.execSQL(DbConstants.SQL_INSERT_FAVORITE_SAVED);
                database.execSQL(DbConstants.SQL_DROP_FAVORITE_TABLE);

                database.execSQL(DbConstants.SQL_CREATE_READ_TABLE);
                database.execSQL(DbConstants.SQL_INSERT_VIEWED_READ);
                database.execSQL(DbConstants.SQL_DROP_VIEWED_TABLE);

                database.execSQL(DbConstants.SQL_CREATE_READABLE_TABLE);
                database.execSQL(DbConstants.SQL_INSERT_READABILITY_READABLE);
                database.execSQL(DbConstants.SQL_DROP_READABILITY_TABLE);
            }
        });
    }

    /**
     * Gets the base URI for saved stories.
     *
     * @return the base URI for saved stories
     */
    public static Uri getBaseSavedUri() {
        return Uri.parse(BASE_URI).buildUpon().appendPath("saved").build();
    }

    /**
     * Gets the base URI for read stories.
     *
     * @return the base URI for read stories
     */
    public static Uri getBaseReadUri() {
        return Uri.parse(BASE_URI).buildUpon().appendPath("read").build();
    }

    public abstract SavedStoriesDao getSavedStoriesDao();

    public abstract ReadStoriesDao getReadStoriesDao();

    public abstract ReadableDao getReadableDao();

    /**
     * Gets a {@link LiveData} that is notified of changes to the database.
     *
     * @return a {@link LiveData} that is notified of changes to the database
     */
    public LiveData<Uri> getLiveData() {
        return mLiveData;
    }

    /**
     * Sets the value of the {@link LiveData} to notify observers of a change.
     *
     * @param uri the URI of the changed data
     */
    public void setLiveValue(Uri uri) {
        mLiveData.setValue(uri);
        // clear notification Uri after notifying all active observers
        mLiveData.setValue(null);
    }

    /**
     * Creates a URI for a read story.
     *
     * @param itemId the ID of the story
     * @return a URI for the read story
     */
    public Uri createReadUri(String itemId) {
        return MaterialisticDatabase.getBaseReadUri().buildUpon().appendPath(itemId).build();
    }

    /**
     * A Room entity that represents a read story.
     */
    @Entity(tableName = "read")
    public static class ReadStory {
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "_id")
        private int id;
        @ColumnInfo(name = "itemid")
        private String itemId;

        /**
         * Constructs a new {@code ReadStory}.
         *
         * @param itemId the ID of the story
         */
        public ReadStory(String itemId) {
            this.itemId = itemId;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getItemId() {
            return itemId;
        }

        public void setItemId(String itemId) {
            this.itemId = itemId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            ReadStory readStory = (ReadStory) o;

            if (id != readStory.id)
                return false;
            return itemId != null ? itemId.equals(readStory.itemId) : readStory.itemId == null;
        }

        @Override
        public int hashCode() {
            int result = id;
            result = 31 * result + (itemId != null ? itemId.hashCode() : 0);
            return result;
        }
    }

    /**
     * A Room entity that represents readable content for a story.
     */
    @Entity
    public static class Readable {
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "_id")
        private int id;
        @ColumnInfo(name = "itemid")
        private String itemId;
        private String content;

        /**
         * Constructs a new {@code Readable}.
         *
         * @param itemId  the ID of the story
         * @param content the readable content
         */
        public Readable(String itemId, String content) {
            this.itemId = itemId;
            this.content = content;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getItemId() {
            return itemId;
        }

        public void setItemId(String itemId) {
            this.itemId = itemId;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            Readable readable = (Readable) o;

            if (id != readable.id)
                return false;
            if (itemId != null ? !itemId.equals(readable.itemId) : readable.itemId != null)
                return false;
            return content != null ? content.equals(readable.content) : readable.content == null;
        }

        @Override
        public int hashCode() {
            int result = id;
            result = 31 * result + (itemId != null ? itemId.hashCode() : 0);
            result = 31 * result + (content != null ? content.hashCode() : 0);
            return result;
        }
    }

    /**
     * A Room entity that represents a saved story.
     */
    @Entity(tableName = "saved")
    public static class SavedStory {
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "_id")
        private int id;
        @ColumnInfo(name = "itemid")
        private String itemId;
        private String url;
        private String title;
        private String time;

        /**
         * Creates a {@code SavedStory} from a {@link WebItem}.
         *
         * @param story the {@link WebItem} to convert
         * @return a {@code SavedStory}
         */
        static SavedStory from(WebItem story) {
            SavedStory savedStory = new SavedStory();
            savedStory.itemId = story.getId();
            savedStory.url = story.getUrl();
            savedStory.title = story.getDisplayedTitle();
            savedStory.time = String.valueOf(story instanceof Favorite ? ((Favorite) story).getTime()
                    : String.valueOf(System.currentTimeMillis()));
            return savedStory;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getItemId() {
            return itemId;
        }

        public void setItemId(String itemId) {
            this.itemId = itemId;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }
    }

    /**
     * A DAO for accessing read stories.
     */
    @Dao
    public interface ReadStoriesDao {
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void insert(ReadStory readStory);

        @Query("SELECT * FROM read WHERE itemid = :itemId LIMIT 1")
        ReadStory selectByItemId(String itemId);
    }

    /**
     * A DAO for accessing readable content.
     */
    @Dao
    public interface ReadableDao {
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void insert(Readable readable);

        @Query("SELECT * FROM readable WHERE itemid = :itemId LIMIT 1")
        Readable selectByItemId(String itemId);
    }

    static class DbConstants {
        static final String DB_NAME = "Materialistic.db";
        static final String SQL_CREATE_READ_TABLE = "CREATE TABLE read (_id INTEGER NOT NULL PRIMARY KEY, itemid TEXT)";
        static final String SQL_CREATE_READABLE_TABLE = "CREATE TABLE readable (_id INTEGER NOT NULL PRIMARY KEY, itemid TEXT, content TEXT)";
        static final String SQL_CREATE_SAVED_TABLE = "CREATE TABLE saved (_id INTEGER NOT NULL PRIMARY KEY, itemid TEXT, url TEXT, title TEXT, time TEXT)";
        static final String SQL_INSERT_FAVORITE_SAVED = "INSERT INTO saved SELECT * FROM favorite";
        static final String SQL_INSERT_VIEWED_READ = "INSERT INTO read SELECT * FROM viewed";
        static final String SQL_INSERT_READABILITY_READABLE = "INSERT INTO readable SELECT * FROM readability";
        static final String SQL_DROP_FAVORITE_TABLE = "DROP TABLE IF EXISTS favorite";
        static final String SQL_DROP_VIEWED_TABLE = "DROP TABLE IF EXISTS viewed";
        static final String SQL_DROP_READABILITY_TABLE = "DROP TABLE IF EXISTS readability";
    }

    public interface FavoriteEntry extends BaseColumns {
        String COLUMN_NAME_ITEM_ID = "itemid";
        String COLUMN_NAME_URL = "url";
        String COLUMN_NAME_TITLE = "title";
        String COLUMN_NAME_TIME = "time";
    }
}
