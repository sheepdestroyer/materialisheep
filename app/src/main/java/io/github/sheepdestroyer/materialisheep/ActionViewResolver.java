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

import android.view.MenuItem;
import android.view.View;

/**
 * A utility class responsible for resolving and providing the action view for a given menu item.
 * This class can be injected to facilitate testing and customization of menu item behaviors.
 */
class ActionViewResolver {
    /**
     * Returns the currently set action view for this menu item.
     *
     * @param menuItem the item to query
     * @return This item's action view
     */
    View getActionView(MenuItem menuItem) {
        return menuItem.getActionView();
    }
}
