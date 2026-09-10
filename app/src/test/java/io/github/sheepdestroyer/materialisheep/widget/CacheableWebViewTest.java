package io.github.sheepdestroyer.materialisheep.widget;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.net.Uri;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import androidx.test.core.app.ApplicationProvider;
import java.io.File;
import java.io.IOException;
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
    when(wrongExt.getUrl()).thenReturn(Uri.parse("file://" + context.getCacheDir().getAbsolutePath() + "/webarchive-test.txt"));
    assertNull(webView.interceptRequest(wrongExt));
  }

  @Test
  public void testInterceptRequestValidArchive() throws IOException {
    Context context = ApplicationProvider.getApplicationContext();
    CacheableWebView webView = new CacheableWebView(context);

    File cacheFile = new File(context.getCacheDir(), "webarchive-test123.mht");
    cacheFile.createNewFile();
    try {
      WebResourceRequest validRequest = mock(WebResourceRequest.class);
      when(validRequest.isForMainFrame()).thenReturn(true);
      when(validRequest.getUrl()).thenReturn(Uri.fromFile(cacheFile));

      WebResourceResponse response = webView.interceptRequest(validRequest);
      assertNotNull(response);
    } finally {
      cacheFile.delete();
    }
  }

  @Test
  public void testInterceptRequestSiblingDirectoryEscape() {
    Context context = ApplicationProvider.getApplicationContext();
    CacheableWebView webView = new CacheableWebView(context);

    File siblingDir = new File(context.getCacheDir().getParentFile(), "cache-sibling");
    File fakeFile = new File(siblingDir, "webarchive-test.mht");

    WebResourceRequest siblingRequest = mock(WebResourceRequest.class);
    when(siblingRequest.isForMainFrame()).thenReturn(true);
    when(siblingRequest.getUrl()).thenReturn(Uri.fromFile(fakeFile));

    assertNull(webView.interceptRequest(siblingRequest));
  }
}
