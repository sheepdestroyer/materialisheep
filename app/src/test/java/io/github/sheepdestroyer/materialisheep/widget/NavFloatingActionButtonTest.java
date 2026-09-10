package io.github.sheepdestroyer.materialisheep.widget;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.os.SystemClock;
import android.view.MotionEvent;
import io.github.sheepdestroyer.materialisheep.Navigable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class NavFloatingActionButtonTest {

  private Activity mActivity;
  private NavFloatingActionButton mNavFab;

  @Before
  public void setUp() {
    mActivity = Robolectric.buildActivity(Activity.class).create().get();
    mNavFab = new NavFloatingActionButton(mActivity);
    mNavFab.setNavigable(new Navigable() {
      @Override
      public void onNavigate(int direction) {}
    });
  }

  @Test
  public void testStartDragPositionCompensation() {
    // Initial FAB position at (100, 200)
    mNavFab.setX(100f);
    mNavFab.setY(200f);

    // Initial touch inside FAB at raw coordinates (130, 240), so relative touch offset is (30, 40)
    float touchOffsetX = 30f;
    float touchOffsetY = 40f;
    mNavFab.startDrag(touchOffsetX, touchOffsetY);

    long downTime = SystemClock.uptimeMillis();

    // Simulate drag ACTION_MOVE to raw position (180, 290)
    MotionEvent moveEvent = MotionEvent.obtain(
        downTime,
        downTime + 100,
        MotionEvent.ACTION_MOVE,
        180f,
        290f,
        0
    );

    mNavFab.dispatchTouchEvent(moveEvent);

    // Expected position: rawX - touchOffsetX = 180 - 30 = 150, rawY - touchOffsetY = 290 - 40 = 250
    assertEquals(150f, mNavFab.getX(), 0.001f);
    assertEquals(250f, mNavFab.getY(), 0.001f);
    assertTrue(mNavFab.mMoved);

    moveEvent.recycle();
  }
}
