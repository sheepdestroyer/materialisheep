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
import android.view.Menu;
import android.view.MenuInflater;
import androidx.fragment.app.Fragment;

/**
 * Base fragment which performs injection using its parent activity's object graphs, if available.
 * It also handles menu creation and preparation, and tracks the fragment's attached state.
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
   * Called when the fragment's activity has been created and this fragment's view hierarchy
   * instantiated.
   *
   * @param savedInstanceState If the fragment is being re-created from a previous saved state, this
   *     is the state.
   */
  /**
   * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} has returned,
   * but before any saved state has been restored in to the view.
   */
  @Override
  public void onViewCreated(
      @androidx.annotation.NonNull android.view.View view,
      @androidx.annotation.Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mMenuTintDelegate.onActivityCreated(getActivity());
    requireActivity()
        .addMenuProvider(
            new androidx.core.view.MenuProvider() {
              @Override
              public void onCreateMenu(
                  @androidx.annotation.NonNull Menu menu,
                  @androidx.annotation.NonNull MenuInflater menuInflater) {
                if (isAttached()) {
                  createOptionsMenu(menu, menuInflater);
                  mMenuTintDelegate.onOptionsMenuCreated(menu);
                }
              }

              @Override
              public boolean onMenuItemSelected(
                  @androidx.annotation.NonNull android.view.MenuItem menuItem) {
                return BaseFragment.this.onOptionsItemSelected(menuItem);
              }

              @Override
              public void onPrepareMenu(@androidx.annotation.NonNull Menu menu) {
                if (isAttached()) {
                  prepareOptionsMenu(menu);
                }
              }
            },
            getViewLifecycleOwner(),
            androidx.lifecycle.Lifecycle.State.RESUMED);
  }

  /** Called when the fragment is no longer attached to its activity. */
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
   * @param menu The options menu.
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
