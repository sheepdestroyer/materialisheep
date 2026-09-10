package io.github.sheepdestroyer.materialisheep.data;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AlgoliaClientTest {

    @Mock
    RestServiceFactory restServiceFactory;

    @Mock
    AlgoliaClient.RestService restService;

    @Mock
    ItemManager hackerNewsClient;

    @Mock
    ResponseListener<Item[]> responseListener;

    private AlgoliaClient algoliaClient;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(restServiceFactory.rxEnabled(true)).thenReturn(restServiceFactory);
        when(restServiceFactory.create(anyString(), eq(AlgoliaClient.RestService.class))).thenReturn(restService);

        algoliaClient = new AlgoliaClient(restServiceFactory, hackerNewsClient, Schedulers.trampoline());
    }

    @After
    public void tearDown() {
        AlgoliaClient.sSortByTime = true;
    }

    @Test
    public void testGetItem_delegatesToHackerNewsClient() {
        ResponseListener<Item> itemListener = mock(ResponseListener.class);
        algoliaClient.getItem("1", ItemManager.MODE_DEFAULT, itemListener);
        verify(hackerNewsClient).getItem("1", ItemManager.MODE_DEFAULT, itemListener);
    }

    @Test
    public void testGetItemSync_delegatesToHackerNewsClient() {
        Item mockItem = mock(Item.class);
        when(hackerNewsClient.getItem("1", ItemManager.MODE_DEFAULT)).thenReturn(mockItem);

        Item item = algoliaClient.getItem("1", ItemManager.MODE_DEFAULT);
        assertEquals(mockItem, item);
        verify(hackerNewsClient).getItem("1", ItemManager.MODE_DEFAULT);
    }

    @Test
    public void testGetStories_async_success() {
        AlgoliaClient.AlgoliaHits hits = new AlgoliaClient.AlgoliaHits();
        hits.hits = new AlgoliaClient.Hit[2];
        hits.hits[0] = new AlgoliaClient.Hit();
        hits.hits[0].objectID = "123";
        hits.hits[1] = new AlgoliaClient.Hit();
        hits.hits[1].objectID = "456";

        when(restService.searchByDateRx(anyString(), isNull())).thenReturn(Observable.just(hits));

        algoliaClient.getStories("filter", ItemManager.MODE_DEFAULT, responseListener);

        ArgumentCaptor<Item[]> captor = ArgumentCaptor.forClass(Item[].class);
        verify(responseListener).onResponse(captor.capture());

        Item[] items = captor.getValue();
        assertEquals(2, items.length);
        assertEquals("123", items[0].getId());
        assertEquals("456", items[1].getId());
        assertEquals(1, items[0].getRank());
        assertEquals(2, items[1].getRank());
    }

    @Test
    public void testGetStories_async_error() {
        when(restService.searchByDateRx(anyString(), isNull())).thenReturn(Observable.error(new RuntimeException("Test error")));

        algoliaClient.getStories("filter", ItemManager.MODE_DEFAULT, responseListener);

        verify(responseListener).onError("Test error");
        verify(responseListener, never()).onResponse(any());
    }

    @Test
    public void testGetStories_async_nullError() {
        when(restService.searchByDateRx(anyString(), isNull())).thenReturn(Observable.error(new Exception((String) null)));

        algoliaClient.getStories("filter", ItemManager.MODE_DEFAULT, responseListener);

        verify(responseListener).onError(isNull());
        verify(responseListener, never()).onResponse(any());
    }

    @Test
    public void testGetStories_async_nullListener() {
        algoliaClient.getStories("filter", ItemManager.MODE_DEFAULT, null);
        verify(restService, never()).searchByDateRx(anyString(), anyString());
    }

    @Test
    public void testGetStories_async_sortByRelevance() {
        AlgoliaClient.sSortByTime = false;
        AlgoliaClient.AlgoliaHits hits = new AlgoliaClient.AlgoliaHits();
        when(restService.searchRx(anyString(), isNull())).thenReturn(Observable.just(hits));

        algoliaClient.getStories("filter", ItemManager.MODE_DEFAULT, responseListener);

        verify(restService).searchRx(eq("filter"), isNull());
    }

    @Test
    public void testGetStories_sync_success() throws IOException {
        AlgoliaClient.AlgoliaHits hits = new AlgoliaClient.AlgoliaHits();
        hits.hits = new AlgoliaClient.Hit[1];
        hits.hits[0] = new AlgoliaClient.Hit();
        hits.hits[0].objectID = "789";

        Call<AlgoliaClient.AlgoliaHits> mockCall = mock(Call.class);
        Response<AlgoliaClient.AlgoliaHits> response = Response.success(hits);
        when(mockCall.execute()).thenReturn(response);
        when(restService.searchByDate(anyString(), isNull())).thenReturn(mockCall);

        Item[] items = algoliaClient.getStories("filter", ItemManager.MODE_DEFAULT);

        assertNotNull(items);
        assertEquals(1, items.length);
        assertEquals("789", items[0].getId());
    }

    @Test
    public void testGetStories_sync_sortByRelevance() throws IOException {
        AlgoliaClient.sSortByTime = false;
        AlgoliaClient.AlgoliaHits hits = new AlgoliaClient.AlgoliaHits();

        Call<AlgoliaClient.AlgoliaHits> mockCall = mock(Call.class);
        Response<AlgoliaClient.AlgoliaHits> response = Response.success(hits);
        when(mockCall.execute()).thenReturn(response);
        when(restService.search(anyString(), isNull())).thenReturn(mockCall);

        Item[] items = algoliaClient.getStories("filter", ItemManager.MODE_DEFAULT);

        assertNotNull(items);
        verify(restService).search(eq("filter"), isNull());
    }

    @Test
    public void testGetStories_sync_error() throws IOException {
        Call<AlgoliaClient.AlgoliaHits> mockCall = mock(Call.class);
        when(mockCall.execute()).thenThrow(new IOException("Network error"));
        when(restService.searchByDate(anyString(), isNull())).thenReturn(mockCall);

        Item[] items = algoliaClient.getStories("filter", ItemManager.MODE_DEFAULT);

        assertNotNull(items);
        assertEquals(0, items.length);
    }

    @Test
    public void testGetStories_sync_nullHits() throws IOException {
        Call<AlgoliaClient.AlgoliaHits> mockCall = mock(Call.class);
        Response<AlgoliaClient.AlgoliaHits> response = Response.success(null);
        when(mockCall.execute()).thenReturn(response);
        when(restService.searchByDate(anyString(), isNull())).thenReturn(mockCall);

        Item[] items = algoliaClient.getStories("filter", ItemManager.MODE_DEFAULT);

        assertNotNull(items);
        assertEquals(0, items.length);
    }

    @Test
    public void testGetStories_sync_emptyHits() throws IOException {
        AlgoliaClient.AlgoliaHits hits = new AlgoliaClient.AlgoliaHits();
        Call<AlgoliaClient.AlgoliaHits> mockCall = mock(Call.class);
        Response<AlgoliaClient.AlgoliaHits> response = Response.success(hits);
        when(mockCall.execute()).thenReturn(response);
        when(restService.searchByDate(anyString(), isNull())).thenReturn(mockCall);

        Item[] items = algoliaClient.getStories("filter", ItemManager.MODE_DEFAULT);

        assertNotNull(items);
        assertEquals(0, items.length);
    }
}
