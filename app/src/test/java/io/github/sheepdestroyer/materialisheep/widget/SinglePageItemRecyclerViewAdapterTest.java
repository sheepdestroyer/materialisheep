package io.github.sheepdestroyer.materialisheep.widget;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Spannable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;

import io.github.sheepdestroyer.materialisheep.data.Item;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class SinglePageItemRecyclerViewAdapterTest {

    @Test
    public void testSavedStateParceling() {
        ArrayList<Item> list = new ArrayList<>();
        TestParcelableItem item1 = new TestParcelableItem(1L);
        TestParcelableItem item2 = new TestParcelableItem(2L);
        list.add(item1);
        list.add(item2);

        SinglePageItemRecyclerViewAdapter.SavedState savedState =
                new SinglePageItemRecyclerViewAdapter.SavedState(list);

        savedState.expand(item1);

        Parcel parcel = Parcel.obtain();
        savedState.writeToParcel(parcel, 0);

        parcel.setDataPosition(0);

        SinglePageItemRecyclerViewAdapter.SavedState createdFromParcel =
                SinglePageItemRecyclerViewAdapter.SavedState.CREATOR.createFromParcel(parcel);

        parcel.recycle();

        // 2 items + 1 footer = 3 items total
        assertEquals(3, createdFromParcel.size());
        assertNotNull(createdFromParcel.get(0));
        assertEquals("1", createdFromParcel.get(0).getId());
        assertNotNull(createdFromParcel.get(1));
        assertEquals("2", createdFromParcel.get(1).getId());
        assertNull(createdFromParcel.get(2)); // footer is null

        assertTrue(createdFromParcel.isExpanded(item1));
    }

    public static class TestParcelableItem implements Item {
        public static final Parcelable.Creator<TestParcelableItem> CREATOR = new Parcelable.Creator<TestParcelableItem>() {
            @Override
            public TestParcelableItem createFromParcel(Parcel source) {
                return new TestParcelableItem(source.readLong());
            }

            @Override
            public TestParcelableItem[] newArray(int size) {
                return new TestParcelableItem[size];
            }
        };

        private final long id;

        public TestParcelableItem(long id) {
            this.id = id;
        }

        @Override
        public String getRawType() {
            return STORY_TYPE;
        }

        @Override
        public String getRawUrl() {
            return "http://example.com/" + id;
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
            return "Title " + id;
        }

        @Override
        public String getSource() {
            return "example.com";
        }

        @Override
        public void setFavorite(boolean favorite) {
        }

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
        public boolean isViewed() {
            return false;
        }

        @Override
        public void setIsViewed(boolean isViewed) {
        }

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
        public int getKidCount() {
            return 0;
        }

        @Override
        public Item[] getKidItems() {
            return new Item[0];
        }

        @Override
        public String getText() {
            return null;
        }

        @Override
        public String getBy() {
            return null;
        }

        @Override
        public long getTime() {
            return 0;
        }

        @Override
        public int getDescendants() {
            return 0;
        }

        @Override
        public boolean isCollapsed() {
            return false;
        }

        @Override
        public void setCollapsed(boolean collapsed) {
        }

        @Override
        public int getRank() {
            return 0;
        }

        @Override
        public void populate(Item info) {
        }

        @Override
        public long[] getKids() {
            return new long[0];
        }

        @Override
        public String getTitle() {
            return "Title " + id;
        }

        @Override
        public int getLastKidCount() {
            return 0;
        }

        @Override
        public void setLastKidCount(int lastKidCount) {
        }

        @Override
        public boolean hasNewKids() {
            return false;
        }

        @Override
        public int getLocalRevision() {
            return 0;
        }

        @Override
        public void setLocalRevision(int localRevision) {
        }

        @Override
        public void incrementScore() {
        }

        @Override
        public boolean isVoted() {
            return false;
        }

        @Override
        public boolean isPendingVoted() {
            return false;
        }

        @Override
        public void clearPendingVoted() {
        }

        @Override
        public long getNeighbour(int direction) {
            return 0;
        }

        @Override
        public Spannable getDisplayedAuthor(Context context, boolean linkify, int color) {
            return null;
        }

        @Override
        public Spannable getDisplayedTime(Context context) {
            return null;
        }

        @Override
        public CharSequence getDisplayedText() {
            return null;
        }

        @Override
        public boolean isContentExpanded() {
            return false;
        }

        @Override
        public void setContentExpanded(boolean expanded) {
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(id);
        }
    }
}
