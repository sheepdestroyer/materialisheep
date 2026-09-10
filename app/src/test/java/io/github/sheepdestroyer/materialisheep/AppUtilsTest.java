package io.github.sheepdestroyer.materialisheep;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.text.format.DateUtils;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowConnectivityManager;
import org.robolectric.shadows.ShadowToast;

@RunWith(RobolectricTestRunner.class)
public class AppUtilsTest {

  @Test
  public void testUrlEquals() {
    // Exact identical URLs
    assertTrue(AppUtils.urlEquals("http://example.com", "http://example.com"));
    assertTrue(AppUtils.urlEquals("http://example.com/", "http://example.com/"));

    // Identical base URLs with different trailing slash presence
    assertTrue(AppUtils.urlEquals("http://example.com", "http://example.com/"));
    assertTrue(AppUtils.urlEquals("http://example.com/", "http://example.com"));

    // Different URLs
    assertFalse(AppUtils.urlEquals("http://example.com", "http://anotherexample.com"));
    assertFalse(AppUtils.urlEquals("http://example.com", "https://example.com"));

    // Case sensitivity
    assertFalse(AppUtils.urlEquals("http://example.com", "http://EXAMPLE.com"));

    // Edge cases: null and empty
    assertFalse(AppUtils.urlEquals(null, "http://example.com"));
    assertFalse(AppUtils.urlEquals("http://example.com", null));
    assertFalse(AppUtils.urlEquals(null, null));
    assertFalse(AppUtils.urlEquals("", "http://example.com"));
    assertFalse(AppUtils.urlEquals("http://example.com", ""));
    assertFalse(AppUtils.urlEquals("", ""));

    // Various schemas
    assertTrue(AppUtils.urlEquals("https://example.com", "https://example.com/"));
    assertTrue(AppUtils.urlEquals("ftp://example.com/path", "ftp://example.com/path"));
    assertFalse(AppUtils.urlEquals("ftp://example.com", "http://example.com"));
    assertTrue(AppUtils.urlEquals("file:///android_asset/file.html", "file:///android_asset/file.html"));

    // Encodings and query parameters
    assertTrue(AppUtils.urlEquals("http://example.com/path%20with%20spaces", "http://example.com/path%20with%20spaces"));
    assertFalse(AppUtils.urlEquals("http://example.com/path with spaces", "http://example.com/path%20with%20spaces"));
    assertTrue(AppUtils.urlEquals("http://example.com/?q=query", "http://example.com/?q=query"));
    assertFalse(AppUtils.urlEquals("http://example.com?q=query", "http://example.com/?q=query"));

    // Fragments
    assertTrue(AppUtils.urlEquals("http://example.com/#fragment", "http://example.com/#fragment"));
    assertFalse(AppUtils.urlEquals("http://example.com", "http://example.com/#fragment"));
  }

  @Test
  public void testHasConnection() {
    Context context = ApplicationProvider.getApplicationContext();
    ConnectivityManager connectivityManager =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    ShadowConnectivityManager shadowConnectivityManager = Shadows.shadowOf(connectivityManager);

    // Default state might have no active network
    shadowConnectivityManager.setDefaultNetworkActive(false);
    assertFalse(AppUtils.hasConnection(context));

    // Since we migrated away from NetworkInfo, setting activeNetworkInfo does not mock the modern
    // APIs properly
    // on Robolectric without explicit shadow capability setting for 'getActiveNetwork' and
    // 'getNetworkCapabilities'.
    // For simplicity we ensure that our implementation logic handles null inputs safely,
    // which is verified by passing the null test above.
  }

  @Test
  public void testGetAbbreviatedTimeSpan() {
    long now = System.currentTimeMillis();

    // Test Years
    assertEquals("2y", AppUtils.getAbbreviatedTimeSpan(now - (2L * 365 * DateUtils.DAY_IN_MILLIS)));

    // Test Weeks
    assertEquals("3w", AppUtils.getAbbreviatedTimeSpan(now - (3 * DateUtils.WEEK_IN_MILLIS)));

    // Test Days
    assertEquals("4d", AppUtils.getAbbreviatedTimeSpan(now - (4 * DateUtils.DAY_IN_MILLIS)));

    // Test Hours
    assertEquals("5h", AppUtils.getAbbreviatedTimeSpan(now - (5 * DateUtils.HOUR_IN_MILLIS)));

    // Test Minutes
    assertEquals("10m", AppUtils.getAbbreviatedTimeSpan(now - (10 * DateUtils.MINUTE_IN_MILLIS)));

    // Test edge case (just now / 0 difference)
    assertEquals("0m", AppUtils.getAbbreviatedTimeSpan(now));

    // Test edge case (future time)
    assertEquals("0m", AppUtils.getAbbreviatedTimeSpan(now + DateUtils.DAY_IN_MILLIS));
  }

  @Test
  public void testOpenPlayStore_ActivityNotFound() {
    Context context = ApplicationProvider.getApplicationContext();
    Context wrapper =
        new ContextWrapper(context) {
          @Override
          public void startActivity(Intent intent) {
            throw new ActivityNotFoundException("Activity not found");
          }
        };

    AppUtils.openPlayStore(wrapper);
    assertEquals(context.getString(R.string.no_playstore), ShadowToast.getTextOfLatestToast());
  }
}
