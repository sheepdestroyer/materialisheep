package io.github.sheepdestroyer.materialisheep.widget;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.net.Uri;
import android.webkit.WebResourceRequest;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class CacheableWebViewTest {

  @Test
  public void testFileAndContentAccessDisabled() {
    Context context = ApplicationProvider.getApplicationContext();
    CacheableWebView webView = new CacheableWebView(context);

    assertFalse("File access should be disabled on CacheableWebView", webView.getSettings().getAllowFileAccess());
    assertFalse("Content access should be disabled on CacheableWebView", webView.getSettings().getAllowContentAccess());
  }

  @Test
  public void testInterceptRequestNullOrNonMainFrame() {
    Context context = ApplicationProvider.getApplicationContext();
    CacheableWebView webView = new CacheableWebView(context);

    assertNull(webView.interceptRequest(null));

    WebResourceRequest subresourceRequest = mock(WebResourceRequest.class);
    when(subresourceRequest.isForMainFrame()).thenReturn(false);
    when(subresourceRequest.getUrl()).thenReturn(Uri.parse("file://" + context.getCacheDir().getAbsolutePath() + "/archive_test.mht"));
    assertNull(webView.interceptRequest(subresourceRequest));
  }

  @Test
  public void testInterceptRequestInvalidPathOrNonMht() {
    Context context = ApplicationProvider.getApplicationContext();
    CacheableWebView webView = new CacheableWebView(context);

    // Traversal outside cache dir
    WebResourceRequest traversalRequest = mock(WebResourceRequest.class);
    when(traversalRequest.isForMainFrame()).thenReturn(true);
    when(traversalRequest.getUrl()).thenReturn(Uri.parse("file:///etc/hosts"));
    assertNull(webView.interceptRequest(traversalRequest));

    // Wrong prefix
    WebResourceRequest wrongPrefix = mock(WebResourceRequest.class);
    when(wrongPrefix.isForMainFrame()).thenReturn(true);
    when(wrongPrefix.getUrl()).thenReturn(Uri.parse("file://" + context.getCacheDir().getAbsolutePath() + "/malicious.mht"));
    assertNull(webView.interceptRequest(wrongPrefix));

    // Wrong extension
    WebResourceRequest wrongExt = mock(WebResourceRequest.class);
    when(wrongExt.isForMainFrame()).thenReturn(true);
    when(wrongExt.getUrl()).thenReturn(Uri.parse("file://" + context.getCacheDir().getAbsolutePath() + "/archive_test.txt"));
    assertNull(webView.interceptRequest(wrongExt));
  }
}
