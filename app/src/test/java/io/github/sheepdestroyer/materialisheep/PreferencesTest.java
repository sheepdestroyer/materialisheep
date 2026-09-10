package io.github.sheepdestroyer.materialisheep;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import androidx.preference.PreferenceManager;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowPackageManager;

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
  public void testIsReleaseNotesSeen_handlesNameNotFoundException() {
    Preferences.sReleaseNotesSeen = null;
    ShadowPackageManager shadowPackageManager = Shadows.shadowOf(context.getPackageManager());
    shadowPackageManager.removePackage(context.getPackageName());

    // Should catch NameNotFoundException internally and evaluate fallback release notes check
    boolean seen = Preferences.isReleaseNotesSeen(context);
    assertFalse(seen);
  }

  @Test
  public void testGetListSwipePreferences_invalidActionFallback() {
    sharedPreferences
        .edit()
        .putString(context.getString(R.string.pref_list_swipe_left), "INVALID_ACTION")
        .commit();

    Preferences.SwipeAction[] actions = Preferences.getListSwipePreferences(context);
    assertEquals(Preferences.SwipeAction.None, actions[0]);
  }

  @Test
  public void testGetLineHeight_invalidFloatFallback() {
    sharedPreferences
        .edit()
        .putString(context.getString(R.string.pref_line_height), "not_a_number")
        .commit();

    float lineHeight = Preferences.getLineHeight(context);
    assertEquals(1.0f, lineHeight, 0.001f);
  }
}
