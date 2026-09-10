package io.github.sheepdestroyer.materialisheep.widget;

import android.app.Activity;
import android.os.Parcel;
import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import io.github.sheepdestroyer.materialisheep.MultiPaneListener;
import io.github.sheepdestroyer.materialisheep.data.FavoriteManager;
import io.github.sheepdestroyer.materialisheep.data.Item;
import io.github.sheepdestroyer.materialisheep.data.WebItem;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class StoryRecyclerViewAdapterTest {

    private StoryRecyclerViewAdapter mAdapter;

    @Mock
    private FavoriteManager mFavoriteManager;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        TestActivity activity = Robolectric.buildActivity(TestActivity.class).create().get();
        mAdapter = new StoryRecyclerViewAdapter(activity);
        mAdapter.mFavoriteManager = mFavoriteManager;
    }

    @Test
    public void testMapLookupAndSetItems() {
        TestStoryItem item1 = new TestStoryItem(100);
        TestStoryItem item2 = new TestStoryItem(200);

        mAdapter.setItems(new Item[]{item1, item2});

        assertEquals(2, mAdapter.getItemCount());
        assertEquals(0, mAdapter.getPosition(item1));
        assertEquals(1, mAdapter.getPosition(item2));

        mAdapter.toggleSave("100");
        verify(mFavoriteManager).add(mAdapter.mContext, item1);

        mAdapter.toggleSave("200");
        verify(mFavoriteManager).add(mAdapter.mContext, item2);
    }

    @Test
    public void testVoteStateUpdate() {
        TestStoryItem item = new TestStoryItem(300);
        mAdapter.setItems(new Item[]{item});

        assertEquals(0, item.getScore());
        assertEquals(false, item.isVoted());

        item.incrementScore();
        assertEquals(1, item.getScore());
        assertEquals(true, item.isVoted());
        assertEquals(true, item.isPendingVoted());

        item.clearPendingVoted();
        assertEquals(false, item.isPendingVoted());
    }

    static class TestActivity extends Activity implements MultiPaneListener {
        @Override
        public void onItemSelected(WebItem item) {
        }

        @Override
        public WebItem getSelectedItem() {
            return null;
        }

        @Override
        public boolean isMultiPane() {
            return false;
        }
    }

    static class TestStoryItem implements Item {
        private final long id;
        private boolean favorite;
        private int score;
        private boolean voted;
        private boolean pendingVoted;

        TestStoryItem(long id) {
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
            this.favorite = favorite;
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
            return favorite;
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
            return score;
        }

        @Override
        public void incrementScore() {
            score++;
            voted = true;
            pendingVoted = true;
        }

        @Override
        public boolean isVoted() {
            return voted;
        }

        @Override
        public boolean isPendingVoted() {
            return pendingVoted;
        }

        @Override
        public void clearPendingVoted() {
            pendingVoted = false;
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
