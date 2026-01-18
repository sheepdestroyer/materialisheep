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

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;

/**
 * Base fragment which performs injection using its parent activity's object
 * graphs, if available.
 * It also handles menu creation and preparation, and tracks the fragment's
 * attached state.
 */
public abstract class BaseFragment extends Fragment {
    protected final MenuTintDelegate mMenuTintDelegate = new MenuTintDelegate();
    private boolean mAttached;

    /**
     * Called when a fragment is first attached to its context.
     *
     * @param context The context.
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mAttached = true;
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
        mMenuTintDelegate.onActivityCreated(getActivity());
    }

    /**
     * Initialize the contents of the Fragment's standard options menu.
     *
     * @param menu     The options menu in which you place your items.
     * @param inflater You can use this to inflate your menu XML files into the
     *                 menu.
     */
    @Override
    public final void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (isAttached()) { // TODO http://b.android.com/80783
            createOptionsMenu(menu, inflater);
            mMenuTintDelegate.onOptionsMenuCreated(menu);
        }
    }

    /**
     * Prepare the Fragment's standard options menu to be displayed.
     *
     * @param menu The options menu as last shown or first initialized by
     *             onCreateOptionsMenu().
     */
    @Override
    public final void onPrepareOptionsMenu(Menu menu) {
        if (isAttached()) { // TODO http://b.android.com/80783
            prepareOptionsMenu(menu);
        }
    }

    /**
     * Called when the fragment is no longer attached to its activity.
     */
    @Override
    public void onDetach() {
        mAttached = false;
        super.onDetach();
    }

    /**
     * Checks if the fragment is attached to its activity.
     *
     * @return True if the fragment is attached, false otherwise.
     */
    public boolean isAttached() {
        return mAttached;
    }

    /**
     * Creates the options menu.
     *
     * @param menu     The options menu.
     * @param inflater The menu inflater.
     */
    protected void createOptionsMenu(Menu menu, MenuInflater inflater) {
        // override to create options menu
    }

    /**
     * Prepares the options menu.
     *
     * @param menu The options menu.
     */
    protected void prepareOptionsMenu(Menu menu) {
        // override to prepare options menu
    }
}
