package io.github.sheepdestroyer.materialisheep;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import androidx.test.core.app.ApplicationProvider;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class AdBlockerTest {

    @Test
    public void init_whenAssetManagerThrowsIOException_listRemainsEmpty() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();

        Field field = AdBlocker.class.getDeclaredField("AD_HOSTS");
        field.setAccessible(true);
        Set<String> adHosts = (Set<String>) field.get(null);

        // Wait for the background task from Application to finish
        int retries = 50;
        while (adHosts.isEmpty() && retries > 0) {
            Thread.sleep(100);
            retries--;
        }

        // Clear it now that it's done
        adHosts.clear();

        AssetManager mockAssetManager = mock(AssetManager.class);
        when(mockAssetManager.open(anyString())).thenThrow(new IOException("Mock exception"));

        Context wrapper = new ContextWrapper(context) {
            @Override
            public AssetManager getAssets() {
                return mockAssetManager;
            }
        };

        AdBlocker.init(wrapper, Schedulers.trampoline());

        assertTrue("AD_HOSTS should be empty when IOException occurs", adHosts.isEmpty());
    }
}
