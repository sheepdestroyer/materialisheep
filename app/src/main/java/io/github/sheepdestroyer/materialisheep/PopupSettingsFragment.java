/*
 * Copyright (c) 2016 Ha Duy Trung
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

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialog;

/**
 * A dialog fragment that displays settings in a popup.
 */
@SuppressWarnings("deprecation") // TODO: Uses deprecated Fragment APIs (instantiate, onActivityCreated)
public class PopupSettingsFragment extends AppCompatDialogFragment {
    static final String EXTRA_TITLE = PopupSettingsFragment.class.getName() + ".EXTRA_TITLE";
    static final String EXTRA_SUMMARY = PopupSettingsFragment.class.getName() + ".EXTRA_SUMMARY";
    static final String EXTRA_XML_PREFERENCES = PopupSettingsFragment.class.getName() +
            ".EXTRA_XML_PREFERENCES";

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
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_popup_settings, container, false);
    }

    /**
     * Override to build your own custom Dialog container.
     *
     * @param savedInstanceState The last saved instance state of the Fragment,
     *                           or null if this is a freshly created Fragment.
     * @return Return a new Dialog instance to be displayed by the Fragment.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new BottomSheetDialog(getActivity(), getTheme());
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
        if (savedInstanceState == null) {
            Fragment fragment = new PreferenceFragment();
            fragment.setArguments(getArguments());
            getChildFragmentManager()
                    .beginTransaction()
                    .add(R.id.content, fragment)
                    .commit();
        }
    }

    public static class PreferenceFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            addPreferencesFromResource(R.xml.preferences_category);
            Preference category = findPreference(getString(R.string.pref_category));
            int summary, title;
            if ((title = getArguments().getInt(EXTRA_TITLE, 0)) != 0) {
                category.setTitle(title);
            }
            if ((summary = getArguments().getInt(EXTRA_SUMMARY, 0)) != 0) {
                category.setSummary(summary);
            }
            int[] preferences = getArguments().getIntArray(EXTRA_XML_PREFERENCES);
            if (preferences != null) {
                for (int preference : preferences) {
                    addPreferencesFromResource(preference);
                }
            }
        }
    }

    static class BottomSheetDialog extends com.google.android.material.bottomsheet.BottomSheetDialog {
        public BottomSheetDialog(@NonNull Context context, @StyleRes int theme) {
            super(context, theme);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            int width = getContext().getResources().getDimensionPixelSize(R.dimen.bottom_sheet_width);
            // noinspection ConstantConditions
            getWindow().setLayout(
                    width > 0 ? width : ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
        }

    }
}
