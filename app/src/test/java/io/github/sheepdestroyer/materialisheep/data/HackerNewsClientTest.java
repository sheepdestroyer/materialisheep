package io.github.sheepdestroyer.materialisheep.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Call;

@RunWith(RobolectricTestRunner.class)
public class HackerNewsClientTest {

    @Mock
    RestServiceFactory factory;
    @Mock
    HackerNewsClient.RestService restService;
    @Mock
    SessionManager sessionManager;
    @Mock
    FavoriteManager favoriteManager;
    @Mock
    ResponseListener<Item[]> storiesListener;
    @Mock
    ResponseListener<Item> itemListener;

    @Captor
    ArgumentCaptor<String> errorCaptor;

    private HackerNewsClient client;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        when(factory.rxEnabled(anyBoolean())).thenReturn(factory);
        when(factory.create(anyString(), any())).thenReturn(restService);

        client = new HackerNewsClient(factory, sessionManager, favoriteManager);
        client.mIoScheduler = Schedulers.trampoline();
        client.mMainThreadScheduler = Schedulers.trampoline();
    }

    @Test
    public void testGetStoriesError() {
        when(restService.topStoriesRx()).thenReturn(Observable.error(new IOException("Network error")));

        client.getStories(ItemManager.TOP_FETCH_MODE, ItemManager.MODE_DEFAULT, storiesListener);

        verify(storiesListener).onError(errorCaptor.capture());
        assertEquals("Network error", errorCaptor.getValue());
    }

    @Test
    public void testGetStoriesErrorNullMessage() {
        when(restService.topStoriesRx()).thenReturn(Observable.error(new NullPointerException()));

        client.getStories(ItemManager.TOP_FETCH_MODE, ItemManager.MODE_DEFAULT, storiesListener);

        verify(storiesListener).onError(errorCaptor.capture());
        assertEquals("", errorCaptor.getValue());
    }

    @Test
    public void testGetItemError() {
        when(restService.itemRx("1")).thenReturn(Observable.error(new IOException("Network error")));
        when(sessionManager.isViewed("1")).thenReturn(Observable.just(false));
        when(favoriteManager.check("1")).thenReturn(Observable.just(false));

        client.getItem("1", ItemManager.MODE_DEFAULT, itemListener);

        verify(itemListener).onError(errorCaptor.capture());
        assertEquals("Network error", errorCaptor.getValue());
    }

    @Test
    public void testGetItemErrorNullMessage() {
        when(restService.itemRx("1")).thenReturn(Observable.error(new NullPointerException()));
        when(sessionManager.isViewed("1")).thenReturn(Observable.just(false));
        when(favoriteManager.check("1")).thenReturn(Observable.just(false));

        client.getItem("1", ItemManager.MODE_DEFAULT, itemListener);

        verify(itemListener).onError(errorCaptor.capture());
        assertEquals("", errorCaptor.getValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetStoriesSyncError() throws IOException {
        Call<int[]> mockCall = mock(Call.class);
        when(mockCall.execute()).thenThrow(new IOException("Network error"));
        when(restService.topStories()).thenReturn(mockCall);

        Item[] stories = client.getStories(ItemManager.TOP_FETCH_MODE, ItemManager.MODE_DEFAULT);

        assertNotNull(stories);
        assertEquals(0, stories.length);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetItemSyncError() throws IOException {
        Call<HackerNewsItem> mockCall = mock(Call.class);
        when(mockCall.execute()).thenThrow(new IOException("Network error"));
        when(restService.item("1")).thenReturn(mockCall);

        Item item = client.getItem("1", ItemManager.MODE_DEFAULT);

        assertNull(item);
    }
}
