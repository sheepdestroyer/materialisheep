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

package io.github.sheepdestroyer.materialisheep.data;

import androidx.annotation.Nullable;

/**
 * A callback interface for API requests.
 *
 * @param <T> the type of the response
 */
public interface ResponseListener<T> {
    /**
     * Called when the request is successful.
     *
     * @param response the response from the API
     */
    void onResponse(@Nullable T response);

    /**
     * Called when the request fails.
     *
     * @param errorMessage an error message, or `null` if no error message is available
     */
    void onError(String errorMessage);
}
