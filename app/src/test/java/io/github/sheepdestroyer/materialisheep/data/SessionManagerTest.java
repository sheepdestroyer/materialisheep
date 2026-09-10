package io.github.sheepdestroyer.materialisheep.data;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import io.reactivex.rxjava3.schedulers.Schedulers;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class SessionManagerTest {

    @Mock
    LocalCache cache;

    private SessionManager sessionManager;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        sessionManager = new SessionManager(Schedulers.trampoline(), cache);
    }

    @Test
    public void testIsViewedNull() {
        sessionManager.isViewed(null).test().assertValue(false);
    }

    @Test
    public void testIsViewedEmpty() {
        sessionManager.isViewed("").test().assertValue(false);
    }

    @Test
    public void testIsViewedTrue() {
        when(cache.isViewed("123")).thenReturn(true);
        sessionManager.isViewed("123").test().assertValue(true);
    }

    @Test
    public void testIsViewedFalse() {
        when(cache.isViewed("123")).thenReturn(false);
        sessionManager.isViewed("123").test().assertValue(false);
    }

    @Test
    public void testViewNull() {
        sessionManager.view(null);
        verify(cache, never()).setViewed(anyString());
    }

    @Test
    public void testViewEmpty() {
        sessionManager.view("");
        verify(cache, never()).setViewed(anyString());
    }

    @Test
    public void testViewValid() {
        sessionManager.view("123");
        verify(cache).setViewed("123");
    }

    @Test
    public void testViewError() {
        doThrow(new RuntimeException("Test exception")).when(cache).setViewed("123");
        // This should trigger the RxJava error handler which logs the error.
        // With Robolectric, Log.e won't crash the test.
        sessionManager.view("123");
        verify(cache).setViewed("123");
    }
}
