/*
 * Copyright (c) 2015 Ha Duy Trung
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.sheepdestroyer.materialisheep;

import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.Window;

import javax.inject.Inject;
import javax.inject.Named;

import static io.github.sheepdestroyer.materialisheep.DataModule.HN;
import io.github.sheepdestroyer.materialisheep.data.Item;
import io.github.sheepdestroyer.materialisheep.data.ItemManager;
import io.github.sheepdestroyer.materialisheep.widget.CommentItemDecoration;
import io.github.sheepdestroyer.materialisheep.widget.SnappyLinearLayoutManager;
import io.github.sheepdestroyer.materialisheep.widget.ThreadPreviewRecyclerViewAdapter;

/**
 * An activity that displays a preview of a comment thread.
 */
public class ThreadPreviewActivity extends ThemedActivity {
    public static final String EXTRA_ITEM = ThreadPreviewActivity.class.getName() + ".EXTRA_ITEM";

    @Inject @Named(HN) ItemManager mItemManager;
    @Inject KeyDelegate mKeyDelegate;

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState(Bundle)}.
     *                           Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MaterialisticApplication) getApplication()).applicationComponent.inject(this);
        Item item = getIntent().getParcelableExtra(EXTRA_ITEM);
        if (item == null) {
            finish();
            return;
        }
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_thread_preview);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME |
                ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_HOME_AS_UP);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new SnappyLinearLayoutManager(this, false));
        recyclerView.addItemDecoration(new CommentItemDecoration(this));
        recyclerView.setAdapter(new ThreadPreviewRecyclerViewAdapter(mItemManager, item));
        mKeyDelegate.setScrollable(
                new KeyDelegate.RecyclerViewHelper(recyclerView,
                        KeyDelegate.RecyclerViewHelper.SCROLL_ITEM), null);
    }

    /**
     * Called when the activity is becoming visible to the user.
     */
    @Override
    protected void onStart() {
        super.onStart();
        mKeyDelegate.attach(this);
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     *         proceed, true to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when the activity is no longer visible to the user.
     */
    @Override
    protected void onStop() {
        super.onStop();
        mKeyDelegate.detach(this);
    }

    /**
     * Called when a key was pressed down and not handled by any of the views
     * inside of the activity.
     *
     * @param keyCode The value in {@link KeyEvent#getKeyCode()}.
     * @param event   Description of the key event.
     * @return If you handled the event, return true. If you want to allow the
     *         event to be handled by the next receiver, return false.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return mKeyDelegate.onKeyDown(keyCode, event) ||
                super.onKeyDown(keyCode, event);
    }

    /**
     * Called when a key was released and not handled by any of the views
     * inside of the activity.
     *
     * @param keyCode The value in {@link KeyEvent#getKeyCode()}.
     * @param event   Description of the key event.
     * @return If you handled the event, return true. If you want to allow the
     *         event to be handled by the next receiver, return false.
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return mKeyDelegate.onKeyUp(keyCode, event) ||
                super.onKeyUp(keyCode, event);
    }

    /**
     * Called when a long press has occurred and not handled by any of the views
     * inside of the activity.
     *
     * @param keyCode The value in {@link KeyEvent#getKeyCode()}.
     * @param event   Description of the key event.
     * @return If you handled the event, return true. If you want to allow the
     *         event to be handled by the next receiver, return false.
     */
    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return mKeyDelegate.onKeyLongPress(keyCode, event) ||
                super.onKeyLongPress(keyCode, event);
    }

    /**
     * Checks if the activity should be displayed as a dialog.
     *
     * @return True if the activity should be displayed as a dialog, false otherwise.
     */
    @Override
    protected boolean isDialogTheme() {
        return true;
    }
}
