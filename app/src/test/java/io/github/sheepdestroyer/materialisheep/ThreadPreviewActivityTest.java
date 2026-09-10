package io.github.sheepdestroyer.materialisheep;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import androidx.test.core.app.ApplicationProvider;
import io.github.sheepdestroyer.materialisheep.data.Item;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

@RunWith(RobolectricTestRunner.class)
public class ThreadPreviewActivityTest {

  @Test
  public void testWithoutExtraItem_finishesActivity() {
    Context context = ApplicationProvider.getApplicationContext();
    Intent intent = new Intent(context, ThreadPreviewActivity.class);
    ActivityController<ThreadPreviewActivity> controller =
        Robolectric.buildActivity(ThreadPreviewActivity.class, intent);
    ThreadPreviewActivity activity = controller.get();
    controller.create();

    assertTrue(activity.isFinishing());
  }

  @Test
  public void testWithExtraItem_doesNotFinish() {
    Context context = ApplicationProvider.getApplicationContext();
    TestItem item = new TestItem(12345L);
    Intent intent =
        new Intent(context, ThreadPreviewActivity.class)
            .putExtra(ThreadPreviewActivity.EXTRA_ITEM, item);

    ActivityController<ThreadPreviewActivity> controller =
        Robolectric.buildActivity(ThreadPreviewActivity.class, intent);
    ThreadPreviewActivity activity = controller.get();
    controller.create();

    assertFalse(activity.isFinishing());
  }

  public static class TestItem implements Item {
    private final long id;

    public TestItem(long id) {
      this.id = id;
    }

    protected TestItem(Parcel in) {
      id = in.readLong();
    }

    public static final Creator<TestItem> CREATOR =
        new Creator<TestItem>() {
          @Override
          public TestItem createFromParcel(Parcel in) {
            return new TestItem(in);
          }

          @Override
          public TestItem[] newArray(int size) {
            return new TestItem[size];
          }
        };

    @Override
    public int describeContents() {
      return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
      dest.writeLong(id);
    }

    @Override
    public void populate(Item info) {}

    @Override
    public String getRawType() {
      return STORY_TYPE;
    }

    @Override
    public String getRawUrl() {
      return "http://example.com/" + id;
    }

    @Override
    public long[] getKids() {
      return new long[0];
    }

    @Override
    public String getBy() {
      return "user";
    }

    @Override
    public long getTime() {
      return 0;
    }

    @Override
    public String getTitle() {
      return "Title";
    }

    @Override
    public String getText() {
      return null;
    }

    @Override
    public int getKidCount() {
      return 0;
    }

    @Override
    public int getLastKidCount() {
      return 0;
    }

    @Override
    public void setLastKidCount(int lastKidCount) {}

    @Override
    public boolean hasNewKids() {
      return false;
    }

    @Override
    public Item[] getKidItems() {
      return new Item[0];
    }

    @Override
    public int getLocalRevision() {
      return 0;
    }

    @Override
    public void setLocalRevision(int localRevision) {}

    @Override
    public int getDescendants() {
      return 0;
    }

    @Override
    public boolean isViewed() {
      return false;
    }

    @Override
    public void setIsViewed(boolean isViewed) {}

    @Override
    public int getLevel() {
      return 0;
    }

    @Override
    public String getParent() {
      return null;
    }

    @Override
    public Item getParentItem() {
      return null;
    }

    @Override
    public boolean isDeleted() {
      return false;
    }

    @Override
    public boolean isDead() {
      return false;
    }

    @Override
    public int getScore() {
      return 0;
    }

    @Override
    public void incrementScore() {}

    @Override
    public boolean isVoted() {
      return false;
    }

    @Override
    public boolean isPendingVoted() {
      return false;
    }

    @Override
    public void clearPendingVoted() {}

    @Override
    public boolean isCollapsed() {
      return false;
    }

    @Override
    public void setCollapsed(boolean collapsed) {}

    @Override
    public int getRank() {
      return 0;
    }

    @Override
    public boolean isContentExpanded() {
      return false;
    }

    @Override
    public void setContentExpanded(boolean expanded) {}

    @Override
    public long getNeighbour(int direction) {
      return 0;
    }

    @Override
    public CharSequence getDisplayedText() {
      return null;
    }

    @Override
    public String getId() {
      return String.valueOf(id);
    }

    @Override
    public long getLongId() {
      return id;
    }

    @Override
    public String getUrl() {
      return "http://example.com/" + id;
    }

    @Override
    public String getDisplayedTitle() {
      return "Title";
    }

    @Override
    public String getSource() {
      return "example.com";
    }

    @Override
    public void setFavorite(boolean favorite) {}

    @Override
    public String getType() {
      return STORY_TYPE;
    }

    @Override
    public boolean isStoryType() {
      return true;
    }

    @Override
    public boolean isFavorite() {
      return false;
    }

    @Override
    public android.text.Spannable getDisplayedAuthor(
        Context context, boolean linkify, int color) {
      return null;
    }

    @Override
    public android.text.Spannable getDisplayedTime(Context context) {
      return null;
    }
  }
}
