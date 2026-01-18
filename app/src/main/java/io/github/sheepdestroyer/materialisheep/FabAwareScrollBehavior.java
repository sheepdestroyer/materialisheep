/*
 * Copyright 2015 The Android Open Source Project
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
import androidx.annotation.NonNull;
import com.google.android.material.appbar.AppBarLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;

/**
 * A {@link CoordinatorLayout.Behavior} that hides a {@link FloatingActionButton} when the user
 * scrolls down and shows it when the user scrolls up.
 */
public class FabAwareScrollBehavior extends AppBarLayout.ScrollingViewBehavior {
    static final Object HIDDEN = new Object();

    public FabAwareScrollBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Determines whether the behavior depends on another view.
     *
     * @param parent     The CoordinatorLayout parent.
     * @param child      The child view.
     * @param dependency The dependency view.
     * @return True if the behavior depends on the dependency, false otherwise.
     */
    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return super.layoutDependsOn(parent, child, dependency) ||
                dependency instanceof FloatingActionButton;
    }

    /**
     * Called when a nested scroll is about to start.
     *
     * @param coordinatorLayout The CoordinatorLayout parent.
     * @param child             The child view.
     * @param directTargetChild The direct target of the scroll.
     * @param target            The target of the scroll.
     * @param axes              The scroll axes.
     * @param type              The type of the scroll.
     * @return True if the behavior accepts the nested scroll, false otherwise.
     */
    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
        // Ensure we react to vertical scrolling
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL
                || super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type);
    }

    /**
     * Called when a nested scroll is in progress.
     *
     * @param coordinatorLayout The CoordinatorLayout parent.
     * @param child             The child view.
     * @param target            The target of the scroll.
     * @param dxConsumed        The horizontal distance consumed by the target.
     * @param dyConsumed        The vertical distance consumed by the target.
     * @param dxUnconsumed      The horizontal distance not consumed by the target.
     * @param dyUnconsumed      The vertical distance not consumed by the target.
     * @param type              The type of the scroll.
     */
    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);
        if (dyConsumed > 0) {
            // User scrolled down -> hide the FAB
            List<View> dependencies = coordinatorLayout.getDependencies(child);
            for (View view : dependencies) {
                if (view instanceof FloatingActionButton) {
                    ((FloatingActionButton) view).hide();
                }
            }
        } else if (dyConsumed < 0) {
            // User scrolled up -> show the FAB
            List<View> dependencies = coordinatorLayout.getDependencies(child);
            for (View view : dependencies) {
                if (view instanceof FloatingActionButton && !HIDDEN.equals(view.getTag())) {
                    ((FloatingActionButton) view).show();
                }
            }
        }
    }
}
