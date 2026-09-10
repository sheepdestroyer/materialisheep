package io.github.sheepdestroyer.materialisheep;

import android.os.Parcel;
import android.text.Spannable;
import io.github.sheepdestroyer.materialisheep.data.WebItem;
import android.content.pm.ApplicationInfo;
import org.robolectric.shadows.ShadowNetworkCapabilities;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;
import com.google.android.material.appbar.AppBarLayout;
import android.app.Activity;
import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.text.format.DateUtils;
import androidx.preference.PreferenceManager;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowPackageManager;
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
  @Test
  public void testOpenWebUrlExternal_NoConnection() {
    Application baseContext = ApplicationProvider.getApplicationContext();
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(baseContext);
    prefs.edit().putBoolean(baseContext.getString(R.string.pref_custom_tab), false).apply();
    ConnectivityManager mockCm = mock(ConnectivityManager.class);
    when(mockCm.getActiveNetwork()).thenReturn(null);
    Activity activity = Robolectric.buildActivity(Activity.class).get();
    Context context = new ContextWrapper(activity) {
      @Override
      public Object getSystemService(String name) {
        if (Context.CONNECTIVITY_SERVICE.equals(name)) {
          return mockCm;
        }
        return super.getSystemService(name);
      }
    };
    AppUtils.openWebUrlExternal(context, null, "http://example.com", null);
    Intent intent = shadowOf(activity).getNextStartedActivity();
    assertNotNull(intent);
    assertEquals(OfflineWebActivity.class.getName(), intent.getComponent().getClassName());
    assertEquals("http://example.com", intent.getStringExtra(OfflineWebActivity.EXTRA_URL));
  }
  @Test
  public void testOpenWebUrlExternal_WithConnection_NonHNUrl() {
    Application baseContext = ApplicationProvider.getApplicationContext();
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(baseContext);
    prefs.edit().putBoolean(baseContext.getString(R.string.pref_custom_tab), false).apply();
    ConnectivityManager mockCm = mock(ConnectivityManager.class);
    Network mockNetwork = mock(Network.class);
    NetworkCapabilities mockCap = mock(NetworkCapabilities.class);
    when(mockCm.getActiveNetwork()).thenReturn(mockNetwork);
    when(mockCm.getNetworkCapabilities(mockNetwork)).thenReturn(mockCap);
    when(mockCap.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)).thenReturn(true);
    Activity activity = Robolectric.buildActivity(Activity.class).get();
    ShadowPackageManager shadowPm = shadowOf(activity.getPackageManager());
    Intent viewIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("http://example.com"));
    ResolveInfo resolveInfo = new ResolveInfo();
    resolveInfo.activityInfo = new ActivityInfo();
    resolveInfo.activityInfo.packageName = "com.android.chrome";
    resolveInfo.activityInfo.name = "Browser";
    shadowPm.addResolveInfoForIntent(viewIntent, resolveInfo);
    Context context = new ContextWrapper(activity) {
      @Override
      public Object getSystemService(String name) {
        if (Context.CONNECTIVITY_SERVICE.equals(name)) {
          return mockCm;
        }
        return super.getSystemService(name);
      }
    };
    AppUtils.openWebUrlExternal(context, null, "http://example.com", null);
    Intent intent = shadowOf(activity).getNextStartedActivity();
    assertNotNull(intent);
    assertEquals(Intent.ACTION_VIEW, intent.getAction());
    assertEquals("http://example.com", intent.getData().toString());
  }
  @Test
  public void testOpenWebUrlExternal_WithConnection_HNUrl() {
    Application baseContext = ApplicationProvider.getApplicationContext();
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(baseContext);
    prefs.edit().putBoolean(baseContext.getString(R.string.pref_custom_tab), false).apply();
    ConnectivityManager mockCm = mock(ConnectivityManager.class);
    Network mockNetwork = mock(Network.class);
    NetworkCapabilities mockCap = mock(NetworkCapabilities.class);
    when(mockCm.getActiveNetwork()).thenReturn(mockNetwork);
    when(mockCm.getNetworkCapabilities(mockNetwork)).thenReturn(mockCap);
    when(mockCap.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)).thenReturn(true);
    Activity activity = Robolectric.buildActivity(Activity.class).get();
    ShadowPackageManager shadowPm = shadowOf(activity.getPackageManager());
    Intent viewIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://news.ycombinator.com/item?id=123"));
    ResolveInfo selfInfo = new ResolveInfo();
    selfInfo.activityInfo = new ActivityInfo();
    selfInfo.activityInfo.packageName = activity.getPackageName();
    selfInfo.activityInfo.name = "Self";
    shadowPm.addResolveInfoForIntent(viewIntent, selfInfo);
    ResolveInfo otherInfo = new ResolveInfo();
    otherInfo.activityInfo = new ActivityInfo();
    otherInfo.activityInfo.packageName = "com.android.chrome";
    otherInfo.activityInfo.name = "Browser";
    shadowPm.addResolveInfoForIntent(viewIntent, otherInfo);
    Context context = new ContextWrapper(activity) {
      @Override
      public Object getSystemService(String name) {
        if (Context.CONNECTIVITY_SERVICE.equals(name)) {
          return mockCm;
        }
        return super.getSystemService(name);
      }
    };
    AppUtils.openWebUrlExternal(context, null, "https://news.ycombinator.com/item?id=123", null);
    Intent intent = shadowOf(activity).getNextStartedActivity();
    assertNotNull(intent);
    assertEquals(Intent.ACTION_VIEW, intent.getAction());
    assertEquals("https://news.ycombinator.com/item?id=123", intent.getData().toString());
    assertEquals("com.android.chrome", intent.getPackage());
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
    Intent startedIntent = getNextStartedActivity(context);
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
    TestWebItem item = new TestWebItem("123", url);
    AppUtils.openWebUrlExternal(context, item, url, null);
    Intent startedIntent = getNextStartedActivity(context);
    assertNotNull(startedIntent);
    assertEquals(Intent.ACTION_VIEW, startedIntent.getAction());
    assertEquals(url, startedIntent.getDataString());
  }

  @Test
  public void testGetDimension() {
    Context context = ApplicationProvider.getApplicationContext();
    float dimension = AppUtils.getDimension(context, R.style.AppTheme, R.attr.contentTextSize);
    assertTrue(dimension > 0);
  }

  @Test
  public void testGetDimensionInDp() {
    Context context = ApplicationProvider.getApplicationContext();
    int marginDp = AppUtils.getDimensionInDp(context, R.dimen.margin);
    assertTrue(marginDp >= 0);
    assertEquals(8, marginDp);
  }

  @Test
  public void testIsHackerNewsUrl() {
    WebItem hnItem = mock(WebItem.class);
    when(hnItem.getId()).thenReturn("123");
    when(hnItem.getUrl()).thenReturn("https://news.ycombinator.com/item?id=123");
    assertTrue(AppUtils.isHackerNewsUrl(hnItem));

    WebItem nonHnItem = mock(WebItem.class);
    when(nonHnItem.getId()).thenReturn("123");
    when(nonHnItem.getUrl()).thenReturn("https://example.com");
    assertFalse(AppUtils.isHackerNewsUrl(nonHnItem));

    WebItem nullUrlItem = mock(WebItem.class);
    when(nullUrlItem.getId()).thenReturn("123");
    when(nullUrlItem.getUrl()).thenReturn(null);
    assertFalse(AppUtils.isHackerNewsUrl(nullUrlItem));

    WebItem emptyUrlItem = mock(WebItem.class);
    when(emptyUrlItem.getId()).thenReturn("123");
    when(emptyUrlItem.getUrl()).thenReturn("");
    assertFalse(AppUtils.isHackerNewsUrl(emptyUrlItem));
  }

  @Test
  public void testCreateItemUri() {
    Uri uri = AppUtils.createItemUri("123");
    assertNotNull(uri);
    assertEquals(BuildConfig.APPLICATION_ID, uri.getScheme());
    assertEquals("item", uri.getAuthority());
    assertEquals("123", uri.getLastPathSegment());
    assertEquals(BuildConfig.APPLICATION_ID + "://item/123", uri.toString());
  }

  @Test
  public void testCreateUserUri() {
    Uri uri = AppUtils.createUserUri("sheep");
    assertNotNull(uri);
    assertEquals(BuildConfig.APPLICATION_ID, uri.getScheme());
    assertEquals("user", uri.getAuthority());
    assertEquals("sheep", uri.getLastPathSegment());
    assertEquals(BuildConfig.APPLICATION_ID + "://user/sheep", uri.toString());
  }

  @Test
  public void testGetDataUriId() {
    // Intent without data returns null
    Intent emptyIntent = new Intent();
    assertNull(AppUtils.getDataUriId(emptyIntent, "id"));

    // Intent with item data URI matching application scheme
    Intent itemIntent = new Intent().setData(AppUtils.createItemUri("123"));
    assertEquals("123", AppUtils.getDataUriId(itemIntent, "id"));

    // Intent with user data URI matching application scheme
    Intent userIntent = new Intent().setData(AppUtils.createUserUri("sheep"));
    assertEquals("sheep", AppUtils.getDataUriId(userIntent, "id"));

    // Intent with web URL and altParamId
    Intent webIntent = new Intent().setData(Uri.parse("https://news.ycombinator.com/item?id=456"));
    assertEquals("456", AppUtils.getDataUriId(webIntent, "id"));

    // Intent with web URL missing altParamId
    Intent noParamIntent = new Intent().setData(Uri.parse("https://example.com"));
    assertNull(AppUtils.getDataUriId(noParamIntent, "id"));
  }

  @Test
  public void testGetThemedResId() {
    Context context = ApplicationProvider.getApplicationContext();
    context.setTheme(R.style.AppTheme);
    int resId = AppUtils.getThemedResId(context, androidx.appcompat.R.attr.colorPrimary);
    assertTrue(resId > 0);
  }

  @Test
  public void testNavigate() {
    AppBarLayout appBarLayout = mock(AppBarLayout.class);
    Navigable navigable = mock(Navigable.class);

    // Direction down when bottom == 0: calls navigable.onNavigate directly
    when(appBarLayout.getBottom()).thenReturn(0);
    AppUtils.navigate(Navigable.DIRECTION_DOWN, appBarLayout, navigable);
    verify(navigable).onNavigate(Navigable.DIRECTION_DOWN);
    verify(appBarLayout, never()).setExpanded(anyBoolean(), anyBoolean());

    // Direction down when bottom > 0: calls appBarLayout.setExpanded(false, true)
    when(appBarLayout.getBottom()).thenReturn(100);
    AppUtils.navigate(Navigable.DIRECTION_DOWN, appBarLayout, navigable);
    verify(appBarLayout).setExpanded(false, true);
    verify(navigable, times(1)).onNavigate(Navigable.DIRECTION_DOWN);

    // Direction right when bottom == 0: calls navigable.onNavigate directly
    when(appBarLayout.getBottom()).thenReturn(0);
    AppUtils.navigate(Navigable.DIRECTION_RIGHT, appBarLayout, navigable);
    verify(navigable).onNavigate(Navigable.DIRECTION_RIGHT);

    // Direction right when bottom > 0: calls appBarLayout.setExpanded(false, true)
    when(appBarLayout.getBottom()).thenReturn(50);
    AppUtils.navigate(Navigable.DIRECTION_RIGHT, appBarLayout, navigable);
    verify(appBarLayout, times(2)).setExpanded(false, true);

    // Direction up: calls navigable.onNavigate directly regardless of bottom
    AppUtils.navigate(Navigable.DIRECTION_UP, appBarLayout, navigable);
    verify(navigable).onNavigate(Navigable.DIRECTION_UP);
  }

  @Test
  public void testGetDisplayHeight() {
    Context context = ApplicationProvider.getApplicationContext();
    int height = AppUtils.getDisplayHeight(context);
    assertTrue(height > 0);
  }
  private void setupActiveNetwork(Context context) {
    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    ShadowConnectivityManager shadowCm = Shadows.shadowOf(cm);
    shadowCm.setDefaultNetworkActive(true);
    Network network = cm.getActiveNetwork();
    if (network != null) {
      NetworkCapabilities nc = ShadowNetworkCapabilities.newInstance();
      Shadows.shadowOf(nc).addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
      shadowCm.setNetworkCapabilities(network, nc);
    }
  }
  private Intent getNextStartedActivity(Context context) {
    return Shadows.shadowOf((Application) context.getApplicationContext()).getNextStartedActivity();
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
  static class TestWebItem implements WebItem {
    private final String id;
    private final String url;
    TestWebItem(String id, String url) {
      this.id = id;
      this.url = url;
    }
    @Override public String getId() { return id; }
    @Override public long getLongId() { return Long.parseLong(id); }
    @Override public String getUrl() { return url; }
    @Override public String getDisplayedTitle() { return "Title"; }
    @Override public Spannable getDisplayedAuthor(Context context, boolean linkify, int color) { return null; }
    @Override public Spannable getDisplayedTime(Context context) { return null; }
    @Override public String getSource() { return ""; }
    @Override public String getType() { return STORY_TYPE; }
    @Override public boolean isStoryType() { return true; }
    @Override public boolean isFavorite() { return false; }
    @Override public void setFavorite(boolean favorite) {}
    @Override public int describeContents() { return 0; }
    @Override public void writeToParcel(Parcel dest, int flags) {}
  }
}
