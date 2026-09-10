package io.github.sheepdestroyer.materialisheep;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.res.AssetManager;
import android.webkit.WebResourceResponse;
import androidx.test.core.app.ApplicationProvider;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class AdBlockerTest {

  @Test
  public void testInitSuccessAndIsAd() {
    Context context = ApplicationProvider.getApplicationContext();
    AdBlocker.init(context, Schedulers.trampoline());

    assertTrue(AdBlocker.isAd("http://pagead2.googlesyndication.com/foo"));
    assertFalse(AdBlocker.isAd("https://example.com/bar"));
    assertFalse(AdBlocker.isAd(""));
  }

  @Test
  public void testCreateEmptyResource() {
    WebResourceResponse response = AdBlocker.createEmptyResource();
    assertNotNull(response);
    assertEquals("text/plain", response.getMimeType());
    assertEquals("utf-8", response.getEncoding());
  }

  @Test
  public void testInitAssetLoadingError() throws IOException {
    Context context = mock(Context.class);
    AssetManager assetManager = mock(AssetManager.class);

    when(context.getAssets()).thenReturn(assetManager);
    when(assetManager.open(anyString())).thenThrow(new IOException("Failed to load asset"));

    // Call init with the mocked context where opening assets throws IOException.
    // Error handler in RxJava subscription catches and logs the exception without crashing.
    AdBlocker.init(context, Schedulers.trampoline());
  }
}
