/*
 * Copyright (c) 2018 Ha Duy Trung
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

package io.github.sheepdestroyer.materialisheep.data

import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

class SessionManagerTest {

  private lateinit var cache: LocalCache
  private lateinit var sessionManager: SessionManager

  @Before
  fun setUp() {
    cache = mock(LocalCache::class.java)
    sessionManager = SessionManager(Schedulers.trampoline(), cache)
  }

  @Test
  fun testIsViewedNullItemId() {
    sessionManager.isViewed(null)
        .test()
        .assertValue(false)
        .assertComplete()

    verify(cache, never()).isViewed(anyString())
  }

  @Test
  fun testIsViewedEmptyItemId() {
    sessionManager.isViewed("")
        .test()
        .assertValue(false)
        .assertComplete()

    verify(cache, never()).isViewed(anyString())
  }

  @Test
  fun testIsViewedValidItemId() {
    `when`(cache.isViewed("123")).thenReturn(true)
    `when`(cache.isViewed("456")).thenReturn(false)

    sessionManager.isViewed("123")
        .test()
        .assertValue(true)
        .assertComplete()

    sessionManager.isViewed("456")
        .test()
        .assertValue(false)
        .assertComplete()

    verify(cache).isViewed("123")
    verify(cache).isViewed("456")
  }

  @Test
  fun testViewNullItemId() {
    sessionManager.view(null)
    verify(cache, never()).setViewed(anyString())
  }

  @Test
  fun testViewEmptyItemId() {
    sessionManager.view("")
    verify(cache, never()).setViewed(anyString())
  }

  @Test
  fun testViewValidItemId() {
    sessionManager.view("123")
    verify(cache).setViewed("123")
  }
}
