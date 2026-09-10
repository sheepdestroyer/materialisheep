package io.github.sheepdestroyer.materialisheep.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Response;

@RunWith(RobolectricTestRunner.class)
public class AlgoliaClientTest {

    @Mock
    RestServiceFactory mFactory;
    @Mock
    AlgoliaClient.RestService mRestService;
    @Mock
    ItemManager mHackerNewsClient;
    @Mock
    Call<AlgoliaClient.AlgoliaHits> mCall;
    @Mock
    ResponseListener<Item[]> mItemArrayListener;
    @Mock
    ResponseListener<Item> mItemListener;

    private AlgoliaClient mClient;
    private AutoCloseable mMockitoCloseable;

    @Before
    public void setUp() {
        mMockitoCloseable = MockitoAnnotations.openMocks(this);
        AlgoliaClient.sSortByTime = true;

        when(mFactory.rxEnabled(anyBoolean())).thenReturn(mFactory);
        when(mFactory.create(eq("https://hn.algolia.com/api/v1/"), eq(AlgoliaClient.RestService.class)))
                .thenReturn(mRestService);

        mClient = new AlgoliaClient(mFactory, mHackerNewsClient, Schedulers.trampoline());
    }

    @After
    public void tearDown() throws Exception {
        AlgoliaClient.sSortByTime = true;
        if (mMockitoCloseable != null) {
            mMockitoCloseable.close();
        }
    }

    @Test
    public void testGetStoriesSyncSortByTimeTrue() throws IOException {
        AlgoliaClient.sSortByTime = true;
        AlgoliaClient.AlgoliaHits hits = createHits("101", "102");

        when(mRestService.searchByDate("query", null)).thenReturn(mCall);
        when(mCall.execute()).thenReturn(Response.success(hits));

        Item[] items = mClient.getStories("query", ItemManager.MODE_DEFAULT);

        verify(mRestService).searchByDate("query", null);
        verify(mRestService, never()).search("query", null);
        assertNotNull(items);
        assertEquals(2, items.length);
        assertEquals("101", items[0].getId());
        assertEquals(1, items[0].getRank());
        assertEquals("102", items[1].getId());
        assertEquals(2, items[1].getRank());
    }

    @Test
    public void testGetStoriesSyncSortByTimeFalse() throws IOException {
        AlgoliaClient.sSortByTime = false;
        AlgoliaClient.AlgoliaHits hits = createHits("201");

        when(mRestService.search("query", null)).thenReturn(mCall);
        when(mCall.execute()).thenReturn(Response.success(hits));

        Item[] items = mClient.getStories("query", ItemManager.MODE_DEFAULT);

        verify(mRestService).search("query", null);
        verify(mRestService, never()).searchByDate("query", null);
        assertNotNull(items);
        assertEquals(1, items.length);
        assertEquals("201", items[0].getId());
        assertEquals(1, items[0].getRank());
    }

    @Test
    public void testGetStoriesSyncIOExceptionFallback() throws IOException {
        when(mRestService.searchByDate("query", null)).thenReturn(mCall);
        when(mCall.execute()).thenThrow(new IOException("Network failure"));

        Item[] items = mClient.getStories("query", ItemManager.MODE_DEFAULT);

        assertNotNull(items);
        assertEquals(0, items.length);
    }

    @Test
    public void testGetStoriesSyncNullHitsFallback() throws IOException {
        when(mRestService.searchByDate("query", null)).thenReturn(mCall);
        when(mCall.execute()).thenReturn(Response.success(null));

        Item[] items = mClient.getStories("query", ItemManager.MODE_DEFAULT);

        assertNotNull(items);
        assertEquals(0, items.length);

        AlgoliaClient.AlgoliaHits emptyHits = new AlgoliaClient.AlgoliaHits();
        emptyHits.hits = null;
        when(mCall.execute()).thenReturn(Response.success(emptyHits));

        Item[] items2 = mClient.getStories("query", ItemManager.MODE_DEFAULT);
        assertNotNull(items2);
        assertEquals(0, items2.length);
    }

    @Test
    public void testGetStoriesAsyncSortByTimeTrueSuccess() {
        AlgoliaClient.sSortByTime = true;
        AlgoliaClient.AlgoliaHits hits = createHits("301", "302");

        when(mRestService.searchByDateRx("rxQuery", null)).thenReturn(Observable.just(hits));

        mClient.getStories("rxQuery", ItemManager.MODE_DEFAULT, mItemArrayListener);

        verify(mRestService).searchByDateRx("rxQuery", null);
        verify(mRestService, never()).searchRx("rxQuery", null);

        ArgumentCaptor<Item[]> captor = ArgumentCaptor.forClass(Item[].class);
        verify(mItemArrayListener).onResponse(captor.capture());

        Item[] result = captor.getValue();
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("301", result[0].getId());
        assertEquals(1, result[0].getRank());
        assertEquals("302", result[1].getId());
        assertEquals(2, result[1].getRank());
    }

    @Test
    public void testGetStoriesAsyncSortByTimeFalseSuccess() {
        AlgoliaClient.sSortByTime = false;
        AlgoliaClient.AlgoliaHits hits = createHits("401");

        when(mRestService.searchRx("rxQuery", null)).thenReturn(Observable.just(hits));

        mClient.getStories("rxQuery", ItemManager.MODE_DEFAULT, mItemArrayListener);

        verify(mRestService).searchRx("rxQuery", null);
        verify(mRestService, never()).searchByDateRx("rxQuery", null);

        ArgumentCaptor<Item[]> captor = ArgumentCaptor.forClass(Item[].class);
        verify(mItemArrayListener).onResponse(captor.capture());

        Item[] result = captor.getValue();
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("401", result[0].getId());
    }

    @Test
    public void testGetStoriesAsyncError() {
        when(mRestService.searchByDateRx("rxQuery", null))
                .thenReturn(Observable.error(new RuntimeException("Rx error")));

        mClient.getStories("rxQuery", ItemManager.MODE_DEFAULT, mItemArrayListener);

        verify(mItemArrayListener).onError("Rx error");
    }

    @Test
    public void testGetStoriesAsyncNullMessageError() {
        when(mRestService.searchByDateRx("rxQuery", null))
                .thenReturn(Observable.error(new NullPointerException()));

        mClient.getStories("rxQuery", ItemManager.MODE_DEFAULT, mItemArrayListener);

        verify(mItemArrayListener).onError(isNull());
    }

    @Test
    public void testGetStoriesAsyncNullListener() {
        when(mRestService.searchByDateRx("rxQuery", null))
                .thenReturn(Observable.just(createHits("501")));

        mClient.getStories("rxQuery", ItemManager.MODE_DEFAULT, null);
    }

    @Test
    public void testGetItemSyncDelegation() {
        Item mockItem = mock(Item.class);
        when(mHackerNewsClient.getItem("123", ItemManager.MODE_DEFAULT)).thenReturn(mockItem);

        Item result = mClient.getItem("123", ItemManager.MODE_DEFAULT);

        assertEquals(mockItem, result);
        verify(mHackerNewsClient).getItem("123", ItemManager.MODE_DEFAULT);
    }

    @Test
    public void testGetItemAsyncDelegation() {
        mClient.getItem("123", ItemManager.MODE_DEFAULT, mItemListener);

        verify(mHackerNewsClient).getItem("123", ItemManager.MODE_DEFAULT, mItemListener);
    }

    private AlgoliaClient.AlgoliaHits createHits(String... objectIds) {
        AlgoliaClient.AlgoliaHits algoliaHits = new AlgoliaClient.AlgoliaHits();
        if (objectIds != null) {
            algoliaHits.hits = new AlgoliaClient.Hit[objectIds.length];
            for (int i = 0; i < objectIds.length; i++) {
                algoliaHits.hits[i] = new AlgoliaClient.Hit();
                algoliaHits.hits[i].objectID = objectIds[i];
            }
        }
        return algoliaHits;
    }
}
