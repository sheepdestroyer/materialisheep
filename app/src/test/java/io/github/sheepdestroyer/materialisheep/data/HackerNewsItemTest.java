package io.github.sheepdestroyer.materialisheep.data;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class HackerNewsItemTest {

    @Test
    public void testGetDisplayedTitle() {
        HackerNewsItem item = new HackerNewsItem(1L);
        Item mockItem = mock(Item.class);
        when(mockItem.getParent()).thenReturn("0");

        when(mockItem.getRawType()).thenReturn(Item.COMMENT_TYPE);
        when(mockItem.getText()).thenReturn("Comment Text");
        when(mockItem.getTitle()).thenReturn("Comment Title");
        item.populate(mockItem);
        assertEquals("Comment Text", item.getDisplayedTitle());

        when(mockItem.getRawType()).thenReturn(Item.JOB_TYPE);
        when(mockItem.getText()).thenReturn("Job Text");
        when(mockItem.getTitle()).thenReturn("Job Title");
        item.populate(mockItem);
        assertEquals("Job Title", item.getDisplayedTitle());

        when(mockItem.getRawType()).thenReturn(Item.STORY_TYPE);
        when(mockItem.getText()).thenReturn("Story Text");
        when(mockItem.getTitle()).thenReturn("Story Title");
        item.populate(mockItem);
        assertEquals("Story Title", item.getDisplayedTitle());

        when(mockItem.getRawType()).thenReturn(Item.POLL_TYPE);
        when(mockItem.getText()).thenReturn("Poll Text");
        when(mockItem.getTitle()).thenReturn("Poll Title");
        item.populate(mockItem);
        assertEquals("Poll Title", item.getDisplayedTitle());

        when(mockItem.getRawType()).thenReturn("unknown");
        when(mockItem.getText()).thenReturn("Unknown Text");
        when(mockItem.getTitle()).thenReturn("Unknown Title");
        item.populate(mockItem);
        assertEquals("Unknown Title", item.getDisplayedTitle());
    }

    @Test
    public void testGetUrl() {
        HackerNewsItem item = new HackerNewsItem(1L);
        Item mockItem = mock(Item.class);
        when(mockItem.getParent()).thenReturn("0");

        when(mockItem.getRawType()).thenReturn(Item.JOB_TYPE);
        when(mockItem.getRawUrl()).thenReturn("http://example.com");
        item.populate(mockItem);
        assertEquals("https://news.ycombinator.com/item?id=1", item.getUrl());

        when(mockItem.getRawType()).thenReturn(Item.POLL_TYPE);
        item.populate(mockItem);
        assertEquals("https://news.ycombinator.com/item?id=1", item.getUrl());

        when(mockItem.getRawType()).thenReturn(Item.COMMENT_TYPE);
        item.populate(mockItem);
        assertEquals("https://news.ycombinator.com/item?id=1", item.getUrl());

        when(mockItem.getRawType()).thenReturn(Item.STORY_TYPE);
        item.populate(mockItem);
        assertEquals("http://example.com", item.getUrl());

        when(mockItem.getRawType()).thenReturn("unknown");
        when(mockItem.getRawUrl()).thenReturn("");
        item.populate(mockItem);
        assertEquals("https://news.ycombinator.com/item?id=1", item.getUrl());
    }

    @Test
    public void testIsStoryType() {
        HackerNewsItem item = new HackerNewsItem(1L);
        Item mockItem = mock(Item.class);
        when(mockItem.getParent()).thenReturn("0");

        when(mockItem.getRawType()).thenReturn(Item.STORY_TYPE);
        item.populate(mockItem);
        assertTrue(item.isStoryType());

        when(mockItem.getRawType()).thenReturn(Item.POLL_TYPE);
        item.populate(mockItem);
        assertTrue(item.isStoryType());

        when(mockItem.getRawType()).thenReturn(Item.JOB_TYPE);
        item.populate(mockItem);
        assertTrue(item.isStoryType());

        when(mockItem.getRawType()).thenReturn(Item.COMMENT_TYPE);
        item.populate(mockItem);
        assertFalse(item.isStoryType());

        when(mockItem.getRawType()).thenReturn("unknown");
        item.populate(mockItem);
        assertFalse(item.isStoryType());
    }
}
