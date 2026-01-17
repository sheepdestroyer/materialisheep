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

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import javax.inject.Inject;

import io.github.hidroh.materialistic.widget.ListRecyclerViewAdapter;
import io.github.hidroh.materialistic.widget.SnappyLinearLayoutManager;

/**
 * An abstract base fragment that displays a list of items. This fragment
 * handles common list
 * functionality, such as setting up the RecyclerView, handling scroll events,
 * and managing
 * adapter state.
 */
abstract class BaseListFragment extends BaseFragment implements Scrollable {
    private static final String STATE_ADAPTER = "state:adapter";
    @Inject
    CustomTabsDelegate mCustomTabsDelegate;
    private KeyDelegate.RecyclerViewHelper mScrollableHelper;
    protected RecyclerView mRecyclerView;
    private final Preferences.Observable mPreferenceObservable = new Preferences.Observable();

    /**
     * Called when a fragment is first attached to its context.
     *
     * @param context The context.
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mPreferenceObservable.subscribe(context, this::onPreferenceChanged,
                R.string.pref_font,
                R.string.pref_text_size,
                R.string.pref_list_item_view);
    }

    /**
     * Called to do initial creation of a fragment.
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param view               The View returned by
     *                           {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView.setLayoutManager(new SnappyLinearLayoutManager(getActivity(), false));
        final int verticalMargin = getResources()
                .getDimensionPixelSize(R.dimen.cardview_vertical_margin);
        final int horizontalMargin = getResources()
                .getDimensionPixelSize(R.dimen.cardview_horizontal_margin);
        final int divider = getResources().getDimensionPixelSize(R.dimen.divider);
        mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                    RecyclerView.State state) {
                if (getAdapter().isCardViewEnabled()) {
                    outRect.set(horizontalMargin, verticalMargin, horizontalMargin, 0);
                } else {
                    outRect.set(0, 0, 0, divider);
                }
            }
        });
    }

    /**
     * Called when the fragment's activity has been created and this
     * fragment's view hierarchy instantiated.
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            getAdapter().restoreState(savedInstanceState.getBundle(STATE_ADAPTER));
        }
        getAdapter().setCardViewEnabled(Preferences.isListItemCardView(getActivity()));
        getAdapter().setCustomTabsDelegate(mCustomTabsDelegate);
        mRecyclerView.setAdapter(getAdapter());
        mScrollableHelper = new KeyDelegate.RecyclerViewHelper(mRecyclerView,
                KeyDelegate.RecyclerViewHelper.SCROLL_PAGE);
    }

    /**
     * Initialize the contents of the Fragment's standard options menu.
     *
     * @param menu     The options menu in which you place your items.
     * @param inflater You can use this to inflate your menu XML files into the
     *                 menu.
     */
    @Override
    protected void createOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_list, menu);
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
        if (item.getItemId() == R.id.menu_list) {
            showPreferences();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showPreferences() {
        Bundle args = new Bundle();
        args.putInt(PopupSettingsFragment.EXTRA_TITLE, R.string.list_display_options);
        args.putInt(PopupSettingsFragment.EXTRA_SUMMARY, R.string.pull_up_hint);
        args.putIntArray(PopupSettingsFragment.EXTRA_XML_PREFERENCES, new int[] {
                R.xml.preferences_font,
                R.xml.preferences_list });
        ((DialogFragment) Fragment.instantiate(getActivity(),
                PopupSettingsFragment.class.getName(), args))
                .show(getFragmentManager(), PopupSettingsFragment.class.getName());
    }

    /**
     * Called to ask the fragment to save its current dynamic state, so it
     * can later be reconstructed in a new instance of its process is
     * restarted.
     *
     * @param outState Bundle in which to place your saved state.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(STATE_ADAPTER, getAdapter().saveState());
    }

    /**
     * Called when the fragment is no longer attached to its activity.
     */
    @Override
    public void onDetach() {
        mPreferenceObservable.unsubscribe(getActivity());
        mRecyclerView.setAdapter(null); // force adapter detach
        super.onDetach();
    }

    /**
     * Scrolls the list to the top.
     */
    @Override
    public void scrollToTop() {
        mScrollableHelper.scrollToTop();
    }

    /**
     * Scrolls the list to the next item.
     *
     * @return True if the list was scrolled, false otherwise.
     */
    @Override
    public boolean scrollToNext() {
        return mScrollableHelper.scrollToNext();
    }

    /**
     * Scrolls the list to the previous item.
     *
     * @return True if the list was scrolled, false otherwise.
     */
    @Override
    public boolean scrollToPrevious() {
        return mScrollableHelper.scrollToPrevious();
    }

    private void onPreferenceChanged(int key, boolean contextChanged) {
        if (contextChanged) {
            mRecyclerView.setAdapter(getAdapter());
        } else if (key == R.string.pref_list_item_view) {
            getAdapter().setCardViewEnabled(Preferences.isListItemCardView(getActivity()));
        }
    }

    /**
     * Gets the adapter for the RecyclerView.
     *
     * @return The adapter.
     */
    protected abstract ListRecyclerViewAdapter getAdapter();
}
