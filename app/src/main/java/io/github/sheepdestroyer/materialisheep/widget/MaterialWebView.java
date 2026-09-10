/*
 * Copyright (c) 2016 Ha Duy Trung
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.sheepdestroyer.materialisheep.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebViewClient;
import io.github.sheepdestroyer.materialisheep.AppUtils;
import io.github.sheepdestroyer.materialisheep.annotation.Synthetic;

public class MaterialWebView extends android.webkit.WebView {
  static final String BLANK = "about:blank";
  static final String FILE = "file:///";
  private final HistoryWebViewClient mClient = new HistoryWebViewClient();
  @Synthetic String mPendingUrl, mPendingHtml;

  public MaterialWebView(Context context) {
    this(context, null);
  }

  public MaterialWebView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public MaterialWebView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    super.setWebViewClient(mClient);
  }

  @Override
  public void setWebViewClient(WebViewClient client) {
    mClient.wrap(client);
  }

  @Override
  public boolean canGoBack() {
    return TextUtils.isEmpty(mPendingUrl) && super.canGoBack();
  }

  public void reloadUrl(String url) {
    if (getProgress() < 100) {
      stopLoading(); // this will fire onPageFinished for current URL
    }
    mPendingUrl = url;
    loadUrl(BLANK); // clear current web resources, load pending URL upon onPageFinished
  }

  public void reloadHtml(String html) {
    mPendingHtml = html;
    reloadUrl(FILE);
  }

  protected WebResourceResponse interceptRequest(WebResourceRequest request) {
    return null;
  }

  static class HistoryWebViewClient extends WebViewClient {
    private WebViewClient mClient;

    @Override
    public void onPageStarted(android.webkit.WebView view, String url, Bitmap favicon) {
      super.onPageStarted(view, url, favicon);
      view.pageUp(true);
      MaterialWebView webView = (MaterialWebView) view;
      if (AppUtils.urlEquals(url, webView.mPendingUrl)) {
        view.setVisibility(VISIBLE);
      }
      if (mClient != null) {
        mClient.onPageStarted(view, url, favicon);
      }
    }

    @Override
    public void onPageFinished(android.webkit.WebView view, String url) {
      super.onPageFinished(view, url);
      MaterialWebView webView = (MaterialWebView) view;
      if (TextUtils.equals(url, BLANK)) { // has pending reload, open corresponding URL
        if (!TextUtils.isEmpty(webView.mPendingHtml)) {
          view.loadDataWithBaseURL(
              webView.mPendingUrl, webView.mPendingHtml, "text/html", "UTF-8", webView.mPendingUrl);
        } else {
          view.loadUrl(webView.mPendingUrl);
        }
      } else if (!TextUtils.isEmpty(webView.mPendingUrl)
          && TextUtils.equals(url, webView.mPendingUrl)) { // reload done, clear history
        webView.mPendingUrl = null;
        webView.mPendingHtml = null;
        view.clearHistory();
      }
      if (mClient != null) {
        mClient.onPageFinished(view, url);
      }
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(
        android.webkit.WebView view, WebResourceRequest request) {
      if (view instanceof MaterialWebView) {
        WebResourceResponse response = ((MaterialWebView) view).interceptRequest(request);
        if (response != null) {
          return response;
        }
      }
      return mClient != null
          ? mClient.shouldInterceptRequest(view, request)
          : super.shouldInterceptRequest(view, request);
    }

    @Synthetic
    void wrap(WebViewClient client) {
      mClient = client;
    }
  }
}
