package io.github.sheepdestroyer.materialisheep.data;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import retrofit2.Call;
import retrofit2.Response;
import java.io.IOException;


@RunWith(MockitoJUnitRunner.class)
public class HackerNewsClientTest {

    @Mock
    private RestServiceFactory restServiceFactory;
    @Mock
    private HackerNewsClient.RestService restService;
    @Mock
    private SessionManager sessionManager;
    @Mock
    private FavoriteManager favoriteManager;
    @Mock
    private Call<int[]> mockCall;

    private HackerNewsClient client;

    @Before
    public void setUp() {
        when(restServiceFactory.rxEnabled(true)).thenReturn(restServiceFactory);
        when(restServiceFactory.create(HackerNewsClient.BASE_API_URL, HackerNewsClient.RestService.class)).thenReturn(restService);

        client = new HackerNewsClient(restServiceFactory, sessionManager, favoriteManager);
        // We do not have direct access to set mIoScheduler and mMainThreadScheduler,
        // but the synchronous method getStories(filter, cacheMode) does not use them anyway.
    }

    @Test
    public void getStories_ioException_returnsEmptyArray() throws IOException {
        when(restService.topStories()).thenReturn(mockCall);
        when(mockCall.execute()).thenThrow(new IOException("Network error"));

        Item[] items = client.getStories(ItemManager.TOP_FETCH_MODE, ItemManager.MODE_DEFAULT);

        assertNotNull(items);
        assertEquals(0, items.length);
    }
}
