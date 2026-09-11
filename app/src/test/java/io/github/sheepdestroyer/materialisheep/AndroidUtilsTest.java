package io.github.sheepdestroyer.materialisheep;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class AndroidUtilsTest {

    @Test
    public void testEquals_bothNull() {
        assertTrue(AndroidUtils.TextUtils.equals(null, null));
    }

    @Test
    public void testEquals_oneNull() {
        assertFalse(AndroidUtils.TextUtils.equals(null, "abc"));
        assertFalse(AndroidUtils.TextUtils.equals("abc", null));
    }

    @Test
    public void testEquals_sameInstance() {
        String s = "abc";
        assertTrue(AndroidUtils.TextUtils.equals(s, s));
    }

    @Test
    public void testEquals_identicalStrings() {
        assertTrue(AndroidUtils.TextUtils.equals("abc", new String("abc")));
    }

    @Test
    public void testEquals_differentStrings() {
        assertFalse(AndroidUtils.TextUtils.equals("abc", "def"));
    }

    @Test
    public void testEquals_differentLengths() {
        assertFalse(AndroidUtils.TextUtils.equals("abc", "abcd"));
    }

    @Test
    public void testEquals_charSequenceAndString() {
        assertTrue(AndroidUtils.TextUtils.equals(new StringBuilder("abc"), "abc"));
        assertTrue(AndroidUtils.TextUtils.equals("abc", new StringBuilder("abc")));
        assertFalse(AndroidUtils.TextUtils.equals(new StringBuilder("abc"), "abd"));
    }

    @Test
    public void testEquals_twoCustomCharSequences() {
        assertTrue(AndroidUtils.TextUtils.equals(new StringBuilder("abc"), new StringBuffer("abc")));
        assertFalse(AndroidUtils.TextUtils.equals(new StringBuilder("abc"), new StringBuffer("xyz")));
    }

    @Test
    public void testIsEmpty() {
        assertTrue(AndroidUtils.TextUtils.isEmpty(null));
        assertTrue(AndroidUtils.TextUtils.isEmpty(""));
        assertFalse(AndroidUtils.TextUtils.isEmpty("hello"));
        assertFalse(AndroidUtils.TextUtils.isEmpty(" "));
        assertTrue(AndroidUtils.TextUtils.isEmpty(new StringBuilder()));
        assertFalse(AndroidUtils.TextUtils.isEmpty(new StringBuilder("text")));
    }

    @Test
    public void testConstructor() {
        assertNotNull(new AndroidUtils.TextUtils());
    }
}
