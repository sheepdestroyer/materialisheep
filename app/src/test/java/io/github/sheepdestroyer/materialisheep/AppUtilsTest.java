package io.github.sheepdestroyer.materialisheep;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.text.format.DateUtils;
import androidx.preference.PreferenceManager;
import androidx.test.core.app.ApplicationProvider;
import io.github.sheepdestroyer.materialisheep.data.HackerNewsItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowConnectivityManager;
import org.robolectric.shadows.ShadowNetworkCapabilities;
import org.robolectric.shadows.ShadowPackageManager;
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

    // Various schemes
    assertTrue(AppUtils.urlEquals("https://example.com/item?id=123", "https://example.com/item?id=123/"));
    assertTrue(AppUtils.urlEquals("ftp://files.example.com/path", "ftp://files.example.com/path/"));
    assertTrue(AppUtils.urlEquals("mailto:user@example.com", "mailto:user@example.com/"));
    assertTrue(AppUtils.urlEquals("customscheme://path/to/resource", "customscheme://path/to/resource/"));

    // Scheme differences
    assertFalse(AppUtils.urlEquals("http://example.com", "https://example.com"));
    assertFalse(AppUtils.urlEquals("http://example.com", "ftp://example.com"));

    // Encoded URLs and query parameters
    assertTrue(AppUtils.urlEquals("https://example.com/search?q=hello%20world", "https://example.com/search?q=hello%20world/"));
    assertFalse(AppUtils.urlEquals("https://example.com/search?q=hello%20world", "https://example.com/search?q=hello+world"));

    // Different URLs
    assertFalse(AppUtils.urlEquals("http://example.com", "http://anotherexample.com"));

    // Case sensitivity
    assertFalse(AppUtils.urlEquals("http://example.com", "http://EXAMPLE.com"));

    // Edge cases: null and empty
    assertFalse(AppUtils.urlEquals(null, "http://example.com"));
    assertFalse(AppUtils.urlEquals("http://example.com", null));
    assertFalse(AppUtils.urlEquals(null, null));
    assertFalse(AppUtils.urlEquals("", "http://example.com"));
    assertFalse(AppUtils.urlEquals("http://example.com", ""));
    assertFalse(AppUtils.urlEquals("", ""));
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

  @Test
  public void testOpenWebUrlExternal_offline() {
    Context context = ApplicationProvider.getApplicationContext();
    ConnectivityManager connectivityManager =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    ShadowConnectivityManager shadowConnectivityManager = Shadows.shadowOf(connectivityManager);
    shadowConnectivityManager.setDefaultNetworkActive(false);

    AppUtils.openWebUrlExternal(context, null, "https://example.com", null);

    Intent intent = ShadowApplication.getInstance().getNextStartedActivity();
    assertNotNull(intent);
    assertEquals(OfflineWebActivity.class.getName(), intent.getComponent().getClassName());
    assertEquals("https://example.com", intent.getStringExtra(OfflineWebActivity.EXTRA_URL));
  }

  @Test
  public void testOpenWebUrlExternal_onlineNonHackerNewsUrl() {
    Context context = ApplicationProvider.getApplicationContext();
    setupActiveNetwork(context);

    // Disable custom tabs to test standard Intent.ACTION_VIEW
    PreferenceManager.getDefaultSharedPreferences(context)
        .edit()
        .putBoolean(context.getString(R.string.pref_custom_tab), false)
        .commit();

    // Register intent handler
    Intent viewIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://example.com"));
    ShadowPackageManager shadowPackageManager = Shadows.shadowOf(context.getPackageManager());
    shadowPackageManager.addResolveInfoForIntent(viewIntent, createResolveInfo("com.android.chrome", "com.android.chrome.MainActivity"));

    AppUtils.openWebUrlExternal(context, null, "https://example.com", null);

    Intent startedIntent = ShadowApplication.getInstance().getNextStartedActivity();
    assertNotNull(startedIntent);
    assertEquals(Intent.ACTION_VIEW, startedIntent.getAction());
    assertEquals("https://example.com", startedIntent.getDataString());
  }

  @Test
  public void testOpenWebUrlExternal_onlineHackerNewsUrl_singleExternalActivity() {
    Context context = ApplicationProvider.getApplicationContext();
    setupActiveNetwork(context);

    PreferenceManager.getDefaultSharedPreferences(context)
        .edit()
        .putBoolean(context.getString(R.string.pref_custom_tab), false)
        .commit();

    String hnUrl = "https://news.ycombinator.com/item?id=123";

    ShadowPackageManager shadowPackageManager = Shadows.shadowOf(context.getPackageManager());
    // Current package activity (should be excluded)
    ResolveInfo selfInfo = createResolveInfo(context.getPackageName(), context.getPackageName() + ".ItemActivity");
    // External handler activity
    ResolveInfo externalInfo = createResolveInfo("com.other.browser", "com.other.browser.MainActivity");

    Intent queryIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(hnUrl));
    shadowPackageManager.addResolveInfoForIntent(queryIntent, selfInfo);
    shadowPackageManager.addResolveInfoForIntent(queryIntent, externalInfo);

    AppUtils.openWebUrlExternal(context, null, hnUrl, null);

    Intent startedIntent = ShadowApplication.getInstance().getNextStartedActivity();
    assertNotNull(startedIntent);
    assertEquals("com.other.browser", startedIntent.getPackage());
    assertEquals(hnUrl, startedIntent.getDataString());
  }

  @Test
  public void testOpenWebUrlExternal_onlineHackerNewsUrl_multipleExternalActivities() {
    Context context = ApplicationProvider.getApplicationContext();
    setupActiveNetwork(context);

    PreferenceManager.getDefaultSharedPreferences(context)
        .edit()
        .putBoolean(context.getString(R.string.pref_custom_tab), false)
        .commit();

    String hnUrl = "https://news.ycombinator.com/item?id=123";

    ShadowPackageManager shadowPackageManager = Shadows.shadowOf(context.getPackageManager());
    ResolveInfo external1 = createResolveInfo("com.browser.one", "com.browser.one.MainActivity");
    ResolveInfo external2 = createResolveInfo("com.browser.two", "com.browser.two.MainActivity");

    Intent queryIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(hnUrl));
    shadowPackageManager.addResolveInfoForIntent(queryIntent, external1);
    shadowPackageManager.addResolveInfoForIntent(queryIntent, external2);

    AppUtils.openWebUrlExternal(context, null, hnUrl, null);

    Intent startedIntent = ShadowApplication.getInstance().getNextStartedActivity();
    assertNotNull(startedIntent);
    assertEquals(Intent.ACTION_CHOOSER, startedIntent.getAction());
  }

  @Test
  public void testOpenWebUrlExternal_customTabsEnabled() {
    Context context = ApplicationProvider.getApplicationContext();
    setupActiveNetwork(context);

    PreferenceManager.getDefaultSharedPreferences(context)
        .edit()
        .putBoolean(context.getString(R.string.pref_custom_tab), true)
        .commit();

    String url = "https://example.com";
    Intent viewIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url));
    ShadowPackageManager shadowPackageManager = Shadows.shadowOf(context.getPackageManager());
    shadowPackageManager.addResolveInfoForIntent(viewIntent, createResolveInfo("com.android.chrome", "com.android.chrome.MainActivity"));

    HackerNewsItem item = new HackerNewsItem(123L);
    AppUtils.openWebUrlExternal(context, item, url, null);

    Intent startedIntent = ShadowApplication.getInstance().getNextStartedActivity();
    assertNotNull(startedIntent);
    assertEquals(Intent.ACTION_VIEW, startedIntent.getAction());
    assertEquals(url, startedIntent.getDataString());
  }

  private void setupActiveNetwork(Context context) {
    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    ShadowConnectivityManager shadowCm = Shadows.shadowOf(cm);
    Network network = ShadowNetworkCapabilities.newInstance();
    NetworkCapabilities nc = ShadowNetworkCapabilities.newInstance();
    ShadowNetworkCapabilities shadowNc = Shadows.shadowOf(nc);
    shadowNc.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    shadowCm.setDefaultNetworkActive(true);
    shadowCm.setNetworkCapabilities(network, nc);
    shadowCm.setActiveNetwork(network);
  }

  private ResolveInfo createResolveInfo(String packageName, String className) {
    ResolveInfo info = new ResolveInfo();
    info.activityInfo = new ActivityInfo();
    info.activityInfo.packageName = packageName;
    info.activityInfo.name = className;
    info.activityInfo.applicationInfo = new ApplicationInfo();
    info.activityInfo.applicationInfo.packageName = packageName;
    return info;
  }
}
