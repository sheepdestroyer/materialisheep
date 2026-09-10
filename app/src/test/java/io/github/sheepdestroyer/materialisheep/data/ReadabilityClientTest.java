package io.github.sheepdestroyer.materialisheep.data;

import android.content.Context;
import android.content.res.AssetManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;

import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
public class ReadabilityClientTest {

    @Test
    public void testInit_assetException() throws IOException {
        Context context = mock(Context.class);
        AssetManager assetManager = mock(AssetManager.class);
        LocalCache cache = mock(LocalCache.class);

        when(context.getAssets()).thenReturn(assetManager);
        when(assetManager.open(eq("Readability.js"))).thenThrow(new IOException("Test exception"));

        Scheduler ioScheduler = Schedulers.trampoline();
        Scheduler mainThreadScheduler = Schedulers.trampoline();

        // This should handle the IOException internally and not crash
        ReadabilityClient.Impl client = new ReadabilityClient.Impl(context, cache, ioScheduler, mainThreadScheduler);

        assertNotNull(client);
        verify(assetManager).open(eq("Readability.js"));
    }
}
