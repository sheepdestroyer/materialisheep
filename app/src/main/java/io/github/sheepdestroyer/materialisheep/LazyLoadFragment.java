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
import androidx.lifecycle.ViewModelProvider;

/**
 * A base fragment that delays loading data until it is visible to the user or
 * if WIFI is enabled.
 */
public abstract class LazyLoadFragment extends BaseFragment {
    public static final String EXTRA_EAGER_LOAD = LazyLoadFragment.class.getName() + ".EXTRA_EAGER_LOAD";
    public static final String EXTRA_RETAIN_INSTANCE = WebFragment.class.getName() + ".EXTRA_RETAIN_INSTANCE";
    private static final String STATE_EAGER_LOAD = "state:eagerLoad";
    private static final String STATE_LOADED = "state:loaded";
    private boolean mActivityCreated;
    private boolean mLoaded;
    private LazyLoadViewModel mViewModel;

    /**
     * Called to do initial creation of a fragment.
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(LazyLoadViewModel.class);
        if (!mViewModel.isInitialized()) {
            mViewModel.setInitialized(true);
            if (savedInstanceState != null) {
                mViewModel.setEagerLoad(savedInstanceState.getBoolean(STATE_EAGER_LOAD));
            } else {
                boolean eager = getArguments() != null && getArguments().getBoolean(EXTRA_EAGER_LOAD) ||
                        !Preferences.shouldLazyLoad(getActivity());
                mViewModel.setEagerLoad(eager);
            }
        }
    }

    /**
     * Called immediately after
     * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * has returned, but before any saved state has been restored into the view.
     */
    @Override
    public void onViewCreated(android.view.View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mActivityCreated = true;
        eagerLoad();
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
        outState.putBoolean(STATE_EAGER_LOAD, mViewModel.isEagerLoad());
        outState.putBoolean(STATE_LOADED, false); // allow re-loading on state restoration
    }

    /**
     * Called when the view previously created by {@link #onCreateView} has been
     * detached from the fragment.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mActivityCreated = false;
    }

    /**
     * Called when the fragment is no longer attached to its activity.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mActivityCreated = false;
    }

    /**
     * Loads the data immediately.
     */
    public void loadNow() {
        if (mActivityCreated) {
            mViewModel.setEagerLoad(true);
            eagerLoad();
        }
    }

    /**
     * Loads the data.
     */
    protected abstract void load();

    /**
     * Attempts to load data if eager load is enabled and data is not yet loaded.
     */
    /**
     * Attempts to load data if eager load is enabled and data is not yet loaded.
     */
    final void eagerLoad() {
        if (mViewModel.isEagerLoad() && !mLoaded) {
            mLoaded = true;
            load();
        }
    }
}
