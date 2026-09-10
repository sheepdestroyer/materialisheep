package io.github.sheepdestroyer.materialisheep.widget;

import static org.junit.Assert.assertFalse;

import android.content.Context;
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
}
