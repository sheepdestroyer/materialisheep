package io.github.sheepdestroyer.materialisheep.data;

import static org.mockito.Mockito.times;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import java.io.IOException;
import java.util.concurrent.Executor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
@RunWith(RobolectricTestRunner.class)
public class SyncDelegateTest {
  private Context context;
  private RestServiceFactory factory;
  private HackerNewsClient.RestService hnRestService;
  private ReadabilityClient readabilityClient;
  private SyncDelegate syncDelegate;
  @Before
  public void setUp() {
    context = ApplicationProvider.getApplicationContext();
    factory = mock(RestServiceFactory.class);
    hnRestService = mock(HackerNewsClient.RestService.class);
    readabilityClient = mock(ReadabilityClient.class);
    when(factory.create(
            anyString(), eq(HackerNewsClient.RestService.class), any(Executor.class)))
        .thenReturn(hnRestService);
    syncDelegate = new SyncDelegate(context, factory, readabilityClient);
  }
  @Test
  public void testGetFromCache_Success_DoesNotFallbackToNetwork() throws IOException {
    String itemId = "123";
    SyncDelegate.Job job =
        new SyncDelegate.JobBuilder(context, itemId)
            .setConnectionEnabled(true)
            .setCommentsEnabled(false)
            .setReadabilityEnabled(false)
            .setArticleEnabled(false)
            .setNotificationEnabled(false)
            .build();
    SyncDelegate.ProgressListener listener = mock(SyncDelegate.ProgressListener.class);
    syncDelegate.subscribe(listener);
    HackerNewsItem cachedItem = mock(HackerNewsItem.class);
    when(cachedItem.getId()).thenReturn(itemId);
    Call<HackerNewsItem> cachedCall = mock(Call.class);
    when(cachedCall.execute()).thenReturn(Response.success(cachedItem));
    when(hnRestService.cachedItem(itemId)).thenReturn(cachedCall);
    syncDelegate.performSync(job);
    verify(hnRestService).cachedItem(itemId);
    verify(hnRestService, never()).networkItem(anyString());
    verify(listener).onDone(itemId);
    assertFalse(job.connectionEnabled);
  }
  @Test
  public void testGetFromCache_IOException_FallsBackToNetwork() throws IOException {
    String itemId = "123";
    SyncDelegate.Job job =
        new SyncDelegate.JobBuilder(context, itemId)
            .setConnectionEnabled(true)
            .setCommentsEnabled(false)
            .setReadabilityEnabled(false)
            .setArticleEnabled(false)
            .setNotificationEnabled(false)
            .build();
    Call<HackerNewsItem> cachedCall = mock(Call.class);
    when(cachedCall.execute()).thenThrow(new IOException("Cache retrieval error"));
    when(hnRestService.cachedItem(itemId)).thenReturn(cachedCall);
    Call<HackerNewsItem> networkCall = mock(Call.class);
    when(hnRestService.networkItem(itemId)).thenReturn(networkCall);
    syncDelegate.performSync(job);
    verify(hnRestService).cachedItem(itemId);
    verify(hnRestService).networkItem(itemId);
    verify(networkCall).enqueue(any());
  }
  @Test
  public void testGetFromCache_IOException_NetworkFailure_StateCleanup() throws IOException {
    String itemId = "123";
    SyncDelegate.Job job =
        new SyncDelegate.JobBuilder(context, itemId)
            .setConnectionEnabled(true)
            .setCommentsEnabled(false)
            .setReadabilityEnabled(false)
            .setArticleEnabled(false)
            .setNotificationEnabled(true)
            .build();
    SyncDelegate.ProgressListener listener = mock(SyncDelegate.ProgressListener.class);
    syncDelegate.subscribe(listener);
    Call<HackerNewsItem> cachedCall = mock(Call.class);
    when(cachedCall.execute()).thenThrow(new IOException("Cache error"));
    when(hnRestService.cachedItem(itemId)).thenReturn(cachedCall);
    Call<HackerNewsItem> networkCall = mock(Call.class);
    doAnswer(
            invocation -> {
              Callback<HackerNewsItem> callback = invocation.getArgument(0);
              callback.onFailure(networkCall, new IOException("Network error"));
              return null;
            })
        .when(networkCall)
        .enqueue(any());
    when(hnRestService.networkItem(itemId)).thenReturn(networkCall);
    syncDelegate.performSync(job);
    verify(listener).onDone(itemId);
    assertFalse(job.connectionEnabled);
  }
  @Test
  public void testGetFromCache_IOException_NetworkSuccess_CompletesSync() throws IOException {
    String itemId = "123";
    SyncDelegate.Job job =
        new SyncDelegate.JobBuilder(context, itemId)
            .setConnectionEnabled(true)
            .setCommentsEnabled(false)
            .setReadabilityEnabled(false)
            .setArticleEnabled(false)
            .setNotificationEnabled(false)
            .build();
    SyncDelegate.ProgressListener listener = mock(SyncDelegate.ProgressListener.class);
    syncDelegate.subscribe(listener);
    Call<HackerNewsItem> cachedCall = mock(Call.class);
    when(cachedCall.execute()).thenThrow(new IOException("Cache error"));
    when(hnRestService.cachedItem(itemId)).thenReturn(cachedCall);
    HackerNewsItem networkItem = mock(HackerNewsItem.class);
    when(networkItem.getId()).thenReturn(itemId);
    Call<HackerNewsItem> networkCall = mock(Call.class);
    doAnswer(
            invocation -> {
              Callback<HackerNewsItem> callback = invocation.getArgument(0);
              callback.onResponse(networkCall, Response.success(networkItem));
              return null;
            })
        .when(networkCall)
        .enqueue(any());
    when(hnRestService.networkItem(itemId)).thenReturn(networkCall);
    syncDelegate.performSync(job);
    verify(listener).onDone(itemId);
    assertFalse(job.connectionEnabled);
  }
  @Test
  public void testFinishExecutedOnlyOnce() {
    SyncDelegate.ProgressListener listener = mock(SyncDelegate.ProgressListener.class);
    syncDelegate.subscribe(listener);
    Call<HackerNewsItem> networkCall = mock(Call.class);
    when(hnRestService.networkItem("1")).thenReturn(networkCall);
    SyncDelegate.Job job =
        new SyncDelegate.JobBuilder(context, "1")
            .setConnectionEnabled(true)
            .setCommentsEnabled(false)
            .setReadabilityEnabled(false)
            .setArticleEnabled(false)
            .setNotificationEnabled(false)
            .build();
    syncDelegate.performSync(job);
    // Max progress for this job: 1 + totalKids(0) + readability(0) + article(0) = 1.
    // Notify item once to complete sync
    syncDelegate.notifyItem("1", null);
    // Try triggering progress updates multiple times
    syncDelegate.notifyItem("1", null);
    syncDelegate.notifyArticle(100);
    // Verify listener.onDone was called exactly once
    verify(listener, times(1)).onDone("1");
  }

  @Test
  public void testStopSyncPreventsFinish() {
    SyncDelegate.ProgressListener listener = mock(SyncDelegate.ProgressListener.class);
    syncDelegate.subscribe(listener);
    Call<HackerNewsItem> networkCall = mock(Call.class);
    when(hnRestService.networkItem("2")).thenReturn(networkCall);
    SyncDelegate.Job job =
        new SyncDelegate.JobBuilder(context, "2")
            .setConnectionEnabled(true)
            .setCommentsEnabled(false)
            .setReadabilityEnabled(false)
            .setArticleEnabled(false)
            .setNotificationEnabled(false)
            .build();
    syncDelegate.performSync(job);
    // Stop sync before progress reaches max
    syncDelegate.stopSync();
    // Late callback arrives
    syncDelegate.notifyItem("2", null);
    // Verify listener.onDone was never called
    verify(listener, times(0)).onDone("2");
  }
}
