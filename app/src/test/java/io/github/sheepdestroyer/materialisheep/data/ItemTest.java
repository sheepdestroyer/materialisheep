package io.github.sheepdestroyer.materialisheep.data;

import android.os.Parcel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
public class ItemTest {
    @Test
    public void testItem() {
        TestItem item = new TestItem(1);
        assertEquals("1", item.getId());
        assertEquals(1, item.getLongId());
        assertEquals("http://example.com/1", item.getUrl());
        assertEquals(true, item.isStoryType());
        assertNotNull(item.getDisplayedTitle());
    }

    // Helper class to implement abstract Item
    static class TestItem implements Item {
        private final long id;

        TestItem(long id) {
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
            return "Title";
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

        public boolean isJobType() {
            return false;
        }

        public boolean isPollType() {
            return false;
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

        public boolean hasKidCount() {
            return false;
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
            return "Title";
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
        public android.text.Spannable getDisplayedAuthor(android.content.Context context, boolean linkify, int color) {
            return null;
        }

        @Override
        public android.text.Spannable getDisplayedTime(android.content.Context context) {
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
        }
    }
}
