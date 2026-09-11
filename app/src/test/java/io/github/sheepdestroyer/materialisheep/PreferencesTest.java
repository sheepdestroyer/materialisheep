package io.github.sheepdestroyer.materialisheep;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
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
    Preferences.clearDrafts(context);
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
  public void testGetDefaultStoryView() {
    // Default is Article
    org.junit.Assert.assertEquals(Preferences.StoryViewMode.Article, Preferences.getDefaultStoryView(context));

    // Test Comment mode
    sharedPreferences
        .edit()
        .putString(
            context.getString(R.string.pref_story_display),
            context.getString(R.string.pref_story_display_value_comments))
        .commit();
    org.junit.Assert.assertEquals(Preferences.StoryViewMode.Comment, Preferences.getDefaultStoryView(context));

    // Test Readability mode
    sharedPreferences
        .edit()
        .putString(
            context.getString(R.string.pref_story_display),
            context.getString(R.string.pref_story_display_value_readability))
        .commit();
    org.junit.Assert.assertEquals(Preferences.StoryViewMode.Readability, Preferences.getDefaultStoryView(context));

    // Test Article mode (explicitly set)
    sharedPreferences
        .edit()
        .putString(
            context.getString(R.string.pref_story_display),
            context.getString(R.string.pref_story_display_value_article))
        .commit();
    org.junit.Assert.assertEquals(Preferences.StoryViewMode.Article, Preferences.getDefaultStoryView(context));
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
    // Test default value
    org.junit.Assert.assertEquals(
        context.getString(R.string.pref_launch_screen_value_top),
        Preferences.getLaunchScreen(context));

    // Test with a different value
    sharedPreferences
        .edit()
        .putString(
            context.getString(R.string.pref_launch_screen),
            context.getString(R.string.pref_launch_screen_value_new))
        .commit();
    org.junit.Assert.assertEquals(
        context.getString(R.string.pref_launch_screen_value_new),
        Preferences.getLaunchScreen(context));
  }

  @Test
  public void testIsLaunchScreenLast() {
    // Test default value
    assertFalse(Preferences.isLaunchScreenLast(context));

    // Test with last value
    sharedPreferences
        .edit()
        .putString(
            context.getString(R.string.pref_launch_screen),
            context.getString(R.string.pref_launch_screen_value_last))
        .commit();
    assertTrue(Preferences.isLaunchScreenLast(context));
  }

  @Test
  public void testGetFloatFromStringErrorHandling() {
    // Test default value (NullPointerException case inside getFloatFromString)
    assertEquals(1.0f, Preferences.getLineHeight(context), 0.001f);

    // Test valid float string
    sharedPreferences
        .edit()
        .putString(context.getString(R.string.pref_line_height), "2.5")
        .commit();
    assertEquals(2.5f, Preferences.getLineHeight(context), 0.001f);

    // Test invalid non-float string (NumberFormatException case inside getFloatFromString)
    sharedPreferences
        .edit()
        .putString(context.getString(R.string.pref_line_height), "not_a_float")
        .commit();
    assertEquals(1.0f, Preferences.getLineHeight(context), 0.001f);
  }

  @Test
  public void testParseSwipeActionErrorHandling() throws Exception {
    java.lang.reflect.Method method = Preferences.class.getDeclaredMethod("parseSwipeAction", String.class);
    method.setAccessible(true);

    // Test invalid string (IllegalArgumentException)
    Object resultInvalid = method.invoke(null, "InvalidSwipeAction");
    assertEquals(Preferences.SwipeAction.None, resultInvalid);

    // Test null (NullPointerException)
    Object resultNull = method.invoke(null, (String) null);
    assertEquals(Preferences.SwipeAction.None, resultNull);
  }

  @Test
  public void testUsername() {
    // Verify initial getUsername(context) is null
    assertNull(Preferences.getUsername(context));

    // setUsername(context, "sheep") returns "sheep"
    Preferences.setUsername(context, "sheep");
    assertEquals("sheep", Preferences.getUsername(context));

    // setUsername(context, null) clears it or returns null
    Preferences.setUsername(context, null);
    assertNull(Preferences.getUsername(context));
  }

  @Test
  public void testAdBlockEnabled() {
    // Test default value
    assertTrue(Preferences.adBlockEnabled(context));

    // Disable ad block
    sharedPreferences
        .edit()
        .putBoolean(context.getString(R.string.pref_ad_block), false)
        .commit();
    assertFalse(Preferences.adBlockEnabled(context));

    // Re-enable ad block
    sharedPreferences
        .edit()
        .putBoolean(context.getString(R.string.pref_ad_block), true)
        .commit();
    assertTrue(Preferences.adBlockEnabled(context));
  }

  @Test
  public void testDraftManagement() {
    // Verify getDraft on unknown ID returns null
    assertNull(Preferences.getDraft(context, "unknown"));

    // Save draft and verify
    Preferences.saveDraft(context, "123", "my comment draft");
    assertEquals("my comment draft", Preferences.getDraft(context, "123"));

    // Delete draft and verify
    Preferences.deleteDraft(context, "123");
    assertNull(Preferences.getDraft(context, "123"));

    // Save draft on "456" and "789"
    Preferences.saveDraft(context, "456", "draft 456");
    Preferences.saveDraft(context, "789", "draft 789");
    assertEquals("draft 456", Preferences.getDraft(context, "456"));
    assertEquals("draft 789", Preferences.getDraft(context, "789"));

    // Call clearDrafts(context) and verify both return null
    Preferences.clearDrafts(context);
    assertNull(Preferences.getDraft(context, "456"));
    assertNull(Preferences.getDraft(context, "789"));
  }
}
