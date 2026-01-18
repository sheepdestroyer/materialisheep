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

/**
 * A base fragment that delays loading data until it is visible to the user or if WIFI is enabled.
 */
public abstract class LazyLoadFragment extends BaseFragment {
    public static final String EXTRA_EAGER_LOAD = LazyLoadFragment.class.getName() + ".EXTRA_EAGER_LOAD";
    public static final String EXTRA_RETAIN_INSTANCE = WebFragment.class.getName() + ".EXTRA_RETAIN_INSTANCE";
    private static final String STATE_EAGER_LOAD = "state:eagerLoad";
    private static final String STATE_LOADED = "state:loaded";
    private boolean mEagerLoad, mLoaded, mActivityCreated;
    private boolean mNewInstance;

    /**
     * Called when a fragment is first attached to its context.
     *
     * @param context The context.
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mNewInstance = false;
    }

    /**
     * Called to do initial creation of a fragment.
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(getArguments().getBoolean(EXTRA_RETAIN_INSTANCE, false));
        mNewInstance = true;
        if (savedInstanceState != null) {
            mEagerLoad = savedInstanceState.getBoolean(STATE_EAGER_LOAD);
            mLoaded = savedInstanceState.getBoolean(STATE_LOADED);
        } else {
            mEagerLoad = getArguments() != null && getArguments().getBoolean(EXTRA_EAGER_LOAD) ||
                    !Preferences.shouldLazyLoad(getActivity());
        }
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
        mActivityCreated = true;
        if (isNewInstance()) {
            eagerLoad();
        }
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
        outState.putBoolean(STATE_EAGER_LOAD, mEagerLoad);
        outState.putBoolean(STATE_LOADED, false); // allow re-loading on state restoration
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
            mEagerLoad = true;
            eagerLoad();
        }
    }

    /**
     * Loads the data.
     */
    protected abstract void load();

    /**
     * Checks if the fragment is a new instance.
     *
     * @return True if the fragment is a new instance, false otherwise.
     */
    protected boolean isNewInstance() {
        return !getRetainInstance() || mNewInstance;
    }

    final void eagerLoad() {
        if (mEagerLoad && !mLoaded) {
            mLoaded = true;
            load();
        }
    }
}
