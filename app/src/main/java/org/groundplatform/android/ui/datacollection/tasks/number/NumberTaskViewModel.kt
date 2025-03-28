/*
 * Copyright 2021 Google LLC
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
package org.groundplatform.android.ui.datacollection.tasks.number

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import javax.inject.Inject
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import org.groundplatform.android.model.submission.NumberTaskData
import org.groundplatform.android.model.submission.TaskData
import org.groundplatform.android.ui.datacollection.tasks.AbstractTaskViewModel

class NumberTaskViewModel @Inject constructor() : AbstractTaskViewModel() {

  /** Transcoded text to be displayed for the current [TaskData]. */
  val responseText: LiveData<String> =
    taskTaskData.filterIsInstance<NumberTaskData?>().map { it?.number ?: "" }.asLiveData()
}
