package io.github.sheepdestroyer.materialisheep;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.view.MenuItem;
import com.google.android.material.textfield.TextInputLayout;
import java.lang.reflect.Method;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.fakes.RoboMenuItem;

@RunWith(RobolectricTestRunner.class)
public class SubmitActivityTest {
  private SubmitActivity activity;

  @Before
  public void setUp() {
    activity = Robolectric.buildActivity(SubmitActivity.class).create().get();
  }

  @Test
  public void testIsUrl() throws Exception {
    Method isUrlMethod = SubmitActivity.class.getDeclaredMethod("isUrl", String.class);
    isUrlMethod.setAccessible(true);

    // Valid URLs
    assertTrue((Boolean) isUrlMethod.invoke(activity, "http://example.com"));
    assertTrue((Boolean) isUrlMethod.invoke(activity, "https://example.com/path?query=1"));

    // Invalid URLs
    assertFalse((Boolean) isUrlMethod.invoke(activity, "invalid-url"));
    assertFalse((Boolean) isUrlMethod.invoke(activity, ""));
    assertFalse((Boolean) isUrlMethod.invoke(activity, "ftp://no-colon.com"));
    assertFalse((Boolean) isUrlMethod.invoke(activity, "file:///etc/hosts"));
    assertFalse((Boolean) isUrlMethod.invoke(activity, "javascript:alert(1)"));
    assertFalse((Boolean) isUrlMethod.invoke(activity, "ht tp://spaces.com"));
    assertFalse((Boolean) isUrlMethod.invoke(activity, (String) null));
  }

  @Test
  public void testValidationError() {
    MenuItem sendMenuItem = new RoboMenuItem(R.id.menu_send);
    activity.onOptionsItemSelected(sendMenuItem);

    TextInputLayout titleLayout = activity.findViewById(R.id.textinput_title);
    TextInputLayout contentLayout = activity.findViewById(R.id.textinput_content);

    assertEquals(
        activity.getString(R.string.title_required),
        titleLayout.getError() != null ? titleLayout.getError().toString() : null);
    assertEquals(
        activity.getString(R.string.url_text_required),
        contentLayout.getError() != null ? contentLayout.getError().toString() : null);
  }
}
