package io.github.sheepdestroyer.materialisheep.data;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import retrofit2.Call;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

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
    private Call<int[]> mockStoriesCall;
    @Mock
    private Call<HackerNewsItem> mockItemCall;

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
        when(restService.topStories()).thenReturn(mockStoriesCall);
        when(mockStoriesCall.execute()).thenThrow(new IOException("Network error"));

        Item[] items = client.getStories(ItemManager.TOP_FETCH_MODE, ItemManager.MODE_DEFAULT);

        assertNotNull(items);
        assertEquals(0, items.length);
    }

    @Test
    public void testGetItem_IOExceptionReturnsNull() throws IOException {
        when(restService.item(anyString())).thenReturn(mockItemCall);
        when(mockItemCall.execute()).thenThrow(new IOException("Test exception"));

        Item item = client.getItem("1", ItemManager.MODE_DEFAULT);

        assertNull("getItem should return null when execute throws IOException", item);
    }
}
