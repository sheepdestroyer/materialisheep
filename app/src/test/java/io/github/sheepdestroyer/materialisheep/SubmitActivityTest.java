package io.github.sheepdestroyer.materialisheep;

import static org.junit.Assert.assertEquals;

import android.content.Intent;
import android.widget.TextView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

@RunWith(RobolectricTestRunner.class)
public class SubmitActivityTest {

  @Test
  public void testExtractUrlFromIntentExtraText() {
    Intent intent = new Intent();
    intent.putExtra(Intent.EXTRA_TEXT, "Article Title: https://example.com/article");

    try (ActivityController<SubmitActivity> controller =
        Robolectric.buildActivity(SubmitActivity.class, intent)) {
      controller.create().start().resume();
      SubmitActivity activity = controller.get();

      TextView titleEditText = activity.findViewById(R.id.edittext_title);
      TextView contentEditText = activity.findViewById(R.id.edittext_content);

      assertEquals("Article Title", titleEditText.getText().toString());
      assertEquals("https://example.com/article", contentEditText.getText().toString());
    }
  }
}
