/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.groundplatform.android

import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import org.groundplatform.android.databinding.SettingsActivityBinding

@AndroidEntryPoint
class SettingsActivity : AbstractActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val binding = SettingsActivityBinding.inflate(layoutInflater)
    setContentView(binding.root)

    with(binding.settingsToolbar) {
      setSupportActionBar(this)
      setNavigationOnClickListener { finish() }
    }
  }
}
