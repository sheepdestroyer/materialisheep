package io.github.sheepdestroyer.materialisheep.data;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.os.Parcel;
import android.text.Spannable;

import androidx.test.core.app.ApplicationProvider;

import io.github.sheepdestroyer.materialisheep.Navigable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class HackerNewsItemTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void testEqualsAndHashCode() {
        HackerNewsItem item1 = new HackerNewsItem(100L);
        HackerNewsItem item2 = new HackerNewsItem(100L);
        HackerNewsItem item3 = new HackerNewsItem(200L);

        assertEquals(item1, item2);
        assertEquals(item1.hashCode(), item2.hashCode());

        assertFalse(item1.equals(item3));
        assertFalse(item1.equals(null));
        assertFalse(item1.equals("Not a HackerNewsItem"));
    }

    @Test
    public void testParcelable() {
        HackerNewsItem original = new HackerNewsItem(12345L);
        original.populate(new ItemTest.TestItem(12345L) {
            @Override
            public String getTitle() {
                return "Sample Title";
            }

            @Override
            public long getTime() {
                return 1600000000L;
            }

            @Override
            public String getBy() {
                return "author";
            }

            @Override
            public long[] getKids() {
                return new long[]{101L, 102L};
            }

            @Override
            public String getRawUrl() {
                return "https://example.com/story";
            }

            @Override
            public String getText() {
                return "Sample text";
            }

            @Override
            public String getRawType() {
                return Item.STORY_TYPE;
            }

            @Override
            public int getDescendants() {
                return 5;
            }

            @Override
            public String getParent() {
                return "999";
            }

            @Override
            public boolean isDeleted() {
                return true;
            }

            @Override
            public boolean isDead() {
                return true;
            }

            @Override
            public int getScore() {
                return 42;
            }

            @Override
            public boolean isViewed() {
                return true;
            }

            @Override
            public boolean isFavorite() {
                return true;
            }
        });

        original.setCollapsed(true);
        original.setContentExpanded(true);
        original.incrementScore(); // sets score to 43, voted = true, pendingVoted = true

        Parcel parcel = Parcel.obtain();
        original.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        HackerNewsItem unparceled = HackerNewsItem.CREATOR.createFromParcel(parcel);
        parcel.recycle();

        assertEquals(original.getLongId(), unparceled.getLongId());
        assertEquals(original.getId(), unparceled.getId());
        assertEquals(original.getTitle(), unparceled.getTitle());
        assertEquals(original.getTime(), unparceled.getTime());
        assertEquals(original.getBy(), unparceled.getBy());
        assertArrayEquals(original.getKids(), unparceled.getKids());
        assertEquals(original.getRawUrl(), unparceled.getRawUrl());
        assertEquals(original.getText(), unparceled.getText());
        assertEquals(original.getRawType(), unparceled.getRawType());
        assertEquals(original.isFavorite(), unparceled.isFavorite());
        assertEquals(original.getDescendants(), unparceled.getDescendants());
        assertEquals(original.getScore(), unparceled.getScore());
        assertEquals(original.isViewed(), unparceled.isViewed());
        assertEquals(original.getLocalRevision(), unparceled.getLocalRevision());
        assertEquals(original.getLevel(), unparceled.getLevel());
        assertEquals(original.isDead(), unparceled.isDead());
        assertEquals(original.isDeleted(), unparceled.isDeleted());
        assertEquals(original.isCollapsed(), unparceled.isCollapsed());
        assertEquals(original.isContentExpanded(), unparceled.isContentExpanded());
        assertEquals(original.getRank(), unparceled.getRank());
        assertEquals(original.getLastKidCount(), unparceled.getLastKidCount());
        assertEquals(original.hasNewKids(), unparceled.hasNewKids());
        assertEquals(original.getParent(), unparceled.getParent());
        assertEquals(original.isVoted(), unparceled.isVoted());
        assertEquals(original.isPendingVoted(), unparceled.isPendingVoted());

        HackerNewsItem[] array = HackerNewsItem.CREATOR.newArray(5);
        assertEquals(5, array.length);
    }

    @Test
    public void testDescribeContents() {
        HackerNewsItem item = new HackerNewsItem(100L);
        assertEquals(0, item.describeContents());
    }

    @Test
    public void testTypeAndIsStoryType() {
        HackerNewsItem item = new HackerNewsItem(1L);
        assertEquals(Item.STORY_TYPE, item.getType());
        assertTrue(item.isStoryType());

        item.populate(new ItemTest.TestItem(1L) {
            @Override
            public String getRawType() {
                return Item.JOB_TYPE;
            }

            @Override
            public String getParent() {
                return "0";
            }
        });
        assertEquals(Item.JOB_TYPE, item.getType());
        assertTrue(item.isStoryType());

        item.populate(new ItemTest.TestItem(1L) {
            @Override
            public String getRawType() {
                return Item.POLL_TYPE;
            }

            @Override
            public String getParent() {
                return "0";
            }
        });
        assertEquals(Item.POLL_TYPE, item.getType());
        assertTrue(item.isStoryType());

        item.populate(new ItemTest.TestItem(1L) {
            @Override
            public String getRawType() {
                return Item.COMMENT_TYPE;
            }

            @Override
            public String getParent() {
                return "0";
            }
        });
        assertEquals(Item.COMMENT_TYPE, item.getType());
        assertFalse(item.isStoryType());
    }

    @Test
    public void testDisplayedTitle() {
        HackerNewsItem storyItem = new HackerNewsItem(1L);
        storyItem.populate(new ItemTest.TestItem(1L) {
            @Override
            public String getTitle() {
                return "Story Title";
            }

            @Override
            public String getRawType() {
                return Item.STORY_TYPE;
            }

            @Override
            public String getParent() {
                return "0";
            }
        });
        assertEquals("Story Title", storyItem.getDisplayedTitle());

        HackerNewsItem commentItem = new HackerNewsItem(2L);
        commentItem.populate(new ItemTest.TestItem(2L) {
            @Override
            public String getText() {
                return "Comment Text";
            }

            @Override
            public String getRawType() {
                return Item.COMMENT_TYPE;
            }

            @Override
            public String getParent() {
                return "0";
            }
        });
        assertEquals("Comment Text", commentItem.getDisplayedTitle());
    }

    @Test
    public void testKidCountAndNewKids() {
        HackerNewsItem item = new HackerNewsItem(1L);
        assertEquals(0, item.getKidCount());

        item.setLastKidCount(2);

        item.populate(new ItemTest.TestItem(1L) {
            @Override
            public int getDescendants() {
                return 5;
            }

            @Override
            public long[] getKids() {
                return new long[]{10L, 11L, 12L};
            }

            @Override
            public String getParent() {
                return "0";
            }
        });

        assertEquals(5, item.getKidCount());
        assertEquals(5, item.getLastKidCount());
        assertTrue(item.hasNewKids());

        // Test kid count fallback to kids.length when descendants <= 0
        HackerNewsItem itemNoDescendants = new HackerNewsItem(2L);
        itemNoDescendants.populate(new ItemTest.TestItem(2L) {
            @Override
            public int getDescendants() {
                return 0;
            }

            @Override
            public long[] getKids() {
                return new long[]{20L, 21L};
            }

            @Override
            public String getParent() {
                return "0";
            }
        });
        assertEquals(2, itemNoDescendants.getKidCount());
    }

    @Test
    public void testUrlAndSource() {
        HackerNewsItem storyItem = new HackerNewsItem(100L);
        storyItem.populate(new ItemTest.TestItem(100L) {
            @Override
            public String getRawUrl() {
                return "https://news.ycombinator.com/item?id=100";
            }

            @Override
            public String getRawType() {
                return Item.STORY_TYPE;
            }

            @Override
            public String getParent() {
                return "0";
            }
        });
        assertEquals("https://news.ycombinator.com/item?id=100", storyItem.getUrl());
        assertEquals("news.ycombinator.com", storyItem.getSource());

        HackerNewsItem commentItem = new HackerNewsItem(200L);
        commentItem.populate(new ItemTest.TestItem(200L) {
            @Override
            public String getRawType() {
                return Item.COMMENT_TYPE;
            }

            @Override
            public String getParent() {
                return "0";
            }
        });
        assertEquals(String.format(HackerNewsClient.WEB_ITEM_PATH, "200"), commentItem.getUrl());

        HackerNewsItem storyNoUrl = new HackerNewsItem(300L);
        assertEquals(String.format(HackerNewsClient.WEB_ITEM_PATH, "300"), storyNoUrl.getUrl());
    }

    @Test
    public void testKidItems() {
        HackerNewsItem parentItem = new HackerNewsItem(1L);

        // Kids is empty or null
        assertEquals(0, parentItem.getKidItems().length);

        parentItem.populate(new ItemTest.TestItem(1L) {
            @Override
            public long[] getKids() {
                return new long[]{10L, 20L, 30L};
            }

            @Override
            public String getParent() {
                return "0";
            }
        });

        HackerNewsItem[] kidItems = parentItem.getKidItems();
        assertEquals(3, kidItems.length);

        // Check kid 0
        assertEquals("10", kidItems[0].getId());
        assertEquals(1, kidItems[0].getLevel());
        assertEquals(1, kidItems[0].getRank());
        assertEquals(0L, kidItems[0].getNeighbour(Navigable.DIRECTION_UP));
        assertEquals(20L, kidItems[0].getNeighbour(Navigable.DIRECTION_DOWN));

        // Check kid 1
        assertEquals("20", kidItems[1].getId());
        assertEquals(2, kidItems[1].getRank());
        assertEquals(10L, kidItems[1].getNeighbour(Navigable.DIRECTION_UP));
        assertEquals(30L, kidItems[1].getNeighbour(Navigable.DIRECTION_DOWN));

        // Check kid 2
        assertEquals("30", kidItems[2].getId());
        assertEquals(3, kidItems[2].getRank());
        assertEquals(20L, kidItems[2].getNeighbour(Navigable.DIRECTION_UP));
        assertEquals(0L, kidItems[2].getNeighbour(Navigable.DIRECTION_DOWN));

        // Repeat call should return cached array
        assertTrue(kidItems == parentItem.getKidItems());
    }

    @Test
    public void testParentItem() {
        HackerNewsItem itemNoParent = new HackerNewsItem(1L);
        assertNull(itemNoParent.getParentItem());

        HackerNewsItem itemWithParent = new HackerNewsItem(2L);
        itemWithParent.populate(new ItemTest.TestItem(2L) {
            @Override
            public String getParent() {
                return "50";
            }
        });

        Item parent = itemWithParent.getParentItem();
        assertNotNull(parent);
        assertEquals("50", parent.getId());
        // Lazy creation check
        assertTrue(parent == itemWithParent.getParentItem());
    }

    @Test
    public void testVotingAndScore() {
        HackerNewsItem item = new HackerNewsItem(1L);
        item.populate(new ItemTest.TestItem(1L) {
            @Override
            public int getScore() {
                return 10;
            }

            @Override
            public String getParent() {
                return "0";
            }
        });

        assertEquals(10, item.getScore());
        assertFalse(item.isVoted());
        assertFalse(item.isPendingVoted());

        item.incrementScore();
        assertEquals(11, item.getScore());
        assertTrue(item.isVoted());
        assertTrue(item.isPendingVoted());

        item.clearPendingVoted();
        assertFalse(item.isPendingVoted());
        assertTrue(item.isVoted());
    }

    @Test
    public void testGetNeighbour() {
        HackerNewsItem item = new HackerNewsItem(100L);
        item.populate(new ItemTest.TestItem(100L) {
            @Override
            public String getParent() {
                return "50";
            }

            @Override
            public long[] getKids() {
                return new long[]{200L, 300L};
            }
        });

        // Direction LEFT when level <= 1 returns 0
        assertEquals(0L, item.getNeighbour(Navigable.DIRECTION_LEFT));

        // Direction RIGHT returns first kid
        assertEquals(200L, item.getNeighbour(Navigable.DIRECTION_RIGHT));

        // Test kid item level > 1 for DIRECTION_LEFT
        HackerNewsItem[] kids = item.getKidItems();
        // level of kid item is 1
        assertEquals(1, kids[0].getLevel());
        assertEquals(0L, kids[0].getNeighbour(Navigable.DIRECTION_LEFT));

        // Get kids of kid 0 to test level 2
        kids[0].populate(new ItemTest.TestItem(200L) {
            @Override
            public String getParent() {
                return "100";
            }

            @Override
            public long[] getKids() {
                return new long[]{400L};
            }
        });
        HackerNewsItem grandKidItem = kids[0].getKidItems()[0]; // level = 2
        grandKidItem.populate(new ItemTest.TestItem(400L) {
            @Override
            public String getParent() {
                return "200";
            }
        });
        assertEquals(2, grandKidItem.getLevel());
        assertEquals(200L, grandKidItem.getNeighbour(Navigable.DIRECTION_LEFT)); // parent id of grandKidItem is 200

        // Invalid direction returns 0
        assertEquals(0L, item.getNeighbour(999));
    }

    @Test
    public void testDisplayedAuthorTimeText() {
        HackerNewsItem item = new HackerNewsItem(1L);
        item.populate(new ItemTest.TestItem(1L) {
            @Override
            public String getBy() {
                return "testuser";
            }

            @Override
            public long getTime() {
                return 1600000000L;
            }

            @Override
            public String getText() {
                return "<p>Hello <b>World</b></p>";
            }

            @Override
            public boolean isDead() {
                return true;
            }

            @Override
            public boolean isDeleted() {
                return true;
            }

            @Override
            public String getParent() {
                return "0";
            }
        });

        Spannable authorSpannable = item.getDisplayedAuthor(context, true, 0);
        assertNotNull(authorSpannable);
        assertTrue(authorSpannable.toString().contains("testuser"));

        // Call again to hit cached branch
        assertEquals(authorSpannable, item.getDisplayedAuthor(context, true, 0));

        Spannable timeSpannable = item.getDisplayedTime(context);
        assertNotNull(timeSpannable);
        assertTrue(timeSpannable.toString().contains(context.getString(io.github.sheepdestroyer.materialisheep.R.string.dead_prefix)));

        CharSequence text = item.getDisplayedText();
        assertNotNull(text);
        assertTrue(text.toString().contains("Hello World"));

        // Test empty author
        HackerNewsItem itemNoAuthor = new HackerNewsItem(2L);
        Spannable noAuthorSpannable = itemNoAuthor.getDisplayedAuthor(context, false, 0);
        assertEquals("", noAuthorSpannable.toString());
    }

    @Test
    public void testPreload() {
        HackerNewsItem item = new HackerNewsItem(1L);
        item.populate(new ItemTest.TestItem(1L) {
            @Override
            public String getText() {
                return "Test text";
            }

            @Override
            public long[] getKids() {
                return new long[]{10L};
            }

            @Override
            public String getParent() {
                return "0";
            }
        });

        item.preload();

        assertEquals(1, item.getKidItems().length);
        assertNotNull(item.getDisplayedText());
    }

    @Test
    public void testSettersAndGetters() {
        HackerNewsItem item = new HackerNewsItem(1L);

        item.setFavorite(true);
        assertTrue(item.isFavorite());

        item.setIsViewed(true);
        assertTrue(item.isViewed());

        item.setLocalRevision(5);
        assertEquals(5, item.getLocalRevision());

        item.setCollapsed(true);
        assertTrue(item.isCollapsed());

        item.setContentExpanded(true);
        assertTrue(item.isContentExpanded());
    }

    @Test
    public void testPopulateInvalidParent() {
        HackerNewsItem itemNullParent = new HackerNewsItem(1L);
        itemNullParent.populate(new ItemTest.TestItem(1L) {
            @Override
            public String getParent() {
                return null;
            }
        });
        assertEquals(0L, itemNullParent.getParent());

        HackerNewsItem itemNonDigitParent = new HackerNewsItem(2L);
        itemNonDigitParent.populate(new ItemTest.TestItem(2L) {
            @Override
            public String getParent() {
                return "abc";
            }
        });
        assertEquals(0L, itemNonDigitParent.getParent());
    }
}
