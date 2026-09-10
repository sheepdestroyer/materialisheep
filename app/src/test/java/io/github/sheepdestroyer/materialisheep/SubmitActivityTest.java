package io.github.sheepdestroyer.materialisheep;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Robolectric;
import java.lang.reflect.Method;

@RunWith(RobolectricTestRunner.class)
public class SubmitActivityTest {
    private SubmitActivity activity;

    @Before
    public void setUp() {
        activity = Robolectric.buildActivity(SubmitActivity.class).get();
    }

    @Test
    public void testIsUrl() throws Exception {
        Method isUrlMethod = SubmitActivity.class.getDeclaredMethod("isUrl", String.class);
        isUrlMethod.setAccessible(true);

        // Happy paths
        assertTrue((Boolean) isUrlMethod.invoke(activity, "http://example.com"));
        assertTrue((Boolean) isUrlMethod.invoke(activity, "https://example.com/path?query=1"));

        // Invalid URLs triggering the exception path
        assertFalse((Boolean) isUrlMethod.invoke(activity, "invalid-url"));
        assertFalse((Boolean) isUrlMethod.invoke(activity, ""));
        assertFalse((Boolean) isUrlMethod.invoke(activity, "ftp//no-colon.com"));
        assertFalse((Boolean) isUrlMethod.invoke(activity, "ht tp://spaces.com"));
        assertFalse((Boolean) isUrlMethod.invoke(activity, (String) null));
    }
}
