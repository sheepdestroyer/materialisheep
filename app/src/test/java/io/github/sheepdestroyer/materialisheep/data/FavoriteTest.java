package io.github.sheepdestroyer.materialisheep.data;

import android.os.Parcel;
import android.text.Spannable;
import android.content.Context;
import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
public class FavoriteTest {

    @Test
    public void testGetSource() {
        Favorite favorite = new Favorite("1", "http://example.com/path", "title", 12345L);
        assertEquals("example.com", favorite.getSource());
    }

    @Test
    public void testGetSourceWithEmptyUrl() {
        Favorite favorite = new Favorite("2", "", "title", 12345L);
        assertNull(favorite.getSource());
    }

    @Test
    public void testGetSourceWithNullUrl() {
        Favorite favorite = new Favorite("3", null, "title", 12345L);
        assertNull(favorite.getSource());
    }

    @Test
    public void testProperties() {
        Favorite favorite = new Favorite("1", "http://example.com", "title", 12345L);
        assertEquals("1", favorite.getId());
        assertEquals(1L, favorite.getLongId());
        assertEquals("http://example.com", favorite.getUrl());
        assertEquals("title", favorite.getDisplayedTitle());
        assertEquals(12345L, favorite.getTime());
        assertTrue(favorite.isStoryType());
        assertTrue(favorite.isFavorite());
        assertEquals(Item.STORY_TYPE, favorite.getType());
        assertNotNull(favorite.getDisplayedAuthor(null, false, 0));
        assertNotNull(favorite.toString());

        favorite.setFavorite(false);
        assertFalse(favorite.isFavorite());
    }

    @Test
    public void testGetDisplayedTime() {
        Context context = ApplicationProvider.getApplicationContext();
        Favorite favorite = new Favorite("1", "http://example.com", "title", System.currentTimeMillis());
        Spannable displayedTime = favorite.getDisplayedTime(context);
        assertNotNull(displayedTime);

        // Ensure caching works
        assertEquals(displayedTime, favorite.getDisplayedTime(context));
    }

    @Test
    public void testParcelable() {
        Favorite favorite = new Favorite("1", "http://example.com", "title", 12345L);

        Parcel parcel = Parcel.obtain();
        favorite.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        Favorite createdFromParcel = Favorite.CREATOR.createFromParcel(parcel);

        assertEquals("1", createdFromParcel.getId());
        assertEquals("http://example.com", createdFromParcel.getUrl());
        assertEquals("title", createdFromParcel.getDisplayedTitle());
        assertEquals(12345L, createdFromParcel.getTime());
        assertTrue(createdFromParcel.isFavorite());
        assertEquals(0, createdFromParcel.describeContents());

        parcel.recycle();

        Favorite[] array = Favorite.CREATOR.newArray(5);
        assertEquals(5, array.length);
    }
}
