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

package io.github.hidroh.materialistic;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;

import static io.github.hidroh.materialistic.DataModule.HN;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import io.github.hidroh.materialistic.annotation.Synthetic;
import io.github.hidroh.materialistic.data.ItemManager;
import io.github.hidroh.materialistic.data.ResponseListener;
import io.github.hidroh.materialistic.data.UserManager;
import io.github.hidroh.materialistic.widget.CommentItemDecoration;
import io.github.hidroh.materialistic.widget.SnappyLinearLayoutManager;
import io.github.hidroh.materialistic.widget.SubmissionRecyclerViewAdapter;

/**
 * An activity that displays a user's profile.
 */
public class UserActivity extends ThemedActivity implements Scrollable {
    public static final String EXTRA_USERNAME = UserActivity.class.getName() + ".EXTRA_USERNAME";
    private static final String STATE_USER = "state:user";
    private static final String PARAM_ID = "id";
    private static final String KARMA = " (%1$s)";
    @Inject UserManager mUserManager;
    @Inject @Named(HN) ItemManager mItemManger;
    @Inject KeyDelegate mKeyDelegate;
    private KeyDelegate.RecyclerViewHelper mScrollableHelper;
    private String mUsername;
    private UserManager.User mUser;
    private TextView mTitle;
    private TextView mInfo;
    private TextView mAbout;
    @Synthetic RecyclerView mRecyclerView;
    private TabLayout mTabLayout;
    private View mEmpty;
    private BottomSheetBehavior<View> mBottomSheetBehavior;

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState(Bundle)}.
     *                           Otherwise it is null.
     */
    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MaterialisticApplication) getApplication()).applicationComponent.inject(this);
        mUsername = getIntent().getStringExtra(EXTRA_USERNAME);
        if (TextUtils.isEmpty(mUsername)) {
            mUsername = AppUtils.getDataUriId(getIntent(), PARAM_ID);
        }
        if (TextUtils.isEmpty(mUsername)) {
            finish();
            return;
        }
        setTaskTitle(mUsername);
        AppUtils.setStatusBarDim(getWindow(), true);
        setContentView(R.layout.activity_user);
        findViewById(R.id.touch_outside).setOnClickListener(v -> finish());
        mBottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet));
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        finish();
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        AppUtils.setStatusBarDim(getWindow(), false);
                        mRecyclerView.setLayoutFrozen(false);
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                    case BottomSheetBehavior.STATE_DRAGGING:
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                    case BottomSheetBehavior.STATE_SETTLING:
                    default:
                        AppUtils.setStatusBarDim(getWindow(), true);
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // no op
            }
        });
        mTitle = (TextView) findViewById(R.id.title);
        mTitle.setText(mUsername);
        mInfo = (TextView) findViewById(R.id.user_info);
        mAbout = (TextView) findViewById(R.id.about);
        mEmpty = findViewById(R.id.empty);
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // no op
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // no op
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                scrollToTop();
            }
        });
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new SnappyLinearLayoutManager(this, true));
        mRecyclerView.addItemDecoration(new CommentItemDecoration(this));
        mScrollableHelper = new KeyDelegate.RecyclerViewHelper(mRecyclerView,
                KeyDelegate.RecyclerViewHelper.SCROLL_ITEM);
        if (savedInstanceState != null) {
            mUser = savedInstanceState.getParcelable(STATE_USER);
        }
        if (mUser == null) {
            load();
        } else {
            bind();
        }
        if (!AppUtils.hasConnection(this)) {
            Snackbar.make(findViewById(R.id.content_frame),
                    R.string.offline_notice, Snackbar.LENGTH_LONG)
                    .show();
        }
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
     * Called to retrieve per-instance state from an activity before being killed
     * so that the state can be restored in {@link #onCreate(Bundle)} or
     * {@link #onRestoreInstanceState(Bundle)}.
     *
     * @param outState Bundle in which to place your saved state.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_USER, mUser);
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
        mKeyDelegate.setScrollable(this, null);
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
     * Scrolls the item list to the top.
     */
    @Override
    public void scrollToTop() {
        mScrollableHelper.scrollToTop();
    }

    /**
     * Scrolls the item list to the next item.
     *
     * @return True if the list was scrolled, false otherwise.
     */
    @Override
    public boolean scrollToNext() {
        return mScrollableHelper.scrollToNext();
    }

    /**
     * Scrolls the item list to the previous item.
     *
     * @return True if the list was scrolled, false otherwise.
     */
    @Override
    public boolean scrollToPrevious() {
        return mScrollableHelper.scrollToPrevious();
    }

    /**
     * Checks if the activity should be translucent.
     *
     * @return True if the activity should be translucent, false otherwise.
     */
    @Override
    protected boolean isTranslucent() {
        return true;
    }

    private void load() {
        mUserManager.getUser(mUsername, new UserResponseListener(this));
    }

    @Synthetic
    void onUserLoaded(UserManager.User response) {
        if (response != null) {
            mUser = response;
            bind();
        } else {
            showEmpty();
        }
    }

    private void showEmpty() {
        mInfo.setVisibility(View.GONE);
        mAbout.setVisibility(View.GONE);
        mEmpty.setVisibility(View.VISIBLE);
        mTabLayout.addTab(mTabLayout.newTab()
                .setText(getResources().getQuantityString(R.plurals.submissions_count, 0, "").trim()));
    }

    private void bind() {
        SpannableString karma = new SpannableString(String.format(Locale.US, KARMA,
                NumberFormat.getInstance(Locale.getDefault()).format(mUser.getKarma())));
        karma.setSpan(new RelativeSizeSpan(0.8f), 0, karma.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        mTitle.append(karma);
        mInfo.setText(getString(R.string.user_info, mUser.getCreated(this)));
        if (TextUtils.isEmpty(mUser.getAbout())) {
            mAbout.setVisibility(View.GONE);
        } else {
            AppUtils.setTextWithLinks(mAbout, AppUtils.fromHtml(mUser.getAbout(), true));
        }
        int count = mUser.getItems().length;
        mTabLayout.addTab(mTabLayout.newTab()
                .setText(getResources().getQuantityString(R.plurals.submissions_count, count, count)));
        mRecyclerView.setAdapter(new SubmissionRecyclerViewAdapter(mItemManger, mUser.getItems()));
        mRecyclerView.setLayoutFrozen(mBottomSheetBehavior.getState() !=
                BottomSheetBehavior.STATE_EXPANDED);
    }

    static class UserResponseListener implements ResponseListener<UserManager.User> {
        private final WeakReference<UserActivity> mUserActivity;

        @Synthetic
        UserResponseListener(UserActivity userActivity) {
            mUserActivity = new WeakReference<>(userActivity);
        }

        @Override
        public void onResponse(@Nullable UserManager.User response) {
            if (mUserActivity.get() != null && !mUserActivity.get().isDestroyed()) {
                mUserActivity.get().onUserLoaded(response);
            }
        }

        @Override
        public void onError(String errorMessage) {
            if (mUserActivity.get() != null && !mUserActivity.get().isDestroyed()) {
                Toast.makeText(mUserActivity.get(), R.string.user_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
