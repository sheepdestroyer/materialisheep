package io.github.sheepdestroyer.materialisheep;

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
}
