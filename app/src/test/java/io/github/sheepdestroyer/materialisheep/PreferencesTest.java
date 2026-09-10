package io.github.sheepdestroyer.materialisheep;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class PreferencesTest {

  private Context context;
  private SharedPreferences sharedPreferences;

  @Before
  public void setUp() {
    context = ApplicationProvider.getApplicationContext();
    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    Preferences.reset(context);
  }

  @Test
  public void testIsListItemCardView() {
    // Test default value
    assertFalse(Preferences.isListItemCardView(context));

    // Test with value true
    sharedPreferences
        .edit()
        .putBoolean(context.getString(R.string.pref_list_item_view), true)
        .commit();
    assertTrue(Preferences.isListItemCardView(context));

    // Test with value false
    sharedPreferences
        .edit()
        .putBoolean(context.getString(R.string.pref_list_item_view), false)
        .commit();
    assertFalse(Preferences.isListItemCardView(context));
  }

  @Test
  public void testIsSortByRecent() {
    // Test default value
    assertTrue(Preferences.isSortByRecent(context));

    // Test with value popular
    sharedPreferences
        .edit()
        .putString(
            context.getString(R.string.pref_search_sort),
            context.getString(R.string.pref_search_sort_value_default))
        .commit();
    assertFalse(Preferences.isSortByRecent(context));
  }

  @Test
  public void testSetSortByRecent() {
    // Set to true
    Preferences.setSortByRecent(context, true);
    assertTrue(Preferences.isSortByRecent(context));

    // Set to false
    Preferences.setSortByRecent(context, false);
    assertFalse(Preferences.isSortByRecent(context));
  }

  @Test
  public void testGetLaunchScreen() {
    // Default value
    assertEquals(
        context.getString(R.string.pref_launch_screen_value_top),
        Preferences.getLaunchScreen(context));
    assertFalse(Preferences.isLaunchScreenLast(context));

    // Custom value: last
    sharedPreferences
        .edit()
        .putString(
            context.getString(R.string.pref_launch_screen),
            context.getString(R.string.pref_launch_screen_value_last))
        .commit();
    assertEquals(
        context.getString(R.string.pref_launch_screen_value_last),
        Preferences.getLaunchScreen(context));
    assertTrue(Preferences.isLaunchScreenLast(context));

    // Custom value: catchup
    sharedPreferences
        .edit()
        .putString(
            context.getString(R.string.pref_launch_screen),
            context.getString(R.string.pref_launch_screen_value_best))
        .commit();
    assertEquals(
        context.getString(R.string.pref_launch_screen_value_best),
        Preferences.getLaunchScreen(context));
    assertFalse(Preferences.isLaunchScreenLast(context));
  }

  @Test
  public void testGetDefaultStoryView() {
    // Default value
    assertEquals(Preferences.StoryViewMode.Article, Preferences.getDefaultStoryView(context));

    // Comment
    sharedPreferences
        .edit()
        .putString(
            context.getString(R.string.pref_story_display),
            context.getString(R.string.pref_story_display_value_comments))
        .commit();
    assertEquals(Preferences.StoryViewMode.Comment, Preferences.getDefaultStoryView(context));

    // Readability
    sharedPreferences
        .edit()
        .putString(
            context.getString(R.string.pref_story_display),
            context.getString(R.string.pref_story_display_value_readability))
        .commit();
    assertEquals(Preferences.StoryViewMode.Readability, Preferences.getDefaultStoryView(context));

    // Article
    sharedPreferences
        .edit()
        .putString(
            context.getString(R.string.pref_story_display),
            context.getString(R.string.pref_story_display_value_article))
        .commit();
    assertEquals(Preferences.StoryViewMode.Article, Preferences.getDefaultStoryView(context));

    // Invalid value fallback
    sharedPreferences
        .edit()
        .putString(context.getString(R.string.pref_story_display), "invalid_value")
        .commit();
    assertEquals(Preferences.StoryViewMode.Article, Preferences.getDefaultStoryView(context));
  }

  @Test
  public void testGetFloatFromString() {
    // Default values
    assertEquals(1.0f, Preferences.getLineHeight(context), 0.001f);
    assertEquals(1.0f, Preferences.getReadabilityLineHeight(context), 0.001f);

    // Valid float strings
    sharedPreferences
        .edit()
        .putString(context.getString(R.string.pref_line_height), "1.5")
        .putString(context.getString(R.string.pref_readability_line_height), "1.2")
        .commit();
    assertEquals(1.5f, Preferences.getLineHeight(context), 0.001f);
    assertEquals(1.2f, Preferences.getReadabilityLineHeight(context), 0.001f);

    // Invalid float strings fallback
    sharedPreferences
        .edit()
        .putString(context.getString(R.string.pref_line_height), "not_a_float")
        .putString(context.getString(R.string.pref_readability_line_height), "invalid")
        .commit();
    assertEquals(1.0f, Preferences.getLineHeight(context), 0.001f);
    assertEquals(1.0f, Preferences.getReadabilityLineHeight(context), 0.001f);
  }

  @Test
  public void testParseSwipeAction() {
    // Default values (Save, Vote)
    assertArrayEquals(
        new Preferences.SwipeAction[] {
          Preferences.SwipeAction.Save, Preferences.SwipeAction.Vote
        },
        Preferences.getListSwipePreferences(context));

    // Valid swipe actions
    sharedPreferences
        .edit()
        .putString(context.getString(R.string.pref_list_swipe_left), "Refresh")
        .putString(context.getString(R.string.pref_list_swipe_right), "Share")
        .commit();
    assertArrayEquals(
        new Preferences.SwipeAction[] {
          Preferences.SwipeAction.Refresh, Preferences.SwipeAction.Share
        },
        Preferences.getListSwipePreferences(context));

    // Invalid swipe actions fallback to None
    sharedPreferences
        .edit()
        .putString(context.getString(R.string.pref_list_swipe_left), "UNKNOWN_ACTION")
        .putString(context.getString(R.string.pref_list_swipe_right), "invalid")
        .commit();
    assertArrayEquals(
        new Preferences.SwipeAction[] {
          Preferences.SwipeAction.None, Preferences.SwipeAction.None
        },
        Preferences.getListSwipePreferences(context));
  }
}
