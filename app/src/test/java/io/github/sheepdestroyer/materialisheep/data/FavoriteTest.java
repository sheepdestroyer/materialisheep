package io.github.sheepdestroyer.materialisheep.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.os.Parcel;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class FavoriteTest {

  @Test
  public void testFavoriteGettersAndProperties() {
    Context context = ApplicationProvider.getApplicationContext();
    String itemId = "12345";
    String url = "https://example.com/article";
    String title = "Example Article";
    long time = 1000000L;

    Favorite favorite = new Favorite(itemId, url, title, time);

    assertEquals(itemId, favorite.getId());
    assertEquals(12345L, favorite.getLongId());
    assertEquals(url, favorite.getUrl());
    assertEquals(title, favorite.getDisplayedTitle());
    assertEquals(time, favorite.getTime());
    assertTrue(favorite.isFavorite());
    assertTrue(favorite.isStoryType());
    assertEquals(Item.STORY_TYPE, favorite.getType());
    assertEquals("example.com", favorite.getSource());
    assertNotNull(favorite.getDisplayedAuthor(context, false, 0));
    assertNotNull(favorite.getDisplayedTime(context));

    String expectedToString = String.format("%s (%s) - %s", title, url, String.format(HackerNewsClient.WEB_ITEM_PATH, itemId));
    assertEquals(expectedToString, favorite.toString());
  }

  @Test
  public void testSetFavorite() {
    Favorite favorite = new Favorite("1", "https://example.com", "Title", 1000L);
    assertTrue(favorite.isFavorite());

    favorite.setFavorite(false);
    assertFalse(favorite.isFavorite());

    favorite.setFavorite(true);
    assertTrue(favorite.isFavorite());
  }

  @Test
  public void testGetSourceWithNullOrEmptyUrl() {
    Favorite favoriteEmpty = new Favorite("1", "", "Title", 1000L);
    assertNull(favoriteEmpty.getSource());

    Favorite favoriteNull = new Favorite("1", null, "Title", 1000L);
    assertNull(favoriteNull.getSource());
  }

  @Test
  public void testParcelable() {
    String itemId = "67890";
    String url = "https://example.org";
    String title = "Parcelable Test Title";
    long time = 5000000L;

    Favorite original = new Favorite(itemId, url, title, time);
    original.setFavorite(false);

    Parcel parcel = Parcel.obtain();
    original.writeToParcel(parcel, 0);
    parcel.setDataPosition(0);

    Favorite createdFromParcel = Favorite.CREATOR.createFromParcel(parcel);

    assertEquals(original.getId(), createdFromParcel.getId());
    assertEquals(original.getUrl(), createdFromParcel.getUrl());
    assertEquals(original.getDisplayedTitle(), createdFromParcel.getDisplayedTitle());
    assertEquals(original.isFavorite(), createdFromParcel.isFavorite());
    assertEquals(original.getTime(), createdFromParcel.getTime());

    assertEquals(0, original.describeContents());

    Favorite[] array = Favorite.CREATOR.newArray(5);
    assertEquals(5, array.length);

    parcel.recycle();
  }
}
