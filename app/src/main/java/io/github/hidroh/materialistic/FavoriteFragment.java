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

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import io.github.hidroh.materialistic.data.FavoriteManager;
import io.github.hidroh.materialistic.data.LocalItemManager;
import io.github.hidroh.materialistic.widget.FavoriteRecyclerViewAdapter;

/**
 * Fragment that displays a list of favorite stories.
 */
public class FavoriteFragment extends BaseListFragment
        implements FavoriteRecyclerViewAdapter.ActionModeDelegate, LocalItemManager.Observer {
    public static final String EXTRA_FILTER = FavoriteFragment.class.getName() + ".EXTRA_FILTER";
    private static final String STATE_FILTER = "state:filter";
    private static final String STATE_SEARCH_VIEW_EXPANDED = "state:searchViewExpanded";
    private FavoriteRecyclerViewAdapter mAdapter;
    private ActionMode mActionMode;
    private String mFilter;
    private boolean mSearchViewExpanded;
    @Inject
    FavoriteManager mFavoriteManager;
    @Inject
    ActionViewResolver mActionViewResolver;
    @Inject
    AlertDialogBuilder mAlertDialogBuilder;
    private View mEmptySearchView;
    private View mEmptyView;

    /**
     * Called to do initial creation of a fragment.
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mFilter = savedInstanceState.getString(STATE_FILTER);
            mSearchViewExpanded = savedInstanceState.getBoolean(STATE_SEARCH_VIEW_EXPANDED);
        } else {
            mFilter = getArguments().getString(EXTRA_FILTER);
        }
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater           The LayoutInflater object that can be used to
     *                           inflate
     *                           any views in the fragment,
     * @param container          If non-null, this is the parent view that the
     *                           fragment's
     *                           UI should be attached to. The fragment should not
     *                           add the view itself,
     *                           but this can be used to generate the LayoutParams
     *                           of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mEmptySearchView = view.findViewById(R.id.empty_search);
        mEmptyView = view.findViewById(R.id.empty);
        mEmptyView.findViewById(R.id.header_card_view)
                .setOnLongClickListener(v -> {
                    View bookmark = mEmptyView.findViewById(R.id.bookmarked);
                    bookmark.setVisibility(bookmark.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
                    return true;
                });
        mEmptyView.setVisibility(View.INVISIBLE);
        return view;
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
    }

    /**
     * Called when the Fragment is visible to the user.
     */
    @Override
    public void onStart() {
        super.onStart();
        mFavoriteManager.attach(this, mFilter);
    }

    /**
     * Initialize the contents of the Fragment's standard options menu.
     *
     * @param menu     The options menu in which you place your items.
     * @param inflater You can use this to inflate your menu XML files into the
     *                 menu.
     */
    @Override
    protected void createOptionsMenu(final Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
        createSearchView(menu.findItem(R.id.menu_search));
        if (getAdapter().getItemCount() > 0) {
            inflater.inflate(R.menu.menu_favorite, menu);
            super.createOptionsMenu(menu, inflater);
        }
    }

    /**
     * Prepare the Fragment's standard options menu to be displayed.
     *
     * @param menu The options menu as last shown or first initialized by
     *             onCreateOptionsMenu().
     */
    @Override
    protected void prepareOptionsMenu(Menu menu) {
        // allow clearing filter if empty, or filter if non-empty
        menu.findItem(R.id.menu_search).setVisible(!TextUtils.isEmpty(mFilter) ||
                getAdapter().getItemCount() > 0);
        if (getAdapter().getItemCount() > 0) {
            super.prepareOptionsMenu(menu);
        }
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
        if (item.getItemId() == R.id.menu_clear) {
            clear();
            return true;
        }
        if (item.getItemId() == R.id.menu_export) {
            mFavoriteManager.export(getActivity(), mFilter);
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        outState.putString(STATE_FILTER, mFilter);
        outState.putBoolean(STATE_SEARCH_VIEW_EXPANDED, mSearchViewExpanded);
    }

    /**
     * Called when the Fragment is no longer started.
     */
    @Override
    public void onStop() {
        super.onStop();
        mFavoriteManager.detach();
    }

    /**
     * Called when the fragment is no longer attached to its activity.
     */
    @Override
    public void onDetach() {
        if (mActionMode != null) {
            mActionMode.finish();
        }
        super.onDetach();
    }

    /**
     * Filters list data by given query.
     *
     * @param query query used to filter data
     */
    public void filter(String query) {
        mSearchViewExpanded = false;
        mFilter = query;
        mFavoriteManager.attach(this, mFilter);
    }

    /**
     * Gets the RecyclerView adapter for this fragment.
     *
     * @return The RecyclerView adapter.
     */
    @Override
    protected FavoriteRecyclerViewAdapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = new FavoriteRecyclerViewAdapter(getContext(), this);
        }
        return mAdapter;
    }

    /**
     * Starts the action mode.
     *
     * @param callback The action mode callback.
     * @return True if the action mode was started, false otherwise.
     */
    @Override
    public boolean startActionMode(ActionMode.Callback callback) {
        if (mSearchViewExpanded) {
            return false;
        }
        if (mActionMode == null) {
            mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(callback);
        }
        return true;
    }

    /**
     * Checks if the fragment is in action mode.
     *
     * @return True if the fragment is in action mode, false otherwise.
     */
    @Override
    public boolean isInActionMode() {
        return mActionMode != null && !mSearchViewExpanded;
    }

    /**
     * Stops the action mode.
     */
    @Override
    public void stopActionMode() {
        mActionMode = null;
    }

    /**
     * Called when the data has changed.
     */
    @Override
    public void onChanged() {
        getAdapter().notifyChanged();
        if (!isDetached()) {
            toggleEmptyView(getAdapter().getItemCount() == 0, mFilter);
            getActivity().invalidateOptionsMenu();
        }
    }

    private void toggleEmptyView(boolean isEmpty, String filter) {
        if (isEmpty) {
            if (TextUtils.isEmpty(filter)) {
                mEmptySearchView.setVisibility(View.INVISIBLE);
                mEmptyView.setVisibility(View.VISIBLE);
                mEmptyView.bringToFront();
            } else {
                mEmptyView.setVisibility(View.INVISIBLE);
                mEmptySearchView.setVisibility(View.VISIBLE);
                mEmptySearchView.bringToFront();
            }
        } else {
            mEmptyView.setVisibility(View.INVISIBLE);
            mEmptySearchView.setVisibility(View.INVISIBLE);
        }
    }

    private void createSearchView(MenuItem menuSearch) {
        final SearchView searchView = (SearchView) mActionViewResolver.getActionView(menuSearch);
        searchView.setQueryHint(getString(R.string.hint_search_saved_stories));
        searchView.setSearchableInfo(((SearchManager) getActivity()
                .getSystemService(Context.SEARCH_SERVICE))
                .getSearchableInfo(getActivity().getComponentName()));
        searchView.setIconified(!mSearchViewExpanded);
        searchView.setQuery(mFilter, false);
        searchView.setOnSearchClickListener(v -> {
            mSearchViewExpanded = true;
            v.requestFocus();
        });
        searchView.setOnCloseListener(() -> {
            // trigger a dummy empty search intent, as empty query does not get submitted
            searchView.setQuery(FavoriteActivity.EMPTY_QUERY, true);
            return false;
        });
    }

    private void clear() {
        mAlertDialogBuilder
                .init(getActivity())
                .setMessage(R.string.confirm_clear)
                .setPositiveButton(android.R.string.ok,
                        (dialog, which) -> mFavoriteManager.clear(getActivity(), mFilter))
                .setNegativeButton(android.R.string.cancel, null)
                .create().show();
    }
}
