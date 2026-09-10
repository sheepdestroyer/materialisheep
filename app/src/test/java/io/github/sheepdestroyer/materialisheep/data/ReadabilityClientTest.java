package io.github.sheepdestroyer.materialisheep.data;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.webkit.WebView;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.shadows.ShadowWebView;

import java.io.IOException;

import io.reactivex.rxjava3.schedulers.Schedulers;

@RunWith(RobolectricTestRunner.class)
@Config(shadows = {ReadabilityClientTest.AutoLoadShadowWebView.class})
public class ReadabilityClientTest {

  @Implements(WebView.class)
  public static class AutoLoadShadowWebView extends ShadowWebView {
    @RealObject private WebView realWebView;

    @Implementation
    @Override
    protected void loadUrl(String url) {
      super.loadUrl(url);
      if (getWebViewClient() != null) {
        getWebViewClient().onPageFinished(realWebView, url);
      }
    }
  }

  private Context mContext;
  private AssetManager mAssetManager;
  private LocalCache mLocalCache;

  @Before
  public void setUp() throws IOException {
    Context appContext = ApplicationProvider.getApplicationContext();
    mAssetManager = mock(AssetManager.class);
    mLocalCache = mock(LocalCache.class);

    mContext = new ContextWrapper(appContext) {
      @Override
      public AssetManager getAssets() {
        return mAssetManager;
      }
    };

    when(mAssetManager.open("Readability.js")).thenThrow(new IOException("Asset loading error"));
    when(mLocalCache.getReadability(anyString())).thenReturn(null);
  }

  @Test
  public void testIOExceptionOnAssetLoading_HandledSafely() {
    ReadabilityClient.Impl client = new ReadabilityClient.Impl(
        mContext,
        mLocalCache,
        Schedulers.trampoline(),
        Schedulers.trampoline()
    );

    ReadabilityClient.Callback callback = mock(ReadabilityClient.Callback.class);
    client.parse("item1", "https://example.com", callback);

    ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

    verify(callback).onResponse(null);
  }

  @Test
  public void testIOExceptionOnAssetLoading_PreParseHandledSafely() {
    ReadabilityClient.Impl client = new ReadabilityClient.Impl(
        mContext,
        mLocalCache,
        Schedulers.trampoline(),
        Schedulers.trampoline()
    );

    client.parse("item2", "https://example.com");

    ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
  }
}
