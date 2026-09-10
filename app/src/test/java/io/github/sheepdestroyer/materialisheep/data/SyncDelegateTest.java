package io.github.sheepdestroyer.materialisheep.data;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;
import java.util.concurrent.Executor;

import retrofit2.Call;

import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class SyncDelegateTest {

    @Mock
    RestServiceFactory factory;

    @Mock
    ReadabilityClient readabilityClient;

    @Mock
    HackerNewsClient.RestService restService;

    @Mock
    Call<HackerNewsItem> mockCall;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(factory.create(
                eq(HackerNewsClient.BASE_API_URL),
                eq(HackerNewsClient.RestService.class),
                any(Executor.class)
        )).thenReturn(restService);
    }

    @Test
    public void testGetFromCache_ioException_returnsNull() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        SyncDelegate syncDelegate = new SyncDelegate(context, factory, readabilityClient);

        String itemId = "123";
        when(restService.cachedItem(itemId)).thenReturn(mockCall);
        when(mockCall.execute()).thenThrow(new IOException("Test exception"));

        // Use reflection to access the private method
        java.lang.reflect.Method method = SyncDelegate.class.getDeclaredMethod("getFromCache", String.class);
        method.setAccessible(true);
        HackerNewsItem result = (HackerNewsItem) method.invoke(syncDelegate, itemId);

        assertNull(result);
    }
}
