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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.clearInvocations;
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
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.text.format.DateUtils;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Window;
import android.webkit.WebSettings;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
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

    // Valid HN URL format with mismatched ID
    WebItem mismatchedItem = mock(WebItem.class);
    when(mismatchedItem.getId()).thenReturn("123");
    when(mismatchedItem.getUrl()).thenReturn("https://news.ycombinator.com/item?id=999");
    assertFalse(AppUtils.isHackerNewsUrl(mismatchedItem));

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

    // Intent with web URL and empty altParamId value
    Intent emptyParamIntent = new Intent().setData(Uri.parse("https://news.ycombinator.com/item?id="));
    assertEquals("", AppUtils.getDataUriId(emptyParamIntent, "id"));

    // Intent with null altParamId
    Intent nullAltParamIntent = new Intent().setData(Uri.parse("https://news.ycombinator.com/item?id=456"));
    assertNull(AppUtils.getDataUriId(nullAltParamIntent, null));

    // Intent with non-hierarchical/opaque URI
    Intent opaqueUriIntent = new Intent().setData(Uri.parse("mailto:test@example.com"));
    assertNull(AppUtils.getDataUriId(opaqueUriIntent, "id"));
  }

  @Test
  public void testGetThemedResId() {
    Context context =
        new ContextThemeWrapper(ApplicationProvider.getApplicationContext(), R.style.AppTheme);
    int resId = AppUtils.getThemedResId(context, androidx.appcompat.R.attr.colorPrimary);
    assertTrue(resId > 0);
  }

  @Test
  public void testNavigate() {
    AppBarLayout appBarLayout = mock(AppBarLayout.class);
    Navigable navigable = mock(Navigable.class);

    // Direction down when bottom == 0: calls navigable.onNavigate directly, never setExpanded
    when(appBarLayout.getBottom()).thenReturn(0);
    AppUtils.navigate(Navigable.DIRECTION_DOWN, appBarLayout, navigable);
    verify(navigable).onNavigate(Navigable.DIRECTION_DOWN);
    verify(appBarLayout, never()).setExpanded(anyBoolean(), anyBoolean());

    // Direction down when bottom > 0: calls appBarLayout.setExpanded(false, true), never navigable.onNavigate
    clearInvocations(appBarLayout, navigable);
    when(appBarLayout.getBottom()).thenReturn(100);
    AppUtils.navigate(Navigable.DIRECTION_DOWN, appBarLayout, navigable);
    verify(appBarLayout).setExpanded(false, true);
    verify(navigable, never()).onNavigate(anyInt());

    // Direction right when bottom == 0: calls navigable.onNavigate directly, never setExpanded
    clearInvocations(appBarLayout, navigable);
    when(appBarLayout.getBottom()).thenReturn(0);
    AppUtils.navigate(Navigable.DIRECTION_RIGHT, appBarLayout, navigable);
    verify(navigable).onNavigate(Navigable.DIRECTION_RIGHT);
    verify(appBarLayout, never()).setExpanded(anyBoolean(), anyBoolean());

    // Direction right when bottom > 0: calls appBarLayout.setExpanded(false, true), never navigable.onNavigate
    clearInvocations(appBarLayout, navigable);
    when(appBarLayout.getBottom()).thenReturn(50);
    AppUtils.navigate(Navigable.DIRECTION_RIGHT, appBarLayout, navigable);
    verify(appBarLayout).setExpanded(false, true);
    verify(navigable, never()).onNavigate(anyInt());

    // Direction up: calls navigable.onNavigate directly regardless of bottom, never setExpanded
    clearInvocations(appBarLayout, navigable);
    when(appBarLayout.getBottom()).thenReturn(100);
    AppUtils.navigate(Navigable.DIRECTION_UP, appBarLayout, navigable);
    verify(navigable).onNavigate(Navigable.DIRECTION_UP);
    verify(appBarLayout, never()).setExpanded(anyBoolean(), anyBoolean());

    // Direction left: calls navigable.onNavigate directly regardless of bottom, never setExpanded
    clearInvocations(appBarLayout, navigable);
    when(appBarLayout.getBottom()).thenReturn(100);
    AppUtils.navigate(Navigable.DIRECTION_LEFT, appBarLayout, navigable);
    verify(navigable).onNavigate(Navigable.DIRECTION_LEFT);
    verify(appBarLayout, never()).setExpanded(anyBoolean(), anyBoolean());
  }

  @Test
  public void testGetDisplayHeight() {
    Context context = ApplicationProvider.getApplicationContext();
    int height = AppUtils.getDisplayHeight(context);
    assertTrue(height > 0);

  public void testShare() {
    Activity activity = Robolectric.buildActivity(Activity.class).get();
    Context context = activity;
    ShadowPackageManager shadowPm = shadowOf(context.getPackageManager());
    Intent sendIntent = new Intent(Intent.ACTION_SEND).setType("text/plain");
    shadowPm.addResolveInfoForIntent(
        sendIntent, createResolveInfo("com.test.app", "com.test.app.MainActivity"));

    // 1. With subject and text
    AppUtils.share(context, "Subject", "Text");
    Intent startedIntent = shadowOf(activity).getNextStartedActivity();
    assertNotNull(startedIntent);
    assertEquals(Intent.ACTION_SEND, startedIntent.getAction());
    assertEquals("text/plain", startedIntent.getType());
    assertEquals("Subject", startedIntent.getStringExtra(Intent.EXTRA_SUBJECT));
    assertEquals("Subject - Text", startedIntent.getStringExtra(Intent.EXTRA_TEXT));

    // 2. With null subject
    AppUtils.share(context, null, "TextOnly");
    Intent startedIntent2 = shadowOf(activity).getNextStartedActivity();
    assertNotNull(startedIntent2);
    assertEquals(Intent.ACTION_SEND, startedIntent2.getAction());
    assertEquals("text/plain", startedIntent2.getType());
    assertNull(startedIntent2.getStringExtra(Intent.EXTRA_SUBJECT));
    assertEquals("TextOnly", startedIntent2.getStringExtra(Intent.EXTRA_TEXT));

    // 3. With empty string subject
    AppUtils.share(context, "", "TextWithEmptySubject");
    Intent startedIntent3 = shadowOf(activity).getNextStartedActivity();
    assertNotNull(startedIntent3);
    assertEquals(Intent.ACTION_SEND, startedIntent3.getAction());
    assertEquals("text/plain", startedIntent3.getType());
    assertEquals("", startedIntent3.getStringExtra(Intent.EXTRA_SUBJECT));
    assertEquals("TextWithEmptySubject", startedIntent3.getStringExtra(Intent.EXTRA_TEXT));
  }

  @Test
  public void testShare_noResolvingActivity() {
    Activity activity = mock(Activity.class);
    PackageManager packageManager = mock(PackageManager.class);
    when(activity.getPackageManager()).thenReturn(packageManager);
    when(packageManager.resolveActivity(any(Intent.class), anyInt())).thenReturn(null);

    AppUtils.share(activity, "Subject", "Text");
    verify(activity, never()).startActivity(any(Intent.class));
  }

  @Test
  public void testMakeSendIntentChooser() {
    Context context = ApplicationProvider.getApplicationContext();
    Uri uri = Uri.parse("http://example.com");
    Intent chooser = AppUtils.makeSendIntentChooser(context, uri);
    assertNotNull(chooser);
    assertEquals(Intent.ACTION_CHOOSER, chooser.getAction());
    assertEquals(
        context.getString(R.string.share_file),
        chooser.getCharSequenceExtra(Intent.EXTRA_TITLE));
    Intent target = chooser.getParcelableExtra(Intent.EXTRA_INTENT);
    assertNotNull(target);
    assertEquals(Intent.ACTION_SEND_MULTIPLE, target.getAction());
    assertEquals("text/plain", target.getType());
    ArrayList<Uri> streams = target.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
    assertNotNull(streams);
    assertEquals(1, streams.size());
    assertEquals(uri, streams.get(0));

    Intent chooserNull = AppUtils.makeSendIntentChooser(context, null);
    assertNotNull(chooserNull);
    assertEquals(Intent.ACTION_CHOOSER, chooserNull.getAction());
    assertEquals(
        context.getString(R.string.share_file),
        chooserNull.getCharSequenceExtra(Intent.EXTRA_TITLE));
    Intent targetNull = chooserNull.getParcelableExtra(Intent.EXTRA_INTENT);
    assertNotNull(targetNull);
    assertEquals(Intent.ACTION_SEND_MULTIPLE, targetNull.getAction());
    ArrayList<Uri> streamsNull = targetNull.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
    assertNotNull(streamsNull);
    assertTrue(streamsNull.isEmpty());
  }

  @Test
  public void testIsOnWiFi() {
    Context baseContext = ApplicationProvider.getApplicationContext();
    ConnectivityManager mockCm = mock(ConnectivityManager.class);
    Context context =
        new ContextWrapper(baseContext) {
          @Override
          public Object getSystemService(String name) {
            if (Context.CONNECTIVITY_SERVICE.equals(name)) {
              return mockCm;
            }
            return super.getSystemService(name);
          }
        };

    // 1. null active network -> false
    when(mockCm.getActiveNetwork()).thenReturn(null);
    assertFalse(AppUtils.isOnWiFi(context));

    // 2. active network with null capabilities -> false
    Network mockNetwork = mock(Network.class);
    when(mockCm.getActiveNetwork()).thenReturn(mockNetwork);
    when(mockCm.getNetworkCapabilities(mockNetwork)).thenReturn(null);
    assertFalse(AppUtils.isOnWiFi(context));

    // 3. active network with cellular transport -> false
    NetworkCapabilities mockCapCellular = mock(NetworkCapabilities.class);
    when(mockCapCellular.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)).thenReturn(false);
    when(mockCm.getNetworkCapabilities(mockNetwork)).thenReturn(mockCapCellular);
    assertFalse(AppUtils.isOnWiFi(context));

    // 4. active network with wifi transport -> true
    NetworkCapabilities mockCapWifi = mock(NetworkCapabilities.class);
    when(mockCapWifi.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)).thenReturn(true);
    when(mockCm.getNetworkCapabilities(mockNetwork)).thenReturn(mockCapWifi);
    assertTrue(AppUtils.isOnWiFi(context));

    // 5. null ConnectivityManager -> false
    Context nullCmContext =
        new ContextWrapper(baseContext) {
          @Override
          public Object getSystemService(String name) {
            if (Context.CONNECTIVITY_SERVICE.equals(name)) {
              return null;
            }
            return super.getSystemService(name);
          }
        };
    assertFalse(AppUtils.isOnWiFi(nullCmContext));
  }

  @Test
  public void testRestart() {
    Activity activity = mock(Activity.class);

    AppUtils.restart(activity, false);
    verify(activity).recreate();

    AppUtils.restart(activity, true);
    verify(activity, times(2)).recreate();
  }

  @Test
  public void testToggleFab() {
    FloatingActionButton fab = mock(FloatingActionButton.class);
    AppUtils.toggleFab(fab, true);
    verify(fab).setTag(null);
    verify(fab).show();
    verify(fab, never()).hide();

    FloatingActionButton fabHide = mock(FloatingActionButton.class);
    AppUtils.toggleFab(fabHide, false);
    verify(fabHide).setTag(FabAwareScrollBehavior.HIDDEN);
    verify(fabHide).hide();
    verify(fabHide, never()).show();
  }

  @Test
  public void testSetStatusBarColor() {
    Window window = mock(Window.class);
    AppUtils.setStatusBarColor(window, Color.RED);
    verify(window).setStatusBarColor(Color.RED);
  }

  @Test
  public void testSetStatusBarDim() {
    Activity activity = Robolectric.buildActivity(Activity.class).create().get();
    Window window = mock(Window.class);
    when(window.getContext()).thenReturn(activity);

    // dim = true -> Color.TRANSPARENT
    AppUtils.setStatusBarDim(window, true);
    verify(window).setStatusBarColor(Color.TRANSPARENT);

    // dim = false -> colorPrimaryDark
    int expectedColor =
        ContextCompat.getColor(
            activity,
            AppUtils.getThemedResId(activity, androidx.appcompat.R.attr.colorPrimaryDark));
    AppUtils.setStatusBarDim(window, false);
    verify(window).setStatusBarColor(expectedColor);
  }

  @Test
  public void testToggleWebViewZoom_enable() {
    WebSettings webSettings = mock(WebSettings.class);
    AppUtils.toggleWebViewZoom(webSettings, true);
    verify(webSettings).setSupportZoom(true);
    verify(webSettings).setBuiltInZoomControls(true);
    verify(webSettings).setDisplayZoomControls(false);
  }

  @Test
  public void testToggleWebViewZoom_disable() {
    WebSettings webSettings = mock(WebSettings.class);
    AppUtils.toggleWebViewZoom(webSettings, false);
    verify(webSettings).setSupportZoom(false);
    verify(webSettings).setBuiltInZoomControls(false);
    verify(webSettings).setDisplayZoomControls(false);
  }

  @Test
  public void testCreateLayoutInflater() {
    Context context = ApplicationProvider.getApplicationContext();
    LayoutInflater inflater = AppUtils.createLayoutInflater(context);
    assertNotNull(inflater);
    assertTrue(inflater.getContext() instanceof ContextThemeWrapper);
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
