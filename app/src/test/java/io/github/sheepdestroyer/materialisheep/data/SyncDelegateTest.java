package io.github.sheepdestroyer.materialisheep.data;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

@RunWith(RobolectricTestRunner.class)
public class SyncDelegateTest {

    private RestServiceFactory factory;
    private ReadabilityClient readabilityClient;
    private HackerNewsClient.RestService hnRestService;
    private SyncDelegate syncDelegate;

    @Before
    public void setUp() throws IOException {
        Context context = ApplicationProvider.getApplicationContext();
        factory = mock(RestServiceFactory.class);
        readabilityClient = mock(ReadabilityClient.class);
        hnRestService = mock(HackerNewsClient.RestService.class);

        when(factory.create(any(), eq(HackerNewsClient.RestService.class), any())).thenReturn(hnRestService);

        Call<HackerNewsItem> call = mock(Call.class);
        when(call.execute()).thenReturn(Response.success(null));
        when(hnRestService.cachedItem(any())).thenReturn(call);
        when(hnRestService.networkItem(any())).thenReturn(call);

        syncDelegate = new SyncDelegate(context, factory, readabilityClient);
    }

    @Test
    public void testFinishExecutedOnlyOnce() {
        SyncDelegate.ProgressListener listener = mock(SyncDelegate.ProgressListener.class);
        syncDelegate.subscribe(listener);

        SyncDelegate.Job job = new SyncDelegate.Job("1");
        job.connectionEnabled = true;
        job.commentsEnabled = false;
        job.readabilityEnabled = false;
        job.articleEnabled = false;

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

        SyncDelegate.Job job = new SyncDelegate.Job("2");
        job.connectionEnabled = true;

        syncDelegate.performSync(job);

        // Stop sync before progress reaches max
        syncDelegate.stopSync();

        // Late callback arrives
        syncDelegate.notifyItem("2", null);

        // Verify listener.onDone was never called
        verify(listener, times(0)).onDone("2");
    }
}
